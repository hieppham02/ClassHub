CREATE DATABASE IF NOT EXISTS muontrathietbi
CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE muontrathietbi;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS phieu_muon;
DROP TABLE IF EXISTS phong_hoc_thiet_bi;
DROP TABLE IF EXISTS thiet_bi;
DROP TABLE IF EXISTS phong_hoc;
DROP TABLE IF EXISTS tai_khoan;
DROP TABLE IF EXISTS toa_nha;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1. BẢNG TÒA NHÀ
-- =========================================================

CREATE TABLE toa_nha (
    ma_toa_nha VARCHAR(50) NOT NULL,
    ten_toa_nha VARCHAR(255) NOT NULL,

    PRIMARY KEY (ma_toa_nha)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_general_ci;


-- =========================================================
-- 2. BẢNG TÀI KHOẢN
-- =========================================================

CREATE TABLE tai_khoan (
    ma_sv VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    ho_ten VARCHAR(100) NOT NULL,
    mat_khau VARCHAR(255) NOT NULL,
    sdt VARCHAR(15) DEFAULT NULL,
    vai_tro VARCHAR(20) NOT NULL,
    ten_lop VARCHAR(50) DEFAULT NULL,

    PRIMARY KEY (ma_sv),

    UNIQUE KEY uq_tai_khoan_email (email)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_general_ci;


-- =========================================================
-- 3. BẢNG PHÒNG HỌC
-- =========================================================

CREATE TABLE phong_hoc (
    ma_phong VARCHAR(255) NOT NULL,
    ten_phong VARCHAR(255) NOT NULL,
    suc_chua INT DEFAULT NULL,
    trang_thai VARCHAR(20) NOT NULL DEFAULT '0',
    ma_toa_nha VARCHAR(50) DEFAULT NULL,

    PRIMARY KEY (ma_phong),

    KEY fk_phong_toanha (ma_toa_nha),

    CONSTRAINT fk_phong_toanha
        FOREIGN KEY (ma_toa_nha)
        REFERENCES toa_nha (ma_toa_nha)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_general_ci;


-- =========================================================
-- 4. BẢNG LOẠI THIẾT BỊ
-- Chỉ lưu 4 loại thiết bị
-- =========================================================

CREATE TABLE thiet_bi (
    id INT NOT NULL AUTO_INCREMENT,
    ten_thiet_bi VARCHAR(100) NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uq_thiet_bi_ten (ten_thiet_bi)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_general_ci;


-- =========================================================
-- 5. BẢNG LIÊN KẾT PHÒNG - THIẾT BỊ
--
-- Ví dụ:
-- DTD101 | Bộ loa mic             | 1
-- DTD101 | Điều khiển điều hòa    | 1
-- DTD101 | Điều khiển máy chiếu   | 1
-- DTD101 | Chìa khóa              | 2
-- =========================================================

CREATE TABLE phong_hoc_thiet_bi (
    ma_phong VARCHAR(255) NOT NULL,
    thiet_bi_id INT NOT NULL,
    so_luong INT NOT NULL DEFAULT 1,

    PRIMARY KEY (ma_phong, thiet_bi_id),

    KEY fk_phongtb_thietbi (thiet_bi_id),

    CONSTRAINT fk_phongtb_phong
        FOREIGN KEY (ma_phong)
        REFERENCES phong_hoc (ma_phong)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_phongtb_thietbi
        FOREIGN KEY (thiet_bi_id)
        REFERENCES thiet_bi (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_general_ci;


-- =========================================================
-- 6. BẢNG PHIẾU MƯỢN
-- =========================================================

CREATE TABLE phieu_muon (
    id INT NOT NULL AUTO_INCREMENT,
    ma_sv VARCHAR(50) NOT NULL,
    ma_phong VARCHAR(255) NOT NULL,
    ngay_muon DATE NOT NULL,
    ca_muon INT NOT NULL,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    thoi_gian_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY fk_phieu_taikhoan (ma_sv),
    KEY fk_phieu_phong (ma_phong),

    CONSTRAINT fk_phieu_taikhoan
        FOREIGN KEY (ma_sv)
        REFERENCES tai_khoan (ma_sv)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_phieu_phong
        FOREIGN KEY (ma_phong)
        REFERENCES phong_hoc (ma_phong)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_general_ci;


-- =========================================================
-- SEED DATA
-- =========================================================


-- =========================================================
-- 7. TÒA NHÀ
-- =========================================================

INSERT INTO toa_nha (ma_toa_nha, ten_toa_nha) VALUES
('DTD', 'Đinh Trọng Dật'),
('EAUT', 'EAUT'),
('PLC', 'POLYCO'),
('TT', 'Thuận Thành'),
('VNB', 'Việt Nam Building');


-- =========================================================
-- 8. TÀI KHOẢN
-- =========================================================

INSERT INTO tai_khoan
(
    ma_sv,
    email,
    ho_ten,
    mat_khau,
    sdt,
    vai_tro,
    ten_lop
)
VALUES
(
    '20231206',
    '20231206@eaut.edu.vn',
    'Phạm Hoàng Hiệp',
    '123456',
    '0969299262',
    'ADMIN',
    'DCCNTT.14.3'
),
(
    '20230001',
    '20230001@eaut.edu.vn',
    'Nguyễn Văn An',
    '123456',
    '0900000001',
    'SINHVIEN',
    'DCCNTT.14.1'
),
(
    '20230002',
    '20230002@eaut.edu.vn',
    'Trần Thị Bình',
    '123456',
    '0900000002',
    'SINHVIEN',
    'DCCNTT.14.2'
),
(
    '20230003',
    '20230003@eaut.edu.vn',
    'Lê Minh Cường',
    '123456',
    '0900000003',
    'ADMIN',
    'DCCNTT.14.3'
);


-- =========================================================
-- 9. TẠO PHÒNG HỌC
-- Mỗi tòa:
-- 6 tầng
-- mỗi tầng 8 phòng
--
-- 5 x 6 x 8 = 240 phòng
-- =========================================================

INSERT INTO phong_hoc
(
    ma_phong,
    ten_phong,
    suc_chua,
    trang_thai,
    ma_toa_nha
)
SELECT
    CONCAT(
        t.ma_toa_nha,
        f.tang,
        LPAD(p.phong, 2, '0')
    ) AS ma_phong,

    CONCAT(
        t.ma_toa_nha,
        '-',
        f.tang,
        LPAD(p.phong, 2, '0')
    ) AS ten_phong,

    70 AS suc_chua,
    '0' AS trang_thai,
    t.ma_toa_nha

FROM toa_nha t

CROSS JOIN (
    SELECT 1 AS tang
    UNION ALL SELECT 2
    UNION ALL SELECT 3
    UNION ALL SELECT 4
    UNION ALL SELECT 5
    UNION ALL SELECT 6
) f

CROSS JOIN (
    SELECT 1 AS phong
    UNION ALL SELECT 2
    UNION ALL SELECT 3
    UNION ALL SELECT 4
    UNION ALL SELECT 5
    UNION ALL SELECT 6
    UNION ALL SELECT 7
    UNION ALL SELECT 8
) p;


-- =========================================================
-- 10. 4 LOẠI THIẾT BỊ
-- Bảng này chỉ có đúng 4 record
-- =========================================================

INSERT INTO thiet_bi (ten_thiet_bi) VALUES
('Bộ loa mic'),
('Điều khiển điều hòa'),
('Điều khiển máy chiếu'),
('Chìa khóa');


-- =========================================================
-- 11. GÁN THIẾT BỊ CHO TẤT CẢ PHÒNG
--
-- 3 thiết bị đầu: số lượng = 1
-- Chìa khóa: số lượng = 2
-- =========================================================

INSERT INTO phong_hoc_thiet_bi
(
    ma_phong,
    thiet_bi_id,
    so_luong
)
SELECT
    p.ma_phong,
    tb.id,

    CASE
        WHEN tb.ten_thiet_bi = 'Chìa khóa' THEN 2
        ELSE 1
    END AS so_luong

FROM phong_hoc p
CROSS JOIN thiet_bi tb;