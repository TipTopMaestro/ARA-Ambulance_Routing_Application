-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 15, 2026 at 10:23 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `ara_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `ambulances`
--

CREATE TABLE `ambulances` (
  `id` bigint(20) NOT NULL,
  `hospital_id` bigint(20) NOT NULL,
  `status` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `generated_paths`
--

CREATE TABLE `generated_paths` (
  `id` bigint(20) NOT NULL,
  `calculated_at` datetime(6) DEFAULT NULL,
  `estimated_distance_meters` double DEFAULT NULL,
  `estimated_time_seconds` double DEFAULT NULL,
  `route_json` text NOT NULL,
  `source_location_id` bigint(20) NOT NULL,
  `target_location_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `hospitals`
--

CREATE TABLE `hospitals` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `location_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `locations`
--

CREATE TABLE `locations` (
  `id` bigint(20) NOT NULL,
  `latitude` double NOT NULL,
  `location_type` enum('HOSPITAL','NODE','PATIENT') NOT NULL,
  `longitude` double NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `osm_node_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `locations`
--

INSERT INTO `locations` (`id`, `latitude`, `location_type`, `longitude`, `name`, `osm_node_id`) VALUES
(1, 7.3024451, 'HOSPITAL', 125.6784332, 'Panabo Polymedic Hospital', 'H1');

-- --------------------------------------------------------

--
-- Table structure for table `missions`
--

CREATE TABLE `missions` (
  `id` bigint(20) NOT NULL,
  `ambulance_id` varchar(255) DEFAULT NULL,
  `arrival_time` datetime(6) DEFAULT NULL,
  `dispatch_time` datetime(6) DEFAULT NULL,
  `driver_id` bigint(20) DEFAULT NULL,
  `emergency_type` varchar(255) DEFAULT NULL,
  `estimated_time` double DEFAULT NULL,
  `hospital_id` varchar(255) DEFAULT NULL,
  `patient_lat` double DEFAULT NULL,
  `patient_lng` double DEFAULT NULL,
  `patient_location_name` varchar(255) DEFAULT NULL,
  `patient_name` varchar(255) DEFAULT NULL,
  `route_coordinates_json` text DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `transport_time` datetime(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `patients`
--

CREATE TABLE `patients` (
  `id` bigint(20) NOT NULL,
  `contact_number` varchar(255) DEFAULT NULL,
  `emergency_type` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `request_logs`
--

CREATE TABLE `request_logs` (
  `id` bigint(20) NOT NULL,
  `arrived_at_patient_at` datetime(6) DEFAULT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `dispatched_at` datetime(6) DEFAULT NULL,
  `emergency_type` varchar(255) NOT NULL,
  `requested_at` datetime(6) DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','DISPATCHED','EN_ROUTE','PENDING','TRANSPORT') NOT NULL,
  `ambulance_id` bigint(20) NOT NULL,
  `dispatcher_id` bigint(20) NOT NULL,
  `driver_id` bigint(20) DEFAULT NULL,
  `path_id` bigint(20) DEFAULT NULL,
  `patient_id` bigint(20) NOT NULL,
  `source_location_id` bigint(20) NOT NULL,
  `target_location_id` bigint(20) NOT NULL,
  `hospital_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `active` bit(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `password`, `role`, `username`, `active`) VALUES
(1, 'password', 'DISPATCHER', 'dispatcher1', b'0'),
(2, 'password', 'DRIVER', 'driver1', b'0'),
(3, 'password', 'DRIVER', 'driver2', b'0');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `ambulances`
--
ALTER TABLE `ambulances`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK1b7vsuy2v7fo9ccvtxnf9mueg` (`hospital_id`);

--
-- Indexes for table `generated_paths`
--
ALTER TABLE `generated_paths`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `hospitals`
--
ALTER TABLE `hospitals`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `locations`
--
ALTER TABLE `locations`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKh7tr6vye7oxp4dho7s42guvd7` (`latitude`,`longitude`),
  ADD UNIQUE KEY `UKeto3dgsjx1bogkrnou7j28h5p` (`osm_node_id`);

--
-- Indexes for table `missions`
--
ALTER TABLE `missions`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `patients`
--
ALTER TABLE `patients`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `request_logs`
--
ALTER TABLE `request_logs`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `ambulances`
--
ALTER TABLE `ambulances`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `generated_paths`
--
ALTER TABLE `generated_paths`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `hospitals`
--
ALTER TABLE `hospitals`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `locations`
--
ALTER TABLE `locations`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `missions`
--
ALTER TABLE `missions`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `patients`
--
ALTER TABLE `patients`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `request_logs`
--
ALTER TABLE `request_logs`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `ambulances`
--
ALTER TABLE `ambulances`
  ADD CONSTRAINT `FK1b7vsuy2v7fo9ccvtxnf9mueg` FOREIGN KEY (`hospital_id`) REFERENCES `hospitals` (`id`);

--
-- Constraints for table `generated_paths`
--
ALTER TABLE `generated_paths`
  ADD CONSTRAINT `FK86poxcq2ai5wtq8omg89w8kty` FOREIGN KEY (`target_location_id`) REFERENCES `locations` (`id`),
  ADD CONSTRAINT `FKpf659b5dw1cu9bsd6nw5311uc` FOREIGN KEY (`source_location_id`) REFERENCES `locations` (`id`);

--
-- Constraints for table `hospitals`
--
ALTER TABLE `hospitals`
  ADD CONSTRAINT `FKnhepp5tmgyi98qpypnmwe92mj` FOREIGN KEY (`location_id`) REFERENCES `locations` (`id`);

--
-- Constraints for table `request_logs`
--
ALTER TABLE `request_logs`
  ADD CONSTRAINT `FK1mbd6pxn3ipsxndu0n8jrxiqa` FOREIGN KEY (`path_id`) REFERENCES `generated_paths` (`id`),
  ADD CONSTRAINT `FKbluul1agkadnrerjoa58b4263` FOREIGN KEY (`dispatcher_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKgq44kfn5b28xf6t15apy9flfh` FOREIGN KEY (`hospital_id`) REFERENCES `hospitals` (`id`),
  ADD CONSTRAINT `FKjbtcadmswu23osq51hipkewop` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`),
  ADD CONSTRAINT `FKk5wh9h9mrahp8wjc9h4kau2y9` FOREIGN KEY (`driver_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKn9jgd5517ocab1iri4agwgkdh` FOREIGN KEY (`ambulance_id`) REFERENCES `ambulances` (`id`),
  ADD CONSTRAINT `FKpi1mrvmio534drf1ivyhy2j61` FOREIGN KEY (`target_location_id`) REFERENCES `locations` (`id`),
  ADD CONSTRAINT `FKt38w1hhdkjg6f9upwf455f9v1` FOREIGN KEY (`source_location_id`) REFERENCES `locations` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
