package com.example.bellmanford.repository;

import com.example.bellmanford.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, java.util.UUID> {
}
