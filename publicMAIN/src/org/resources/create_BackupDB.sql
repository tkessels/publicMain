-- -----------------------------------------------------
-- Datenbank löschen wenn sie existiert, SCHEMA ist ein
-- Alias für DATABASE und eine neue Datenbank mit dem
-- Namen `db_publicmain` anlegen wenn noch nicht
-- vorhanden.
-- Die neue Datenbank im Anschluss zum Benutzen
-- auswählen.
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `db_publicmain_backup` ;
CREATE SCHEMA IF NOT EXISTS `db_publicmain_backup` DEFAULT CHARACTER SET utf8 ;
USE `db_publicmain_backup` ;

-- -----------------------------------------------------
-- Tabelle `db_publicmain`.`t_users` anlegen, mit den
-- erforderlichen Attributen befüllen und den
-- Primärschlüssel festlegen.
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain_backup`.`t_users` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain_backup`.`t_users` (
  `userID` BIGINT(20) NOT NULL ,
  `displayName` VARCHAR(45) NOT NULL ,
  `userName` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`userID`) )
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Tabelle `db_publicmain`.`t_msgType` anlegen, mit den
-- erforderlichen Attributen befüllen und den
-- Primärschlüssel festlegen.
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain_backup`.`t_msgType` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain_backup`.`t_msgType` (
  `msgTypeID` INT NOT NULL ,
  `name` VARCHAR(45) NOT NULL ,
  `description` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`msgTypeID`) )
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Tabelle `db_publicmain`.`t_backupUser` anlegen, mit den
-- erforderlichen Attributen befüllen und den
-- Primärschlüssel festlegen.
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain_backup`.`t_backupUser` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain_backup`.`t_backupUser` (
  `username` VARCHAR(45) NOT NULL ,
  `password` VARCHAR(45) NOT NULL ,
  `backupUserID` BIGINT NOT NULL AUTO_INCREMENT ,
  PRIMARY KEY (`backupUserID`) )
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Tabelle `db_publicmain`.`t_messages` anlegen, mit den
-- erforderlichen Attributen befüllen und den zusammen-
-- gesetzten Primärschlüssel sowie die Fremdschlüssel
-- festlegen.
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain_backup`.`t_messages` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain_backup`.`t_messages` (
  `msgID` INT(11) NOT NULL ,
  `timestmp` BIGINT(20) NOT NULL ,
  `fk_t_users_userID_sender` BIGINT(20) NOT NULL ,
  `groupName` VARCHAR(20) NULL DEFAULT NULL ,
  `txt` VARCHAR(200) NOT NULL ,
  `displayName` VARCHAR(45) NOT NULL ,
  `fk_t_users_userID_empfaenger` BIGINT(20) NULL DEFAULT NULL ,
  `fk_t_msgType_ID` INT NULL DEFAULT NULL ,
  `fk_t_backupUser_backupUserID` BIGINT NOT NULL ,
  PRIMARY KEY (`msgID`, `timestmp`, `fk_t_users_userID_sender`, `fk_t_backupUser_backupUserID`) ,
  INDEX `fk_t_messages_t_user1_idx` (`fk_t_users_userID_sender` ASC) ,
  INDEX `fk_t_messages_t_user2_idx` (`fk_t_users_userID_empfaenger` ASC) ,
  INDEX `fk_t_msgType_ID` (`fk_t_msgType_ID` ASC) ,
  INDEX `fk_t_backupUser_backupUserID` (`fk_t_backupUser_backupUserID` ASC) ,
  CONSTRAINT `fk_t_users_userID_sender`
    FOREIGN KEY (`fk_t_users_userID_sender` )
    REFERENCES `db_publicmain_backup`.`t_users` (`userID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_users_userID_empfaenger`
    FOREIGN KEY (`fk_t_users_userID_empfaenger` )
    REFERENCES `db_publicmain_backup`.`t_users` (`userID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_msgType_ID`
    FOREIGN KEY (`fk_t_msgType_ID` )
    REFERENCES `db_publicmain_backup`.`t_msgType` (`msgTypeID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_messages_t_backupUser1`
    FOREIGN KEY (`fk_t_backupUser_backupUserID` )
    REFERENCES `db_publicmain_backup`.`t_backupUser` (`backupUserID` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Tabelle `db_publicmain`.`t_settings` anlegen, mit den
-- erforderlichen Attributen befüllen und den Primär-
-- sowie Fremdschlüssel festlegen.
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain_backup`.`t_settings` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain_backup`.`t_settings` (
  `settingsKey` VARCHAR(45) NOT NULL ,
  `fk_t_backupUser_backupUserID_2` BIGINT NOT NULL ,
  `settingsValue` VARCHAR(100) NOT NULL ,
  INDEX `fk_t_settings_t_backupUser1_idx` (`fk_t_backupUser_backupUserID_2` ASC) ,
  PRIMARY KEY (`settingsKey`, `fk_t_backupUser_backupUserID_2`) ,
  CONSTRAINT `fk_t_backupUser_backupUserID_2`
    FOREIGN KEY (`fk_t_backupUser_backupUserID_2` )
    REFERENCES `db_publicmain_backup`.`t_backupUser` (`backupUserID` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Tabelle `db_publicmain`.`t_dbVersion` anlegen. Diese
-- Tabelle hält nur die Versionnummer vor, um zu prüfen
-- ob Programm- und Datenbankversion kompatibel sind.
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain_backup`.`t_dbVersion` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain_backup`.`t_dbVersion` (
  `dbVersion` INT NOT NULL )
ENGINE = InnoDB;

USE `db_publicmain_backup` ;

-- -----------------------------------------------------
-- Platzhalter Tabelle `db_publicmain`.`v_searchInHistory`
-- für die View um Views auf Views zu verhindern.
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `db_publicmain_backup`.`v_searchInHistory` (`settingsKey` INT, `fk_t_backupUser_backupUserID_2` INT, `settingsValue` INT);

-- -----------------------------------------------------
-- View `db_publicmain`.`v_searchInHistory` löschen wenn
-- sie existiert, die Platzhaltertabelle löschen und die
-- View anlegen oder ersetzen.
-- -----------------------------------------------------
DROP VIEW IF EXISTS `db_publicmain_backup`.`v_searchInHistory` ;
DROP TABLE IF EXISTS `db_publicmain_backup`.`v_searchInHistory`;
USE `db_publicmain_backup`;
CREATE  OR REPLACE VIEW `db_publicmain_backup`.`v_searchInHistory` AS SELECT * FROM t_settings;

-- -----------------------------------------------------
-- Datenbank `db_publicmain` zum Benutzen auswählen und
-- Daten in die Tabelle `db_publicmain`.`t_dbVersion`
-- einfügen.
-- -----------------------------------------------------
START TRANSACTION;
USE `db_publicmain_backup`;
INSERT INTO `db_publicmain_backup`.`t_dbVersion` (`dbVersion`) VALUES (1);