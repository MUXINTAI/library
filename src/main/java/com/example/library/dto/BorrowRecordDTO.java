package com.example.library.dto;

import com.example.library.entity.BorrowRecord;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BorrowRecordDTO {
    
    private Long id;
    private UserDTO user;
    private BookDTO book;
    private BorrowRecord.Status status;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static BorrowRecordDTO fromEntity(BorrowRecord borrowRecord) {
        BorrowRecordDTO dto = new BorrowRecordDTO();
        dto.setId(borrowRecord.getId());
        if (borrowRecord.getUser() != null) {
            dto.setUser(UserDTO.fromEntity(borrowRecord.getUser()));
        }
        if (borrowRecord.getBook() != null) {
            dto.setBook(BookDTO.fromEntity(borrowRecord.getBook()));
        }
        dto.setStatus(borrowRecord.getStatus());
        dto.setBorrowDate(borrowRecord.getBorrowDate());
        dto.setDueDate(borrowRecord.getDueDate());
        dto.setReturnDate(borrowRecord.getReturnDate());
        dto.setRemarks(borrowRecord.getRemarks());
        dto.setCreatedAt(borrowRecord.getCreatedAt());
        dto.setUpdatedAt(borrowRecord.getUpdatedAt());
        return dto;
    }
} 