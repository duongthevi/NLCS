package com.dtvi.Pet.Ecommerce.service.impl;

import com.dtvi.Pet.Ecommerce.dto.CategoryDto;
import com.dtvi.Pet.Ecommerce.dto.Response;
import com.dtvi.Pet.Ecommerce.entity.Category;
import com.dtvi.Pet.Ecommerce.exception.CategoryOperationException;
import com.dtvi.Pet.Ecommerce.exception.NotFoundException;
import com.dtvi.Pet.Ecommerce.mapper.EntityDtoMapper;
import com.dtvi.Pet.Ecommerce.repository.CategoryRepo;
import com.dtvi.Pet.Ecommerce.repository.ProductRepo;
import com.dtvi.Pet.Ecommerce.service.interf.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;
    private final ProductRepo productRepo;
    private final EntityDtoMapper entityDtoMapper;




    @Override
    public Response createCategory(CategoryDto categoryRequest) {

        // Kiểm tra nếu tên danh mục đã tồn tại
        if (categoryRepo.existsByName(categoryRequest.getName())) {
            throw new CategoryOperationException("Category name already exists: " + categoryRequest.getName());
        }

        if (categoryRequest.getName() == null || categoryRequest.getName().trim().isEmpty()) {
            throw new CategoryOperationException("Category name cannot be empty");
        }

        Category category = new Category();
        category.setName(categoryRequest.getName());
        categoryRepo.save(category);
        return Response.builder()
                .status(200)
                .message("Category created successfully")
                .build();
    }

    @Override
    public Response updateCategory(Long categoryId, CategoryDto categoryRequest) {

        // Kiểm tra nếu tên danh mục đã tồn tại
        if (categoryRepo.existsByName(categoryRequest.getName())) {
            throw new CategoryOperationException("Category name already exists: " + categoryRequest.getName());
        }

        // Kiểm tra nếu tên category đã tồn tại (trừ chính nó)
        if (categoryRepo.existsByNameAndIdNot(categoryRequest.getName(), categoryId)) {
            throw new CategoryOperationException("Category name already exists: " + categoryRequest.getName());
        }

        Category category = categoryRepo.findById(categoryId).orElseThrow(()-> new NotFoundException("Category Not Found"));

        if (categoryRequest.getName() == null || categoryRequest.getName().trim().isEmpty()) {
            throw new CategoryOperationException("Category name cannot be empty");
        }

        category.setName(categoryRequest.getName());
        categoryRepo.save(category);
        return Response.builder()
                .status(200)
                .message("category updated successfully")
                .build();
    }

    @Override
    public Response getAllCategories() {
        List<Category> categories = categoryRepo.findAll();
        List<CategoryDto> categoryDtoList = categories.stream()
                .map(entityDtoMapper::mapCategoryToDtoBasic)
                .collect(Collectors.toList());

        return  Response.builder()
                .status(200)
                .categoryList(categoryDtoList)
                .build();
    }

    @Override
    public Response getCategoryById(Long categoryId) {
        Category category = categoryRepo.findById(categoryId).orElseThrow(()-> new NotFoundException("Category Not Found"));
        CategoryDto categoryDto = entityDtoMapper.mapCategoryToDtoBasic(category);
        return Response.builder()
                .status(200)
                .category(categoryDto)
                .build();
    }

    @Override
    public Response deleteCategory(Long categoryId) {
        Category category = categoryRepo.findById(categoryId).orElseThrow(()-> new NotFoundException("Category Not Found"));
        

        // Kiểm tra nếu danh mục đang chứa sản phẩm
        if (productRepo.existsByCategoryId(categoryId)) {
            throw new CategoryOperationException("Cannot delete category with existing products");
        }
        // Xóa danh mục
        categoryRepo.delete(category);
        return Response.builder()
                .status(200)
                .message("Category was deleted successfully")
                .build();
    }
}
