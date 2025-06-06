package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.application.command.DeleteProductFromUserVanityCommand;
import com.myvanitys.api.product.application.port.primary.DeleteProductFromUserVanityUseCase;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.domain.model.Product;
import com.myvanitys.api.product.domain.port.secondary.ProductRepository;
import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class DeleteProductFromUserVanity implements DeleteProductFromUserVanityUseCase {

    private ProductUserRepository productUserRepository;
    private ProductRepository productRepository;

    @Override
    @Transactional
    public Product execute(DeleteProductFromUserVanityCommand command) {
        if (productUserRepository.findByProductIdAndUserId(command.productId().getValue(), command.userId().getValue()).isEmpty()) {
            throw new ProductNotFoundException("Product is not in user's vanity collection\n");
        }

        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));


        productUserRepository.deleteByProductIdAndUserId(command.productId().getValue(), command.userId().getValue());
        return product;
    }
}
