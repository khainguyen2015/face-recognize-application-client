-- MySQL dump 10.13  Distrib 8.0.12, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: facedetectapplication
-- ------------------------------------------------------
-- Server version	8.0.12

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account_cb`
--

DROP TABLE IF EXISTS `account_cb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `account_cb` (
  `MSCB` varchar(10) DEFAULT NULL,
  `USER_NAME` varchar(10) NOT NULL,
  `PASS_WORD` varchar(9) NOT NULL,
  `PRIVILEGE` char(7) DEFAULT NULL,
  UNIQUE KEY `USER_NAME` (`USER_NAME`),
  KEY `FK_MSCB` (`MSCB`),
  CONSTRAINT `FK_MSCB` FOREIGN KEY (`MSCB`) REFERENCES `canbo` (`mscb`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_cb`
--

LOCK TABLES `account_cb` WRITE;
/*!40000 ALTER TABLE `account_cb` DISABLE KEYS */;
INSERT INTO `account_cb` VALUES ('0000','admin','admin','admin'),('0001','user','user','user');
/*!40000 ALTER TABLE `account_cb` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `canbo`
--

DROP TABLE IF EXISTS `canbo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `canbo` (
  `MSCB` varchar(10) NOT NULL,
  `TENCB` varchar(50) DEFAULT NULL,
  `NGAYSINHCB` date DEFAULT NULL,
  `GIOITINH` char(5) DEFAULT NULL,
  `BOMON` char(30) DEFAULT NULL,
  `KHOA` char(30) DEFAULT NULL,
  `AVATAR_PATH` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`MSCB`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `canbo`
--

LOCK TABLES `canbo` WRITE;
/*!40000 ALTER TABLE `canbo` DISABLE KEYS */;
INSERT INTO `canbo` VALUES ('0000','Nguyễn Văn A','1978-01-01','ADMIN','ADMIN','ADMIN','images\\canbo\\img-avatar-blank-300x300.jpg'),('0001','Nguyen Van A','0001-01-01','Nam','Unknow','Unknow','images\\canbo\\img-avatar-blank-300x300.jpg');
/*!40000 ALTER TABLE `canbo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lichsudiemdanh`
--

DROP TABLE IF EXISTS `lichsudiemdanh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `lichsudiemdanh` (
  `MSSV` varchar(8) DEFAULT NULL,
  `NGAYDIEMDANH` date DEFAULT NULL,
  `THOIGIANDIEMDANH` time DEFAULT NULL,
  UNIQUE KEY `THOIGIANDIEMDANH` (`THOIGIANDIEMDANH`),
  KEY `FK_MSSV` (`MSSV`),
  CONSTRAINT `FK_MSSV` FOREIGN KEY (`MSSV`) REFERENCES `sinhvien` (`mssv`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lichsudiemdanh`
--

LOCK TABLES `lichsudiemdanh` WRITE;
/*!40000 ALTER TABLE `lichsudiemdanh` DISABLE KEYS */;
INSERT INTO `lichsudiemdanh` VALUES ('B1609685','2019-11-22','22:32:33'),('B1606417','2019-11-22','22:32:39'),('B1609685','2019-11-22','22:33:51'),('B1606417','2019-11-22','22:33:57');
/*!40000 ALTER TABLE `lichsudiemdanh` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mon`
--

DROP TABLE IF EXISTS `mon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `mon` (
  `MAMON` varchar(10) NOT NULL,
  `TENMONHOC` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`MAMON`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mon`
--

LOCK TABLES `mon` WRITE;
/*!40000 ALTER TABLE `mon` DISABLE KEYS */;
/*!40000 ALTER TABLE `mon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sinhvien`
--

DROP TABLE IF EXISTS `sinhvien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `sinhvien` (
  `MSSV` varchar(8) NOT NULL,
  `TENSV` varchar(50) DEFAULT NULL,
  `NGAYSINHSV` date DEFAULT NULL,
  `GIOITINH` char(5) DEFAULT NULL,
  `NGANH` varchar(50) DEFAULT NULL,
  `KHOAS` varchar(3) DEFAULT NULL,
  `KHOA` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`MSSV`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sinhvien`
--

LOCK TABLES `sinhvien` WRITE;
/*!40000 ALTER TABLE `sinhvien` DISABLE KEYS */;
INSERT INTO `sinhvien` VALUES ('B1606417','NGUYEN HOANG HUYNH','1998-01-10','NAM','CONG NGHE THONG TIN - CLC','42','CNTT'),('B1609526','NGUYEN QUANG KHAI','1998-01-10','NAM','CONG NGHE THONG TIN - CLC','42','CNTT'),('B1609685','NGUYEN ANH TU','1998-01-10','NAM','CONG NGHE THONG TIN - CLC','42','CNTT');
/*!40000 ALTER TABLE `sinhvien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'facedetectapplication'
--

--
-- Dumping routines for database 'facedetectapplication'
--
/*!50003 DROP PROCEDURE IF EXISTS `DOCLICHSU` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`khai`@`localhost` PROCEDURE `DOCLICHSU`(IN m DATE)
BEGIN
		SELECT * FROM LICHSUDIEMDANH JOIN SINHVIEN ON LICHSUDIEMDANH.MSSV = SINHVIEN.MSSV WHERE NGAYDIEMDANH = m;  
	END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `DOCLICHSUTHEOMSSV` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`khai`@`localhost` PROCEDURE `DOCLICHSUTHEOMSSV`(IN m varchar(8), IN d DATE)
BEGIN
		SELECT * 
        FROM LICHSUDIEMDANH JOIN SINHVIEN ON LICHSUDIEMDANH.MSSV = SINHVIEN.MSSV 
        WHERE LICHSUDIEMDANH.MSSV = m AND LICHSUDIEMDANH.NGAYDIEMDANH = d;  
	END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `GHILICHSU` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`khai`@`localhost` PROCEDURE `GHILICHSU`(
	MSSV VARCHAR(8),
    NGAYDIEMDANH DATE,
    THOIGIANDIEMDANH TIME)
BEGIN
		INSERT LICHSUDIEMDANH VALUE (
		MSSV,
		NGAYDIEMDANH,
        THOIGIANDIEMDANH);
	END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `THEM_SINHVIEN` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`khai`@`localhost` PROCEDURE `THEM_SINHVIEN`(
		MSSV VARCHAR(8),
		TENSV VARCHAR(50),
		NGAYSINHSV DATE,
		GIOITINH CHAR(5),
		NGANH VARCHAR(50),
		KHOAS VARCHAR(3),
		KHOA VARCHAR(50))
BEGIN
		INSERT SINHVIEN VALUE (
		MSSV,
		TENSV,
		NGAYSINHSV,
		GIOITINH,
		NGANH,
		KHOAS,
		KHOA);
	END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `UPDATECANBO` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`khai`@`localhost` PROCEDURE `UPDATECANBO`(
	input_MSCB VARCHAR(10),
    input_TENCB VARCHAR(50),
    input_NGAYSINHCB DATE,
    input_GIOITINH CHAR(5),
    input_BOMON CHAR(30),
    input_KHOA CHAR (30),
    input_AVATAR_PATH VARCHAR(200))
BEGIN
		UPDATE CANBO VALUE
        SET TENCB = input_TENCB, NGAYSINHCB = input_NGAYSINHCB, 
			GIOITINH = input_GIOITINH, BOMON = input_BOMON, 
            KHOA = input_KHOA, AVATAR_PATH = input_AVATAR_PATH
        WHERE MSCB = input_MSCB;
	END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-12-04  9:00:33
