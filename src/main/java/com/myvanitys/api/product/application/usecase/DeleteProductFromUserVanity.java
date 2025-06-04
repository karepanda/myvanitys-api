package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.application.command.DeleteProductFromUserVanityCommand;
import com.myvanitys.api.product.application.port.primary.DeleteProductFromUserVanityUseCase;
import com.myvanitys.api.product.domain.exception.ProductNotFoundException;
import com.myvanitys.api.product.domain.port.secondary.ProductUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class DeleteProductFromUserVanity implements DeleteProductFromUserVanityUseCase {

    private ProductUserRepository productUserRepository;

    @Override
    @Transactional
    public void execute(DeleteProductFromUserVanityCommand command) {
        if (!productUserRepository.existsByProductIdAndUserId(command.productId(), command.userId())) {
            throw new ProductNotFoundException("Product is not in user's vanity collection\n");
        }

        productUserRepository.deleteByProductIdAndUserId(command.productId(), command.userId());
    }
}
