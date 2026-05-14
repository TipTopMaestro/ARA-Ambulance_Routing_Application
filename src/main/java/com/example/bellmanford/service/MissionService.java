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

    private final RequestLogRepository requestLogRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final PatientRepository patientRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;

    public MissionService(RequestLogRepository requestLogRepository, 
                          AmbulanceRepository ambulanceRepository,
                          PatientRepository patientRepository,
                          LocationRepository locationRepository,
                          UserRepository userRepository,
                          HospitalRepository hospitalRepository) {
        this.requestLogRepository = requestLogRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.patientRepository = patientRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
    }

    @Transactional
    public RequestLog createMission(Mission missionDto) {
        // 1. Create or get Patient
        Patient patient = new Patient(missionDto.getPatientName(), null, missionDto.getEmergencyType());
        patient = patientRepository.save(patient);

        // 2. Create or get Locations
        Location targetLoc = new Location(
            null, missionDto.getPatientLocationName(), 
            missionDto.getPatientLat(), missionDto.getPatientLng(), 
            Location.LocationType.PATIENT
        );
        targetLoc = locationRepository.save(targetLoc);

        // For source, we usually use the hospital's location
        Ambulance ambulance = ambulanceRepository.findByAmbulanceCode(missionDto.getAmbulanceId())
            .orElseThrow(() -> new RuntimeException("Ambulance not found"));
        
        Location sourceLoc = ambulance.getHospital().getLocation();

        // 3. Get Dispatcher
        User dispatcher = userRepository.findById(1L) // Default for now, should come from auth
            .orElseThrow(() -> new RuntimeException("Dispatcher not found"));

        // 4. Create RequestLog
        RequestLog log = new RequestLog();
        log.setPatient(patient);
        log.setDispatcher(dispatcher);
        log.setAmbulance(ambulance);
        log.setEmergencyType(missionDto.getEmergencyType());
        log.setSourceLocation(sourceLoc);
        log.setTargetLocation(targetLoc);
        log.setStatus(RequestLog.MissionStatus.DISPATCHED);
        log.setDispatchedAt(LocalDateTime.now());
        
        // Update ambulance status
        ambulance.setStatus(Ambulance.AmbulanceStatus.RESERVED);
        ambulanceRepository.save(ambulance);

        return requestLogRepository.save(log);
    }

    public List<RequestLog> getAllMissions() {
        return requestLogRepository.findAll();
    }

    public Optional<RequestLog> getActiveMissionForDriver(Long driverId) {
        return userRepository.findById(driverId)
            .flatMap(driver -> requestLogRepository.findByDriverAndStatusIn(
                driver, 
                List.of(RequestLog.MissionStatus.DISPATCHED, RequestLog.MissionStatus.EN_ROUTE, RequestLog.MissionStatus.TRANSPORT)
            ));
    }

    @Transactional
    public RequestLog updateMissionStatus(Long id, String statusStr) {
        RequestLog log = requestLogRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mission not found"));

        RequestLog.MissionStatus newStatus = RequestLog.MissionStatus.valueOf(statusStr);
        log.setStatus(newStatus);

        Ambulance ambulance = log.getAmbulance();

        switch (newStatus) {
            case EN_ROUTE:
                ambulance.setStatus(Ambulance.AmbulanceStatus.EN_ROUTE);
                break;
            case TRANSPORT:
                log.setArrivedAtPatientAt(LocalDateTime.now());
                ambulance.setStatus(Ambulance.AmbulanceStatus.TRANSPORT);
                break;
            case COMPLETED:
                log.setCompletedAt(LocalDateTime.now());
                ambulance.setStatus(Ambulance.AmbulanceStatus.AVAILABLE);
                break;
            case CANCELLED:
                ambulance.setStatus(Ambulance.AmbulanceStatus.AVAILABLE);
                break;
            default:
                break;
        }

        ambulanceRepository.save(ambulance);
        return requestLogRepository.save(log);
    }
}
