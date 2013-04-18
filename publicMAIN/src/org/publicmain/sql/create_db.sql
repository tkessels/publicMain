DROP SCHEMA IF EXISTS `db_publicmain` ;
CREATE SCHEMA IF NOT EXISTS `db_publicmain` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `db_publicmain` ;
-- -----------------------------------------------------
-- Table `db_publicmain`.`t_user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_user` ;
CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_user` (  `userID` BIGINT(20) NOT NULL ,  `alias` VARCHAR(45) NOT NULL ,  `username` VARCHAR(45) NOT NULL ,  PRIMARY KEY (`userID`) )ENGINE = InnoDB;
-- -----------------------------------------------------
-- Table `db_publicmain`.`t_groups`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_groups` ;
CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_groups` (  `name` VARCHAR(20) NOT NULL ,  `t_user_userID` BIGINT(20) NOT NULL ,  PRIMARY KEY (`name`) ,  CONSTRAINT `fk_t_groups_t_user1`    FOREIGN KEY (`t_user_userID` )    REFERENCES `db_publicmain`.`t_user` (`userID` )    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = InnoDB;
CREATE INDEX `fk_t_groups_t_user1_idx` ON `db_publicmain`.`t_groups` (`t_user_userID` ASC) ;
-- -----------------------------------------------------
-- Table `db_publicmain`.`t_msgType`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_msgType` ;
CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_msgType` (  `name` VARCHAR(45) NOT NULL ,  PRIMARY KEY (`name`) )ENGINE = InnoDB;
-- -----------------------------------------------------
-- Table `db_publicmain`.`t_messages`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_messages` ;
CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_messages` (  `timestamp` BIGINT(20) NOT NULL ,  `msgID` INT(11) NOT NULL ,  `data` VARCHAR(200) NOT NULL ,  `t_user_userID_sender` BIGINT(20) NOT NULL ,  `t_user_userID_empfaenger` BIGINT(20) NOT NULL ,  `t_groups_name` VARCHAR(20) NOT NULL ,  `t_msgType_name` VARCHAR(45) NOT NULL ,  PRIMARY KEY (`msgID`, `timestamp`, `t_user_userID_sender`) ,  CONSTRAINT `fk_t_messages_t_user1`    FOREIGN KEY (`t_user_userID_sender` )    REFERENCES `db_publicmain`.`t_user` (`userID` )    ON DELETE NO ACTION    ON UPDATE NO ACTION,  CONSTRAINT `fk_t_messages_t_user2`    FOREIGN KEY (`t_user_userID_empfaenger` )    REFERENCES `db_publicmain`.`t_user` (`userID` )    ON DELETE NO ACTION    ON UPDATE NO ACTION,  CONSTRAINT `fk_t_messages_t_groups1`    FOREIGN KEY (`t_groups_name` )    REFERENCES `db_publicmain`.`t_groups` (`name` )    ON DELETE NO ACTION    ON UPDATE NO ACTION,  CONSTRAINT `fk_t_messages_t_msgType1`    FOREIGN KEY (`t_msgType_name` )    REFERENCES `db_publicmain`.`t_msgType` (`name` )    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = InnoDB;
CREATE INDEX `fk_t_messages_t_user1_idx` ON `db_publicmain`.`t_messages` (`t_user_userID_sender` ASC) ;
CREATE INDEX `fk_t_messages_t_user2_idx` ON `db_publicmain`.`t_messages` (`t_user_userID_empfaenger` ASC) ;
CREATE INDEX `fk_t_messages_t_groups1_idx` ON `db_publicmain`.`t_messages` (`t_groups_name` ASC) ;
CREATE INDEX `fk_t_messages_t_msgType1_idx` ON `db_publicmain`.`t_messages` (`t_msgType_name` ASC) ;
-- -----------------------------------------------------
-- Table `db_publicmain`.`t_settings`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_settings` ;
CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_settings` (  `ProfilID` VARCHAR(45) NOT NULL ,  `SystemAlias` VARCHAR(20) NULL ,  `GuiMaxAliasLength` INT NULL ,  `GuiNamePattern` VARCHAR(45) NULL ,  `GuiMaxGroupLength` INT NULL ,  `CePingEnabled` TINYINT(1) NULL ,  `ChPingIntervall` INT NULL ,  `ChPingEnabled` TINYINT(1) NULL ,  `NeMaxClients` INT NULL ,  `NeMaxFileSize` BIGINT NULL ,  `NeDiscoverTimeout` SMALLINT NULL ,  `NeMulticastGroupIp` VARCHAR(15) NULL ,  `NeMulticastGroupPort` SMALLINT NULL ,  `NeFileTransferTimeout` INT NULL ,  `NeMulticastTTL` TINYINT NULL ,  `NeTreeBuildTime` SMALLINT NULL ,  `LogVerbosity` TINYINT NULL ,  `NeRootClaimTimeout` SMALLINT NULL ,  `SqlLocalDbCreatet` TINYINT NULL ,  `SqlLocalDbDatabasename` VARCHAR(45) NULL ,  `SqlLocalDbPassword` VARCHAR(45) NULL ,  `SqlLocalDbPort` SMALLINT NULL ,  `SqlBackupDbPort` SMALLINT NULL ,  `SqlLocalDbUser` VARCHAR(45) NULL ,  `SqlBackupDbUser` VARCHAR(45) NULL ,  `SqlBackupDbDatabasename` VARCHAR(45) NULL ,  `SqlBackupDbPassword` VARCHAR(45) NULL ,  `SqlBackupDbChoosenUsername` VARCHAR(45) NULL ,  `SqlBackupDbChoosenUserPasswordHash` INT NULL ,  `SqlBackupDbChoosenIP` VARCHAR(15) NULL ,  `t_user_userID` BIGINT(20) NOT NULL ,  PRIMARY KEY (`ProfilID`) ,  CONSTRAINT `fk_t_settings_t_user1`    FOREIGN KEY (`t_user_userID` )    REFERENCES `db_publicmain`.`t_user` (`userID` )    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = InnoDB;
CREATE INDEX `fk_t_settings_t_user1_idx` ON `db_publicmain`.`t_settings` (`t_user_userID` ASC) ;
-- -----------------------------------------------------
-- Table `db_publicmain`.`t_nodes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_nodes` ;
CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_nodes` (  `nodeID` BIGINT(20) NOT NULL ,  `hostname` VARCHAR(45) NOT NULL ,  `t_user_userID` BIGINT(20) NULL ,  `t_nodes_nodeID` BIGINT(20) NULL ,  PRIMARY KEY (`nodeID`) ,  CONSTRAINT `fk_t_nodes_t_user1`    FOREIGN KEY (`t_user_userID` )    REFERENCES `db_publicmain`.`t_user` (`userID` )    ON DELETE CASCADE    ON UPDATE NO ACTION,  CONSTRAINT `fk_t_nodes_t_nodes1`    FOREIGN KEY (`t_nodes_nodeID` )    REFERENCES `db_publicmain`.`t_nodes` (`nodeID` )    ON DELETE NO ACTION    ON UPDATE NO ACTION)ENGINE = InnoDB;
CREATE INDEX `fk_t_nodes_t_user1_idx` ON `db_publicmain`.`t_nodes` (`t_user_userID` ASC) ;
CREATE INDEX `fk_t_nodes_t_nodes1_idx` ON `db_publicmain`.`t_nodes` (`t_nodes_nodeID` ASC) ;
USE `db_publicmain` ;