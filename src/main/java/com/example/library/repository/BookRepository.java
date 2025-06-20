package com.example.library.repository;

import com.example.library.entity.Book;
import com.example.library.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    Optional<Book> findByIsbn(String isbn);
    
    boolean existsByIsbn(String isbn);
    
    Page<Book> findByCategory(Category category, Pageable pageable);
    
    Page<Book> findByStatus(Book.Status status, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Book> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.category = :category AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Book> findByCategoryAndKeyword(@Param("category") Category category, 
                                       @Param("keyword") String keyword, 
                                       Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.availableQuantity > 0 AND b.status = 'AVAILABLE'")
    Page<Book> findAvailableBooks(Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.availableQuantity > 0 AND b.status = 'AVAILABLE' AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Book> findAvailableBooksByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    long countByStatus(Book.Status status);
} 