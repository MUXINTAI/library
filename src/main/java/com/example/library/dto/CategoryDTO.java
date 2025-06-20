package com.example.library.dto;

import com.example.library.entity.Category;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryDTO {
    
    private Long id;
    private String name;
    private String description;
    private Long bookCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static CategoryDTO fromEntity(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setBookCount(category.getBooks() != null ? (long) category.getBooks().size() : 0L);
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }
} 