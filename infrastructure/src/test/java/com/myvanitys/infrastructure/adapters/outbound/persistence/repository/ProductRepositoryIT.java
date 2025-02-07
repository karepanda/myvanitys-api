package com.myvanitys.infrastructure.adapters.outbound.persistence.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Optional;

import com.myvanitys.infrastructure.adapters.outbound.persistence.entity.CategoryEntity;
import com.myvanitys.infrastructure.adapters.outbound.persistence.entity.ProductEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
      .withDatabaseName("myvanitysdb")
      .withUsername("myvanitys")
      .withPassword("secret");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private EntityManager entityManager;

  @Test
  @Transactional
  void shouldSaveAndFindProduct() {
    // Crea y guarda una categoría
    CategoryEntity category = new CategoryEntity();
    category.setName("Makeup");
    category = categoryRepository.save(category); // Asegúrate de tener un repositorio para CategoryEntity

    // Crea un nuevo producto
    ProductEntity product = new ProductEntity();
    product.setName("Lipstick");
    product.setBrand("Maybelline");
    product.setColorHex(0xFF5733); // Ejemplo de color
    product.setCategoryEntity(category); // Asocia la categoría

    // Guarda el producto
    product = productRepository.save(product);

    // Limpia el contexto
    entityManager.flush(); // Sincroniza el contexto
    entityManager.clear();  // Limpia el contexto

    // Busca el producto guardado
    Optional<ProductEntity> foundProduct = productRepository.findById(product.getId());
    assertThat(foundProduct).isPresent();
    assertThat(foundProduct.get().getName()).isEqualTo("Lipstick");
  }
}
