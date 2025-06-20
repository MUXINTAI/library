package com.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookRequest {
    
    @NotBlank(message = "图书标题不能为空")
    private String title;
    
    @NotBlank(message = "作者不能为空")
    private String author;
    
    private String isbn;
    
    private String publisher;
    
    private String publishDate;
    
    private String description;
    
    private String coverImage;
    
    @NotNull(message = "总数量不能为空")
    @PositiveOrZero(message = "总数量不能为负数")
    private Integer totalQuantity;
    
    @NotNull(message = "可借数量不能为空")
    @PositiveOrZero(message = "可借数量不能为负数")
    private Integer availableQuantity;
    
    private BigDecimal price;
    
    private Long categoryId;
} 