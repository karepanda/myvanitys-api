package com.myvanitys.api.common.config;

import com.myvanitys.api.auth.infrastructure.config.GoogleClientProperties;
import com.myvanitys.api.common.LogSanitizer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Component
public class ApplicationStartupLogger implements ApplicationListener<ApplicationStartedEvent> {

    private final Environment env;

  private final GoogleClientProperties googleClientProperties;

  @Value("${server.servlet.context-path:/}")
  private String contextPath;

  @Value("${server.port:8080}")
  private String serverPort;

  public ApplicationStartupLogger(Environment env, GoogleClientProperties googleClientProperties) {
    this.env = env;
    this.googleClientProperties = googleClientProperties;
  }

  @Override
  public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
    String protocol = "http";
    if (env.getProperty("server.ssl.key-store") != null) {
      protocol = "https";
    }

    String hostAddress = "localhost";
    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      log.warn("Could not determine host address", e);
    }

    String railwayStaticUrl = System.getenv("RAILWAY_STATIC_URL");
    String railwayPublicDomain = System.getenv("RAILWAY_PUBLIC_DOMAIN");
    String railwayService = System.getenv("RAILWAY_SERVICE_NAME");

    if (log.isInfoEnabled()) {
      log.info("""
              ----------------------------------------------------------
              \tApplication '{}' is RUNNING! Access URLs:
              \tLocal: \t\t{}://localhost:{}{}
              \tExternal: \t{}://{}:{}{}
              \tProfile(s): \t{}
              \tContext: \t{}
              \tHealth Endpoint: \t{}
              ----------------------------------------------------------""",
          env.getProperty("spring.application.name"),
          protocol,
          serverPort,
          contextPath,
          protocol,
          hostAddress,
          serverPort,
          contextPath,
          String.join(", ", env.getActiveProfiles()),
          contextPath,
          contextPath + "/actuator/health"
      );
    }

    String notAvailable = "Not available";
    log.info("""
            ----------------------------------------------------------
            \tRAILWAY INFORMATION:
            \tRailway Static URL: \t{}
            \tRailway Public Domain: \t{}
            \tRailway Service Name: \t{}
            \tPossible Full URL: \t{}{}/actuator/health
            ----------------------------------------------------------""",
        railwayStaticUrl != null ? railwayStaticUrl : notAvailable,
        railwayPublicDomain != null ? railwayPublicDomain : notAvailable,
        railwayService != null ? railwayService : notAvailable,
        railwayStaticUrl != null ? railwayStaticUrl : "https://[service].[project].railway.app",
        contextPath
    );

    log.info("""
            ----------------------------------------------------------
            \tGOOGLE OAUTH CONFIGURATION:
            \tClient ID: \t{}
            \tRedirect URI configured: \t{}
            ----------------------------------------------------------""",
        LogSanitizer.maskId(),
        googleClientProperties.getRedirectUri() != null && !googleClientProperties.getRedirectUri().isBlank()
    );
  }
}
