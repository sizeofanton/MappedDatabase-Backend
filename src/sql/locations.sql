CREATE TABLE `locations` (
  `location_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `location_title` varchar(100) NOT NULL,
  `location_color` int(10) unsigned NOT NULL DEFAULT '0',
  `location_latitude` float NOT NULL,
  `location_longitude` float NOT NULL,
  PRIMARY KEY (`location_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;