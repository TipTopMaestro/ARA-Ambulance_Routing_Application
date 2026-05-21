package com.example.bellmanford.service;

import com.example.bellmanford.model.*;
import com.example.bellmanford.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;

    private final PatientRepository patientRepository;

    public MissionService(MissionRepository missionRepository, 
                          AmbulanceRepository ambulanceRepository,
                          HospitalRepository hospitalRepository,
                          UserRepository userRepository,
                          PatientRepository patientRepository) {
        this.missionRepository = missionRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public Mission createMission(Mission missionDto) {
        Ambulance ambulance = ambulanceRepository.findById(missionDto.getAmbulance().getId())
            .orElseThrow(() -> new RuntimeException("Ambulance not found"));

        if (ambulance.getDriver() == null) {
            throw new RuntimeException("Selected ambulance has no assigned driver");
        }

        User dispatcher = userRepository.findById(missionDto.getDispatcher().getId())
            .orElseThrow(() -> new RuntimeException("Dispatcher not found"));

        // 1. Save Patient first
        Patient patient = missionDto.getPatient();
        if (patient.getId() == null) {
            patient = patientRepository.save(patient);
        }

        // 2. Create Mission with PENDING_CONFIRMATION
        Mission mission = new Mission();
        mission.setDispatcher(dispatcher);
        mission.setDriver(ambulance.getDriver()); // Auto-assign driver from ambulance
        mission.setAmbulance(ambulance);
        mission.setPatient(patient);
        mission.setStartLat(missionDto.getStartLat());
        mission.setStartLng(missionDto.getStartLng());
        mission.setEndLat(missionDto.getEndLat());
        mission.setEndLng(missionDto.getEndLng());
        mission.setEstimatedTime(missionDto.getEstimatedTime());
        mission.setPathJson(missionDto.getPathJson());
        mission.setStatus(Mission.MissionStatus.PENDING_CONFIRMATION);
        mission.setDispatchTime(LocalDateTime.now());

        ambulance.setStatus(Ambulance.AmbulanceStatus.RESERVED);
        ambulanceRepository.save(ambulance);

        return missionRepository.save(mission);
    }

    public List<Mission> getAllMissions() {
        return missionRepository.findAll();
    }

    public Optional<Mission> getActiveMissionForDriver(Long driverId) {
        return userRepository.findById(driverId)
            .flatMap(driver -> missionRepository.findByDriverAndStatusIn(
                driver, 
                List.of(Mission.MissionStatus.PENDING_CONFIRMATION, Mission.MissionStatus.EN_ROUTE)
            ).stream().findFirst());
    }

    @Transactional
    public Mission updateMissionStatus(Long id, String statusStr) {
        Mission mission = missionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mission not found"));

        Mission.MissionStatus newStatus = Mission.MissionStatus.valueOf(statusStr);
        mission.setStatus(newStatus);

        Ambulance ambulance = mission.getAmbulance();

        switch (newStatus) {
            case EN_ROUTE:
                ambulance.setStatus(Ambulance.AmbulanceStatus.EN_ROUTE);
                break;
            case COMPLETED:
                mission.setArrivalTime(LocalDateTime.now());
                ambulance.setStatus(Ambulance.AmbulanceStatus.AVAILABLE);
                break;
            case CANCELLED:
                ambulance.setStatus(Ambulance.AmbulanceStatus.AVAILABLE);
                break;
            default:
                break;
        }

        ambulanceRepository.save(ambulance);
        return missionRepository.save(mission);
    }
}
