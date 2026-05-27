package com.example.bellmanford.repository;

import com.example.bellmanford.model.CustomLogicalEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomLogicalEdgeRepository extends JpaRepository<CustomLogicalEdge, Long> {
}
