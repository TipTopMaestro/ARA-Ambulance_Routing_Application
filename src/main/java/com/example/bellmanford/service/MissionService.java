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

    private final RequestLogRepository requestRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final GeneratedPathRepository pathRepository;

    public MissionService(RequestLogRepository requestRepository, 
                          AmbulanceRepository ambulanceRepository,
                          HospitalRepository hospitalRepository,
                          PatientRepository patientRepository,
                          LocationRepository locationRepository,
                          UserRepository userRepository,
                          GeneratedPathRepository pathRepository) {
        this.requestRepository = requestRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
        this.patientRepository = patientRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.pathRepository = pathRepository;
    }

    @Transactional
    public RequestLog createMission(Mission missionDto) {
        Patient patient = patientRepository.findById(missionDto.getPatientId())
            .orElseThrow(() -> new RuntimeException("Patient not found"));

        Ambulance ambulance = ambulanceRepository.findById(missionDto.getAmbulanceId())
            .orElseThrow(() -> new RuntimeException("Ambulance not found"));

        Hospital hospital = hospitalRepository.findById(missionDto.getHospitalId())
            .orElseThrow(() -> new RuntimeException("Hospital not found"));

        GeneratedPath path = pathRepository.findById(missionDto.getPathId())
            .orElseThrow(() -> new RuntimeException("Generated path not found. Calculate route first."));

        Long dispatcherId = missionDto.getDispatcherId() != null ? missionDto.getDispatcherId() : 1L;
        User dispatcher = userRepository.findById(dispatcherId)
            .orElseThrow(() -> new RuntimeException("Dispatcher not found"));

        RequestLog request = new RequestLog();
        request.setPatient(patient);
        request.setHospital(hospital);
        request.setDispatcher(dispatcher);
        if (missionDto.getDriverId() != null) {
            User driver = userRepository.findById(missionDto.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));
            request.setDriver(driver);
        } else {
            userRepository.findByRoleAndActiveTrue(User.UserRole.DRIVER).stream()
                .findFirst()
                .ifPresent(request::setDriver);
        }
        request.setAmbulance(ambulance);
        request.setEmergencyType(missionDto.getEmergencyType());
        request.setGeneratedPath(path);
        request.setStatus(RequestLog.RequestStatus.DISPATCHED);
        request.setDispatchedAt(LocalDateTime.now());
        
        ambulance.setStatus(Ambulance.AmbulanceStatus.RESERVED);
        ambulanceRepository.save(ambulance);

        return requestRepository.save(request);
    }

    public List<RequestLog> getAllMissions() {
        return requestRepository.findAll();
    }

    public Optional<RequestLog> getActiveMissionForDriver(Long driverId) {
        return userRepository.findById(driverId)
            .flatMap(driver -> requestRepository.findByDriverAndStatusIn(
                driver, 
                List.of(RequestLog.RequestStatus.DISPATCHED, RequestLog.RequestStatus.EN_ROUTE, RequestLog.RequestStatus.TRANSPORT)
            ));
    }

    @Transactional
    public RequestLog updateMissionStatus(Long id, String statusStr) {
        RequestLog request = requestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Request not found"));

        RequestLog.RequestStatus newStatus = RequestLog.RequestStatus.valueOf(statusStr);
        request.setStatus(newStatus);

        Ambulance ambulance = request.getAmbulance();

        switch (newStatus) {
            case EN_ROUTE:
                ambulance.setStatus(Ambulance.AmbulanceStatus.EN_ROUTE);
                break;
            case TRANSPORT:
                request.setArrivedAtPatientAt(LocalDateTime.now());
                ambulance.setStatus(Ambulance.AmbulanceStatus.TRANSPORT);
                break;
            case COMPLETED:
                request.setCompletedAt(LocalDateTime.now());
                ambulance.setStatus(Ambulance.AmbulanceStatus.AVAILABLE);
                break;
            case CANCELLED:
                ambulance.setStatus(Ambulance.AmbulanceStatus.AVAILABLE);
                break;
            default:
                break;
        }

        ambulanceRepository.save(ambulance);
        return requestRepository.save(request);
    }
}
