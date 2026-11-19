package Spacesyntax;

import igeo.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.distance.DistanceOp;
import processing.core.PApplet;
import Zshare.Skeleton.ZSkeleton_share;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.hemesh.HE_Mesh;
import wblut.hemesh.HE_Path;
import wblut.hemesh.HE_Vertex;

import java.util.*;

import static Spacesyntax.getSkeleton.drawEdgesWithIGEO;
import static Spacesyntax.getSkeleton.drawRigesWithIGEO;
import static Spacesyntax.dataTransfer.*;

public class Spacesyntax_00 extends PApplet {
    ZSkeleton_share skeletonExtractor;

    public void settings(){
        size(1300, 900, IG.GL);
    }

    public static void main(String[] args) {
        PApplet.main("Spacesyntax.Spacesyntax_00");
    }

    public void setup() {
        GeometryFactory gf = new GeometryFactory();
        //读取Rhino文件
        basic.ProjectPaths.open("private-data/1211-4.3dm");

        //读取最公共区域点
        IPoint[] pts = IG.layer("pnts").points(); //getting points
        Point[] pubpnts = new Point[pts.length];
        Point[] pubshortestpnts = new Point[pts.length];



        //读取plotcrvs
        ICurve[] plotcrvs = IG.layer("plotcrvs").curves(); //getting curves

        Point[] centralpoints = new Point[plotcrvs.length];
        Point[] shortestpoints = new Point[plotcrvs.length];
        WB_Point[][] plotpoints = new WB_Point[plotcrvs.length][];
        WB_Polygon[] plotpolygons = new WB_Polygon[plotcrvs.length];

        for (int i = 0; i < plotcrvs.length; i++) {
            plotpoints[i] = ICurveToWB_Point(plotcrvs[i]);
            plotpolygons[i] = ICurveToWB_Polygon(plotcrvs[i]);
        }

        //读取crvs
        ICurve[] crvs = IG.layer("crvs").curves(); //getting curves

        WB_Point[][] points = new WB_Point[crvs.length][];
        for(int i = 0; i < crvs.length; i++) {
            points[i] = ICurveToWB_Point(crvs[i]);
        }

        //读取innercrvs
        ICurve[] innercrvs = IG.layer("innercrvs").curves(); //getting curves

        WB_Point[][] innerpoints = new WB_Point[innercrvs.length][];
        for (int i = 0; i < innercrvs.length; i++){
            innerpoints[i] = ICurveToWB_Point(innercrvs[i]);
        }

        WB_Polygon[] polygons = new WB_Polygon[crvs.length];
        for(int i = 0; i < crvs.length; i++){
            polygons[i] = new WB_Polygon(points[i], innerpoints);

            skeletonExtractor = new ZSkeleton_share(polygons[i]);
            List<LineString> ridegs = skeletonExtractor.getRidges();
            List<WB_Polygon> polygonslist = skeletonExtractor.getAllFacePolysWB();
            List<LineString> newridegs = new ArrayList<LineString>();
            newridegs.addAll(ridegs);

            // 打印提取的所有边界
            List<LineString> allEdges = skeletonExtractor.getAllEdges();
            println("Number of edges: " + allEdges.size());

            //针对每一个公共点进行运算
            for (int k = 0; k < pts.length; k++) {
                pubpnts[k] = gf.createPoint(new Coordinate(pts[k].x(), pts[k].y(), pts[k].z()));
                pubshortestpnts[k] = gf.createPoint(getshortestDistancepoint(pubpnts[k],ridegs));
                /*new ICurve(pubshortestpnts[k].getX(), pubshortestpnts[k].getY(), 0, pubpnts[k].getX(), pubpnts[k].getY(), 0);*/
                Coordinate[] newline = new Coordinate[]{
                        new Coordinate(pubshortestpnts[k].getX(), pubshortestpnts[k].getY(), 0),
                        new Coordinate(pubpnts[k].getX(), pubpnts[k].getY(), 0)
                };
                LineString newlineString = gf.createLineString(newline);
                newridegs.add(newlineString);  //给newridegs添加新生成的ridegs
            }

            //针对每一个plot进行运算
            for (int j = 0; j < plotcrvs.length; j++) {
                WB_Point p = plotpolygons[j].getCenter();
                centralpoints[j] = gf.createPoint(new Coordinate(p.xd(),p.yd(), 0));
                shortestpoints[j] = gf.createPoint(getshortestDistancepoint(centralpoints[j],ridegs));
               /* new IPoint(shortestpoints[j].getX(), shortestpoints[j].getY(), 0);*/
               /* new ICurve(shortestpoints[j].getX(), shortestpoints[j].getY(), 0, centralpoints[j].getX(), centralpoints[j].getY(), 0);*/
                Coordinate[] newline = new Coordinate[]{
                    new Coordinate(shortestpoints[j].getX(), shortestpoints[j].getY(), 0),
                    new Coordinate(centralpoints[j].getX(), centralpoints[j].getY(), 0)
                };
                LineString newlineString = gf.createLineString(newline);
                newridegs.add(newlineString);  //给newridegs添加新生成的ridegs
            }

           /* //计算每个地块距离并绘制
            double[] minidist = new double[plotcrvs.length];
            HE_Mesh mesh = skeletonExtractor.getSkeletonMesh();

            System.out.println(shortestpoints[0].getX() + " " + pubshortestpnts[0].getY() + " " + mesh.getNumberOfFaces());

            for (int j = 0; j < plotcrvs.length; j++) {
                minidist[j] = getspacedistance(centralpoints[j],shortestpoints[j],pubpnts[0],pubshortestpnts[0],mesh);
                for (int k = 0; k < pts.length; k++) {
                    double dist = getspacedistance(centralpoints[j],shortestpoints[j],pubpnts[k],pubshortestpnts[k],mesh);
                    if (dist < minidist[j]) {
                        minidist[j] = dist;
                    }
                }
                drawWB_Polygonwithclr(plotpolygons[i],0,0,minidist[i]/4);
            }
*/

            /*// 将LineStrings节点化：将交叉部分切割成多个小段
            Geometry nodedLineStrings = newridegs.get(0);  // 第一个LineString
            for (int j = 1; j < newridegs.size(); j++) {
                nodedLineStrings = nodedLineStrings.union(newridegs.get(j));
            }*/

            //Point -> Coordinate
            Coordinate[] shortestcoords = new Coordinate[plotcrvs.length];
            for (int j = 0; j < plotcrvs.length; j++){
                shortestcoords[j] = new Coordinate(shortestpoints[j].getX(), shortestpoints[j].getY(), 0);
            }
            Coordinate[] pubshortestcoords = new Coordinate[pts.length];
            for (int j = 0; j < pts.length; j++){
                pubshortestcoords[j] = new Coordinate(pubshortestpnts[j].getX(), pubshortestpnts[j].getY(), 0);
            }
            newridegs =cutLineStringsByPoints(newridegs,shortestcoords);
            newridegs =cutLineStringsByPoints(newridegs,pubshortestcoords);

            for (LineString lineString : newridegs) {
                System.out.println(lineString.getNumPoints());
                Coordinate[] coordinates = lineString.getCoordinates();
             /*   new ICurve(coordinates[0].x,coordinates[0].y,0,coordinates[1].x,coordinates[1].y,0).layer("edegs");*/
            }

            // 构建图并添加交点
            Map<Coordinate, List<Edge>> graph = buildGraphWithIntersections(newridegs);

            double[] minidist = new double[plotcrvs.length];
            for (int j = 0; j < plotcrvs.length; j++) {
                Coordinate p1 = new Coordinate(centralpoints[j].getX(), centralpoints[j].getY(), 0);
                Coordinate p2 = new Coordinate(pubshortestpnts[0].getX(), pubshortestpnts[0].getY(), 0);
                minidist[j] = findShortestPath(graph, p1, p2);
                for (int k = 0; k < pts.length; k++) {
                    p2 = new Coordinate(pubshortestpnts[k].getX(), pubshortestpnts[k].getY(), 0);
                    double dist = findShortestPath(graph, p1, p2);
                    if (dist < minidist[j]) {
                        minidist[j] = dist;
                    }
                }
            }

            //可视化
            double max_minidist = minidist[0];
            double min_minidist = minidist[0];
            for (int j = 0; j < plotcrvs.length; j++) {
                if (max_minidist < minidist[j]) {
                    max_minidist = minidist[j];
                }
                if (min_minidist > minidist[j]) {
                    min_minidist = minidist[j];
                }
            }

            for (int j = 0; j < plotcrvs.length; j++) {
                drawWB_Polygonwithclr(plotpolygons[j], (minidist[j]-min_minidist)/(max_minidist-min_minidist), (max_minidist-minidist[j])/(max_minidist-min_minidist), 0, "liveplot");
                System.out.println(minidist[j]);
            }

            for (WB_Polygon p : polygonslist){
                drawWB_Polygon(p);
            }

            // 使用IGEO绘制边界
            drawRigesWithIGEO(newridegs, "newridegs");
            drawEdgesWithIGEO(allEdges);
        }


        ISurface[] srfs = IG.layer("01").surfaces(); //getting surfaces

        IMesh[] meshes = IG.layer("01").meshes(); //getting polygon meshes

        IBrep[] breps = IG.layer("01").breps(); //getting BReps

       /* basic.ProjectPaths.save("output/test_output_file.3dm");*/
    }

    //计算点到riges的最短距离,并返回geom2上的点
    public static Coordinate getshortestDistancepoint(Point geom1, List<LineString> geom2) {

        double shortestDistance = Double.MAX_VALUE;
        Coordinate closestPoint = null;

        for (LineString geom : geom2) {
            DistanceOp distanceOp = new DistanceOp(geom1, geom);
            double distance = distanceOp.distance();
            if (distance < shortestDistance) {
                shortestDistance = distance;
                closestPoint = distanceOp.nearestPoints()[1];
            }
        }
        return closestPoint;
    }


    //计算plot的距离
    public static double getspacedistance(Point plotpnt, Point plotpnt1, Point pubpnt, Point pubpnt1, HE_Mesh mesh) {
        double d1;
        double d2;
        double d3;
        HE_Path hePath;
        d1 = plotpnt.distance(plotpnt1);
        d2 = pubpnt.distance(pubpnt1);
        HE_Vertex plot = new HE_Vertex(plotpnt1.getX(), plotpnt1.getY(), 0);
        HE_Vertex pub = new HE_Vertex(pubpnt1.getX(), pubpnt1.getY(), 0);

        hePath = HE_Path.getShortestPath(plot,pub,mesh);
        d3 = hePath.getPathLength();

        return d1 + d2 + d3;
    }

    // 计算最短路径的核心方法
    public static double findShortestPath(List<LineString> lines, Coordinate start, Coordinate end) {
        return findShortestPath(buildGraphWithIntersections(lines), start, end);
    }

    // 深度优先搜索（DFS）来遍历所有路径
    private static void dfs(Map<Coordinate, List<Edge>> graph, Coordinate current, Coordinate end, Set<Coordinate> visited,
                            List<Coordinate> currentPath, List<List<Coordinate>> allPaths) {
        visited.add(current);
        currentPath.add(current);

        // 如果当前节点是终点，记录路径
        if (current.equals(end)) {
            allPaths.add(new ArrayList<>(currentPath));
        } else {
            // 遍历邻接节点
            for (Edge edge : graph.get(current)) {
                Coordinate neighbor = edge.to;
                if (!visited.contains(neighbor)) {
                    dfs(graph, neighbor, end, visited, currentPath, allPaths);
                }
            }
        }

        // 回溯
        currentPath.remove(currentPath.size() - 1);
        visited.remove(current);
    }

    // 计算路径的总距离
    private static double calculatePathDistance(List<Coordinate> path) {
        double totalDistance = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalDistance += path.get(i).distance(path.get(i + 1));
        }
        return totalDistance;
    }

    // 构建图并加入交点
    public static Map<Coordinate, List<Edge>> buildGraphWithIntersections(List<LineString> lines) {
        Map<Coordinate, List<Edge>> graph = new HashMap<>();
        for (LineString line : getNodedLineStrings(lines)) {
            Coordinate[] coords = line.getCoordinates();
            for (int i = 1; i < coords.length; i++) {
                Coordinate start = canonicalCoordinate(coords[i - 1]);
                Coordinate end = canonicalCoordinate(coords[i]);
                double length = start.distance(end);
                if (length == 0) continue;
                graph.computeIfAbsent(start, k -> new ArrayList<>()).add(new Edge(end, length));
                graph.computeIfAbsent(end, k -> new ArrayList<>()).add(new Edge(start, length));
            }
        }
        return graph;
    }

    /** Consistent 2D keys; tolerance is one millionth of the input coordinate unit. */
    public static Coordinate canonicalCoordinate(Coordinate point) {
        return new Coordinate(Math.rint(point.x * 1e6) / 1e6, Math.rint(point.y * 1e6) / 1e6, 0);
    }

    // Dijkstra 算法计算最短路径
    public static double findShortestPath(Map<Coordinate, List<Edge>> graph, Coordinate start, Coordinate end) {
        start = canonicalCoordinate(start);
        end = canonicalCoordinate(end);
        if (!graph.containsKey(start) || !graph.containsKey(end)) return Double.MAX_VALUE;
        Map<Coordinate, Double> distances = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));
        distances.put(start, 0.0);
        queue.add(new Node(start, 0));
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.distance > distances.getOrDefault(current.coordinate, Double.MAX_VALUE)) continue;
            if (current.coordinate.equals2D(end)) return current.distance;
            for (Edge edge : graph.get(current.coordinate)) {
                double distance = current.distance + edge.weight;
                if (distance < distances.getOrDefault(edge.to, Double.MAX_VALUE)) {
                    distances.put(edge.to, distance);
                    queue.add(new Node(edge.to, distance));
                }
            }
        }
        return Double.MAX_VALUE;
    }

    // 图中的边
    public static class Edge {
        Coordinate to;
        double weight;

        Edge(Coordinate to, double weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    // 图中的节点
    private static class Node {
        Coordinate coordinate;
        double distance;

        Node(Coordinate coordinate, double distance) {
            this.coordinate = coordinate;
            this.distance = distance;
        }
    }

    public static List<LineString> getNodedLineStrings(List<LineString> lines) {
        List<Geometry> normalized = new ArrayList<>();
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(1e6));
        for (LineString line : lines) {
            Coordinate[] points = Arrays.stream(line.getCoordinates())
                    .map(Spacesyntax_00::canonicalCoordinate).toArray(Coordinate[]::new);
            LineString normalizedLine = gf.createLineString(points);
            if (!normalizedLine.isEmpty() && normalizedLine.getLength() > 0) normalized.add(normalizedLine);
        }
        List<LineString> result = new ArrayList<>();
        if (normalized.isEmpty()) return result;
        Geometry union = org.locationtech.jts.operation.union.UnaryUnionOp.union(normalized);
        collectLineStrings(union, result);
        return result;
    }

    private static void collectLineStrings(Geometry geometry, List<LineString> result) {
        if (geometry instanceof LineString line) result.add(line);
        else if (geometry instanceof GeometryCollection collection) {
            for (int i = 0; i < collection.getNumGeometries(); i++) collectLineStrings(collection.getGeometryN(i), result);
        }
    }

    public static List<LineString> cuttedLineStrings(List<LineString> lines) {
        return getNodedLineStrings(lines);
    }

    public static List<LineString> cutLineStringsByPoints(List<LineString> lines, Coordinate[] points) {
        List<LineString> result = new ArrayList<>();
        for (LineString line : lines) {
            org.locationtech.jts.linearref.LengthIndexedLine indexed = new org.locationtech.jts.linearref.LengthIndexedLine(line);
            SortedSet<Double> positions = new TreeSet<>();
            positions.add(0.0);
            positions.add(line.getLength());
            for (Coordinate point : points) {
                double index = indexed.project(point);
                if (indexed.extractPoint(index).distance(point) <= 1e-6) positions.add(index);
            }
            Double previous = null;
            for (double position : positions) {
                if (previous != null && position - previous > 1e-9) {
                    Geometry section = indexed.extractLine(previous, position);
                    collectLineStrings(section, result);
                }
                previous = position;
            }
        }
        return getNodedLineStrings(result);
    }


    // 检查点是否在LineString上
    private static boolean isPointOnLine(LineString line, Coordinate point) {
        GeometryFactory gf = new GeometryFactory();
        // 判断点是否在LineString上
        Point pnt = gf.createPoint(point);
        if (new DistanceOp(pnt,line).distance() < 0.001) return true;
        return false;
    }

    // 判断点是否为LineString的端点
    private static boolean isEndpointOfLine(LineString line, Coordinate point) {
        Coordinate[] coords = line.getCoordinates();
        return point.equals(coords[0]) || point.equals(coords[coords.length - 1]);
    }

    // 在给定的坐标列表中找到合适的插入位置
    private static int findInsertionIndex(List<Coordinate> coordinates, Coordinate point) {
        // 查找一个位置，在坐标顺序上将新点插入合适的位置
        for (int i = 1; i < coordinates.size(); i++) {
            if (coordinates.get(i - 1).distance(point) < coordinates.get(i).distance(point)) {
                return i;
            }
        }
        return coordinates.size();  // 将点添加到末尾
    }

    // 使用坐标列表创建多个新的LineString，确保每个LineString只有两个端点
    private static List<LineString> createSubLineStrings(List<Coordinate> coordinates) {
        List<LineString> subLineStrings = new ArrayList<>();

        for (int i = 1; i < coordinates.size(); i++) {
            Coordinate[] subCoords = {coordinates.get(i - 1), coordinates.get(i)};
            subLineStrings.add(new GeometryFactory().createLineString(subCoords));
        }

        return subLineStrings;
    }

    public static Coordinate getRandomPointOnLineString(LineString lineString) {
        // 获取LineString的长度
        double length = lineString.getLength();

        // 生成一个在 [0, length] 范围内的随机数
        Random random = new Random();
        double randomDistance = random.nextDouble() * length;

        // 获取LineString上的点，沿着随机距离的参数化位置
        Coordinate randomCoordinate = lineString.getCoordinateN(0); // 获取第一个坐标点
        Coordinate secondCoordinate = lineString.getCoordinateN(1); // 获取第二个坐标点

        // 计算两点之间的向量
        double dx = secondCoordinate.x - randomCoordinate.x;
        double dy = secondCoordinate.y - randomCoordinate.y;

        // 使用参数化的方式计算新的点的坐标
        double t = randomDistance / length;
        double randomX = randomCoordinate.x + t * dx;
        double randomY = randomCoordinate.y + t * dy;

        return new Coordinate(randomX, randomY);
    }

    public static Coordinate getmidPointOnLineString(LineString lineString) {

        double x1 = lineString.getCoordinateN(0).x;
        double y1 = lineString.getCoordinateN(0).y;
        double x2 = lineString.getCoordinateN(1).x;
        double y2 = lineString.getCoordinateN(1).y;

        double x0 = (x1+x2)/2;
        double y0 = (y1+y2)/2;

        return new Coordinate(x0, y0);
    }

}
