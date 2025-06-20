package com.example.library.service;

import com.example.library.dto.CategoryDTO;
import com.example.library.entity.Category;
import com.example.library.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public CategoryDTO createCategory(String name, String description) {
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("分类名称已存在");
        }
        
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        
        Category savedCategory = categoryRepository.save(category);
        return CategoryDTO.fromEntity(savedCategory);
    }
    
    public CategoryDTO updateCategory(Long id, String name, String description) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        
        // 检查名称是否已存在（排除当前分类）
        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new RuntimeException("分类名称已存在");
        }
        
        category.setName(name);
        category.setDescription(description);
        
        Category savedCategory = categoryRepository.save(category);
        return CategoryDTO.fromEntity(savedCategory);
    }
    
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        
        // 检查是否有图书使用该分类
        if (category.getBooks() != null && !category.getBooks().isEmpty()) {
            throw new RuntimeException("该分类下还有图书，无法删除");
        }
        
        categoryRepository.deleteById(id);
    }
    
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        return CategoryDTO.fromEntity(category);
    }
    
    public Page<CategoryDTO> getAllCategories(String keyword, Pageable pageable) {
        Page<Category> categories;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            categories = categoryRepository.findByKeyword(keyword.trim(), pageable);
        } else {
            categories = categoryRepository.findAll(pageable);
        }
        
        return categories.map(CategoryDTO::fromEntity);
    }
    
    public List<CategoryDTO> getAllCategoriesAsList() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }
} 