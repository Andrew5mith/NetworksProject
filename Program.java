import java.util.*;

public class Program {
    public static void main(String[] args) {
        Graph graph = new Graph();
        graph.addNextHop("A", "B", 2);
        graph.addNextHop("A", "C", 5);
        graph.addNextHop("A", "D", 1);
        graph.addNextHop("B", "C", 3);
        graph.addNextHop("B", "D", 2);
        graph.addNextHop("C", "E", 1);
        graph.addNextHop("C", "F", 5);
        graph.addNextHop("D", "E", 1);
        graph.addNextHop("E", "F", 2);

        Map<String, Integer> shortestPaths = graph.getShortestPaths("A");

        for (Map.Entry<String, Integer> path : shortestPaths.entrySet()) {
            System.out.println("To " + path.getKey() + ": " + path.getValue());
        }
    }
}

class Graph {
    private final Map<String, List<Edge>> nodes = new HashMap<>();

    public void addNextHop(String from, String to, int weight) {
        nodes.computeIfAbsent(from, k -> new ArrayList<>()).add(new Edge(to, weight));
        nodes.computeIfAbsent(to, k -> new ArrayList<>());
    }

    public Map<String, Integer> getShortestPaths(String start) {
        Map<String, Integer> shortestPaths = new HashMap<>();

        // Initialize shortest paths with "infinity"
        for (String node : nodes.keySet()) {
            shortestPaths.put(node, Integer.MAX_VALUE);
        }

        shortestPaths.put(start, 0);

        PriorityQueue<NodeDistancePair> priorityQueue = new PriorityQueue<>();
        priorityQueue.add(new NodeDistancePair(0, start));

        Set<String> visited = new HashSet<>();

        while (!priorityQueue.isEmpty()) {
            NodeDistancePair current = priorityQueue.poll();
            int currentDistance = current.distance;
            String currentNode = current.node;

            if (visited.contains(currentNode)) {
                continue;
            }

            visited.add(currentNode);

            for (Edge edge : nodes.getOrDefault(currentNode, Collections.emptyList())) {
                String nextHop = edge.node;
                int weight = edge.weight;
                int distance = currentDistance + weight;

                if (distance < shortestPaths.getOrDefault(nextHop, Integer.MAX_VALUE)) {
                    priorityQueue.remove(new NodeDistancePair(shortestPaths.get(nextHop), nextHop));
                    shortestPaths.put(nextHop, distance);
                    priorityQueue.add(new NodeDistancePair(distance, nextHop));
                }
            }
        }

        return shortestPaths;
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

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            NodeDistancePair that = (NodeDistancePair) obj;
            return distance == that.distance && Objects.equals(node, that.node);
        }

        @Override
        public int hashCode() {
            return Objects.hash(distance, node);
        }
    }
}
