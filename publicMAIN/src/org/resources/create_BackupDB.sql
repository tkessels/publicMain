DROP SCHEMA IF EXISTS `db_publicmain_backup` ;
CREATE SCHEMA IF NOT EXISTS `db_publicmain_backup` DEFAULT CHARACTER SET utf8 ;
USE `db_publicmain_backup` ;

-- -----------------------------------------------------
-- Table `db_publicmain_backup`.`t_users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain_backup`.`t_users` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain_backup`.`t_users` (
  `userID` BIGINT(20) NOT NULL ,
  `displayName` VARCHAR(45) NOT NULL ,
  `userName` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`userID`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `db_publicmain_backup`.`t_msgType`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain_backup`.`t_msgType` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain_backup`.`t_msgType` (
  `msgTypeID` INT NOT NULL ,
  `name` VARCHAR(45) NOT NULL ,
  `description` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`msgTypeID`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `db_publicmain_backup`.`t_backupUser`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain_backup`.`t_backupUser` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain_backup`.`t_backupUser` (
  `username` VARCHAR(45) NOT NULL ,
  `password` VARCHAR(45) NOT NULL ,
  `backupUserID` BIGINT NOT NULL AUTO_INCREMENT ,
  PRIMARY KEY (`backupUserID`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `db_publicmain_backup`.`t_messages`
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
  `t_backupUser_backupUserID` BIGINT NOT NULL ,
  PRIMARY KEY (`msgID`, `timestmp`, `fk_t_users_userID_sender`) ,
  INDEX `fk_t_messages_t_user1_idx` (`fk_t_users_userID_sender` ASC) ,
  INDEX `fk_t_messages_t_user2_idx` (`fk_t_users_userID_empfaenger` ASC) ,
  INDEX `fk_t_msgType_ID` (`fk_t_msgType_ID` ASC) ,
  INDEX `fk_t_messages_t_backupUser1_idx` (`t_backupUser_backupUserID` ASC) ,
  CONSTRAINT `fk_t_user_userID_sender`
    FOREIGN KEY (`fk_t_users_userID_sender` )
    REFERENCES `db_publicmain_backup`.`t_users` (`userID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_user_userID_empfaenger`
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
    FOREIGN KEY (`t_backupUser_backupUserID` )
    REFERENCES `db_publicmain_backup`.`t_backupUser` (`backupUserID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `db_publicmain_backup`.`t_settings`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain_backup`.`t_settings` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain_backup`.`t_settings` (
  `settingsKey` VARCHAR(45) NOT NULL ,
  `settingsValue` VARCHAR(45) NOT NULL ,
  `fk_t_backupUser_username` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`settingsKey`) ,
  INDEX `fk_t_settings_t_backupUser1_idx` (`fk_t_backupUser_username` ASC) ,
  CONSTRAINT `fk_t_backupUser_username`
    FOREIGN KEY (`fk_t_backupUser_username` )
    REFERENCES `db_publicmain_backup`.`t_backupUser` (`username` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `db_publicmain_backup`.`t_dbVersion`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain_backup`.`t_dbVersion` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain_backup`.`t_dbVersion` (
  `dbVersion` INT NOT NULL )
ENGINE = InnoDB;

USE `db_publicmain_backup` ;

-- -----------------------------------------------------
-- Placeholder table for view `db_publicmain_backup`.`v_searchInHistory`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `db_publicmain_backup`.`v_searchInHistory` (`settingsKey` INT, `settingsValue` INT, `fk_t_backupUser_username` INT);

-- -----------------------------------------------------
-- View `db_publicmain_backup`.`v_searchInHistory`
-- -----------------------------------------------------
DROP VIEW IF EXISTS `db_publicmain_backup`.`v_searchInHistory` ;
DROP TABLE IF EXISTS `db_publicmain_backup`.`v_searchInHistory`;
USE `db_publicmain_backup`;
CREATE  OR REPLACE VIEW `db_publicmain_backup`.`v_searchInHistory` AS SELECT * FROM t_settings;

DROP USER backupPublicMain;
CREATE USER 'backupPublicMain' IDENTIFIED BY 'backupPublicMain';

GRANT ALL ON `db_publicmain_backup`.* TO 'backupPublicMain';

-- -----------------------------------------------------
-- Data for table `db_publicmain_backup`.`t_dbVersion`
-- -----------------------------------------------------
START TRANSACTION;
USE `db_publicmain_backup`;
INSERT INTO `db_publicmain_backup`.`t_dbVersion` (`dbVersion`) VALUES (1);

COMMIT;