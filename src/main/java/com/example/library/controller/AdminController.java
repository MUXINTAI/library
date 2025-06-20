package com.example.library.controller;

import com.example.library.dto.*;
import com.example.library.entity.Book;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.User;
import com.example.library.service.BookService;
import com.example.library.service.BorrowService;
import com.example.library.service.CategoryService;
import com.example.library.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private BorrowService borrowService;
    
    // ==================== 用户管理 ====================
    
    @GetMapping("/users")
    public ApiResponse<Page<UserDTO>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) User.Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<UserDTO> users = userService.getAllUsers(keyword, role, pageable);
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/users/{id}")
    public ApiResponse<UserDTO> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = userService.getUserById(id);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/users/{id}")
    public ApiResponse<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody RegisterRequest updateRequest) {
        try {
            UserDTO user = userService.updateUser(id, updateRequest);
            return ApiResponse.success("用户信息更新成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/users/{id}/status")
    public ApiResponse<UserDTO> toggleUserStatus(@PathVariable Long id) {
        try {
            UserDTO user = userService.toggleUserStatus(id);
            return ApiResponse.success("用户状态更新成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ApiResponse.success("用户删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // ==================== 图书管理 ====================
    
    @PostMapping("/books")
    public ApiResponse<BookDTO> createBook(@Valid @RequestBody BookRequest bookRequest) {
        try {
            BookDTO book = bookService.createBook(bookRequest);
            return ApiResponse.success("图书创建成功", book);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/books")
    public ApiResponse<Page<BookDTO>> getAllBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Book.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<BookDTO> books = bookService.getAllBooks(keyword, categoryId, status, pageable);
            return ApiResponse.success(books);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/books/{id}")
    public ApiResponse<BookDTO> getBookById(@PathVariable Long id) {
        try {
            BookDTO book = bookService.getBookById(id);
            return ApiResponse.success(book);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/books/{id}")
    public ApiResponse<BookDTO> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest bookRequest) {
        try {
            BookDTO book = bookService.updateBook(id, bookRequest);
            return ApiResponse.success("图书更新成功", book);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/books/{id}/status")
    public ApiResponse<BookDTO> updateBookStatus(@PathVariable Long id, @RequestParam Book.Status status) {
        try {
            BookDTO book = bookService.updateBookStatus(id, status);
            return ApiResponse.success("图书状态更新成功", book);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/books/{id}")
    public ApiResponse<Void> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ApiResponse.success("图书删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // ==================== 分类管理 ====================
    
    @PostMapping("/categories")
    public ApiResponse<CategoryDTO> createCategory(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String description = request.get("description");
            CategoryDTO category = categoryService.createCategory(name, description);
            return ApiResponse.success("分类创建成功", category);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/categories")
    public ApiResponse<Page<CategoryDTO>> getAllCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<CategoryDTO> categories = categoryService.getAllCategories(keyword, pageable);
            return ApiResponse.success(categories);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/categories/{id}")
    public ApiResponse<CategoryDTO> getCategoryById(@PathVariable Long id) {
        try {
            CategoryDTO category = categoryService.getCategoryById(id);
            return ApiResponse.success(category);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/categories/{id}")
    public ApiResponse<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String description = request.get("description");
            CategoryDTO category = categoryService.updateCategory(id, name, description);
            return ApiResponse.success("分类更新成功", category);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ApiResponse.success("分类删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // ==================== 借阅管理 ====================
    
    @GetMapping("/borrow-records")
    public ApiResponse<Page<BorrowRecordDTO>> getAllBorrowRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) BorrowRecord.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<BorrowRecordDTO> records = borrowService.getAllBorrowRecords(userId, bookId, status, pageable);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/borrow-records/pending")
    public ApiResponse<Page<BorrowRecordDTO>> getPendingBorrowRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
            Page<BorrowRecordDTO> records = borrowService.getPendingBorrowRequests(pageable);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/borrow-records/{id}/approve")
    public ApiResponse<BorrowRecordDTO> approveBorrowRequest(@PathVariable Long id, @RequestParam(required = false) String remarks) {
        try {
            BorrowRecordDTO record = borrowService.approveBorrowRequest(id, remarks);
            return ApiResponse.success("借阅申请已批准", record);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/borrow-records/{id}/reject")
    public ApiResponse<BorrowRecordDTO> rejectBorrowRequest(@PathVariable Long id, @RequestParam(required = false) String remarks) {
        try {
            BorrowRecordDTO record = borrowService.rejectBorrowRequest(id, remarks);
            return ApiResponse.success("借阅申请已拒绝", record);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/borrow-records/{id}/return")
    public ApiResponse<BorrowRecordDTO> returnBook(@PathVariable Long id, @RequestParam(required = false) String remarks) {
        try {
            BorrowRecordDTO record = borrowService.returnBook(id, remarks);
            return ApiResponse.success("图书归还成功", record);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/borrow-records/overdue")
    public ApiResponse<List<BorrowRecordDTO>> getOverdueRecords() {
        try {
            List<BorrowRecordDTO> records = borrowService.getOverdueRecords();
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // ==================== 系统统计 ====================
    
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getSystemStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 获取用户统计
            long totalUsers = userService.getTotalUserCount();
            long totalReaders = userService.getUserCountByRole(User.Role.READER);
            long totalAdmins = userService.getUserCountByRole(User.Role.ADMIN);
            
            // 获取图书统计
            long totalBooks = bookService.getTotalBookCount();
            long availableBooks = bookService.getAvailableBookCount();
            
            // 获取借阅统计
            long totalBorrows = borrowService.getTotalBorrowCount();
            long activeBorrows = borrowService.getActiveBorrowCount();
            long pendingBorrows = borrowService.getPendingBorrowCount();
            long overdueBooks = borrowService.getOverdueBorrowCount();
            
            stats.put("totalUsers", totalUsers);
            stats.put("totalReaders", totalReaders);
            stats.put("totalAdmins", totalAdmins);
            stats.put("totalBooks", totalBooks);
            stats.put("availableBooks", availableBooks);
            stats.put("totalBorrows", totalBorrows);
            stats.put("activeBorrows", activeBorrows);
            stats.put("pendingBorrows", pendingBorrows);
            stats.put("overdueBooks", overdueBooks);
            
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
} 