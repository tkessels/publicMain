SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

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
  PRIMARY KEY (`username`) )
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
  `t_backupUser_Username` VARCHAR(45) NOT NULL ,
  `fk_t_users_userID_empfaenger` BIGINT(20) NULL DEFAULT NULL ,
  `fk_t_msgType_ID` INT NULL DEFAULT NULL ,
  PRIMARY KEY (`msgID`, `timestmp`, `fk_t_users_userID_sender`) ,
  INDEX `fk_t_messages_t_user1_idx` (`fk_t_users_userID_sender` ASC) ,
  INDEX `fk_t_messages_t_user2_idx` (`fk_t_users_userID_empfaenger` ASC) ,
  INDEX `fk_t_msgType_ID` (`fk_t_msgType_ID` ASC) ,
  INDEX `fk_t_messages_t_backupUser1_idx` (`t_backupUser_Username` ASC) ,
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
    FOREIGN KEY (`t_backupUser_Username` )
    REFERENCES `db_publicmain_backup`.`t_backupUser` (`username` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `db_publicmain_backup`.`t_settings`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain_backup`.`t_settings` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain_backup`.`t_settings` (
  `key` VARCHAR(45) NOT NULL ,
  `value` VARCHAR(45) NOT NULL ,
  `fk_t_backupUser_username` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`key`) ,
  INDEX `fk_t_settings_t_backupUser1_idx` (`fk_t_backupUser_username` ASC) ,
  CONSTRAINT `fk_t_backupUser_username`
    FOREIGN KEY (`fk_t_backupUser_username` )
    REFERENCES `db_publicmain_backup`.`t_backupUser` (`username` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

USE `db_publicmain_backup` ;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
