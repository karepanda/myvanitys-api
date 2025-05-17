package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.application.port.primary.FindProductByTermUseCase;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FindProductByTerm implements FindProductByTermUseCase {

    private final JpaProductRepository jpaProductRepository;

    private final FindProductService findProductService;

    @Override
    public List<Product> query(String term) {
        final var productEntities = jpaProductRepository.findByNameOrBrand(term, term);

        return findProductService.findProducts(productEntities);
    }
}
