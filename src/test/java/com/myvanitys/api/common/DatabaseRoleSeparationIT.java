package com.myvanitys.api.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies SEC-08 database role separation end to end by executing the real provisioning artifact
 * ({@code db/roles/01-separate-roles.sql}) through {@code psql}, with passwords supplied through
 * the container environment as they are in deployment, and then proving that Flyway migrates under
 * the migration role, Hibernate validates and the application operates under the DML-only runtime
 * role, the optional human read-only role cannot write or run DDL, and the runtime role is rejected
 * when it attempts DDL.
 */
@Testcontainers
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.docker.compose.enabled=false",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
    })
@ActiveProfiles("test")
class DatabaseRoleSeparationIT {

  private static final String APP_USER = "myvanitys_app";
  private static final String APP_PASSWORD = "apppass";
  private static final String MIGRATION_USER = "myvanitys_migration";
  private static final String MIGRATION_PASSWORD = "migrationpass";
  private static final String READONLY_USER = "myvanitys_readonly";
  private static final String READONLY_PASSWORD = "readonlypass";
  private static final String PROVISIONING_SQL = "db/roles/01-separate-roles.sql";

  // JUnit owns and closes static fields annotated with @Container.
  @Container
  static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")
      .withDatabaseName("myvanitysdb")
      .withUsername("admin")
      .withPassword("adminpass")
      .withEnv("FLYWAY_PASSWORD", MIGRATION_PASSWORD)
      .withEnv("APP_DB_PASSWORD", APP_PASSWORD)
      .withEnv("READONLY_DB_PASSWORD", READONLY_PASSWORD);

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @DynamicPropertySource
  static void configureDatabase(DynamicPropertyRegistry registry) throws Exception {
    provisionRoles();
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", () -> APP_USER);
    registry.add("spring.datasource.password", () -> APP_PASSWORD);
    registry.add("spring.flyway.user", () -> MIGRATION_USER);
    registry.add("spring.flyway.password", () -> MIGRATION_PASSWORD);
  }

  private static boolean rolesProvisioned;

  private static synchronized void provisionRoles() throws Exception {
    if (rolesProvisioned) {
      return;
    }
    ExecResult result = executeProvisioningScript(postgres);

    assertThat(result.getExitCode())
        .withFailMessage("Role provisioning failed: %s", result.getStderr())
        .isZero();
    rolesProvisioned = true;
  }

  private static ExecResult executeProvisioningScript(PostgreSQLContainer database) throws Exception {
    String databaseName = database.getDatabaseName();
    database.copyFileToContainer(
        MountableFile.forClasspathResource(PROVISIONING_SQL),
        "/db/roles/01-separate-roles.sql");

    return database.execInContainer(
        "psql",
        "-v", "ON_ERROR_STOP=1",
        "-U", database.getUsername(),
        "-d", databaseName,
        "-v", "database_name=" + databaseName,
        "-f", "/db/roles/01-separate-roles.sql");
  }

  @Test
  @DisplayName("Given the runtime role, when it reads and writes application data, then it succeeds")
  void runtimeRoleCanReadAndWriteApplicationData() {
    Integer categories = jdbcTemplate.queryForObject("SELECT count(*) FROM category", Integer.class);
    assertThat(categories).isNotNull();
    assertThat(categories).isGreaterThan(0);

    UUID userId = UUID.randomUUID();
    String token = "role-separation-" + userId;
    jdbcTemplate.update(
        "INSERT INTO \"user\" (user_id, token, version, email, name, created_at) VALUES (?, ?, 0, ?, ?, now())",
        userId, token, token + "@example.com", "Role Separation");

    Integer inserted = jdbcTemplate.queryForObject(
        "SELECT count(*) FROM \"user\" WHERE token = ?", Integer.class, token);
    assertThat(inserted).isEqualTo(1);

    jdbcTemplate.update("DELETE FROM \"user\" WHERE token = ?", token);
  }

  @Test
  @DisplayName("Given the runtime role, when it attempts DDL, then it is rejected")
  void runtimeRoleCannotRunDdl() throws Exception {
    try (Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), APP_USER, APP_PASSWORD)) {
      assertThatThrownBy(() -> {
        try (Statement statement = connection.createStatement()) {
          statement.execute("CREATE TABLE ddl_should_fail (id UUID PRIMARY KEY)");
        }
      }).isInstanceOf(SQLException.class)
          .hasMessageContaining("permission denied");
    }
  }

  @Test
  @DisplayName("Given the read-only role, when it reads seeded data, then reads succeed but writes and DDL fail")
  void readonlyRoleCanReadButCannotWriteOrRunDdl() throws Exception {
    try (Connection connection = DriverManager.getConnection(
        postgres.getJdbcUrl(), READONLY_USER, READONLY_PASSWORD);
         Statement statement = connection.createStatement()) {
      try (ResultSet result = statement.executeQuery("SELECT count(*) FROM category")) {
        assertThat(result.next()).isTrue();
        assertThat(result.getInt(1)).isGreaterThan(0);
      }

      assertThatThrownBy(() -> statement.executeUpdate(
          "INSERT INTO category (category_id, version, name, created_at, updated_at) "
              + "VALUES ('00000000-0000-0000-0000-000000000000', 0, 'should fail', now(), now())"))
          .isInstanceOf(SQLException.class)
          .hasMessageContaining("permission denied");

      assertThatThrownBy(() -> statement.execute("CREATE TABLE readonly_ddl_should_fail (id UUID PRIMARY KEY)"))
          .isInstanceOf(SQLException.class)
          .hasMessageContaining("permission denied");
    }
  }

  @Test
  @DisplayName("Given no read-only password, when roles are provisioned, then the optional role is skipped")
  void provisioningSucceedsWithoutReadonlyPassword() throws Exception {
    try (PostgreSQLContainer database = new PostgreSQLContainer("postgres:16")
        .withDatabaseName("withoutreadonly")
        .withUsername("admin")
        .withPassword("adminpass")
        .withEnv("FLYWAY_PASSWORD", MIGRATION_PASSWORD)
        .withEnv("APP_DB_PASSWORD", APP_PASSWORD)) {
      database.start();

      ExecResult result = executeProvisioningScript(database);
      assertThat(result.getExitCode())
          .withFailMessage("Role provisioning without READONLY_DB_PASSWORD failed: %s", result.getStderr())
          .isZero();

      try (Connection connection = DriverManager.getConnection(
          database.getJdbcUrl(), database.getUsername(), database.getPassword());
           Statement statement = connection.createStatement();
           ResultSet roles = statement.executeQuery(
               "SELECT count(*) FROM pg_roles WHERE rolname = 'myvanitys_readonly'")) {
        assertThat(roles.next()).isTrue();
        assertThat(roles.getInt(1)).isZero();
      }
    }
  }
}
