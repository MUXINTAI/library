package com.example.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "图书标题不能为空")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "作者不能为空")
    @Column(nullable = false)
    private String author;
    
    @Column(unique = true)
    private String isbn;
    
    @Column
    private String publisher;
    
    @Column
    private String publishDate;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column
    private String coverImage;
    
    @NotNull(message = "总数量不能为空")
    @PositiveOrZero(message = "总数量不能为负数")
    @Column(nullable = false)
    private Integer totalQuantity = 0;
    
    @NotNull(message = "可借数量不能为空")
    @PositiveOrZero(message = "可借数量不能为负数")
    @Column(nullable = false)
    private Integer availableQuantity = 0;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.AVAILABLE;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BorrowRecord> borrowRecords;
    
    public enum Status {
        AVAILABLE("可借阅"),
        UNAVAILABLE("不可借阅"),
        MAINTENANCE("维护中");
        
        private final String description;
        
        Status(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 