package com.example.library.controller;

import com.example.library.dto.ApiResponse;
import com.example.library.entity.Book;
import com.example.library.entity.Category;
import com.example.library.entity.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.CategoryRepository;
import com.example.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @GetMapping("/hello")
    public ApiResponse<Map<String, Object>> hello() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Hello from Library Management System!");
        data.put("timestamp", LocalDateTime.now());
        data.put("status", "running");
        
        return ApiResponse.success("系统运行正常", data);
    }
    
    @GetMapping("/db-connection")
    public ApiResponse<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Connection connection = dataSource.getConnection();
            result.put("connected", true);
            result.put("databaseName", connection.getCatalog());
            result.put("url", connection.getMetaData().getURL());
            connection.close();
        } catch (Exception e) {
            result.put("connected", false);
            result.put("error", e.getMessage());
        }
        
        return ApiResponse.success("数据库连接测试", result);
    }
    
    @GetMapping("/data-count")
    public ApiResponse<Map<String, Object>> getDataCount() {
        Map<String, Object> counts = new HashMap<>();
        
        try {
            long userCount = userRepository.count();
            long bookCount = bookRepository.count();
            long categoryCount = categoryRepository.count();
            
            counts.put("users", userCount);
            counts.put("books", bookCount);
            counts.put("categories", categoryCount);
            
            return ApiResponse.success("数据统计", counts);
        } catch (Exception e) {
            return ApiResponse.error("查询数据失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/sample-data")
    public ApiResponse<Map<String, Object>> getSampleData() {
        Map<String, Object> data = new HashMap<>();
        
        try {
            List<User> users = userRepository.findAll();
            List<Book> books = bookRepository.findAll();
            List<Category> categories = categoryRepository.findAll();
            
            data.put("sampleUsers", users.size() > 0 ? users.subList(0, Math.min(3, users.size())) : users);
            data.put("sampleBooks", books.size() > 0 ? books.subList(0, Math.min(3, books.size())) : books);
            data.put("sampleCategories", categories.size() > 0 ? categories.subList(0, Math.min(3, categories.size())) : categories);
            
            return ApiResponse.success("示例数据", data);
        } catch (Exception e) {
            return ApiResponse.error("获取示例数据失败: " + e.getMessage());
        }
    }
} 