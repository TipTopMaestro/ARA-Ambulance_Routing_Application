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

    public MissionService(MissionRepository missionRepository, 
                          AmbulanceRepository ambulanceRepository,
                          HospitalRepository hospitalRepository,
                          UserRepository userRepository) {
        this.missionRepository = missionRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Mission createMission(Mission missionDto) {
        Ambulance ambulance = ambulanceRepository.findById(missionDto.getAmbulance().getId())
            .orElseThrow(() -> new RuntimeException("Ambulance not found"));

        User dispatcher = userRepository.findById(missionDto.getDispatcher().getId())
            .orElseThrow(() -> new RuntimeException("Dispatcher not found"));

        Mission mission = new Mission();
        mission.setDispatcher(dispatcher);
        mission.setAmbulance(ambulance);
        mission.setPatientName(missionDto.getPatientName());
        mission.setEmergencyType(missionDto.getEmergencyType());
        mission.setStartLocation(missionDto.getStartLocation());
        mission.setEndLocation(missionDto.getEndLocation());
        mission.setPathJson(missionDto.getPathJson());
        mission.setStatus(Mission.MissionStatus.DISPATCHED);
        mission.setDispatchTime(LocalDateTime.now());

        if (missionDto.getDriver() != null) {
            userRepository.findById(missionDto.getDriver().getId()).ifPresent(mission::setDriver);
        }

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
                List.of(Mission.MissionStatus.DISPATCHED, Mission.MissionStatus.EN_ROUTE, Mission.MissionStatus.TRANSPORT)
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
            case TRANSPORT:
                mission.setTransportTime(LocalDateTime.now());
                ambulance.setStatus(Ambulance.AmbulanceStatus.TRANSPORT);
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
