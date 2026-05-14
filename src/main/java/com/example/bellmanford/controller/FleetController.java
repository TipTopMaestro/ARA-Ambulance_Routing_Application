package com.example.bellmanford.controller;

import com.example.bellmanford.model.Ambulance;
import com.example.bellmanford.repository.AmbulanceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/ambulances")
public class FleetController {

    private final AmbulanceRepository ambulanceRepository;

    private final com.example.bellmanford.repository.HospitalRepository hospitalRepository;

    public FleetController(AmbulanceRepository ambulanceRepository, com.example.bellmanford.repository.HospitalRepository hospitalRepository) {
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
    }

    @GetMapping
    public ResponseEntity<List<Ambulance>> getAllAmbulances() {
        return ResponseEntity.ok(ambulanceRepository.findAll());
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<List<Ambulance>> getAmbulancesByHospital(@PathVariable String hospitalId) {
        // Here hospitalId from frontend might still be the alias (e.g., H1)
        // We should search by name or some identifier
        return hospitalRepository.findAll().stream()
            .filter(h -> h.getLocation().getOsmNodeId().equals(hospitalId))
            .findFirst()
            .map(h -> ResponseEntity.ok(ambulanceRepository.findByHospital(h)))
            .orElse(ResponseEntity.notFound().build());
    }
}
