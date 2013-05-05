-- -----------------------------------------------------
-- Datenbank l�schen wenn sie existiert, SCHEMA ist ein
-- Alias f�r DATABASE und eine neue Datenbank mit dem
-- Namen `db_publicmain` anlegen wenn noch nicht
-- vorhanden.
-- Die neue Datenbank im Anschluss zum Benutzen
-- ausw�hlen.
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `db_publicmain` ;
CREATE SCHEMA IF NOT EXISTS `db_publicmain` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `db_publicmain` ;

-- -----------------------------------------------------
-- Tabelle `db_publicmain`.`t_users` anlegen, mit den
-- erforderlichen Attributen bef�llen und den
-- Prim�rschl�ssel festlegen.
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_users` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_users` (
  `userID` BIGINT(20) NOT NULL ,
  `displayName` VARCHAR(45) NOT NULL ,
  `userName` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`userID`) )
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Tabelle `db_publicmain`.`t_groups` anlegen, mit den
-- erforderlichen Attributen bef�llen, den
-- Prim�r- und Fremdschl�ssel festlegen.
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_groups` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_groups` (
  `groupName` VARCHAR(20) NOT NULL ,
  `fk_t_users_userID` BIGINT(20) NOT NULL ,
  INDEX `fk_t_groups_t_user1_idx` (`fk_t_users_userID` ASC) ,
  PRIMARY KEY (`groupName`) ,
  CONSTRAINT `fk_t_users_userID`
    FOREIGN KEY (`fk_t_users_userID` )
    REFERENCES `db_publicmain`.`t_users` (`userID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Tabelle `db_publicmain`.`t_msgType` anlegen, mit den
-- erforderlichen Attributen bef�llen und den
-- Prim�rschl�ssel festlegen.
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_msgType` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_msgType` (
  `msgTypeID` INT NOT NULL ,
  `name` VARCHAR(45) NOT NULL ,
  `description` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`msgTypeID`) )
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Tabelle `db_publicmain`.`t_messages` anlegen, mit den
-- erforderlichen Attributen bef�llen und den zusammen-
-- gesetzten Prim�rschl�ssel sowie die Fremdschl�ssel
-- festlegen.
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_messages` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_messages` (
  `msgID` INT(11) NOT NULL ,
  `timestmp` BIGINT(20) NOT NULL ,
  `fk_t_users_userID_sender` BIGINT(20) NOT NULL ,
  `displayName` VARCHAR(45) NOT NULL ,
  `txt` VARCHAR(200) NOT NULL ,
  `fk_t_users_userID_empfaenger` BIGINT(20) NULL ,
  `fk_t_groups_groupName` VARCHAR(20) NULL ,
  `fk_t_msgType_ID` INT NULL ,
  PRIMARY KEY (`msgID`, `timestmp`, `fk_t_users_userID_sender`) ,
  INDEX `fk_t_messages_t_user1_idx` (`fk_t_users_userID_sender` ASC) ,
  INDEX `fk_t_messages_t_user2_idx` (`fk_t_users_userID_empfaenger` ASC) ,
  INDEX `fk_t_msgType_ID` (`fk_t_msgType_ID` ASC) ,
  INDEX `fk_t_groups_groupName_idx` (`fk_t_groups_groupName` ASC) ,
  CONSTRAINT `fk_t_user_userID_sender`
    FOREIGN KEY (`fk_t_users_userID_sender` )
    REFERENCES `db_publicmain`.`t_users` (`userID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_user_userID_empfaenger`
    FOREIGN KEY (`fk_t_users_userID_empfaenger` )
    REFERENCES `db_publicmain`.`t_users` (`userID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_groups_groupName`
    FOREIGN KEY (`fk_t_groups_groupName` )
    REFERENCES `db_publicmain`.`t_groups` (`groupName` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_msgType_ID`
    FOREIGN KEY (`fk_t_msgType_ID` )
    REFERENCES `db_publicmain`.`t_msgType` (`msgTypeID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Tabelle `db_publicmain`.`t_settings` anlegen, mit den
-- erforderlichen Attributen bef�llen und den Prim�r-
-- sowie Fremdschl�ssel festlegen.
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_settings` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_settings` (
  `settingsKey` VARCHAR(45) NOT NULL ,
  `fk_t_users_userID_3` BIGINT(20) NOT NULL ,
  `settingsValue` VARCHAR(100) NOT NULL ,
  PRIMARY KEY (`settingsKey`) ,
  INDEX `fk_t_settings_t_users1_idx` (`fk_t_users_userID_3` ASC) ,
  CONSTRAINT `fk_t_users_userID_3`
    FOREIGN KEY (`fk_t_users_userID_3` )
    REFERENCES `db_publicmain`.`t_users` (`userID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Tabelle `db_publicmain`.`t_nodes` anlegen, mit den
-- erforderlichen Attributen bef�llen und den Prim�r-
-- sowie Fremdschl�ssel festlegen.
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_nodes` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_nodes` (
  `nodeID` BIGINT(20) NOT NULL ,
  `computerName` VARCHAR(45) NOT NULL ,
  `fk_t_users_userID_2` BIGINT(20) NOT NULL ,
  `fk_t_nodes_nodeID` BIGINT(20) NULL ,
  PRIMARY KEY (`nodeID`) ,
  INDEX `fk_t_nodes_t_user1_idx` (`fk_t_users_userID_2` ASC) ,
  INDEX `fk_t_nodes_t_nodes1_idx` (`fk_t_nodes_nodeID` ASC) ,
  CONSTRAINT `fk_t_users_userID_2`
    FOREIGN KEY (`fk_t_users_userID_2` )
    REFERENCES `db_publicmain`.`t_users` (`userID` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_nodes_nodeID`
    FOREIGN KEY (`fk_t_nodes_nodeID` )
    REFERENCES `db_publicmain`.`t_nodes` (`nodeID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Tabelle `db_publicmain`.`t_dbVersion` anlegen. Diese
-- Tabelle h�lt nur die Versionnummer vor, um zu pr�fen
-- ob Programm- und Datenbankversion kompatibel sind.
-- -----------------------------------------------------
DROP TABLE IF EXISTS `db_publicmain`.`t_dbVersion` ;

CREATE  TABLE IF NOT EXISTS `db_publicmain`.`t_dbVersion` (
  `dbVersion` INT NOT NULL )
ENGINE = InnoDB;

USE `db_publicmain` ;

- -----------------------------------------------------
-- Platzhalter Tabelle `db_publicmain`.`v_pullAll_t_users`
-- f�r die View um Views auf Views zu verhindern.
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `db_publicmain`.`v_pullAll_t_users` (`userID` INT, `displayName` INT, `userName` INT, `nodeID` INT, `computerName` INT, `fk_t_users_userID_2` INT, `fk_t_nodes_nodeID` INT);

-- -----------------------------------------------------
-- Platzhalter Tabelle `db_publicmain`.`v_pullAll_t_messages`
-- f�r die View um Views auf Views zu verhindern.
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `db_publicmain`.`v_pullAll_t_messages` (`msgID` INT, `timestmp` INT, `fk_t_users_userID_sender` INT, `displayName` INT, `txt` INT, `fk_t_users_userID_empfaenger` INT, `fk_t_groups_groupName` INT, `fk_t_msgType_ID` INT);

-- -----------------------------------------------------
-- Platzhalter Tabelle `db_publicmain`.`v_pullALL_t_settings`
-- f�r die View um Views auf Views zu verhindern.
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `db_publicmain`.`v_pullALL_t_settings` (`settingsKey` INT, `fk_t_users_userID_3` INT, `settingsValue` INT);

-- -----------------------------------------------------
-- Platzhalter Tabelle `db_publicmain`.`v_searchInHistory`
-- f�r die View um Views auf Views zu verhindern.
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `db_publicmain`.`v_searchInHistory` (`'userID_Sender'` INT, `'userID_Recipient'` INT, `'time'` INT, `'sender'` INT, `'recipient'` INT, `'message'` INT, `'group'` INT);

-- -----------------------------------------------------
-- View `db_publicmain`.`v_pullAll_t_users` l�schen wenn
-- sie existiert, die Platzhaltertabelle l�schen und die
-- View anlegen oder ersetzen.
-- -----------------------------------------------------
DROP VIEW IF EXISTS `db_publicmain`.`v_pullAll_t_users` ;
DROP TABLE IF EXISTS `db_publicmain`.`v_pullAll_t_users`;
USE `db_publicmain`;
CREATE OR REPLACE VIEW `db_publicmain`.`v_pullAll_t_users` AS 	SELECT * FROM t_users, t_nodes WHERE userID = fk_t_users_userID_2;

-- -----------------------------------------------------
-- View `db_publicmain`.`v_pullAll_t_messages` l�schen
-- wenn sie existiert, die Platzhaltertabelle l�schen
-- und die View anlegen oder ersetzen.
-- -----------------------------------------------------
DROP VIEW IF EXISTS `db_publicmain`.`v_pullAll_t_messages` ;
DROP TABLE IF EXISTS `db_publicmain`.`v_pullAll_t_messages`;
USE `db_publicmain`;
CREATE OR REPLACE VIEW `db_publicmain`.`v_pullAll_t_messages` 	AS SELECT * FROM t_messages;

-- -----------------------------------------------------
-- View `db_publicmain`.`v_pullALL_t_settings` l�schen
-- wenn sie existiert, die Platzhaltertabelle l�schen
-- und die View anlegen oder ersetzen.
-- -----------------------------------------------------
DROP VIEW IF EXISTS `db_publicmain`.`v_pullALL_t_settings` ;
DROP TABLE IF EXISTS `db_publicmain`.`v_pullALL_t_settings`;
USE `db_publicmain`;
CREATE OR REPLACE VIEW `db_publicmain`.`v_pullALL_t_settings` AS SELECT * FROM t_settings;

-- -----------------------------------------------------
-- View `db_publicmain`.`v_searchInHistory` l�schen
-- wenn sie existiert, die Platzhaltertabelle l�schen
-- und die View anlegen oder ersetzen.
-- -----------------------------------------------------
DROP VIEW IF EXISTS `db_publicmain`.`v_searchInHistory` ;
DROP TABLE IF EXISTS `db_publicmain`.`v_searchInHistory`;
USE `db_publicmain`;
CREATE OR REPLACE VIEW `db_publicmain`.`v_searchInHistory` AS SELECT fk_t_users_userID_sender 'userID_Sender', fk_t_users_userID_empfaenger 'userID_Recipient', timestmp 'time', t1.displayName 'sender', t2.displayName 'recipient', txt 'message', fk_t_groups_groupName 'group' FROM t_messages as t1 left join t_users as t2 on fk_t_users_userID_empfaenger = userID WHERE fk_t_msgtype_id is null ORDER BY time;

-- -----------------------------------------------------
-- Datenbank `db_publicmain` zum Benutzen ausw�hlen und
-- Daten in die Tabelle `db_publicmain`.`t_dbVersion`
-- einf�gen.
-- -----------------------------------------------------
START TRANSACTION;
USE `db_publicmain`;
INSERT INTO `db_publicmain`.`t_dbVersion` (`dbVersion`) VALUES (15);

COMMIT;
