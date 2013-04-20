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
  `alias` VARCHAR(45) NOT NULL ,
  `username` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`userID`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `db_publicmain`.`t_groups`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_groups` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_groups` (
  `name` VARCHAR(20) NOT NULL ,
  `t_user_userID` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`name`) ,
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
  `name` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`name`) )
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
  `t_user_userID_empfaenger` BIGINT(20) NOT NULL ,
  `t_groups_name` VARCHAR(20) NOT NULL ,
  `t_msgType_name` VARCHAR(45) NOT NULL ,
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
    REFERENCES `db_publicmain`.`t_groups` (`name` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_messages_t_msgType1`
    FOREIGN KEY (`t_msgType_name` )
    REFERENCES `db_publicmain`.`t_msgType` (`name` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_t_messages_t_user1_idx` ON `db_publicmain`.`t_messages` (`t_user_userID_sender` ASC) ;

CREATE INDEX `fk_t_messages_t_user2_idx` ON `db_publicmain`.`t_messages` (`t_user_userID_empfaenger` ASC) ;

CREATE INDEX `fk_t_messages_t_groups1_idx` ON `db_publicmain`.`t_messages` (`t_groups_name` ASC) ;

CREATE INDEX `fk_t_messages_t_msgType1_idx` ON `db_publicmain`.`t_messages` (`t_msgType_name` ASC) ;


-- -----------------------------------------------------
-- Table `db_publicmain`.`t_settings`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_settings` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_settings` (
  `ProfilID` VARCHAR(45) NOT NULL ,
  `t_user_userID` BIGINT(20) NOT NULL ,
  `SystemAlias` VARCHAR(20) NULL ,
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
  `hostname` VARCHAR(45) NOT NULL ,
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

-- -----------------------------------------------------
-- Data for table `db_publicmain`.`t_msgType`
-- -----------------------------------------------------
START TRANSACTION;
USE `db_publicmain`;
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('GROUP');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('PRIVATE');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('NODE_UPDATE');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('ALIAS_UPDATE');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('NODE_LOOKUP');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('ECHO_REQUEST');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('ECHO_RESPONSE');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('PATH_PING_REQUEST');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('PATH_PING_RESPONSE');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('ROOT_DISCOVERY');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('ROOT_REPLY');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('ROOT_ANNOUNCE');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('POLL_CHILDNODES');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('REPORT_CHILDNODES');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('POLL_ALLNODES');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('REPORT_ALLNODES');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('TREE_DATA_POLL');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('TREE_DATA');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('NODE_SHUTDOWN');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('CHILD_SHUTDOWN');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('GROUP_POLL');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('GROUP_REPLY');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('GROUP_JOIN');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('GROUP_LEAVE');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('GROUP_EMPTY');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('GROUP_ANNOUNCE');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('FILE_REQUEST');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('FILE_REPLY');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('FILE_RECIEVED');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('FILE_TCP_REQUEST');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('FILE_TCP_REPLY');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('FILE_TCP_ABORT');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('CMD_SHUTDOWN');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('CMD_RESTART');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('CMD_RECONNECT');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('CW_INFO_TEXT');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('CW_WARNING_TEXT');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('CW_ERROR_TEXT');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('CW_FILE_REQUEST');
INSERT INTO `db_publicmain`.`t_msgType` (`name`) VALUES ('GUI_INFORM');

COMMIT;
