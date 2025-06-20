package com.example.library.repository;

import com.example.library.entity.BorrowRecord;
import com.example.library.entity.User;
import com.example.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    
    Page<BorrowRecord> findByUser(User user, Pageable pageable);
    
    Page<BorrowRecord> findByBook(Book book, Pageable pageable);
    
    Page<BorrowRecord> findByStatus(BorrowRecord.Status status, Pageable pageable);
    
    Page<BorrowRecord> findByUserAndStatus(User user, BorrowRecord.Status status, Pageable pageable);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = :status AND br.dueDate < :currentDate")
    List<BorrowRecord> findOverdueRecords(@Param("status") BorrowRecord.Status status, 
                                         @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT br FROM BorrowRecord br WHERE " +
           "(:userId IS NULL OR br.user.id = :userId) AND " +
           "(:bookId IS NULL OR br.book.id = :bookId) AND " +
           "(:status IS NULL OR br.status = :status)")
    Page<BorrowRecord> findByFilters(@Param("userId") Long userId, 
                                   @Param("bookId") Long bookId, 
                                   @Param("status") BorrowRecord.Status status, 
                                   Pageable pageable);
    
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.user = :user AND br.status IN ('APPROVED', 'BORROWED')")
    long countActiveBorrowsByUser(@Param("user") User user);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.user = :user AND br.book = :book AND br.status IN ('APPROVED', 'BORROWED')")
    List<BorrowRecord> findActiveBorrowsByUserAndBook(@Param("user") User user, @Param("book") Book book);
    
    long countByStatus(BorrowRecord.Status status);
    
    long countByStatusIn(List<BorrowRecord.Status> statuses);
} 