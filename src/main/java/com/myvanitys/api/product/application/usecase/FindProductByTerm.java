package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.application.port.primary.FindProductByTermUseCase;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FindProductByTerm implements FindProductByTermUseCase {

    private final ProductRepository productRepository;

    @Override
    public List<Product> query(String term) {
        return productRepository.searchProductDetailsByNameOrBrand(term.trim());
    }
}
