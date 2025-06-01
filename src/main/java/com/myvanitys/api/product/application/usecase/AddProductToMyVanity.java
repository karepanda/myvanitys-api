package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.application.command.AddProductToMyVanityCommand;
import com.myvanitys.api.product.application.port.primary.AddProductToMyVanityUseCase;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import com.myvanitys.api.product.domain.valueobject.EntityId;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AddProductToMyVanity implements AddProductToMyVanityUseCase {

    private final ProductRepository productRepository;
    private final ProductUserRepository productUserRepository;

    @Override
    @Transactional
    public Product execute(AddProductToMyVanityCommand command) {
        EntityId productId = new EntityId(command.productId());
        EntityId userId = new EntityId(command.userId());
        
        if(productUserRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new IllegalArgumentException("Product is already associated with the user");
        }

        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            throw new RuntimeException("Product does not exist");
        }

        productUserRepository.saveProductUserRelationship(productId, userId);

        return product.get();
    }
}