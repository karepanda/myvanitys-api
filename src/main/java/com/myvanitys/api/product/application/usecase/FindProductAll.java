package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.application.port.primary.FindProductAllUseCase;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FindProductAll implements FindProductAllUseCase {

    private final JpaProductRepository jpaProductRepository;

    private final FindProductService findProductService;

    public List<Product> query() {
        final var productEntities = jpaProductRepository.findAll();

        return findProductService.findProducts(productEntities);
    }


}
