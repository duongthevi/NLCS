package com.dtvi.Pet.Ecommerce.service.interf;

import com.dtvi.Pet.Ecommerce.dto.CategoryDto;
import com.dtvi.Pet.Ecommerce.dto.Response;

public interface CategoryService {

    Response createCategory(CategoryDto categoryRequest);
    Response updateCategory(Long categoryId, CategoryDto categoryRequest);
    Response getAllCategories();
    Response getCategoryById(Long categoryId);
    Response deleteCategory(Long categoryId);
}
