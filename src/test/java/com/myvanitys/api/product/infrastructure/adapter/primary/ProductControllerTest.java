package com.myvanitys.api.product.infrastructure.adapter.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import com.myvanitys.api.auth.infrastructure.security.AuthenticatedUserContext;
import com.myvanitys.api.model.v1.ProductResponse;
import com.myvanitys.api.product.application.port.primary.AddProductToMyVanityUseCase;
import com.myvanitys.api.product.application.port.primary.CreateProductUseCase;
import com.myvanitys.api.product.application.port.primary.DeleteProductFromUserVanityUseCase;
import com.myvanitys.api.product.application.port.primary.FindProductAllUseCase;
import com.myvanitys.api.product.application.port.primary.FindProductUserUseCase;
import com.myvanitys.api.product.application.usecase.AddReviewToProduct;
import com.myvanitys.api.product.application.usecase.FindProductByTerm;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.infrastructure.adapter.primary.mapper.ProductResponseMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ProductControllerTest {

  private FindProductUserUseCase findProductUserUseCase;
  private ProductResponseMapper productResponseMapper;
  private AuthenticatedUserContext context;
  private ProductController controller;

  @BeforeEach
  void setUp() {
    findProductUserUseCase = mock(FindProductUserUseCase.class);
    productResponseMapper = mock(ProductResponseMapper.class);
    context = new AuthenticatedUserContext();
    controller = new ProductController(
        findProductUserUseCase,
        mock(FindProductAllUseCase.class),
        productResponseMapper,
        mock(CreateProductUseCase.class),
        context,
        mock(AddReviewToProduct.class),
        mock(FindProductByTerm.class),
        mock(AddProductToMyVanityUseCase.class),
        mock(DeleteProductFromUserVanityUseCase.class)
    );
  }

  @Test
  void shouldReturn403WhenPathUserIdDoesNotMatchAuthenticatedUser() {
    UUID authenticatedUserId = UUID.randomUUID();
    UUID pathUserId = UUID.randomUUID();
    context.setUserId(authenticatedUserId);

    ResponseEntity<List<ProductResponse>> response = controller.findProductsByUserId(
        pathUserId, UUID.randomUUID(), UUID.randomUUID(), "en-US", "Mozilla/5.0");

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(findProductUserUseCase, never()).query(any());
  }

  @Test
  void shouldReturn200WhenPathUserIdMatchesAuthenticatedUser() {
    UUID userId = UUID.randomUUID();
    context.setUserId(userId);

    when(findProductUserUseCase.query(any())).thenReturn(List.of());
    when(productResponseMapper.toResponseList(anyList())).thenReturn(List.of());

    ResponseEntity<List<ProductResponse>> response = controller.findProductsByUserId(
        userId, UUID.randomUUID(), UUID.randomUUID(), "en-US", "Mozilla/5.0");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(findProductUserUseCase).query(org.mockito.ArgumentMatchers.argThat(
        query -> query.userId().getValue().equals(userId)));
  }
}
