-- ===========================================
-- 图书管理系统数据库初始化脚本
-- ===========================================

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 1. 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 显示数据库创建结果
SHOW DATABASES LIKE 'library_db';

-- 使用数据库
USE library_db;

-- 显示当前使用的数据库
SELECT DATABASE() as current_database;

-- 2. 删除已存在的表（按依赖关系倒序）
DROP TABLE IF EXISTS borrow_records;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- 3. 创建用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密后）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    role ENUM('ADMIN', 'READER') NOT NULL DEFAULT 'READER' COMMENT '角色',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_enabled (enabled),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 验证用户表创建
DESCRIBE users;

-- 4. 创建分类表
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    description VARCHAR(200) COMMENT '分类描述',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_name (name),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

-- 验证分类表创建
DESCRIBE categories;

-- 5. 创建图书表
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '图书标题',
    author VARCHAR(100) NOT NULL COMMENT '作者',
    isbn VARCHAR(20) UNIQUE COMMENT 'ISBN编号',
    publisher VARCHAR(100) COMMENT '出版社',
    publish_date VARCHAR(20) COMMENT '出版日期',
    description TEXT COMMENT '图书描述',
    cover_image VARCHAR(500) COMMENT '封面图片URL',
    total_quantity INT NOT NULL DEFAULT 0 COMMENT '总数量',
    available_quantity INT NOT NULL DEFAULT 0 COMMENT '可借数量',
    price DECIMAL(10, 2) COMMENT '价格',
    category_id BIGINT COMMENT '分类ID',
    status ENUM('AVAILABLE', 'UNAVAILABLE', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE' COMMENT '图书状态',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_isbn (isbn),
    INDEX idx_publisher (publisher),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_available_quantity (available_quantity),
    INDEX idx_created_at (created_at),
    FULLTEXT KEY ft_search (title, author, publisher)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书表';

-- 验证图书表创建
DESCRIBE books;

-- 6. 创建借阅记录表
CREATE TABLE borrow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    book_id BIGINT NOT NULL COMMENT '图书ID',
    status ENUM('PENDING', 'APPROVED', 'BORROWED', 'RETURNED', 'OVERDUE', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '借阅状态',
    borrow_date TIMESTAMP NULL COMMENT '借阅日期',
    due_date TIMESTAMP NULL COMMENT '应还日期',
    return_date TIMESTAMP NULL COMMENT '实际归还日期',
    remarks VARCHAR(500) COMMENT '备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id),
    INDEX idx_status (status),
    INDEX idx_borrow_date (borrow_date),
    INDEX idx_due_date (due_date),
    INDEX idx_return_date (return_date),
    INDEX idx_created_at (created_at),
    INDEX idx_user_book (user_id, book_id),
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

-- 验证借阅记录表创建
DESCRIBE borrow_records;

-- ===========================================
-- 插入初始数据
-- ===========================================

-- 显示开始插入数据
SELECT '开始插入初始数据...' as message;

-- 1. 插入默认管理员用户（密码：admin123，已加密）
INSERT INTO users (username, password, real_name, email, role, enabled) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKTnl4iu3VLOCPfLNx0VNjqv4eOa', '系统管理员', 'admin@library.com', 'ADMIN', TRUE);

-- 验证管理员用户插入
SELECT 'admin用户已创建' as message, COUNT(*) as admin_count FROM users WHERE role = 'ADMIN';

-- 2. 插入默认图书分类
INSERT INTO categories (name, description) VALUES
('文学', '文学类图书，包括小说、诗歌、散文等'),
('历史', '历史类图书，包括中外历史、传记等'),
('哲学', '哲学类图书，包括哲学理论、思想史等'),
('科学', '科学类图书，包括自然科学、数学、物理等'),
('技术', '技术类图书，包括计算机、工程技术等'),
('艺术', '艺术类图书，包括美术、音乐、设计等'),
('医学', '医学类图书，包括临床医学、基础医学等'),
('法律', '法律类图书，包括法学理论、法律实务等'),
('经济', '经济类图书，包括经济学、管理学等'),
('教育', '教育类图书，包括教育理论、教学方法等');

-- 验证分类插入
SELECT '分类已创建' as message, COUNT(*) as category_count FROM categories;

-- 3. 插入示例图书数据
INSERT INTO books (title, author, isbn, publisher, publish_date, description, total_quantity, available_quantity, price, category_id, status) VALUES
-- 文学类
('红楼梦', '曹雪芹', '9787020002207', '人民文学出版社', '2008-07', '中国古典文学四大名著之一，描写贾宝玉、林黛玉等人的爱情悲剧', 5, 5, 45.00, 1, 'AVAILABLE'),
('西游记', '吴承恩', '9787020002214', '人民文学出版社', '2008-07', '中国古典文学四大名著之一，讲述孙悟空等人西天取经的故事', 3, 3, 42.00, 1, 'AVAILABLE'),
('三国演义', '罗贯中', '9787020002221', '人民文学出版社', '2008-07', '中国古典文学四大名著之一，描写三国时期的历史故事', 4, 4, 48.00, 1, 'AVAILABLE'),
('水浒传', '施耐庵', '9787020002238', '人民文学出版社', '2008-07', '中国古典文学四大名著之一，描写梁山好汉的故事', 3, 3, 46.00, 1, 'AVAILABLE'),

-- 历史类
('史记', '司马迁', '9787101003949', '中华书局', '2006-01', '中国第一部纪传体通史，被誉为"史家之绝唱"', 2, 2, 68.00, 2, 'AVAILABLE'),
('资治通鉴', '司马光', '9787101003956', '中华书局', '2006-01', '中国古代著名的编年体史书', 2, 2, 128.00, 2, 'AVAILABLE'),

-- 科学类
('时间简史', '史蒂芬·霍金', '9787535765444', '湖南科学技术出版社', '2010-04', '著名物理学家霍金的科普著作，探讨宇宙的起源和命运', 6, 6, 39.00, 4, 'AVAILABLE'),
('相对论', '爱因斯坦', '9787100054447', '商务印书馆', '2009-03', '爱因斯坦关于相对论的经典著作', 3, 3, 25.00, 4, 'AVAILABLE'),

-- 技术类
('Java编程思想', 'Bruce Eckel', '9787111213826', '机械工业出版社', '2007-06', 'Java编程经典教材，深入讲解Java语言特性', 8, 8, 99.00, 5, 'AVAILABLE'),
('算法导论', 'Thomas H. Cormen', '9787111407010', '机械工业出版社', '2013-01', '计算机算法领域的经典教材', 5, 5, 128.00, 5, 'AVAILABLE'),
('深入理解计算机系统', 'Randal E. Bryant', '9787111321312', '机械工业出版社', '2011-01', '计算机系统结构经典教材', 4, 4, 139.00, 5, 'AVAILABLE'),
('设计模式', 'Erich Gamma', '9787111075776', '机械工业出版社', '2000-09', '面向对象设计模式的经典著作', 6, 6, 65.00, 5, 'AVAILABLE'),

-- 哲学类
('论语', '孔子', '9787101003963', '中华书局', '2006-01', '儒家经典著作，记录孔子及其弟子的言行', 4, 4, 28.00, 3, 'AVAILABLE'),
('道德经', '老子', '9787101003970', '中华书局', '2006-01', '道家经典著作，阐述道家哲学思想', 3, 3, 25.00, 3, 'AVAILABLE'),

-- 经济类
('国富论', '亚当·斯密', '9787100042895', '商务印书馆', '2005-05', '现代经济学的奠基之作', 3, 3, 58.00, 9, 'AVAILABLE'),
('经济学原理', 'N. Gregory Mankiw', '9787301158197', '北京大学出版社', '2009-04', '经济学入门经典教材', 5, 5, 89.00, 9, 'AVAILABLE');

-- 验证图书插入
SELECT '图书已创建' as message, COUNT(*) as book_count FROM books;

-- 4. 插入示例读者用户（密码：123456，已加密）
INSERT INTO users (username, password, real_name, email, phone, role, enabled) VALUES
('reader1', '$2a$10$DkJHvCqTTpJrJr6.JyFfP.GVqQJ5aCjU9gqhK5oLwdgN5cPGQM8.a', '张三', 'zhangsan@example.com', '13800138001', 'READER', TRUE),
('reader2', '$2a$10$DkJHvCqTTpJrJr6.JyFfP.GVqQJ5aCjU9gqhK5oLwdgN5cPGQM8.a', '李四', 'lisi@example.com', '13800138002', 'READER', TRUE),
('reader3', '$2a$10$DkJHvCqTTpJrJr6.JyFfP.GVqQJ5aCjU9gqhK5oLwdgN5cPGQM8.a', '王五', 'wangwu@example.com', '13800138003', 'READER', TRUE);

-- 验证读者用户插入
SELECT '读者用户已创建' as message, COUNT(*) as reader_count FROM users WHERE role = 'READER';

-- 5. 插入示例借阅记录
INSERT INTO borrow_records (user_id, book_id, status, borrow_date, due_date, return_date, remarks) VALUES
-- 已借出的记录
(2, 1, 'BORROWED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 5 DAY), INTERVAL 30 DAY), NULL, '申请借阅红楼梦'),
(3, 9, 'BORROWED', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 10 DAY), INTERVAL 30 DAY), NULL, '学习Java编程'),
-- 待审核的记录
(2, 10, 'PENDING', NULL, NULL, NULL, '申请借阅算法导论'),
(3, 4, 'PENDING', NULL, NULL, NULL, '想读水浒传'),
-- 已归还的记录
(2, 7, 'RETURNED', DATE_SUB(NOW(), INTERVAL 45 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 45 DAY), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), '已读完时间简史'),
-- 已拒绝的记录
(3, 6, 'REJECTED', NULL, NULL, NULL, '该书暂不外借');

-- 验证借阅记录插入
SELECT '借阅记录已创建' as message, COUNT(*) as borrow_count FROM borrow_records;

-- 更新图书可借数量（减去已借出的数量）
UPDATE books SET available_quantity = available_quantity - 1 WHERE id = 1; -- 红楼梦
UPDATE books SET available_quantity = available_quantity - 1 WHERE id = 9; -- Java编程思想

-- ===========================================
-- 创建视图（可选）
-- ===========================================

-- 删除已存在的视图
DROP VIEW IF EXISTS v_book_details;
DROP VIEW IF EXISTS v_borrow_details;

-- 创建图书详情视图
CREATE VIEW v_book_details AS
SELECT 
    b.id,
    b.title,
    b.author,
    b.isbn,
    b.publisher,
    b.publish_date,
    b.description,
    b.cover_image,
    b.total_quantity,
    b.available_quantity,
    b.price,
    b.status,
    b.created_at,
    b.updated_at,
    c.id AS category_id,
    c.name AS category_name,
    c.description AS category_description
FROM books b
LEFT JOIN categories c ON b.category_id = c.id;

-- 创建借阅记录详情视图
CREATE VIEW v_borrow_details AS
SELECT 
    br.id,
    br.status,
    br.borrow_date,
    br.due_date,
    br.return_date,
    br.remarks,
    br.created_at,
    br.updated_at,
    u.id AS user_id,
    u.username,
    u.real_name,
    u.email,
    b.id AS book_id,
    b.title AS book_title,
    b.author AS book_author,
    b.isbn AS book_isbn,
    c.name AS category_name
FROM borrow_records br
JOIN users u ON br.user_id = u.id
JOIN books b ON br.book_id = b.id
LEFT JOIN categories c ON b.category_id = c.id;

-- ===========================================
-- 创建存储过程（可选）
-- ===========================================

-- 删除已存在的存储过程
DROP PROCEDURE IF EXISTS CheckOverdueBooks;
DROP PROCEDURE IF EXISTS GetUserBorrowStats;
DROP PROCEDURE IF EXISTS GetBookBorrowStats;

DELIMITER //

-- 检查逾期图书的存储过程
CREATE PROCEDURE CheckOverdueBooks()
BEGIN
    UPDATE borrow_records 
    SET status = 'OVERDUE' 
    WHERE status = 'BORROWED' 
    AND due_date < NOW();
    
    SELECT COUNT(*) as overdue_count FROM borrow_records WHERE status = 'OVERDUE';
END //

-- 获取用户借阅统计的存储过程
CREATE PROCEDURE GetUserBorrowStats(IN user_id BIGINT)
BEGIN
    SELECT 
        COUNT(*) as total_borrows,
        SUM(CASE WHEN status = 'BORROWED' THEN 1 ELSE 0 END) as active_borrows,
        SUM(CASE WHEN status = 'RETURNED' THEN 1 ELSE 0 END) as returned_books,
        SUM(CASE WHEN status = 'OVERDUE' THEN 1 ELSE 0 END) as overdue_books
    FROM borrow_records 
    WHERE user_id = user_id;
END //

-- 获取图书借阅统计的存储过程
CREATE PROCEDURE GetBookBorrowStats(IN book_id BIGINT)
BEGIN
    SELECT 
        COUNT(*) as total_borrows,
        SUM(CASE WHEN status IN ('BORROWED', 'OVERDUE') THEN 1 ELSE 0 END) as current_borrows,
        AVG(DATEDIFF(return_date, borrow_date)) as avg_borrow_days
    FROM borrow_records 
    WHERE book_id = book_id AND borrow_date IS NOT NULL;
END //

DELIMITER ;

-- ===========================================
-- 创建触发器（可选）
-- ===========================================

-- 删除已存在的触发器
DROP TRIGGER IF EXISTS tr_borrow_status_update;

DELIMITER //

-- 借阅记录状态变更时更新图书库存的触发器
CREATE TRIGGER tr_borrow_status_update
AFTER UPDATE ON borrow_records
FOR EACH ROW
BEGIN
    -- 如果状态从PENDING变为BORROWED，减少可借数量
    IF OLD.status = 'PENDING' AND NEW.status = 'BORROWED' THEN
        UPDATE books SET available_quantity = available_quantity - 1 WHERE id = NEW.book_id;
    END IF;
    
    -- 如果状态变为RETURNED，增加可借数量
    IF OLD.status IN ('BORROWED', 'OVERDUE') AND NEW.status = 'RETURNED' THEN
        UPDATE books SET available_quantity = available_quantity + 1 WHERE id = NEW.book_id;
    END IF;
END //

DELIMITER ;

-- ===========================================
-- 数据库初始化完成
-- ===========================================

-- 显示所有表
SHOW TABLES;

-- 显示初始化结果
SELECT '=== 数据库初始化完成！===' as message;
SELECT COUNT(*) as user_count, '用户总数' as description FROM users
UNION ALL
SELECT COUNT(*) as category_count, '分类总数' as description FROM categories
UNION ALL
SELECT COUNT(*) as book_count, '图书总数' as description FROM books
UNION ALL
SELECT COUNT(*) as borrow_record_count, '借阅记录总数' as description FROM borrow_records;

-- 显示默认账户信息
SELECT 
    '管理员账户：admin / admin123' as admin_account
UNION ALL
SELECT 
    '示例读者账户：reader1, reader2, reader3 / 123456' as reader_accounts;

-- 验证数据完整性
SELECT 
    b.title,
    c.name as category,
    b.total_quantity,
    b.available_quantity,
    b.status
FROM books b
LEFT JOIN categories c ON b.category_id = c.id
LIMIT 5; 