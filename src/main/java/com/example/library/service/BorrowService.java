package com.example.library.service;

import com.example.library.dto.BorrowRecordDTO;
import com.example.library.entity.Book;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BorrowService {
    
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookService bookService;
    
    private static final int MAX_BORROW_BOOKS = 5; // 最大借阅数量
    private static final int BORROW_DAYS = 30; // 借阅天数
    
    public BorrowRecordDTO createBorrowRequest(String username, Long bookId, String remarks) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        
        // 检查图书是否可借
        if (book.getAvailableQuantity() <= 0 || book.getStatus() != Book.Status.AVAILABLE) {
            throw new RuntimeException("图书暂不可借");
        }
        
        // 检查用户是否已达到最大借阅数量
        long activeBorrows = borrowRecordRepository.countActiveBorrowsByUser(user);
        if (activeBorrows >= MAX_BORROW_BOOKS) {
            throw new RuntimeException("您已达到最大借阅数量限制");
        }
        
        // 检查用户是否已借阅该图书
        List<BorrowRecord> activeBorrowsForBook = borrowRecordRepository.findActiveBorrowsByUserAndBook(user, book);
        if (!activeBorrowsForBook.isEmpty()) {
            throw new RuntimeException("您已借阅该图书，不能重复借阅");
        }
        
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setUser(user);
        borrowRecord.setBook(book);
        borrowRecord.setStatus(BorrowRecord.Status.PENDING);
        borrowRecord.setRemarks(remarks);
        
        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);
        return BorrowRecordDTO.fromEntity(savedRecord);
    }
    
    public BorrowRecordDTO approveBorrowRequest(Long recordId, String remarks) {
        BorrowRecord borrowRecord = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        
        if (borrowRecord.getStatus() != BorrowRecord.Status.PENDING) {
            throw new RuntimeException("只能审核待审核状态的借阅申请");
        }
        
        // 检查图书是否还有库存
        Book book = borrowRecord.getBook();
        if (book.getAvailableQuantity() <= 0) {
            throw new RuntimeException("图书库存不足");
        }
        
        borrowRecord.setStatus(BorrowRecord.Status.BORROWED);
        borrowRecord.setBorrowDate(LocalDateTime.now());
        borrowRecord.setDueDate(LocalDateTime.now().plusDays(BORROW_DAYS));
        borrowRecord.setRemarks(remarks);
        
        // 更新图书库存
        bookService.updateBookQuantity(book.getId(), 1);
        
        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);
        return BorrowRecordDTO.fromEntity(savedRecord);
    }
    
    public BorrowRecordDTO rejectBorrowRequest(Long recordId, String remarks) {
        BorrowRecord borrowRecord = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        
        if (borrowRecord.getStatus() != BorrowRecord.Status.PENDING) {
            throw new RuntimeException("只能拒绝待审核状态的借阅申请");
        }
        
        borrowRecord.setStatus(BorrowRecord.Status.REJECTED);
        borrowRecord.setRemarks(remarks);
        
        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);
        return BorrowRecordDTO.fromEntity(savedRecord);
    }
    
    public BorrowRecordDTO returnBook(Long recordId, String remarks) {
        BorrowRecord borrowRecord = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        
        if (borrowRecord.getStatus() != BorrowRecord.Status.BORROWED && 
            borrowRecord.getStatus() != BorrowRecord.Status.OVERDUE) {
            throw new RuntimeException("只能归还已借出或逾期状态的图书");
        }
        
        borrowRecord.setStatus(BorrowRecord.Status.RETURNED);
        borrowRecord.setReturnDate(LocalDateTime.now());
        borrowRecord.setRemarks(remarks);
        
        // 更新图书库存
        bookService.returnBook(borrowRecord.getBook().getId(), 1);
        
        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);
        return BorrowRecordDTO.fromEntity(savedRecord);
    }
    
    public Page<BorrowRecordDTO> getAllBorrowRecords(Long userId, Long bookId, BorrowRecord.Status status, Pageable pageable) {
        Page<BorrowRecord> records = borrowRecordRepository.findByFilters(userId, bookId, status, pageable);
        return records.map(BorrowRecordDTO::fromEntity);
    }
    
    public Page<BorrowRecordDTO> getUserBorrowRecords(String username, BorrowRecord.Status status, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Page<BorrowRecord> records;
        if (status != null) {
            records = borrowRecordRepository.findByUserAndStatus(user, status, pageable);
        } else {
            records = borrowRecordRepository.findByUser(user, pageable);
        }
        
        return records.map(BorrowRecordDTO::fromEntity);
    }
    
    public BorrowRecordDTO getBorrowRecordById(Long id) {
        BorrowRecord borrowRecord = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        return BorrowRecordDTO.fromEntity(borrowRecord);
    }
    
    public List<BorrowRecordDTO> getOverdueRecords() {
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords(
                BorrowRecord.Status.BORROWED, LocalDateTime.now());
        
        // 更新逾期状态
        for (BorrowRecord record : overdueRecords) {
            record.setStatus(BorrowRecord.Status.OVERDUE);
            borrowRecordRepository.save(record);
        }
        
        return overdueRecords.stream()
                .map(BorrowRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    public Page<BorrowRecordDTO> getPendingBorrowRequests(Pageable pageable) {
        Page<BorrowRecord> records = borrowRecordRepository.findByStatus(BorrowRecord.Status.PENDING, pageable);
        return records.map(BorrowRecordDTO::fromEntity);
    }
    
    // 统计方法
    public long getTotalBorrowCount() {
        return borrowRecordRepository.count();
    }
    
    public long getActiveBorrowCount() {
        return borrowRecordRepository.countByStatusIn(
                List.of(BorrowRecord.Status.BORROWED, BorrowRecord.Status.OVERDUE));
    }
    
    public long getPendingBorrowCount() {
        return borrowRecordRepository.countByStatus(BorrowRecord.Status.PENDING);
    }
    
    public long getOverdueBorrowCount() {
        return borrowRecordRepository.countByStatus(BorrowRecord.Status.OVERDUE);
    }
    
    // 读者取消借阅申请
    public void cancelBorrowRequest(String username, Long recordId) {
        BorrowRecord borrowRecord = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        
        // 验证是否是当前用户的记录
        if (!borrowRecord.getUser().getUsername().equals(username)) {
            throw new RuntimeException("您只能取消自己的借阅申请");
        }
        
        // 只能取消待审核状态的申请
        if (borrowRecord.getStatus() != BorrowRecord.Status.PENDING) {
            throw new RuntimeException("只能取消待审核状态的借阅申请");
        }
        
        // 直接删除记录，这样就不会在管理员那里显示
        borrowRecordRepository.delete(borrowRecord);
    }
} 