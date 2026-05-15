package com.example.bellmanford.repository;

import com.example.bellmanford.model.GeneratedPath;
import com.example.bellmanford.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GeneratedPathRepository extends JpaRepository<GeneratedPath, Long> {
    List<GeneratedPath> findBySourceLocationAndTargetLocation(Location source, Location target);
}
