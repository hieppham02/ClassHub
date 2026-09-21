-- Seed lich su muon/tra tu 08/09/2026 den 20/09/2026.
-- Schema: tai_khoan(7 cot), phieu_muon(7 cot) nhu database hien tai.
-- Mat khau chung cua cac tai khoan seed: 123456

SET NAMES utf8mb4;
START TRANSACTION;

-- 10 giang vien va 10 sinh vien.
INSERT INTO tai_khoan
    (ma_sv, email, ho_ten, mat_khau, sdt, vai_tro, ten_lop)
VALUES
    ('GV101', 'nguyen.minh.anh@eaut.edu.vn', 'Nguyễn Minh Anh', '123456', '0911000101', 'GIANG_VIEN', 'Khoa CNTT'),
    ('GV102', 'tran.thu.ha@eaut.edu.vn', 'Trần Thu Hà', '123456', '0911000102', 'GIANG_VIEN', 'Khoa Điện - Điện tử'),
    ('GV103', 'le.quang.huy@eaut.edu.vn', 'Lê Quang Huy', '123456', '0911000103', 'GIANG_VIEN', 'Khoa Cơ khí'),
    ('GV104', 'pham.ngoc.lan@eaut.edu.vn', 'Phạm Ngọc Lan', '123456', '0911000104', 'GIANG_VIEN', 'Khoa Kinh tế'),
    ('GV105', 'do.van.kien@eaut.edu.vn', 'Đỗ Văn Kiên', '123456', '0911000105', 'GIANG_VIEN', 'Khoa Ô tô'),
    ('GV106', 'bui.thanh.mai@eaut.edu.vn', 'Bùi Thanh Mai', '123456', '0911000106', 'GIANG_VIEN', 'Khoa Ngoại ngữ'),
    ('GV107', 'hoang.duc.long@eaut.edu.vn', 'Hoàng Đức Long', '123456', '0911000107', 'GIANG_VIEN', 'Khoa Xây dựng'),
    ('GV108', 'vu.hai.yen@eaut.edu.vn', 'Vũ Hải Yến', '123456', '0911000108', 'GIANG_VIEN', 'Khoa Quản trị kinh doanh'),
    ('GV109', 'dang.tuan.phong@eaut.edu.vn', 'Đặng Tuấn Phong', '123456', '0911000109', 'GIANG_VIEN', 'Khoa CNTT'),
    ('GV110', 'ngo.thuy.linh@eaut.edu.vn', 'Ngô Thùy Linh', '123456', '0911000110', 'GIANG_VIEN', 'Khoa Điện - Điện tử'),
    ('20241001', '20241001@eaut.edu.vn', 'Nguyễn Đức Minh', '123456', '0922000101', 'SINH_VIEN', 'DCCNTT.15.1'),
    ('20241002', '20241002@eaut.edu.vn', 'Trần Khánh Linh', '123456', '0922000102', 'SINH_VIEN', 'DCCNTT.15.2'),
    ('20241003', '20241003@eaut.edu.vn', 'Lê Hoàng Nam', '123456', '0922000103', 'SINH_VIEN', 'DCDT.15.1'),
    ('20241004', '20241004@eaut.edu.vn', 'Phạm Thu Trang', '123456', '0922000104', 'SINH_VIEN', 'DCKTPM.15.1'),
    ('20241005', '20241005@eaut.edu.vn', 'Đỗ Minh Quân', '123456', '0922000105', 'SINH_VIEN', 'DCCK.15.1'),
    ('20241006', '20241006@eaut.edu.vn', 'Bùi Ngọc Anh', '123456', '0922000106', 'SINH_VIEN', 'DCQTKD.15.2'),
    ('20241007', '20241007@eaut.edu.vn', 'Hoàng Gia Bảo', '123456', '0922000107', 'SINH_VIEN', 'DCCNTT.15.3'),
    ('20241008', '20241008@eaut.edu.vn', 'Vũ Phương Thảo', '123456', '0922000108', 'SINH_VIEN', 'DCOTO.15.1'),
    ('20241009', '20241009@eaut.edu.vn', 'Đặng Quốc Việt', '123456', '0922000109', 'SINH_VIEN', 'DCXD.15.1'),
    ('20241010', '20241010@eaut.edu.vn', 'Ngô Mai Hương', '123456', '0922000110', 'SINH_VIEN', 'DCNN.15.1')
ON DUPLICATE KEY UPDATE
    email = VALUES(email), ho_ten = VALUES(ho_ten),
    mat_khau = VALUES(mat_khau), sdt = VALUES(sdt),
    vai_tro = VALUES(vai_tro), ten_lop = VALUES(ten_lop);

-- Xoa rieng lich su cu cua bo seed de script co the chay lai.
DELETE FROM phieu_muon
WHERE ngay_muon BETWEEN '2026-09-08' AND '2026-09-20'
  AND ma_sv IN (
      'GV101','GV102','GV103','GV104','GV105','GV106','GV107','GV108','GV109','GV110',
      '20241001','20241002','20241003','20241004','20241005',
      '20241006','20241007','20241008','20241009','20241010'
  );

-- Lay truc tiep tat ca phong hien co, khong phu thuoc so tang/so phong co dinh.
DROP TEMPORARY TABLE IF EXISTS seed_rooms;
CREATE TEMPORARY TABLE seed_rooms AS
SELECT ma_phong, ROW_NUMBER() OVER (ORDER BY ma_toa_nha, ma_phong) AS room_no
FROM phong_hoc;

-- Moi phong co it nhat 1 luot. Mot so phong co 2 hoac 3 luot,
-- tao tan su muon/tra khac nhau giua cac phong.
INSERT INTO phieu_muon
    (ma_sv, ma_phong, ngay_muon, ca_muon, trang_thai, thoi_gian_tao)
SELECT
    CASE MOD(events.event_no - 1, 20)
        WHEN 0 THEN 'GV101' WHEN 1 THEN 'GV102' WHEN 2 THEN 'GV103'
        WHEN 3 THEN 'GV104' WHEN 4 THEN 'GV105' WHEN 5 THEN 'GV106'
        WHEN 6 THEN 'GV107' WHEN 7 THEN 'GV108' WHEN 8 THEN 'GV109'
        WHEN 9 THEN 'GV110' WHEN 10 THEN '20241001' WHEN 11 THEN '20241002'
        WHEN 12 THEN '20241003' WHEN 13 THEN '20241004' WHEN 14 THEN '20241005'
        WHEN 15 THEN '20241006' WHEN 16 THEN '20241007' WHEN 17 THEN '20241008'
        WHEN 18 THEN '20241009' ELSE '20241010'
    END AS ma_sv,
    events.ma_phong,
    DATE_ADD('2026-09-08', INTERVAL MOD(events.event_no - 1, 13) DAY) AS ngay_muon,
    MOD(events.event_no + events.dot_seed - 2, 5) + 1 AS ca_muon,
    'DA_TRA' AS trang_thai,
    TIMESTAMP(
        DATE_SUB(DATE_ADD('2026-09-08', INTERVAL MOD(events.event_no - 1, 13) DAY), INTERVAL 1 DAY),
        ADDTIME('13:00:00', SEC_TO_TIME(MOD(events.event_no * 11, 360) * 60))
    ) AS thoi_gian_tao
FROM (
    SELECT room_no AS event_no, ma_phong, 1 AS dot_seed
    FROM seed_rooms

    UNION ALL

    SELECT room_no + 10000 AS event_no, ma_phong, 2 AS dot_seed
    FROM seed_rooms
    WHERE MOD(room_no, 3) = 0

    UNION ALL

    SELECT room_no + 20000 AS event_no, ma_phong, 3 AS dot_seed
    FROM seed_rooms
    WHERE MOD(room_no, 7) = 0
) events;

DROP TEMPORARY TABLE IF EXISTS seed_rooms;
COMMIT;

-- KIEM TRA: 10 GIANG_VIEN va 10 SINH_VIEN.
SELECT vai_tro, COUNT(*) AS so_tai_khoan
FROM tai_khoan
WHERE ma_sv IN (
    'GV101','GV102','GV103','GV104','GV105','GV106','GV107','GV108','GV109','GV110',
    '20241001','20241002','20241003','20241004','20241005',
    '20241006','20241007','20241008','20241009','20241010'
)
GROUP BY vai_tro;

-- KIEM TRA: phan bo lich su theo ngay.
SELECT ngay_muon, COUNT(*) AS so_luot_muon_tra
FROM phieu_muon
WHERE ngay_muon BETWEEN '2026-09-08' AND '2026-09-20'
  AND ma_sv IN (
      'GV101','GV102','GV103','GV104','GV105','GV106','GV107','GV108','GV109','GV110',
      '20241001','20241002','20241003','20241004','20241005',
      '20241006','20241007','20241008','20241009','20241010'
  )
GROUP BY ngay_muon
ORDER BY ngay_muon;

-- KIEM TRA: phu cac toa/tang/phong va tan su tung phong.
SELECT
    pm.ma_phong,
    ph.ma_toa_nha,
    SUBSTRING(pm.ma_phong, LENGTH(ph.ma_toa_nha) + 1, 1) AS tang,
    COUNT(*) AS so_luot_muon_tra
FROM phieu_muon pm
JOIN phong_hoc ph ON ph.ma_phong = pm.ma_phong
WHERE pm.ngay_muon BETWEEN '2026-09-08' AND '2026-09-20'
  AND pm.ma_sv IN (
      'GV101','GV102','GV103','GV104','GV105','GV106','GV107','GV108','GV109','GV110',
      '20241001','20241002','20241003','20241004','20241005',
      '20241006','20241007','20241008','20241009','20241010'
  )
GROUP BY pm.ma_phong, ph.ma_toa_nha
ORDER BY so_luot_muon_tra DESC, ph.ma_toa_nha, pm.ma_phong;
