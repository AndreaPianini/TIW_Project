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
  CONSTRAINT `doc` FOREIGN KEY (`id`) REFERENCES `Utente` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Docenti`
--

LOCK TABLES `Docenti` WRITE;
/*!40000 ALTER TABLE `Docenti` DISABLE KEYS */;
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
  `voto` varchar(32) DEFAULT NULL,
  `stato_valutazione` varchar(32) NOT NULL DEFAULT 'NON INSERITO',
  `verbale` int DEFAULT NULL,
  PRIMARY KEY (`studente`,`corso`,`data`),
  KEY `appello_idx` (`corso`,`data`),
  KEY `verbale_idx` (`verbale`),
  CONSTRAINT `appello` FOREIGN KEY (`corso`, `data`) REFERENCES `Appelli` (`corso`, `data`) ON UPDATE CASCADE,
  CONSTRAINT `studente` FOREIGN KEY (`studente`) REFERENCES `Studente` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `verbale` FOREIGN KEY (`verbale`) REFERENCES `Verbali` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Iscrizioni`
--

LOCK TABLES `Iscrizioni` WRITE;
/*!40000 ALTER TABLE `Iscrizioni` DISABLE KEYS */;
/*!40000 ALTER TABLE `Iscrizioni` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Studente`
--

DROP TABLE IF EXISTS `Studente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Studente` (
  `id` int NOT NULL,
  `matricola` varchar(45) NOT NULL,
  `corso_laurea` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `matricola_UNIQUE` (`matricola`),
  CONSTRAINT `stud` FOREIGN KEY (`id`) REFERENCES `Utente` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Studente`
--

LOCK TABLES `Studente` WRITE;
/*!40000 ALTER TABLE `Studente` DISABLE KEYS */;
/*!40000 ALTER TABLE `Studente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `StudSegueCorso`
--

DROP TABLE IF EXISTS `StudSegueCorso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `StudSegueCorso` (
  `Studente` int NOT NULL,
  `Corso` int NOT NULL,
  PRIMARY KEY (`Studente`,`Corso`),
  KEY `cors_idx` (`Corso`),
  CONSTRAINT `cors` FOREIGN KEY (`Corso`) REFERENCES `Corsi` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `stu` FOREIGN KEY (`Studente`) REFERENCES `Studente` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `StudSegueCorso`
--

LOCK TABLES `StudSegueCorso` WRITE;
/*!40000 ALTER TABLE `StudSegueCorso` DISABLE KEYS */;
/*!40000 ALTER TABLE `StudSegueCorso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Utente`
--

DROP TABLE IF EXISTS `Utente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Utente` (
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
-- Dumping data for table `Utente`
--

LOCK TABLES `Utente` WRITE;
/*!40000 ALTER TABLE `Utente` DISABLE KEYS */;
/*!40000 ALTER TABLE `Utente` ENABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Verbali`
--

LOCK TABLES `Verbali` WRITE;
/*!40000 ALTER TABLE `Verbali` DISABLE KEYS */;
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

-- Dump completed on 2025-07-25 13:08:05
