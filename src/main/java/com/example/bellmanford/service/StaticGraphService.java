package com.example.bellmanford.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.bellmanford.model.GraphEdge;
import com.example.bellmanford.model.GraphNode;

/**
 * The simplest possible graph provider.
 * All nodes and edges are hardcoded here for 100% control.
 */
@Service
public class StaticGraphService {

    private final Map<String, GraphNode> nodes = new HashMap<>();
    private final List<GraphEdge> edges = new ArrayList<>();

    public StaticGraphService() {
        // --- 1. DEFINE NODES (Latitude, Longitude) ---
        // You can add more nodes here manually
        // addNode("H1", 7.3024451, 125.6784332, "Panabo Polymedic Hospital");
        // addNode("H2", 7.2940300, 125.6778678, "Good Shepherd Hospital");
        addNode("H1", 7.3125010, 125.6856676, "Rivera Medical Center");
        
        //Border nodes
        
        addNode("BN1", 7.313923, 125.686383, "Rivera Intersection");
        addNode("BN2", 7.322127, 125.689892, null);
        addNode("BN3", 7.321712, 125.690567, null);
        addNode("BN4",7.321648,125.692756, null);
        addNode("BN5",7.321648, 125.695953, null);
        addNode("BN6",7.321563,125.699623, null);
        addNode("BN7",7.32173, 125.70323, null);
        addNode("BN8",7.319137,125.703163, null);
        addNode("BN9", 7.318403, 125.702745, null);
        addNode("BN10", 7.313518, 125.702509, null);

        addNode("BN11",7.313572,125.698732, null);
        addNode("BN12", 7.313572, 125.695106, null);
        addNode("BN13",7.313529,125.691587, null);
        addNode("BN14", 7.313444, 125.690686, null);
        addNode("BN15", 7.313369, 125.688797, null);
        addNode("BN16",7.313625, 125.687156, null);
        addNode("BN17", 7.317275, 125.687821, null);
        addNode("BN18", 7.318616, 125.688379, null);
        addNode("BN19",7.320361, 125.689130, null);
        addNode("BN20", 7.316924, 125.702680, null);


        //Residencial Entrance Nodes

        addNode("RE1", 7.320052, 125.689859, null);
        addNode("RE2", 7.320052, 125.689859, null);
        addNode("RE3", 7.318328, 125.689141, null);
        addNode("RE4", 7.318169, 125.689377, null);
        addNode("RE5", 7.316849, 125.688829, null);
        addNode("RE6", 7.316977, 125.688561, null);
        addNode("RE7", 7.316338, 125.689988, null);
        addNode("RE8", 7.316519, 125.690246, null);
        addNode("RE9", 7.316445, 125.690954, null);
        addNode("RE10",7.319776, 125.690857, null);
        addNode("RE11",7.316392, 125.691383, null);
        addNode("RE12",7.319786, 125.691329, null);
        addNode("RE13",7.319808, 125.691673, null);
        addNode("RE14",7.319808, 125.691962, null);
        addNode("RE15",7.315689, 125.691726, null);
        addNode("RE16",7.315721, 125.691919, null);
        addNode("RE17",7.319797, 125.692198, null);
        addNode("RE18",7.315721, 125.692198, null);
        addNode("RE19",7.319818, 125.692434, null);
        addNode("RE20",7.315721, 125.692456, null);
        addNode("RE21",7.319839, 125.692778, null);
        addNode("RE22",7.315774, 125.693164, null);
        addNode("RE23",7.317732, 125.690889, null);
        addNode("RE24",7.317754, 125.691329, null);
        addNode("RE25",7.318073, 125.691694, null);
        addNode("RE26",7.318073, 125.691694, null);
        addNode("RE27",7.318073, 125.691694, null);
        addNode("RE28",7.318062, 125.692177, null);
        addNode("RE29",7.318073, 125.692424, null);
        addNode("RE30",7.319808, 125.693507, null);
        addNode("RE31",7.317647, 125.693550, null);
        addNode("RE32",7.318914, 125.694977, null);
        addNode("RE33",7.318956, 125.697402, null);
        addNode("RE34",7.317190, 125.697455, null);
        addNode("RE35",7.316988, 125.699065, null);
        addNode("RE36",7.314721, 125.690686, null);
        addNode("RE37",7.314785, 125.691544, null);
        addNode("RE38", 7.315689, 125.691587, null);


        
        //Patient

        // --- 2. DEFINE EDGES (Source ID, Target ID, Weight) ---
        // Weights can be positive (distance/time) or negative (priority shortcuts)
        
        // MainSource
        addEdge("H1", "BN1", 1.0);

        
        // Connections from
        
        addEdge("BN19", "BN2",-1.0);
        addEdge("BN2", "BN3", 0.2);
        addEdge("BN3", "BN4", 1.0);
        addEdge("BN4", "BN5", 1.0);
        addEdge("BN5", "BN6", 1.0);
        addEdge("BN6", "BN7", 1.0);
        addEdge("BN7", "BN8", 1.0);
        addEdge("BN8", "BN9", .2);
        addEdge("BN15", "RE7", 1.0);
        addEdge("BN15", "BN14", 1.0);
        addEdge("BN14", "BN13", 1.0);
        addEdge("BN13", "BN12", 1.0);
        addEdge("BN12", "BN11", 1.0);
        addEdge("BN11", "BN10", 1.0);
        addEdge("BN10", "BN20", 1.0);
        addEdge("BN20", "BN9", 0.6);
        addEdge("BN16", "BN15", 0.6);
        addEdge("BN18", "RE3", 0.2);
        addEdge("BN17", "RE6", 0.5);
        addEdge("BN19", "RE1", 0.2);
        addEdge("BN16", "RE6", 2.0);
        addEdge("RE1", "RE10", 0.2);
        addEdge("RE2", "RE3", 0.2);
        addEdge("RE6", "RE5", 0.0);
        addEdge("RE5", "RE4", 0.2);
        addEdge("RE3", "RE4", -0.1);
        addEdge("RE5", "RE7", 0.4);
        addEdge("RE7", "RE8", 0.1);
        addEdge("RE8", "RE9", 0.1);
        addEdge("RE9", "RE11", 0.1);
        addEdge("RE9", "RE23", 2.0);
        addEdge("RE4","RE23", 2.0);
        addEdge("RE23", "RE10", 2);
        addEdge("RE23","RE24",0.4);
        addEdge("RE11","RE24",1.0);
        addEdge("RE10","RE12" , 0.2);
        addEdge("RE12","RE13", 0.2);
        addEdge("RE13","RE14", 0.2);
        
        addEdge("RE37", "RE38", 1.0);

        

        




        // --- 3. CUSTOM NEGATIVE EDGES (High Priority Shortcuts) ---
        // This makes the algorithm prefer this path even if it's "longer"
        addEdge("BN1", "BN16", -1.0);
        addEdge("BN1", "BN17", -1.0);
        addEdge("BN17", "BN18", -1.0);
        addEdge("BN18", "BN19", -1.0);
        addEdge("BN14","RE36", -2);
        addEdge("RE36", "RE37", -2);
    }

    private void addNode(String id, double lat, double lon, String name) {
        GraphNode node = new GraphNode(id, lat, lon);
        node.setName(name);
        nodes.put(id, node);
    }

    private void addEdge(String source, String target, double weight) {
        edges.add(new GraphEdge(source, target, weight, 1.0));
    }

    public Map<String, GraphNode> getNodes() {
        return nodes;
    }

    public List<GraphEdge> getEdges() {
        return edges;
    }
}
