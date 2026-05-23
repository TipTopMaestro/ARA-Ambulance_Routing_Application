package com.example.bellmanford.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.example.bellmanford.model.CustomLogicalEdge;
import com.example.bellmanford.model.RouteNode;
import com.example.bellmanford.repository.CustomLogicalEdgeRepository;
import com.example.bellmanford.repository.RouteNodeRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RouteNodeRepository nodeRepo;
    private final CustomLogicalEdgeRepository edgeRepo;
    private final JdbcTemplate jdbcTemplate;
    private final StaticGraphService staticGraphService;

    public DataSeeder(RouteNodeRepository nodeRepo, 
                      CustomLogicalEdgeRepository edgeRepo,
                      JdbcTemplate jdbcTemplate,
                      StaticGraphService staticGraphService) {
        this.nodeRepo = nodeRepo;
        this.edgeRepo = edgeRepo;
        this.jdbcTemplate = jdbcTemplate;
        this.staticGraphService = staticGraphService;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Migrate existing hospitals to nodes table if needed
        migrateHospitals();

        // 2. Seed remaining nodes if empty
        if (nodeRepo.count() <= hospitalCount()) {
            seedNodes();
        }

        // 3. Seed edges if empty
        if (edgeRepo.count() == 0) {
            seedEdges();
        }

        // 4. Force refresh the graph in memory now that the DB is ready
        staticGraphService.refreshGraph();
    }

    private long hospitalCount() {
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hospitals", Long.class);
        } catch (Exception e) {
            return 0;
        }
    }

    private void migrateHospitals() {
        try {
            // Check if node_id column exists (Hibernate should have created it)
            // Then migrate latitude/longitude to route_nodes and update hospitals.node_id
            jdbcTemplate.execute("INSERT IGNORE INTO route_nodes (id, latitude, longitude, name) " +
                               "SELECT CONCAT('H', id), latitude, longitude, name FROM hospitals WHERE node_id IS NULL");
            
            jdbcTemplate.execute("UPDATE hospitals SET node_id = CONCAT('H', id) WHERE node_id IS NULL");
            
            // DROP LEGACY COLUMNS
            try {
                jdbcTemplate.execute("ALTER TABLE hospitals DROP COLUMN latitude, DROP COLUMN longitude");
                System.out.println("Hospital legacy coordinates dropped.");
            } catch (Exception e) {
                // Ignore if columns already dropped
            }
            
            System.out.println("Hospital data migration to route_nodes complete.");
        } catch (Exception e) {
            System.err.println("Hospital migration skipped or failed: " + e.getMessage());
        }
    }

    private void seedNodes() {
        List<RouteNode> nodes = new ArrayList<>();
        
        // Border Nodes
        nodes.add(new RouteNode("BN1", 7.313923, 125.686383, "Rivera Intersection"));
        nodes.add(new RouteNode("BN2", 7.322127, 125.689892, null));
        nodes.add(new RouteNode("BN3", 7.321712, 125.690567, null));
        nodes.add(new RouteNode("BN4", 7.321648, 125.692756, null));
        nodes.add(new RouteNode("BN5", 7.321648, 125.695953, null));
        nodes.add(new RouteNode("BN6", 7.321563, 125.699623, null));
        nodes.add(new RouteNode("BN7", 7.32173, 125.70323, null));
        nodes.add(new RouteNode("BN8", 7.319137, 125.703163, null));
        nodes.add(new RouteNode("BN9", 7.318403, 125.702745, null));
        nodes.add(new RouteNode("BN10", 7.313518, 125.702509, null));
        nodes.add(new RouteNode("BN11", 7.313572, 125.698732, null));
        nodes.add(new RouteNode("BN12", 7.313572, 125.695106, null));
        nodes.add(new RouteNode("BN13", 7.313529, 125.691587, null));
        nodes.add(new RouteNode("BN14", 7.313444, 125.690686, null));
        nodes.add(new RouteNode("BN15", 7.313369, 125.688797, null));
        nodes.add(new RouteNode("BN16", 7.313625, 125.687156, null));
        nodes.add(new RouteNode("BN17", 7.317275, 125.687821, null));
        nodes.add(new RouteNode("BN18", 7.318616, 125.688379, null));
        nodes.add(new RouteNode("BN19", 7.320361, 125.689130, null));
        nodes.add(new RouteNode("BN20", 7.316924, 125.702680, null));

        // Residential Nodes
        nodes.add(new RouteNode("RE1", 7.320052, 125.689859, null));
        nodes.add(new RouteNode("RE2", 7.320052, 125.689859, null));
        nodes.add(new RouteNode("RE3", 7.318328, 125.689141, null));
        nodes.add(new RouteNode("RE4", 7.318169, 125.689377, null));
        nodes.add(new RouteNode("RE5", 7.316849, 125.688829, null));
        nodes.add(new RouteNode("RE6", 7.316977, 125.688561, null));
        nodes.add(new RouteNode("RE7", 7.316338, 125.689988, null));
        nodes.add(new RouteNode("RE8", 7.316530, 125.690219, null));
        nodes.add(new RouteNode("RE9", 7.316445, 125.690954, null));
        nodes.add(new RouteNode("RE10", 7.319776, 125.690857, null));
        nodes.add(new RouteNode("RE11", 7.316392, 125.691383, null));
        nodes.add(new RouteNode("RE12", 7.319786, 125.691329, null));
        nodes.add(new RouteNode("RE13", 7.319808, 125.691673, null));
        nodes.add(new RouteNode("RE14", 7.319808, 125.691962, null));
        nodes.add(new RouteNode("RE15", 7.315689, 125.691726, null));
        nodes.add(new RouteNode("RE16", 7.315721, 125.691919, null));
        nodes.add(new RouteNode("RE17", 7.319797, 125.692198, null));
        nodes.add(new RouteNode("RE18", 7.315721, 125.692198, null));
        nodes.add(new RouteNode("RE19", 7.319818, 125.692434, null));
        nodes.add(new RouteNode("RE20", 7.315721, 125.692456, null));
        nodes.add(new RouteNode("RE21", 7.319839, 125.692778, null));
        nodes.add(new RouteNode("RE22", 7.315774, 125.693164, null));
        nodes.add(new RouteNode("RE23", 7.317732, 125.690889, null));
        nodes.add(new RouteNode("RE24", 7.317754, 125.691329, null));
        nodes.add(new RouteNode("RE25", 7.318073, 125.691694, null));
        nodes.add(new RouteNode("RE26", 7.318073, 125.691694, null));
        nodes.add(new RouteNode("RE27", 7.318073, 125.691694, null));
        nodes.add(new RouteNode("RE28", 7.318062, 125.692177, null));
        nodes.add(new RouteNode("RE29", 7.318073, 125.692424, null));
        nodes.add(new RouteNode("RE30", 7.319808, 125.693507, null));
        nodes.add(new RouteNode("RE31", 7.317647, 125.693550, null));
        nodes.add(new RouteNode("RE32", 7.318914, 125.694977, null));
        nodes.add(new RouteNode("RE33", 7.318956, 125.697402, null));
        nodes.add(new RouteNode("RE34", 7.317190, 125.697455, null));
        nodes.add(new RouteNode("RE35", 7.316988, 125.699065, null));
        nodes.add(new RouteNode("RE36", 7.314721, 125.690686, null));
        nodes.add(new RouteNode("RE37", 7.314785, 125.691544, null));
        nodes.add(new RouteNode("RE38", 7.315689, 125.691587, null));
        nodes.add(new RouteNode("RE39", 7.319845, 125.692904, null));
        nodes.add(new RouteNode("RE40", 7.316104, 125.693153, null));
        nodes.add(new RouteNode("RE41", 7.316120, 125.692912, null));
        nodes.add(new RouteNode("RE42", 7.318116, 125.692896, null));
        nodes.add(new RouteNode("RE43", 7.316099, 125.692445, null));
        nodes.add(new RouteNode("RE44", 7.316120, 125.692654, null));
        nodes.add(new RouteNode("RE45", 7.316924, 125.692670, null));
        nodes.add(new RouteNode("RE46", 7.318116, 125.692665, null));
        nodes.add(new RouteNode("RE47", 7.317163, 125.692670, null));
        nodes.add(new RouteNode("RE48", 7.318062, 125.691935, null));
        nodes.add(new RouteNode("RE49", 7.317445, 125.690374, null));
        nodes.add(new RouteNode("RE50", 7.315780, 125.693496, null));
        nodes.add(new RouteNode("RE51", 7.315796, 125.693963, null));
        nodes.add(new RouteNode("RE52", 7.315780, 125.694194, null));
        nodes.add(new RouteNode("RE53", 7.315806, 125.693716, null));
        nodes.add(new RouteNode("RE54", 7.317004, 125.693470, null));
        nodes.add(new RouteNode("RE55", 7.317019, 125.693706, null));
        nodes.add(new RouteNode("RE56", 7.317025, 125.693952, null));
        nodes.add(new RouteNode("RE57", 7.317025, 125.693952, null));
        nodes.add(new RouteNode("RE58", 7.316998, 125.694183, null));
        nodes.add(new RouteNode("RE59", 7.316189, 125.691394, null));
        nodes.add(new RouteNode("RE60", 7.316158, 125.691576, null));

        nodeRepo.saveAll(nodes);
    }

    private void seedEdges() {
        List<CustomLogicalEdge> edges = new ArrayList<>();
        
        // --- MODERATE-SPEED BASE TRAVEL TIMES (IN MINUTES) ---
        // Estimates based on ~30-60 km/h vehicle speed
        
        // Main Source from Hospital
        edges.add(new CustomLogicalEdge("H1", "BN1", 0.8));
        
        // Connections (BN Cluster)
        edges.add(new CustomLogicalEdge("BN2", "BN3", 0.2));
        edges.add(new CustomLogicalEdge("BN3", "BN4", 0.6));
        edges.add(new CustomLogicalEdge("BN4", "BN5", 0.8));
        edges.add(new CustomLogicalEdge("BN5", "BN6", 1.0));
        edges.add(new CustomLogicalEdge("BN6", "BN7", 1.0));
        edges.add(new CustomLogicalEdge("BN7", "BN8", 0.6));
        edges.add(new CustomLogicalEdge("BN8", "BN9", 0.2));
        edges.add(new CustomLogicalEdge("BN15", "BN14", 0.4));
        edges.add(new CustomLogicalEdge("BN14", "BN13", 0.4));
        edges.add(new CustomLogicalEdge("BN13", "BN12", 0.8));
        edges.add(new CustomLogicalEdge("BN12", "BN11", 0.8));
        edges.add(new CustomLogicalEdge("BN11", "BN10", 0.8));
        edges.add(new CustomLogicalEdge("BN10", "BN20", 1.0));
        edges.add(new CustomLogicalEdge("BN20", "BN9", 0.6));
        edges.add(new CustomLogicalEdge("BN16", "BN15", 0.4));
        edges.add(new CustomLogicalEdge("BN18", "RE3", 0.2));
        edges.add(new CustomLogicalEdge("BN17", "RE6", 0.4));
        edges.add(new CustomLogicalEdge("BN19", "RE1", 0.2));
        edges.add(new CustomLogicalEdge("BN16", "RE6", 1.2));

        // Connections (RE Cluster)
        edges.add(new CustomLogicalEdge("RE1", "RE10", 0.2));
        edges.add(new CustomLogicalEdge("RE2", "RE3", 0.2));
        edges.add(new CustomLogicalEdge("RE6", "RE5", 0.2));
        edges.add(new CustomLogicalEdge("RE5", "RE4", 0.2));
        edges.add(new CustomLogicalEdge("RE3", "RE4", 0.2));
        edges.add(new CustomLogicalEdge("RE5", "RE7", 0.4));
        edges.add(new CustomLogicalEdge("RE7", "RE8", 0.2));
        edges.add(new CustomLogicalEdge("RE8", "RE9", 0.2));
        edges.add(new CustomLogicalEdge("RE9", "RE11", 0.2));
        edges.add(new CustomLogicalEdge("RE9", "RE23", 0.8));
        edges.add(new CustomLogicalEdge("RE4", "RE23", 0.8));
        edges.add(new CustomLogicalEdge("RE23", "RE10", 0.8));
        edges.add(new CustomLogicalEdge("RE23", "RE24", 0.4));
        edges.add(new CustomLogicalEdge("RE11", "RE24", 0.6));
        edges.add(new CustomLogicalEdge("RE10", "RE12", 0.2));
        edges.add(new CustomLogicalEdge("RE12", "RE13", 0.2));
        edges.add(new CustomLogicalEdge("RE13", "RE14", 0.2));
        edges.add(new CustomLogicalEdge("RE37", "RE38", 0.6));
        edges.add(new CustomLogicalEdge("RE38", "RE15", 0.6));
        edges.add(new CustomLogicalEdge("RE15", "RE16", 0.4));
        edges.add(new CustomLogicalEdge("RE16", "RE18", 0.4));
        edges.add(new CustomLogicalEdge("RE18", "RE20", 0.4));
        edges.add(new CustomLogicalEdge("RE20", "RE22", 0.6));
        edges.add(new CustomLogicalEdge("RE8", "RE49", 0.6));
        edges.add(new CustomLogicalEdge("RE11", "RE59", 0.4));
        edges.add(new CustomLogicalEdge("RE59", "RE60", 0.2));
        edges.add(new CustomLogicalEdge("RE60", "RE38", 0.4));
        edges.add(new CustomLogicalEdge("RE15", "RE27", 1.0));
        edges.add(new CustomLogicalEdge("RE27", "RE13", 1.0));
        edges.add(new CustomLogicalEdge("RE16", "RE48", 1.0));
        edges.add(new CustomLogicalEdge("RE48", "RE14", 1.0));
        edges.add(new CustomLogicalEdge("RE18", "RE28", 1.0));
        edges.add(new CustomLogicalEdge("RE28", "RE17", 1.0));
        edges.add(new CustomLogicalEdge("RE20", "RE43", 0.6));
        edges.add(new CustomLogicalEdge("RE43", "RE29", 1.0));
        edges.add(new CustomLogicalEdge("RE29", "RE19", 1.0));
        edges.add(new CustomLogicalEdge("RE14", "RE17", 0.6));
        edges.add(new CustomLogicalEdge("RE17", "RE19", 0.6));
        edges.add(new CustomLogicalEdge("RE43", "RE44", 0.8));
        edges.add(new CustomLogicalEdge("RE44", "RE45", 0.8));
        edges.add(new CustomLogicalEdge("RE44", "RE41", 0.6));
        edges.add(new CustomLogicalEdge("RE41", "RE42", 0.6));
        edges.add(new CustomLogicalEdge("RE42", "RE39", 1.0));
        edges.add(new CustomLogicalEdge("RE29", "RE46", 0.6));
        edges.add(new CustomLogicalEdge("RE46", "RE47", 1.0));
        edges.add(new CustomLogicalEdge("RE19", "RE21", 0.6));
        edges.add(new CustomLogicalEdge("RE21", "RE39", 0.6));
        edges.add(new CustomLogicalEdge("RE46", "RE42", 0.6));
        edges.add(new CustomLogicalEdge("RE27", "RE48", 0.6));
        edges.add(new CustomLogicalEdge("RE48", "RE28", 0.6));
        edges.add(new CustomLogicalEdge("RE28", "RE29", 0.6));
        edges.add(new CustomLogicalEdge("RE41", "RE40", 0.6));
        edges.add(new CustomLogicalEdge("RE22", "RE40", 0.6));
        edges.add(new CustomLogicalEdge("RE22", "RE50", 0.6));
        edges.add(new CustomLogicalEdge("RE50", "RE54", 1.0));
        edges.add(new CustomLogicalEdge("RE50", "RE53", 0.6));
        edges.add(new CustomLogicalEdge("RE53", "RE51", 0.6));
        edges.add(new CustomLogicalEdge("RE51", "RE52", 0.6));
        edges.add(new CustomLogicalEdge("RE51", "RE57", 0.6));
        edges.add(new CustomLogicalEdge("RE53", "RE55", 1.0));
        edges.add(new CustomLogicalEdge("RE52", "RE58", 1.0));
        edges.add(new CustomLogicalEdge("RE39", "RE30", 0.4));
        edges.add(new CustomLogicalEdge("RE30", "RE31", 1.0));
        edges.add(new CustomLogicalEdge("BN20", "RE35", 1.0));
        edges.add(new CustomLogicalEdge("BN6", "RE35", 1.4));
        edges.add(new CustomLogicalEdge("RE35", "RE34", 0.8));
        edges.add(new CustomLogicalEdge("RE34", "RE33", 0.8));
        edges.add(new CustomLogicalEdge("RE33", "RE32", 0.8));
        
        // --- BASE CONNECTIONS (NOW ALL POSITIVE) ---
        edges.add(new CustomLogicalEdge("BN1", "BN16", 0.6));
        edges.add(new CustomLogicalEdge("BN1", "BN17", -0.25));
        edges.add(new CustomLogicalEdge("BN17", "BN18", -0.25));
        edges.add(new CustomLogicalEdge("BN18", "BN19", -0.25));
        edges.add(new CustomLogicalEdge("BN19", "BN2", -0.25));
        edges.add(new CustomLogicalEdge("BN11", "RE35", -0.3));
        edges.add(new CustomLogicalEdge("RE24", "RE12", -0.25));
        edges.add(new CustomLogicalEdge("BN15", "RE7", -0.25));
        edges.add(new CustomLogicalEdge("BN14", "RE36", 0.6));
        edges.add(new CustomLogicalEdge("RE36", "RE37", 0.6));
        edges.add(new CustomLogicalEdge("RE37", "BN13", 0.8));

        edgeRepo.saveAll(edges);
    }
}

