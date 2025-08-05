CREATE DATABASE  IF NOT EXISTS `db_esami` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `db_esami`;
-- MySQL dump 10.13  Distrib 8.0.31, for macos12 (x86_64)
--
-- Host: 127.0.0.1    Database: db_esami
-- ------------------------------------------------------
-- Server version	8.0.31

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
-- Table structure for table `Appelli`
--

DROP TABLE IF EXISTS `Appelli`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Appelli` (
  `corso` int NOT NULL,
  `data` date NOT NULL,
  PRIMARY KEY (`corso`,`data`),
  CONSTRAINT `corso` FOREIGN KEY (`corso`) REFERENCES `Corsi` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Appelli`
--

LOCK TABLES `Appelli` WRITE;
/*!40000 ALTER TABLE `Appelli` DISABLE KEYS */;
INSERT INTO `Appelli` VALUES (101,'2025-06-01'),(101,'2025-07-15'),(101,'2025-09-01'),(102,'2025-06-03'),(102,'2025-07-20'),(103,'2025-06-05'),(105,'2025-06-12'),(105,'2025-07-27'),(105,'2025-09-05'),(106,'2025-06-14'),(106,'2025-07-29'),(107,'2025-06-16'),(109,'2025-06-20'),(109,'2025-08-01'),(110,'2025-06-22');
/*!40000 ALTER TABLE `Appelli` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Corsi`
--

DROP TABLE IF EXISTS `Corsi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Corsi` (
  `id` int NOT NULL,
  `nome` varchar(32) NOT NULL,
  `cfu` int NOT NULL,
  `docente` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `docente_idx` (`docente`),
  CONSTRAINT `docente` FOREIGN KEY (`docente`) REFERENCES `Docenti` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Corsi`
--

LOCK TABLES `Corsi` WRITE;
/*!40000 ALTER TABLE `Corsi` DISABLE KEYS */;
INSERT INTO `Corsi` VALUES (101,'Algoritmi',9,1),(102,'Analisi 1',12,2),(103,'Fisica 1',9,3),(104,'Statistica',6,4),(105,'Reti',6,5),(106,'Sistemi Operativi',9,1),(107,'Database',9,2),(108,'Chimica',6,3),(109,'Programmazione',12,4),(110,'Matematica Discreta',6,5);
/*!40000 ALTER TABLE `Corsi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Docenti`
--

DROP TABLE IF EXISTS `Docenti`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Docenti` (
  `id` int NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `doc` FOREIGN KEY (`id`) REFERENCES `Utenti` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Docenti`
--

LOCK TABLES `Docenti` WRITE;
/*!40000 ALTER TABLE `Docenti` DISABLE KEYS */;
INSERT INTO `Docenti` VALUES (1),(2),(3),(4),(5);
/*!40000 ALTER TABLE `Docenti` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Iscrizioni`
--

DROP TABLE IF EXISTS `Iscrizioni`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Iscrizioni` (
  `studente` int NOT NULL,
  `corso` int NOT NULL,
  `data` date NOT NULL,
  `voto` enum('ASSENTE','RIMANDATO','RIPROVATO','18','19','20','21','22','23','24','25','26','27','28','29','30','30L') DEFAULT NULL,
  `stato_valutazione` enum('NON_INSERITO','INSERITO','PUBBLICATO','RIFIUTATO','VERBALIZZATO') NOT NULL DEFAULT 'NON_INSERITO',
  `verbale` int DEFAULT NULL,
  PRIMARY KEY (`studente`,`corso`,`data`),
  KEY `appello_idx` (`corso`,`data`),
  KEY `verbale_idx` (`verbale`),
  CONSTRAINT `appello` FOREIGN KEY (`corso`, `data`) REFERENCES `Appelli` (`corso`, `data`) ON UPDATE CASCADE,
  CONSTRAINT `studente` FOREIGN KEY (`studente`) REFERENCES `Studenti` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `verbale` FOREIGN KEY (`verbale`) REFERENCES `Verbali` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Iscrizioni`
--

LOCK TABLES `Iscrizioni` WRITE;
/*!40000 ALTER TABLE `Iscrizioni` DISABLE KEYS */;
INSERT INTO `Iscrizioni` VALUES (6,101,'2025-06-01',NULL,'NON_INSERITO',NULL),(6,101,'2025-07-15','28','VERBALIZZATO',1),(6,102,'2025-06-03',NULL,'NON_INSERITO',NULL),(6,102,'2025-07-20','25','INSERITO',NULL),(6,105,'2025-09-05',NULL,'NON_INSERITO',NULL),(7,101,'2025-07-15','27','VERBALIZZATO',1),(7,101,'2025-09-01',NULL,'NON_INSERITO',NULL),(7,103,'2025-06-05',NULL,'NON_INSERITO',NULL),(7,106,'2025-06-14',NULL,'NON_INSERITO',NULL),(8,105,'2025-06-12','29','VERBALIZZATO',2),(8,107,'2025-06-16',NULL,'NON_INSERITO',NULL),(8,109,'2025-06-20','27','VERBALIZZATO',3),(9,101,'2025-07-15','30L','VERBALIZZATO',1),(9,109,'2025-06-20','28','VERBALIZZATO',3),(9,109,'2025-08-01',NULL,'NON_INSERITO',NULL),(9,110,'2025-06-22',NULL,'NON_INSERITO',NULL),(10,105,'2025-07-27','25','VERBALIZZATO',2),(10,106,'2025-07-29','24','INSERITO',NULL),(10,110,'2025-06-22','19','INSERITO',NULL);
/*!40000 ALTER TABLE `Iscrizioni` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Studenti`
--

DROP TABLE IF EXISTS `Studenti`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Studenti` (
  `id` int NOT NULL,
  `matricola` varchar(45) NOT NULL,
  `corso_laurea` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `matricola_UNIQUE` (`matricola`),
  CONSTRAINT `stud` FOREIGN KEY (`id`) REFERENCES `Utenti` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Studenti`
--

LOCK TABLES `Studenti` WRITE;
/*!40000 ALTER TABLE `Studenti` DISABLE KEYS */;
INSERT INTO `Studenti` VALUES (6,'S1001','Informatica'),(7,'S1002','Fisica'),(8,'S1003','Matematica'),(9,'S1004','Ingegneria'),(10,'S1005','Biologia');
/*!40000 ALTER TABLE `Studenti` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `StudSegueCorso`
--

DROP TABLE IF EXISTS `StudSegueCorso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `StudSegueCorso` (
  `studente` int NOT NULL,
  `corso` int NOT NULL,
  PRIMARY KEY (`studente`,`corso`),
  KEY `cors_idx` (`corso`),
  CONSTRAINT `cors` FOREIGN KEY (`corso`) REFERENCES `Corsi` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `stu` FOREIGN KEY (`studente`) REFERENCES `Studenti` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `StudSegueCorso`
--

LOCK TABLES `StudSegueCorso` WRITE;
/*!40000 ALTER TABLE `StudSegueCorso` DISABLE KEYS */;
INSERT INTO `StudSegueCorso` VALUES (6,101),(7,101),(9,101),(6,102),(7,103),(6,105),(8,105),(10,105),(7,106),(10,106),(8,107),(8,109),(9,109),(9,110),(10,110);
/*!40000 ALTER TABLE `StudSegueCorso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Utenti`
--

DROP TABLE IF EXISTS `Utenti`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Utenti` (
  `id` int NOT NULL,
  `password` varchar(32) NOT NULL,
  `nome` varchar(32) NOT NULL,
  `cognome` varchar(32) NOT NULL,
  `email` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Utenti`
--

LOCK TABLES `Utenti` WRITE;
/*!40000 ALTER TABLE `Utenti` DISABLE KEYS */;
INSERT INTO `Utenti` VALUES (1,'pwd1','Mario','Rossi','mario.rossi@uni.it'),(2,'pwd2','Luca','Verdi','luca.verdi@uni.it'),(3,'pwd3','Anna','Bianchi','anna.bianchi@uni.it'),(4,'pwd4','Giulia','Neri','giulia.neri@uni.it'),(5,'pwd5','Paolo','Gialli','paolo.gialli@uni.it'),(6,'pwd6','Marco','Blu','marco.blu@uni.it'),(7,'pwd7','Chiara','Rosa','chiara.rosa@uni.it'),(8,'pwd8','Stefano','Viola','stefano.viola@uni.it'),(9,'pwd9','Elena','Marrone','elena.marrone@uni.it'),(10,'pwd10','Francesca','Grigi','francesca.grigi@uni.it');
/*!40000 ALTER TABLE `Utenti` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Verbali`
--

DROP TABLE IF EXISTS `Verbali`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Verbali` (
  `id` int NOT NULL AUTO_INCREMENT,
  `data_ora_creaz` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Verbali`
--

LOCK TABLES `Verbali` WRITE;
/*!40000 ALTER TABLE `Verbali` DISABLE KEYS */;
INSERT INTO `Verbali` VALUES (1,'2025-07-02 10:00:00'),(2,'2025-07-28 11:00:00'),(3,'2025-09-06 12:00:00'),(4,'2025-06-30 09:30:00');
/*!40000 ALTER TABLE `Verbali` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'db_esami'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-05 19:07:59
