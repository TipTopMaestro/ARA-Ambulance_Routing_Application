package com.example.bellmanford.repository;

import com.example.bellmanford.model.Ambulance;
import com.example.bellmanford.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {
    List<Ambulance> findByHospital(Hospital hospital);
    Optional<Ambulance> findByAmbulanceCode(String ambulanceCode);
}
