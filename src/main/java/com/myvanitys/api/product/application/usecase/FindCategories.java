package com.myvanitys.api.product.application.usecase;

import com.myvanitys.api.product.application.port.primary.FindCategoriesUseCase;
import com.myvanitys.api.product.domain.model.Category;
import com.myvanitys.api.product.domain.port.secondary.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FindCategories implements FindCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public List<Category> query() {
        return categoryRepository.findAll();
    }
}
