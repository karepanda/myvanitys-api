package com.myvanitys.api.product.application.useCase;

import com.myvanitys.api.product.application.command.CreateProductCommand;
import com.myvanitys.api.product.application.port.primary.CreateProductUseCase;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductRepository;
import com.myvanitys.api.product.infrastructure.persistence.repository.JpaProductUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CreateProduct implements CreateProductUseCase {

    protected JpaProductRepository productRepository;

    protected JpaProductRepository reviewRepository;

    protected JpaProductUserRepository productUserRepository;

    @Override
    public Product execute(CreateProductCommand command) {

//        productRepository.save();
//        productUserRepository.save();
//        reviewRepository.save();

        return null;
    }
}
