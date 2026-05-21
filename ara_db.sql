SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- Database: `ara_db`

-- --------------------------------------------------------

-- Table structure for table `hospitals`
CREATE TABLE `hospitals` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

-- Table structure for table `users`
CREATE TABLE `users` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  `active` bit(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

-- Table structure for table `ambulances`
CREATE TABLE `ambulances` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `hospital_id` bigint(20) NOT NULL,
  `driver_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_driver` (`driver_id`),
  CONSTRAINT `FK_ambulance_hospital` FOREIGN KEY (`hospital_id`) REFERENCES `hospitals` (`id`),
  CONSTRAINT `FK_ambulance_driver` FOREIGN KEY (`driver_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

-- Table structure for table `patients`
CREATE TABLE `patients` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `contact_number` varchar(255) DEFAULT NULL,
  `emergency_type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

-- Table structure for table `missions`
CREATE TABLE `missions` (
  `mission_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dispatcher_id` bigint(20) NOT NULL,
  `driver_id` bigint(20) NOT NULL,
  `ambulance_id` bigint(20) NOT NULL,
  `patient_id` bigint(20) NOT NULL,
  `start_lat` double DEFAULT NULL,
  `start_lng` double DEFAULT NULL,
  `end_lat` double DEFAULT NULL,
  `end_lng` double DEFAULT NULL,
  `estimated_time` double DEFAULT NULL,
  `path_json` json DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `dispatch_time` datetime(6) DEFAULT NULL,
  `transport_time` datetime(6) DEFAULT NULL,
  `arrival_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`mission_id`),
  CONSTRAINT `FK_mission_dispatcher` FOREIGN KEY (`dispatcher_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FK_mission_driver` FOREIGN KEY (`driver_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FK_mission_ambulance` FOREIGN KEY (`ambulance_id`) REFERENCES `ambulances` (`id`),
  CONSTRAINT `FK_mission_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping initial data
INSERT INTO `users` (`username`, `password`, `role`, `active`) VALUES
('dispatcher1', 'password', 'DISPATCHER', b'1'),
('driver1', 'password', 'DRIVER', b'1'),
('driver2', 'password', 'DRIVER', b'1');

INSERT INTO `hospitals` (`name`, `latitude`, `longitude`) VALUES
('Panabo City District Hospital', 7.3081, 125.6841),
('Rivera Medical Center Inc.', 7.3050, 125.6800);

INSERT INTO `ambulances` (`hospital_id`, `driver_id`, `status`) VALUES
(1, 2, 'AVAILABLE'),
(2, 3, 'AVAILABLE');

COMMIT;
