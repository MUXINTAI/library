package com.example.library.controller;

import com.example.library.dto.*;
import com.example.library.entity.BorrowRecord;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reader")
public class ReaderController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private BorrowService borrowService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private UserService userService;
    
    // ==================== 图书浏览 ====================
    
    @GetMapping("/books")
    public ApiResponse<Page<BookDTO>> getAvailableBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<BookDTO> books = bookService.getAvailableBooks(keyword, pageable);
            return ApiResponse.success(books);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/books/{id}")
    public ApiResponse<BookDTO> getBookDetail(@PathVariable Long id) {
        try {
            BookDTO book = bookService.getBookById(id);
            return ApiResponse.success(book);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/categories")
    public ApiResponse<List<CategoryDTO>> getAllCategories() {
        try {
            List<CategoryDTO> categories = categoryService.getAllCategoriesAsList();
            return ApiResponse.success(categories);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // ==================== 借阅管理 ====================
    
    @PostMapping("/borrow-requests")
    public ApiResponse<BorrowRecordDTO> createBorrowRequest(
            Authentication authentication,
            @RequestParam Long bookId,
            @RequestParam(required = false) String remarks) {
        try {
            String username = authentication.getName();
            BorrowRecordDTO record = borrowService.createBorrowRequest(username, bookId, remarks);
            return ApiResponse.success("借阅申请提交成功", record);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/borrow-records")
    public ApiResponse<Page<BorrowRecordDTO>> getMyBorrowRecords(
            Authentication authentication,
            @RequestParam(required = false) BorrowRecord.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            String username = authentication.getName();
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<BorrowRecordDTO> records = borrowService.getUserBorrowRecords(username, status, pageable);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/borrow-records/{id}")
    public ApiResponse<BorrowRecordDTO> getBorrowRecordDetail(@PathVariable Long id, Authentication authentication) {
        try {
            BorrowRecordDTO record = borrowService.getBorrowRecordById(id);
            
            // 确保只能查看自己的借阅记录
            String username = authentication.getName();
            if (!record.getUser().getUsername().equals(username)) {
                return ApiResponse.forbidden("您只能查看自己的借阅记录");
            }
            
            return ApiResponse.success(record);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/borrow-records/active")
    public ApiResponse<Page<BorrowRecordDTO>> getActiveBorrowRecords(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String username = authentication.getName();
            Pageable pageable = PageRequest.of(page, size, Sort.by("borrowDate").descending());
            
            Page<BorrowRecordDTO> records = borrowService.getUserBorrowRecords(username, BorrowRecord.Status.BORROWED, pageable);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/borrow-records/history")
    public ApiResponse<Page<BorrowRecordDTO>> getBorrowHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String username = authentication.getName();
            Pageable pageable = PageRequest.of(page, size, Sort.by("returnDate").descending());
            
            Page<BorrowRecordDTO> records = borrowService.getUserBorrowRecords(username, BorrowRecord.Status.RETURNED, pageable);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // ==================== 个人信息管理 ====================
    
    @GetMapping("/profile")
    public ApiResponse<UserDTO> getProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            UserDTO user = userService.getCurrentUser(username);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/profile")
    public ApiResponse<UserDTO> updateProfile(Authentication authentication, @RequestBody Map<String, String> requestData) {
        try {
            String username = authentication.getName();
            System.out.println("收到更新请求，用户: " + username);
            System.out.println("请求数据: " + requestData);
            
            String realName = requestData.get("realName");
            String email = requestData.get("email");
            String phone = requestData.get("phone");
            
            // 手动验证
            if (realName == null || realName.trim().isEmpty()) {
                return ApiResponse.error("真实姓名不能为空");
            }
            
            // 创建ProfileUpdateRequest对象
            ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
            updateRequest.setRealName(realName);
            updateRequest.setEmail(email);
            updateRequest.setPhone(phone);
            
            UserDTO updatedUser = userService.updateProfile(username, updateRequest);
            return ApiResponse.success("个人信息更新成功", updatedUser);
        } catch (Exception e) {
            System.out.println("更新个人信息错误: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(Authentication authentication, @RequestBody Map<String, String> requestData) {
        try {
            String username = authentication.getName();
            System.out.println("收到密码修改请求，用户: " + username);
            System.out.println("请求数据: " + requestData);
            
            String currentPassword = requestData.get("currentPassword");
            String newPassword = requestData.get("newPassword");
            
            // 手动验证
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                return ApiResponse.error("当前密码不能为空");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ApiResponse.error("新密码不能为空");
            }
            if (newPassword.length() < 6) {
                return ApiResponse.error("新密码长度至少6个字符");
            }
            
            // 创建PasswordUpdateRequest对象
            PasswordUpdateRequest passwordRequest = new PasswordUpdateRequest();
            passwordRequest.setCurrentPassword(currentPassword);
            passwordRequest.setNewPassword(newPassword);
            
            userService.updatePassword(username, passwordRequest);
            return ApiResponse.success("密码修改成功");
        } catch (Exception e) {
            System.out.println("修改密码错误: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // ==================== 取消借阅申请 ====================
    
    @DeleteMapping("/borrow-requests/{id}")
    public ApiResponse<Void> cancelBorrowRequest(Authentication authentication, @PathVariable Long id) {
        try {
            String username = authentication.getName();
            borrowService.cancelBorrowRequest(username, id);
            return ApiResponse.success("借阅申请已取消");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
} 