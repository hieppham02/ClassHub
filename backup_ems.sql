-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: htql_muontrathietbi
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `phieu_muon`
--

DROP TABLE IF EXISTS `phieu_muon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieu_muon` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ma_sv` varchar(50) DEFAULT NULL,
  `ma_phong` varchar(50) DEFAULT NULL,
  `ngay_muon` date DEFAULT NULL,
  `ca_muon` int(11) DEFAULT NULL,
  `trang_thai` varchar(20) DEFAULT 'ACTIVE',
  `thoi_gian_tao` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_phieu_taikhoan` (`ma_sv`),
  KEY `fk_phieu_phong` (`ma_phong`),
  CONSTRAINT `fk_phieu_phong` FOREIGN KEY (`ma_phong`) REFERENCES `phong_hoc` (`ma_phong`),
  CONSTRAINT `fk_phieu_taikhoan` FOREIGN KEY (`ma_sv`) REFERENCES `tai_khoan` (`ma_sv`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieu_muon`
--

LOCK TABLES `phieu_muon` WRITE;
/*!40000 ALTER TABLE `phieu_muon` DISABLE KEYS */;
INSERT INTO `phieu_muon` VALUES (1,'20231206','DTD201','2026-08-14',1,'CANCELED','2026-08-14 12:02:38'),(2,'20231206','EAUT103','2026-08-14',1,'COMPLETED','2026-08-14 13:13:43'),(3,'20231206','DTD201','2026-08-14',1,'COMPLETED','2026-08-14 14:09:05'),(4,'20231206','TT102','2026-08-14',1,'COMPLETED','2026-08-14 15:19:43'),(5,'20231206','VNB205','2026-08-14',1,'COMPLETED','2026-08-14 15:20:12'),(6,'20231206','EAUT103','2026-08-14',1,'COMPLETED','2026-08-14 15:59:34');
/*!40000 ALTER TABLE `phieu_muon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phong_hoc`
--

DROP TABLE IF EXISTS `phong_hoc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phong_hoc` (
  `ma_phong` varchar(255) NOT NULL,
  `ten_phong` varchar(255) NOT NULL,
  `suc_chua` int(11) DEFAULT NULL,
  `trang_thai` varchar(255) DEFAULT NULL,
  `ma_toa_nha` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`ma_phong`),
  KEY `fk_phong_toanha` (`ma_toa_nha`),
  CONSTRAINT `fk_phong_toanha` FOREIGN KEY (`ma_toa_nha`) REFERENCES `toa_nha` (`ma_toa_nha`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phong_hoc`
--

LOCK TABLES `phong_hoc` WRITE;
/*!40000 ALTER TABLE `phong_hoc` DISABLE KEYS */;
INSERT INTO `phong_hoc` VALUES ('DTD201','DTD-201',70,'0','DTD'),('DTD202','DTD-202',70,'0','DTD'),('DTD203','DTD-203',70,'0','DTD'),('EAUT101','EAUT-101',70,'0','EAUT'),('EAUT102','EAUT-102',70,'0','EAUT'),('EAUT103','EAUT-103',70,'0','EAUT'),('EAUT104','EAUT-104',70,'0','EAUT'),('EAUT105','EAUT-105',70,'0','EAUT'),('EAUT106','EAUT-106',70,'0','EAUT'),('EAUT107','EAUT-107',70,'0','EAUT'),('EAUT108','EAUT-108',70,'0','EAUT'),('PLC801','PLC-801',70,'0','PLC'),('PLC802','PLC-802',70,'0','PLC'),('PLC803','PLC-803',70,'0','PLC'),('TT101','TT-101',70,'0','TT'),('TT102','TT-102',70,'0','TT'),('TT103','TT-103',70,'0','TT'),('TT104','TT-104',70,'0','TT'),('TT105','TT-105',70,'0','TT'),('TT106','TT-106',70,'0','TT'),('TT107','TT-107',70,'0','TT'),('TT108','TT-108',70,'0','TT'),('VNB201','VNB-201',70,'0','VNB'),('VNB202','VNB-202',70,'0','VNB'),('VNB203','VNB-203',70,'0','VNB'),('VNB204','VNB-204',70,'0','VNB'),('VNB205','VNB-205',70,'0','VNB'),('VNB206','VNB-206',70,'0','VNB'),('VNB207','VNB-207',70,'0','VNB'),('VNB208','VNB-208',70,'0','VNB');
/*!40000 ALTER TABLE `phong_hoc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tai_khoan`
--

DROP TABLE IF EXISTS `tai_khoan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tai_khoan` (
  `ma_sv` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `ho_ten` varchar(100) NOT NULL,
  `mat_khau` varchar(100) NOT NULL,
  `sdt` varchar(15) DEFAULT NULL,
  `vai_tro` varchar(20) NOT NULL,
  `ten_lop` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`ma_sv`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tai_khoan`
--

LOCK TABLES `tai_khoan` WRITE;
/*!40000 ALTER TABLE `tai_khoan` DISABLE KEYS */;
INSERT INTO `tai_khoan` VALUES ('20231206','20231206@eaut.edu.vn','Phạm Hoàng Hiệp','123456','0969299262','ADMIN','DCCNTT.14.3');
/*!40000 ALTER TABLE `tai_khoan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thiet_bi`
--

DROP TABLE IF EXISTS `thiet_bi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thiet_bi` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ten_thiet_bi` varchar(100) DEFAULT NULL,
  `so_luong` int(11) DEFAULT NULL,
  `ma_phong` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_thietbi_phong` (`ma_phong`),
  CONSTRAINT `fk_thietbi_phong` FOREIGN KEY (`ma_phong`) REFERENCES `phong_hoc` (`ma_phong`)
) ENGINE=InnoDB AUTO_INCREMENT=125 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thiet_bi`
--

LOCK TABLES `thiet_bi` WRITE;
/*!40000 ALTER TABLE `thiet_bi` DISABLE KEYS */;
INSERT INTO `thiet_bi` VALUES (1,'Mic',1,'DTD201'),(2,'Mic',1,'DTD202'),(3,'Mic',1,'DTD203'),(4,'Mic',1,'EAUT101'),(5,'Mic',1,'EAUT102'),(6,'Mic',1,'EAUT103'),(7,'Mic',1,'EAUT104'),(8,'Mic',1,'EAUT105'),(9,'Mic',1,'EAUT106'),(10,'Mic',1,'EAUT107'),(11,'Mic',1,'EAUT108'),(12,'Mic',1,'PLC801'),(13,'Mic',1,'PLC802'),(14,'Mic',1,'PLC803'),(15,'Mic',1,'TT101'),(16,'Mic',1,'TT102'),(17,'Mic',1,'TT103'),(18,'Mic',1,'TT104'),(19,'Mic',1,'TT105'),(20,'Mic',1,'TT106'),(21,'Mic',1,'TT107'),(22,'Mic',1,'TT108'),(23,'Mic',1,'VNB201'),(24,'Mic',1,'VNB202'),(25,'Mic',1,'VNB203'),(26,'Mic',1,'VNB204'),(27,'Mic',1,'VNB205'),(28,'Mic',1,'VNB206'),(29,'Mic',1,'VNB207'),(30,'Mic',1,'VNB208'),(32,'Chìa khóa',2,'DTD201'),(33,'Chìa khóa',2,'DTD202'),(34,'Chìa khóa',2,'DTD203'),(35,'Chìa khóa',2,'EAUT101'),(36,'Chìa khóa',2,'EAUT102'),(37,'Chìa khóa',2,'EAUT103'),(38,'Chìa khóa',2,'EAUT104'),(39,'Chìa khóa',2,'EAUT105'),(40,'Chìa khóa',2,'EAUT106'),(41,'Chìa khóa',2,'EAUT107'),(42,'Chìa khóa',2,'EAUT108'),(43,'Chìa khóa',2,'PLC801'),(44,'Chìa khóa',2,'PLC802'),(45,'Chìa khóa',2,'PLC803'),(46,'Chìa khóa',2,'TT101'),(47,'Chìa khóa',2,'TT102'),(48,'Chìa khóa',2,'TT103'),(49,'Chìa khóa',2,'TT104'),(50,'Chìa khóa',2,'TT105'),(51,'Chìa khóa',2,'TT106'),(52,'Chìa khóa',2,'TT107'),(53,'Chìa khóa',2,'TT108'),(54,'Chìa khóa',2,'VNB201'),(55,'Chìa khóa',2,'VNB202'),(56,'Chìa khóa',2,'VNB203'),(57,'Chìa khóa',2,'VNB204'),(58,'Chìa khóa',2,'VNB205'),(59,'Chìa khóa',2,'VNB206'),(60,'Chìa khóa',2,'VNB207'),(61,'Chìa khóa',2,'VNB208'),(63,'ĐK Điều hòa',1,'DTD201'),(64,'ĐK Điều hòa',1,'DTD202'),(65,'ĐK Điều hòa',1,'DTD203'),(66,'ĐK Điều hòa',1,'EAUT101'),(67,'ĐK Điều hòa',1,'EAUT102'),(68,'ĐK Điều hòa',1,'EAUT103'),(69,'ĐK Điều hòa',1,'EAUT104'),(70,'ĐK Điều hòa',1,'EAUT105'),(71,'ĐK Điều hòa',1,'EAUT106'),(72,'ĐK Điều hòa',1,'EAUT107'),(73,'ĐK Điều hòa',1,'EAUT108'),(74,'ĐK Điều hòa',1,'PLC801'),(75,'ĐK Điều hòa',1,'PLC802'),(76,'ĐK Điều hòa',1,'PLC803'),(77,'ĐK Điều hòa',1,'TT101'),(78,'ĐK Điều hòa',1,'TT102'),(79,'ĐK Điều hòa',1,'TT103'),(80,'ĐK Điều hòa',1,'TT104'),(81,'ĐK Điều hòa',1,'TT105'),(82,'ĐK Điều hòa',1,'TT106'),(83,'ĐK Điều hòa',1,'TT107'),(84,'ĐK Điều hòa',1,'TT108'),(85,'ĐK Điều hòa',1,'VNB201'),(86,'ĐK Điều hòa',1,'VNB202'),(87,'ĐK Điều hòa',1,'VNB203'),(88,'ĐK Điều hòa',1,'VNB204'),(89,'ĐK Điều hòa',1,'VNB205'),(90,'ĐK Điều hòa',1,'VNB206'),(91,'ĐK Điều hòa',1,'VNB207'),(92,'ĐK Điều hòa',1,'VNB208'),(94,'ĐK Máy chiếu',1,'DTD201'),(95,'ĐK Máy chiếu',1,'DTD202'),(96,'ĐK Máy chiếu',1,'DTD203'),(97,'ĐK Máy chiếu',1,'EAUT101'),(98,'ĐK Máy chiếu',1,'EAUT102'),(99,'ĐK Máy chiếu',1,'EAUT103'),(100,'ĐK Máy chiếu',1,'EAUT104'),(101,'ĐK Máy chiếu',1,'EAUT105'),(102,'ĐK Máy chiếu',1,'EAUT106'),(103,'ĐK Máy chiếu',1,'EAUT107'),(104,'ĐK Máy chiếu',1,'EAUT108'),(105,'ĐK Máy chiếu',1,'PLC801'),(106,'ĐK Máy chiếu',1,'PLC802'),(107,'ĐK Máy chiếu',1,'PLC803'),(108,'ĐK Máy chiếu',1,'TT101'),(109,'ĐK Máy chiếu',1,'TT102'),(110,'ĐK Máy chiếu',1,'TT103'),(111,'ĐK Máy chiếu',1,'TT104'),(112,'ĐK Máy chiếu',1,'TT105'),(113,'ĐK Máy chiếu',1,'TT106'),(114,'ĐK Máy chiếu',1,'TT107'),(115,'ĐK Máy chiếu',1,'TT108'),(116,'ĐK Máy chiếu',1,'VNB201'),(117,'ĐK Máy chiếu',1,'VNB202'),(118,'ĐK Máy chiếu',1,'VNB203'),(119,'ĐK Máy chiếu',1,'VNB204'),(120,'ĐK Máy chiếu',1,'VNB205'),(121,'ĐK Máy chiếu',1,'VNB206'),(122,'ĐK Máy chiếu',1,'VNB207'),(123,'ĐK Máy chiếu',1,'VNB208');
/*!40000 ALTER TABLE `thiet_bi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `toa_nha`
--

DROP TABLE IF EXISTS `toa_nha`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `toa_nha` (
  `ma_toa_nha` varchar(50) NOT NULL,
  `ten_toa_nha` varchar(255) NOT NULL,
  PRIMARY KEY (`ma_toa_nha`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `toa_nha`
--

LOCK TABLES `toa_nha` WRITE;
/*!40000 ALTER TABLE `toa_nha` DISABLE KEYS */;
INSERT INTO `toa_nha` VALUES ('DTD','Đinh Trọng Dật'),('EAUT','EAUT'),('PLC','POLYCO'),('TT','Thuận Thành'),('VNB','Việt Nam Bulding');
/*!40000 ALTER TABLE `toa_nha` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-14 23:57:06
