CREATE DATABASE `iw_dev` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `iw_dev`;

CREATE TABLE `applicants` (
  `id` varchar(20) NOT NULL,
  `missions` tinyint(2) NOT NULL DEFAULT '0',
  `eval` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `bgs_activity` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(150) DEFAULT NULL,
  `userid` varchar(20) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `amount` int(11) DEFAULT NULL,
  `activity` enum('Bond','Bounty','Failed','Fine','Intel','Mining','Mission','Scan','Smuggling','Trade','Murder') NOT NULL,
  `systemid` int(11) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `bgs_systems` (
  `systemid` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `shortname` varchar(5) NOT NULL,
  `fullname` varchar(50) NOT NULL DEFAULT '0',
  `hidden` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`systemid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='Systems tracked by the BGS';

CREATE TABLE `markov` (
  `idmarkov` int(15) NOT NULL AUTO_INCREMENT,
  `word1` varchar(150) NOT NULL,
  `word2` varchar(150) NOT NULL,
  `word3` varchar(150) NOT NULL,
  `word4` varchar(150) NOT NULL,
  `word5` varchar(150) NOT NULL,
  `prob` int(11) NOT NULL,
  PRIMARY KEY (`idmarkov`),
  KEY `index_pre` (`word1`,`word2`,`word3`,`word4`),
  KEY `index_post` (`word2`,`word3`,`word4`,`word5`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `notes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `authorid` varchar(18) NOT NULL,
  `name` varchar(150) NOT NULL,
  `is_public` bit(1) NOT NULL DEFAULT b'0',
  `content` varchar(2000) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `reminders` (
  `idreminders` int(11) NOT NULL AUTO_INCREMENT,
  `userid` varchar(18) DEFAULT NULL,
  `time` bigint(20) DEFAULT NULL,
  `reason` varchar(250) DEFAULT NULL,
  `reminded` binary(1) DEFAULT '0',
  PRIMARY KEY (`idreminders`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `user` (
  `iduser` bigint(20) NOT NULL,
  `username` varchar(100) NOT NULL,
  `role` varchar(100) NOT NULL,
  `onlinestatus` int(11) NOT NULL,
  `lastonline` datetime DEFAULT NULL,
  `added` datetime NOT NULL,
  `ppurl` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `sessionkey` varchar(9) DEFAULT NULL,
  `salt` varchar(17) DEFAULT NULL,
  `token` varchar(32) DEFAULT NULL,
  `token_used` datetime DEFAULT NULL,
  PRIMARY KEY (`iduser`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

GRANT ALL PRIVILEGES ON iw_dev.* TO `iwbot`@`localhost`;

FLUSH PRIVILEGES;