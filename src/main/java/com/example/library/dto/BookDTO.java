package com.example.library.dto;

import com.example.library.entity.Book;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookDTO {
    
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private String publishDate;
    private String description;
    private String coverImage;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private BigDecimal price;
    private CategoryDTO category;
    private Book.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static BookDTO fromEntity(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setIsbn(book.getIsbn());
        dto.setPublisher(book.getPublisher());
        dto.setPublishDate(book.getPublishDate());
        dto.setDescription(book.getDescription());
        dto.setCoverImage(book.getCoverImage());
        dto.setTotalQuantity(book.getTotalQuantity());
        dto.setAvailableQuantity(book.getAvailableQuantity());
        dto.setPrice(book.getPrice());
        if (book.getCategory() != null) {
            dto.setCategory(CategoryDTO.fromEntity(book.getCategory()));
        }
        dto.setStatus(book.getStatus());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());
        return dto;
    }
} 