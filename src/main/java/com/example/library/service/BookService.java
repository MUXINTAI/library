package com.example.library.service;

import com.example.library.dto.BookDTO;
import com.example.library.dto.BookRequest;
import com.example.library.entity.Book;
import com.example.library.entity.Category;
import com.example.library.repository.BookRepository;
import com.example.library.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public BookDTO createBook(BookRequest bookRequest) {
        if (bookRequest.getIsbn() != null && bookRepository.existsByIsbn(bookRequest.getIsbn())) {
            throw new RuntimeException("ISBN已存在");
        }
        
        Book book = new Book();
        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setIsbn(bookRequest.getIsbn());
        book.setPublisher(bookRequest.getPublisher());
        book.setPublishDate(bookRequest.getPublishDate());
        book.setDescription(bookRequest.getDescription());
        book.setCoverImage(bookRequest.getCoverImage());
        book.setTotalQuantity(bookRequest.getTotalQuantity());
        book.setAvailableQuantity(bookRequest.getAvailableQuantity());
        book.setPrice(bookRequest.getPrice());
        book.setStatus(Book.Status.AVAILABLE);
        
        if (bookRequest.getCategoryId() != null) {
            Category category = categoryRepository.findById(bookRequest.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("分类不存在"));
            book.setCategory(category);
        }
        
        Book savedBook = bookRepository.save(book);
        return BookDTO.fromEntity(savedBook);
    }
    
    public BookDTO updateBook(Long id, BookRequest bookRequest) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        
        // 检查ISBN是否已存在（排除当前图书）
        if (bookRequest.getIsbn() != null && 
            !bookRequest.getIsbn().equals(book.getIsbn()) &&
            bookRepository.existsByIsbn(bookRequest.getIsbn())) {
            throw new RuntimeException("ISBN已存在");
        }
        
        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setIsbn(bookRequest.getIsbn());
        book.setPublisher(bookRequest.getPublisher());
        book.setPublishDate(bookRequest.getPublishDate());
        book.setDescription(bookRequest.getDescription());
        book.setCoverImage(bookRequest.getCoverImage());
        book.setTotalQuantity(bookRequest.getTotalQuantity());
        book.setAvailableQuantity(bookRequest.getAvailableQuantity());
        book.setPrice(bookRequest.getPrice());
        
        if (bookRequest.getCategoryId() != null) {
            Category category = categoryRepository.findById(bookRequest.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("分类不存在"));
            book.setCategory(category);
        } else {
            book.setCategory(null);
        }
        
        Book savedBook = bookRepository.save(book);
        return BookDTO.fromEntity(savedBook);
    }
    
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        
        // 检查是否有未归还的借阅记录
        if (book.getBorrowRecords() != null && 
            book.getBorrowRecords().stream().anyMatch(record -> 
                record.getStatus() == com.example.library.entity.BorrowRecord.Status.BORROWED)) {
            throw new RuntimeException("该图书有未归还的借阅记录，无法删除");
        }
        
        bookRepository.deleteById(id);
    }
    
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        return BookDTO.fromEntity(book);
    }
    
    public Page<BookDTO> getAllBooks(String keyword, Long categoryId, Book.Status status, Pageable pageable) {
        Page<Book> books;
        
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("分类不存在"));
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                books = bookRepository.findByCategoryAndKeyword(category, keyword.trim(), pageable);
            } else {
                books = bookRepository.findByCategory(category, pageable);
            }
        } else if (status != null) {
            books = bookRepository.findByStatus(status, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            books = bookRepository.findByKeyword(keyword.trim(), pageable);
        } else {
            books = bookRepository.findAll(pageable);
        }
        
        return books.map(BookDTO::fromEntity);
    }
    
    public Page<BookDTO> getAvailableBooks(String keyword, Pageable pageable) {
        Page<Book> books;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            books = bookRepository.findAvailableBooksByKeyword(keyword.trim(), pageable);
        } else {
            books = bookRepository.findAvailableBooks(pageable);
        }
        
        return books.map(BookDTO::fromEntity);
    }
    
    public BookDTO updateBookStatus(Long id, Book.Status status) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        
        book.setStatus(status);
        Book savedBook = bookRepository.save(book);
        return BookDTO.fromEntity(savedBook);
    }
    
    public void updateBookQuantity(Long bookId, int borrowedQuantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        
        int newAvailableQuantity = book.getAvailableQuantity() - borrowedQuantity;
        if (newAvailableQuantity < 0) {
            throw new RuntimeException("图书库存不足");
        }
        
        book.setAvailableQuantity(newAvailableQuantity);
        bookRepository.save(book);
    }
    
    public void returnBook(Long bookId, int returnedQuantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        
        int newAvailableQuantity = book.getAvailableQuantity() + returnedQuantity;
        if (newAvailableQuantity > book.getTotalQuantity()) {
            newAvailableQuantity = book.getTotalQuantity();
        }
        
        book.setAvailableQuantity(newAvailableQuantity);
        bookRepository.save(book);
    }
    
    // 统计方法
    public long getTotalBookCount() {
        return bookRepository.count();
    }
    
    public long getAvailableBookCount() {
        return bookRepository.countByStatus(Book.Status.AVAILABLE);
    }
} 