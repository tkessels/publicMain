SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
DROP SCHEMA IF EXISTS `db_publicmain` ;
CREATE SCHEMA IF NOT EXISTS `db_publicmain` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `db_publicmain`;
-- -----------------------------------------------------
-- Table `db_publicmain`.`t_user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_user` ;
CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_user` (  `userID` BIGINT(20) NOT NULL ,  PRIMARY KEY (`userID`) )ENGINE = InnoDB;
-- -----------------------------------------------------
-- Table `db_publicmain`.`t_groups`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_groups` ;
CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_groups` (  `name` VARCHAR(20) NOT NULL ,  `t_user_userID` BIGINT(20) NOT NULL ,  PRIMARY KEY (`name`, `t_user_userID`) ,  CONSTRAINT `fk_t_groups_t_user1`    FOREIGN KEY (`t_user_userID` )    REFERENCES `db_publicmain`.`t_user` (`userID` )    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = InnoDB;
CREATE INDEX `fk_t_groups_t_user1_idx` ON `db_publicmain`.`t_groups` (`t_user_userID` ASC) ;
-- -----------------------------------------------------
-- Table `db_publicmain`.`t_msgType`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_msgType` ;
CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_msgType` (  `name` VARCHAR(7) NOT NULL ,  `t_msgType_name` VARCHAR(7) NOT NULL ,  PRIMARY KEY (`name`, `t_msgType_name`) ,  CONSTRAINT `fk_t_msgType_t_msgType1`    FOREIGN KEY (`t_msgType_name` )    REFERENCES `db_publicmain`.`t_msgType` (`name` )    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = InnoDB;
CREATE INDEX `fk_t_msgType_t_msgType1_idx` ON `db_publicmain`.`t_msgType` (`t_msgType_name` ASC) ;
-- -----------------------------------------------------
-- Table `db_publicmain`.`t_messages`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_messages` ;
CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_messages` (  `empfaenger` BIGINT(20) NOT NULL ,  `timestamp` BIGINT(20) NOT NULL ,  `grp` VARCHAR(20) NOT NULL ,  `typ` VARCHAR(30) NOT NULL ,  `sender` BIGINT(20) NOT NULL ,  `msgID` INT(11) NOT NULL ,  `data` VARCHAR(200) NOT NULL ,  `t_user_userID` BIGINT(20) NOT NULL ,  `t_groups_name` VARCHAR(20) NOT NULL ,  `t_groups_t_user_userID` BIGINT(20) NOT NULL ,  `t_msgType_name` VARCHAR(7) NOT NULL ,  PRIMARY KEY (`msgID`, `timestamp`, `sender`, `t_user_userID`, `t_groups_name`, `t_groups_t_user_userID`, `t_msgType_name`) ,  CONSTRAINT `fk_t_messages_t_user1`    FOREIGN KEY (`t_user_userID` )    REFERENCES `db_publicmain`.`t_user` (`userID` )    ON DELETE NO ACTION    ON UPDATE NO ACTION,  CONSTRAINT `fk_t_messages_t_groups1`    FOREIGN KEY (`t_groups_name` , `t_groups_t_user_userID` )    REFERENCES `db_publicmain`.`t_groups` (`name` , `t_user_userID` )    ON DELETE NO ACTION    ON UPDATE NO ACTION,  CONSTRAINT `fk_t_messages_t_msgType1`    FOREIGN KEY (`t_msgType_name` )    REFERENCES `db_publicmain`.`t_msgType` (`name` )    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = InnoDB;
CREATE INDEX `fk_t_messages_t_user1_idx` ON `db_publicmain`.`t_messages` (`t_user_userID` ASC) ;
CREATE INDEX `fk_t_messages_t_groups1_idx` ON `db_publicmain`.`t_messages` (`t_groups_name` ASC, `t_groups_t_user_userID` ASC) ;
CREATE INDEX `fk_t_messages_t_msgType1_idx` ON `db_publicmain`.`t_messages` (`t_msgType_name` ASC) ;
-- -----------------------------------------------------
-- Table `db_publicmain`.`t_settings`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_settings` ;
CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_settings` (  `alias` VARCHAR(10) NULL ,  `backupServerIP` VARCHAR(15) NULL ,  `backupServerUsername` VARCHAR(45) NULL ,  `backupServerPassword` VARCHAR(45) NULL ,  `t_user_userID` BIGINT(20) NULL ,  PRIMARY KEY (`t_user_userID`) ,  CONSTRAINT `fk_t_settings_t_user1`    FOREIGN KEY (`t_user_userID` )    REFERENCES `db_publicmain`.`t_user` (`userID` )    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = InnoDB;
-- -----------------------------------------------------
-- Table `db_publicmain`.`t_nodes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_nodes` ;
CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_nodes` (  `nodeID` BIGINT(20) NOT NULL ,  `t_user_userID` BIGINT(20) NOT NULL ,  `t_nodes_nodeID` BIGINT(20) NOT NULL ,  `t_nodes_t_user_userID` BIGINT(20) NOT NULL ,  PRIMARY KEY (`nodeID`, `t_user_userID`, `t_nodes_nodeID`, `t_nodes_t_user_userID`) ,  CONSTRAINT `fk_t_nodes_t_user1`    FOREIGN KEY (`t_user_userID` )    REFERENCES `db_publicmain`.`t_user` (`userID` )    ON DELETE NO ACTION    ON UPDATE NO ACTION,  CONSTRAINT `fk_t_nodes_t_nodes1`    FOREIGN KEY (`t_nodes_nodeID` , `t_nodes_t_user_userID` )    REFERENCES `db_publicmain`.`t_nodes` (`nodeID` , `t_user_userID` )    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = InnoDB;
CREATE INDEX `fk_t_nodes_t_user1_idx` ON `db_publicmain`.`t_nodes` ( `t_user_userID` ASC);
CREATE INDEX `fk_t_nodes_t_nodes1_idx` ON `db_publicmain`.`t_nodes` (`t_nodes_nodeID` ASC, `t_nodes_t_user_userID` ASC) ;
USE `db_publicmain` ;
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;