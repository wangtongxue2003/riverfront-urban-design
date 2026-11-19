package Spacesyntax;

import igeo.ICurve;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import java.util.*;

import static Spacesyntax.dataTransfer.ICurveToLineString;

public class Graph {
    private Map<Coordinate, List<Edge>> graph = new HashMap<>();


    public Graph() {
        this.graph = new HashMap<>();
    }

    public Graph(ICurve[] iedges) {
        List<LineString> edgeslist = new ArrayList<>();
        for (ICurve curve : iedges) {
            edgeslist.add(ICurveToLineString(curve));
        }
        buildGraphWithIntersections(edgeslist);
    }

    public Graph(List<LineString> edgeslist) {

        buildGraphWithIntersections(edgeslist);
    }

    // 获取图
    public Map<Coordinate, List<Edge>> getGraph() {
        return this.graph;
    }

    // 添加边到图中
    public void addEdge(Coordinate start, Coordinate end, double weight) {
        start = Spacesyntax_00.canonicalCoordinate(start);
        end = Spacesyntax_00.canonicalCoordinate(end);
        graph.putIfAbsent(start, new ArrayList<>());
        graph.get(start).add(new Edge(end, weight));
        graph.putIfAbsent(end, new ArrayList<>());
        graph.get(end).add(new Edge(start, weight));
    }

    // 计算图中两个点之间的最短路径
    public double findShortestPath(Coordinate start, Coordinate end) {
        start = Spacesyntax_00.canonicalCoordinate(start);
        end = Spacesyntax_00.canonicalCoordinate(end);
        if (!graph.containsKey(start) || !graph.containsKey(end)) return Double.MAX_VALUE;
        // Dijkstra 算法实现
        Map<Coordinate, Double> distances = new HashMap<>();
        Map<Coordinate, Coordinate> previous = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(node -> node.distance));

        for (Coordinate node : graph.keySet()) {
            distances.put(node, Double.MAX_VALUE);
        }
        distances.put(start, 0.0);
        pq.add(new Node(start, 0.0));

        while (!pq.isEmpty()) {
            Node currentNode = pq.poll();
            Coordinate current = currentNode.coordinate;
            double currentDistance = currentNode.distance;

            if (current.equals(end)) {
                return currentDistance;
            }

            for (Edge edge : graph.get(current)) {
                Coordinate neighbor = edge.to;
                double newDist = currentDistance + edge.weight;

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    pq.add(new Node(neighbor, newDist));
                }
            }
        }

        return Double.MAX_VALUE; // 如果无法到达目标点
    }

    // 构建带有交点的图
    public void buildGraphWithIntersections(List<LineString> lines) {
        graph.clear();
        for (LineString line : Spacesyntax_00.getNodedLineStrings(lines)) {
            Coordinate[] coords = line.getCoordinates();
            for (int i = 1; i < coords.length; i++) {
                if (coords[i - 1].distance(coords[i]) > 0) addEdgeToGraph(coords[i - 1], coords[i]);
            }
        }
    }

    // 辅助方法：将交点添加到图中
    private void addEdgeToGraph(Coordinate point) {
        graph.putIfAbsent(point, new ArrayList<>());
    }

    // 辅助方法：将边添加到图中
    private void addEdgeToGraph(Coordinate start, Coordinate end) {
        double distance = start.distance(end);
        addEdge(start, end, distance);
    }

    // Edge类，图中的边
    static class Edge {
        Coordinate to;
        double weight;

        Edge(Coordinate to, double weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    // Node类，Dijkstra算法中的节点
    private static class Node {
        Coordinate coordinate;
        double distance;

        Node(Coordinate coordinate, double distance) {
            this.coordinate = coordinate;
            this.distance = distance;
        }
    }
}