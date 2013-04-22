SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `db_publicmain` ;
CREATE SCHEMA IF NOT EXISTS `db_publicmain` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `db_publicmain` ;

-- -----------------------------------------------------
-- Table `db_publicmain`.`t_users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_users` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_users` (
  `userID` BIGINT(20) NOT NULL ,
  `displayname` VARCHAR(45) NOT NULL ,
  `username` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`userID`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `db_publicmain`.`t_groups`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_groups` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_groups` (
  `groupname` VARCHAR(20) NOT NULL ,
  `t_user_userID` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`groupname`) ,
  CONSTRAINT `fk_t_groups_t_user1`
    FOREIGN KEY (`t_user_userID` )
    REFERENCES `db_publicmain`.`t_users` (`userID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_t_groups_t_user1_idx` ON `db_publicmain`.`t_groups` (`t_user_userID` ASC) ;


-- -----------------------------------------------------
-- Table `db_publicmain`.`t_msgType`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_msgType` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_msgType` (
  `ID` INT NOT NULL ,
  `name` VARCHAR(45) NOT NULL ,
  `description` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`ID`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `db_publicmain`.`t_messages`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_messages` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_messages` (
  `timestmp` BIGINT(20) NOT NULL ,
  `msgID` INT(11) NOT NULL ,
  `txt` VARCHAR(200) NOT NULL ,
  `t_user_userID_sender` BIGINT(20) NOT NULL ,
  `t_user_userID_empfaenger` BIGINT(20) NULL ,
  `t_groups_name` VARCHAR(20) NULL ,
  `t_msgType_ID` INT NOT NULL ,
  PRIMARY KEY (`msgID`, `timestmp`, `t_user_userID_sender`) ,
  CONSTRAINT `fk_t_messages_t_user1`
    FOREIGN KEY (`t_user_userID_sender` )
    REFERENCES `db_publicmain`.`t_users` (`userID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_messages_t_user2`
    FOREIGN KEY (`t_user_userID_empfaenger` )
    REFERENCES `db_publicmain`.`t_users` (`userID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_messages_t_groups1`
    FOREIGN KEY (`t_groups_name` )
    REFERENCES `db_publicmain`.`t_groups` (`groupname` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_messages_t_msgType1`
    FOREIGN KEY (`t_msgType_ID` )
    REFERENCES `db_publicmain`.`t_msgType` (`ID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_t_messages_t_user1_idx` ON `db_publicmain`.`t_messages` (`t_user_userID_sender` ASC) ;

CREATE INDEX `fk_t_messages_t_user2_idx` ON `db_publicmain`.`t_messages` (`t_user_userID_empfaenger` ASC) ;

CREATE INDEX `fk_t_messages_t_groups1_idx` ON `db_publicmain`.`t_messages` (`t_groups_name` ASC) ;

CREATE INDEX `fk_t_messages_t_msgType1_idx` ON `db_publicmain`.`t_messages` (`t_msgType_ID` ASC) ;


-- -----------------------------------------------------
-- Table `db_publicmain`.`t_settings`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_settings` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_settings` (
  `ProfilID` VARCHAR(45) NOT NULL ,
  `t_user_userID` BIGINT(20) NOT NULL ,
  `GuiMaxAliasLength` INT NULL ,
  `GuiNamePattern` VARCHAR(45) NULL ,
  `GuiMaxGroupLength` INT NULL ,
  `CePingEnabled` TINYINT(1) NULL ,
  `ChPingIntervall` INT NULL ,
  `ChPingEnabled` TINYINT(1) NULL ,
  `NeMaxClients` INT NULL ,
  `NeMaxFileSize` BIGINT NULL ,
  `NeDiscoverTimeout` SMALLINT NULL ,
  `NeMulticastGroupIp` VARCHAR(15) NULL ,
  `NeMulticastGroupPort` SMALLINT NULL ,
  `NeFileTransferTimeout` INT NULL ,
  `NeMulticastTTL` TINYINT NULL ,
  `NeTreeBuildTime` SMALLINT NULL ,
  `LogVerbosity` TINYINT NULL ,
  `NeRootClaimTimeout` SMALLINT NULL ,
  `SqlLocalDbCreatet` TINYINT NULL ,
  `SqlLocalDbDatabasename` VARCHAR(45) NULL ,
  `SqlLocalDbPassword` VARCHAR(45) NULL ,
  `SqlLocalDbPort` SMALLINT NULL ,
  `SqlBackupDbPort` SMALLINT NULL ,
  `SqlLocalDbUser` VARCHAR(45) NULL ,
  `SqlBackupDbUser` VARCHAR(45) NULL ,
  `SqlBackupDbDatabasename` VARCHAR(45) NULL ,
  `SqlBackupDbPassword` VARCHAR(45) NULL ,
  `SqlBackupDbChoosenUsername` VARCHAR(45) NULL ,
  `SqlBackupDbChoosenUserPasswordHash` INT NULL ,
  `SqlBackupDbChoosenIP` VARCHAR(15) NULL ,
  PRIMARY KEY (`ProfilID`) ,
  CONSTRAINT `fk_t_settings_t_user1`
    FOREIGN KEY (`t_user_userID` )
    REFERENCES `db_publicmain`.`t_users` (`userID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_t_settings_t_user1_idx` ON `db_publicmain`.`t_settings` (`t_user_userID` ASC) ;


-- -----------------------------------------------------
-- Table `db_publicmain`.`t_nodes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_nodes` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_nodes` (
  `nodeID` BIGINT(20) NOT NULL ,
  `computername` VARCHAR(45) NOT NULL ,
  `t_user_userID` BIGINT(20) NULL ,
  `t_nodes_nodeID` BIGINT(20) NULL ,
  PRIMARY KEY (`nodeID`) ,
  CONSTRAINT `fk_t_nodes_t_user1`
    FOREIGN KEY (`t_user_userID` )
    REFERENCES `db_publicmain`.`t_users` (`userID` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_nodes_t_nodes1`
    FOREIGN KEY (`t_nodes_nodeID` )
    REFERENCES `db_publicmain`.`t_nodes` (`nodeID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_t_nodes_t_user1_idx` ON `db_publicmain`.`t_nodes` (`t_user_userID` ASC) ;

CREATE INDEX `fk_t_nodes_t_nodes1_idx` ON `db_publicmain`.`t_nodes` (`t_nodes_nodeID` ASC) ;

USE `db_publicmain` ;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
