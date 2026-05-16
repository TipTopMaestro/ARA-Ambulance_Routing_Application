package com.example.bellmanford.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;

import com.example.bellmanford.model.GraphEdge;
import com.example.bellmanford.model.GraphNode;
import com.example.bellmanford.model.Hospital;
import com.example.bellmanford.model.Ambulance;
import com.example.bellmanford.model.User;
import com.example.bellmanford.model.Node;
import com.example.bellmanford.repository.UserRepository;
import com.example.bellmanford.repository.AmbulanceRepository;
import com.example.bellmanford.repository.HospitalRepository;
import com.example.bellmanford.repository.NodeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;


@Service
public class MockDatabaseService {

    private final Map<String, GraphNode> nodes = new HashMap<>();
    private final List<GraphEdge> edges = new ArrayList<>();
    
    private final ObjectMapper objectMapper;
    private final Random random = new Random();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    private final UserRepository userRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;
    private final NodeRepository nodeRepository;

    public MockDatabaseService(UserRepository userRepository, 
                               AmbulanceRepository ambulanceRepository,
                               HospitalRepository hospitalRepository,
                               NodeRepository nodeRepository,
                               ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
        this.nodeRepository = nodeRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            InputStream is = getClass().getResourceAsStream("/osm_data.json");
            if (is == null) {
                System.err.println("Could not find osm_data.json in resources");
                return;
            }
            
            JsonNode root = objectMapper.readTree(is);
            JsonNode elements = root.get("elements");
            
            if (elements == null || !elements.isArray()) {
                System.err.println("Invalid OSM JSON structure");
                return;
            }

            // First pass: extract all nodes and count usage to find intersections
            Map<String, Integer> nodeUsage = new HashMap<>();
            for (JsonNode el : elements) {
                String type = el.get("type").asText();
                if ("node".equals(type)) {
                    String id = el.get("id").asText();
                    double lat = el.get("lat").asDouble();
                    double lon = el.get("lon").asDouble();
                    GraphNode node = new GraphNode(id, lat, lon);
                    
                    if (el.has("tags")) {
                        JsonNode tags = el.get("tags");
                        if (tags.has("name")) {
                            node.setName(tags.get("name").asText());
                        } else if (tags.has("highway") && "traffic_signals".equals(tags.get("highway").asText())) {
                            node.setName("Traffic Light at Intersection");
                        }
                    }
                    nodes.put(id, node);
                } else if ("way".equals(type)) {
                    JsonNode wayNodes = el.get("nodes");
                    if (wayNodes != null) {
                        for (JsonNode nodeRef : wayNodes) {
                            String nodeId = nodeRef.asText();
                            nodeUsage.put(nodeId, nodeUsage.getOrDefault(nodeId, 0) + 1);
                        }
                    }
                }
            }

            // Second pass: build simplified graph
            for (JsonNode el : elements) {
                String type = el.get("type").asText();
                if ("way".equals(type)) {
                    JsonNode wayNodes = el.get("nodes");
                    if (wayNodes != null && wayNodes.isArray() && wayNodes.size() >= 2) {
                        
                        boolean oneway = false;
                        if (el.has("tags") && el.get("tags").has("oneway")) {
                            oneway = "yes".equals(el.get("tags").get("oneway").asText());
                        }

                        List<String> currentPath = new ArrayList<>();
                        double currentDistance = 0;

                        for (int i = 0; i < wayNodes.size(); i++) {
                            String nodeId = wayNodes.get(i).asText();
                            currentPath.add(nodeId);

                            if (i > 0) {
                                GraphNode prev = nodes.get(wayNodes.get(i - 1).asText());
                                GraphNode curr = nodes.get(nodeId);
                                if (prev != null && curr != null) {
                                    currentDistance += calculateDistance(prev.getLatitude(), prev.getLongitude(), curr.getLatitude(), curr.getLongitude());
                                }
                            }

                            // A node is significant if it's the start/end of a way, an intersection, or a turn
                            boolean isSignificant = (i == 0 || i == wayNodes.size() - 1 || nodeUsage.getOrDefault(nodeId, 0) > 1);
                            
                            // Check for turn
                            if (!isSignificant && i > 0 && i < wayNodes.size() - 1) {
                                isSignificant = isTurn(wayNodes.get(i - 1).asText(), nodeId, wayNodes.get(i + 1).asText());
                            }

                            if (isSignificant && currentPath.size() > 1) {
                                String sourceId = currentPath.get(0);
                                String targetId = nodeId;
                                
                                double baseTravelTime = currentDistance * 2.0; // 30km/h
                                
                                double finalWeight = baseTravelTime;
                                if (random.nextDouble() < 0.05) {
                                    finalWeight = - (random.nextDouble() * 5.0 + 1.0); // -1.0 to -6.0
                                }

                                edges.add(new GraphEdge(sourceId, targetId, finalWeight, 1.0));
                                if (!oneway) {
                                    // Prevent immediate negative 2-cycles by ensuring the reverse edge 
                                    // stays positive if the forward edge was made negative.
                                    double reverseWeight = (finalWeight < 0) ? baseTravelTime : finalWeight;
                                    edges.add(new GraphEdge(targetId, sourceId, reverseWeight, 1.0));
                                }
                                
                                // Reset for next segment
                                currentPath.clear();
                                currentPath.add(nodeId);
                                currentDistance = 0;
                            }
                        }
                    }
                }
            }
            
            // Add aliases for frontend specific IDs
            addAlias("H1", "6051423911", "Panabo Polymedic Hospital");
            addAlias("H2", "1621665249", "Good Shepherd Hospital");
            addAlias("H3", "1621650390", "Rivera Medical Center");
            addAlias("CITY_HALL", "12743750784", "Panabo City Hall");
            addAlias("AMB_1", "1621665322", "Ambulance Station Alpha");
            addAlias("AMB_2", "968789442", "Ambulance Station Bravo");
            addAlias("AMB_3", "928071323", "Ambulance Station Charlie");
            addAlias("AMB_4", "1298161645", "Downtown Ambulance Stand");

            seedDatabase();
            System.out.println("Mock Database Initialization complete. Loaded " + nodes.size() + " nodes and " + edges.size() + " edges.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seedDatabase() {
        if (userRepository.count() == 0) {
            userRepository.save(new User("dispatcher1", "password", User.UserRole.DISPATCHER));
            userRepository.save(new User("driver1", "password", User.UserRole.DRIVER));
            userRepository.save(new User("driver2", "password", User.UserRole.DRIVER));
        }

        if (hospitalRepository.count() == 0) {
            seedHospital(6051423911L, "Panabo Polymedic Hospital");
            seedHospital(1621665249L, "Good Shepherd Hospital");
            seedHospital(1621650390L, "Rivera Medical Center");
        }

        if (ambulanceRepository.count() == 0) {
            hospitalRepository.findAll().forEach(h -> {
                ambulanceRepository.save(new Ambulance(h, Ambulance.AmbulanceStatus.AVAILABLE));
                ambulanceRepository.save(new Ambulance(h, Ambulance.AmbulanceStatus.AVAILABLE));
            });
        }
    }

    private void seedHospital(Long osmId, String name) {
        GraphNode graphNode = nodes.get(String.valueOf(osmId));
        if (graphNode != null) {
            // First create and save the Node entity
            Node nodeEntity = nodeRepository.findById(osmId).orElseGet(() -> {
                Node n = new Node();
                n.setId(osmId);
                n.setLocation(geometryFactory.createPoint(new Coordinate(graphNode.getLongitude(), graphNode.getLatitude())));
                return nodeRepository.save(n);
            });

            Hospital h = new Hospital();
            h.setName(name);
            h.setNode(nodeEntity);
            hospitalRepository.save(h);
        }
    }

    private void addAlias(String alias, String originalId, String aliasName) {
        GraphNode original = nodes.get(originalId);
        if (original != null) {
            GraphNode aliasNode = new GraphNode(alias, original.getLatitude(), original.getLongitude());
            aliasNode.setName(aliasName != null ? aliasName : original.getName());
            nodes.put(alias, aliasNode);
            
            edges.add(new GraphEdge(alias, originalId, 0.0, 1.0));
            edges.add(new GraphEdge(originalId, alias, 0.0, 1.0));
        }
    }

    private boolean isTurn(String prevId, String currId, String nextId) {
        GraphNode p = nodes.get(prevId);
        GraphNode c = nodes.get(currId);
        GraphNode n = nodes.get(nextId);
        if (p == null || c == null || n == null) return false;

        double angle1 = Math.atan2(c.getLatitude() - p.getLatitude(), c.getLongitude() - p.getLongitude());
        double angle2 = Math.atan2(n.getLatitude() - c.getLatitude(), n.getLongitude() - c.getLongitude());
        double diff = Math.abs(angle1 - angle2);
        if (diff > Math.PI) diff = 2 * Math.PI - diff;
        
        return diff > (Math.PI / 12); // > 15 degrees
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<GraphNode> getSignificantNodes() {
        Set<String> significantIds = new HashSet<>();
        for (GraphEdge edge : edges) {
            significantIds.add(edge.getSource());
            significantIds.add(edge.getTarget());
        }
        List<GraphNode> result = new ArrayList<>();
        for (String id : significantIds) {
            GraphNode node = nodes.get(id);
            if (node != null) {
                result.add(node);
            }
        }
        return result;
    }

    public Map<String, GraphNode> getNodes() {
        return nodes;
    }

    public List<GraphEdge> getEdges() {
        return edges;
    }
    
    public GraphNode getNode(String id) {
        return nodes.get(id);
    }
}
