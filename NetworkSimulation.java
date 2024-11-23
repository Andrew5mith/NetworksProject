// COSC 4317 
// Network simulation group project

import java.util.*;

public class NetworkSimulation {
    public static void main(String[] args) {
        // Initialize the graph with the network structure
        Graph graph = new Graph();
        graph.addNextHop("A", "B", 2);
        graph.addNextHop("A", "D", 5);
        graph.addNextHop("B", "C", 2);
        graph.addNextHop("B", "E", 1);
        graph.addNextHop("C", "D", 2);
        graph.addNextHop("C", "F", 3);
        graph.addNextHop("E", "F", 3);

        // Compute and print forwarding tables for all nodes
        System.out.println("Forwarding Tables for All Nodes:");
        computeAndPrintForwardingTables(graph);

        // Route Packet P1 from A to F
        System.out.println("\nRouting Packet P1 (A to F):");
        routePacket(graph, "A", "F");

        // Simulate link failure between B and C
        System.out.println("\nSimulating Link Failure Between B and C:");
        graph.removeEdge("B", "C");

        // Recompute and print updated forwarding tables
        System.out.println("\nUpdated Forwarding Tables After Link Failure:");
        computeAndPrintForwardingTables(graph);

        // Route Packet P2 from A to F after link failure
        System.out.println("\nRouting Packet P2 (A to F):");
        routePacket(graph, "A", "F");
    }

    // Computes and prints forwarding tables for all nodes
    private static void computeAndPrintForwardingTables(Graph graph) {
        for (String node : graph.getNodes()) {
            System.out.println("Forwarding Table for Node " + node + ":");
            Map<String, ForwardingTableEntry> forwardingTable = graph.computeForwardingTable(node);
            printForwardingTable(forwardingTable);
        }
    }

    // Prints the forwarding table for a single node
    private static void printForwardingTable(Map<String, ForwardingTableEntry> forwardingTable) {
        System.out.println("Destination\tNext Hop\tCost");
        for (Map.Entry<String, ForwardingTableEntry> entry : forwardingTable.entrySet()) {
            String destination = entry.getKey();
            ForwardingTableEntry tableEntry = entry.getValue();
            System.out.printf("%s\t\t%s\t\t%d\n",
                    destination,
                    tableEntry.nextHop == null ? "-" : tableEntry.nextHop,
                    tableEntry.cost == Integer.MAX_VALUE ? -1 : tableEntry.cost);
        }
    }

    // Routes a packet and prints the path
    private static void routePacket(Graph graph, String source, String destination) {
        Map<String, ForwardingTableEntry> forwardingTable = graph.computeForwardingTable(source);

        if (!forwardingTable.containsKey(destination) || forwardingTable.get(destination).cost == Integer.MAX_VALUE) {
            System.out.println("No path exists from " + source + " to " + destination);
            return;
        }

        String currentNode = destination;
        LinkedList<String> path = new LinkedList<>();
        path.addFirst(currentNode);

        // Backtrack using the forwarding table to find the path
        while (!currentNode.equals(source)) {
            currentNode = forwardingTable.get(currentNode).nextHop;
            path.addFirst(currentNode);
        }

        System.out.println("Packet Path: " + String.join(" -> ", path));
    }
}

// Represents a forwarding table entry
class ForwardingTableEntry {
    String nextHop;
    int cost;

    ForwardingTableEntry(String nextHop, int cost) {
        this.nextHop = nextHop;
        this.cost = cost;
    }
}

// Graph class to represent the network
class Graph {
    private final Map<String, List<Edge>> nodes = new HashMap<>();

    public void addNextHop(String from, String to, int weight) {
        nodes.computeIfAbsent(from, k -> new ArrayList<>()).add(new Edge(to, weight));
        nodes.computeIfAbsent(to, k -> new ArrayList<>()).add(new Edge(from, weight));
    }

    public void removeEdge(String from, String to) {
        nodes.getOrDefault(from, new ArrayList<>()).removeIf(edge -> edge.node.equals(to));
        nodes.getOrDefault(to, new ArrayList<>()).removeIf(edge -> edge.node.equals(from));
        System.out.println("Removed edge between " + from + " and " + to);
    }

    public Map<String, ForwardingTableEntry> computeForwardingTable(String start) {
        Map<String, Integer> distance = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<NodeDistancePair> pq = new PriorityQueue<>();

        // Initialize distance and previous maps
        for (String node : nodes.keySet()) {
            distance.put(node, Integer.MAX_VALUE);
            previous.put(node, null);
        }
        distance.put(start, 0);
        pq.add(new NodeDistancePair(0, start));

        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            NodeDistancePair current = pq.poll();
            String currentNode = current.node;

            if (visited.contains(currentNode)) {
                continue;
            }

            visited.add(currentNode);

            for (Edge edge : nodes.getOrDefault(currentNode, Collections.emptyList())) {
                String nextHop = edge.node;
                int weight = edge.weight;
                int newDist = distance.get(currentNode) + weight;

                if (newDist < distance.get(nextHop)) {
                    distance.put(nextHop, newDist);
                    previous.put(nextHop, currentNode);
                    pq.add(new NodeDistancePair(newDist, nextHop));
                }
            }
        }

        // Build the forwarding table
        Map<String, ForwardingTableEntry> forwardingTable = new HashMap<>();
        for (String node : nodes.keySet()) {
            String nextHop = computeNextHop(node, previous, start);
            int cost = distance.get(node);
            forwardingTable.put(node, new ForwardingTableEntry(nextHop, cost));
        }

        return forwardingTable;
    }

    // Computes the next hop for a given destination
    private String computeNextHop(String destination, Map<String, String> previous, String start) {  
    	   String current = destination;  
    	  
    	   // If no valid path exists, return null  
    	   if (previous.get(current) == null || current.equals(start)) {  
    	      return null;  
    	   }  
    	  
    	   // Backtrack to find the next hop  
    	   while (previous.get(current) != null && previous.get(current) != start) {  
    	      current = previous.get(current);  
    	   }  
    	   return previous.get(current);  
    	}

    public Set<String> getNodes() {
        return nodes.keySet();
    }

    private static class Edge {
        String node;
        int weight;

        Edge(String node, int weight) {
            this.node = node;
            this.weight = weight;
        }
    }

    private static class NodeDistancePair implements Comparable<NodeDistancePair> {
        int distance;
        String node;

        NodeDistancePair(int distance, String node) {
            this.distance = distance;
            this.node = node;
        }

        @Override
        public int compareTo(NodeDistancePair other) {
            return Integer.compare(this.distance, other.distance);
        }
    }
}
