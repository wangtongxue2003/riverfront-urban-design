package NSGAII;

import Spacesyntax.Spacesyntax_00;
import igeo.ICurve;
import igeo.IPoint;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.*;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static Spacesyntax.Spacesyntax_00.*;
import static Spacesyntax.dataTransfer.*;

public class Plot {
    private LineString boundary;  // 封闭多边形的边界
    private LineString rideg; // 中间的LineString
    private double location;  // LineString2上的位置
    private double r;  // 左右扩展的距离
    private Polygon polygon;          // Plot的四边形
    private final LineString openEdge;   // 开敞边
    private final GeometryFactory gf = new GeometryFactory();
    private final Coordinate[] coordinates;
    private WB_Polygon wbPolygon;
    private int index;
    private final List<LineString> ridegs;


    public Plot(Polygon plot, List<LineString> ridegs) {
        this.polygon = plot;
        this.openEdge = getopenEdge(ridegs);
        this.coordinates = plot.getCoordinates();
        this.ridegs = ridegs;
    }

    // 构造函数，初始化必要的参数
    public Plot(LineString boundary, LineString rideg, double location, double r, List<LineString> ridegs) {
        this.boundary = boundary;
        this.rideg = rideg;
        this.location = location;
        this.r = r;
        this.wbPolygon = this.calculatePlot();
        this.polygon = WB_PolygonToPolygon(wbPolygon);
        this.openEdge = getopenEdge(ridegs);
        this.coordinates = polygon.getCoordinates();
        this.ridegs = ridegs;
    }

    public Plot(LineString boundary, LineString rideg, double point1, double point2, List<LineString> ridegs, int plotnum) {
        this.boundary = boundary;
        this.rideg = rideg;
        this.location = (point1 + point2) / 2;
        this.r = (point2 - point1) / 2;
        this.wbPolygon = this.calculatePlot();
        this.polygon = WB_PolygonToPolygon(wbPolygon);
        this.openEdge = getopenEdge(ridegs);
        this.coordinates = polygon.getCoordinates();
        this.ridegs = ridegs;
    }

    //set&get
    public WB_Polygon getWB_Polygon() {return wbPolygon;}
    public Polygon getPlotpolygon() {return polygon;}
    public LineString getOpenEdge() {return openEdge;}

    // 计算Plot的核心方法
    public WB_Polygon calculatePlot() {
        // 1. 计算LineString2上两个点P1和P2
        double totalLength = rideg.getLength();

        // 确保location和r在合理范围内
        double locStart = Math.max(0, Math.min(totalLength, location - r));
        double locEnd = Math.max(0, Math.min(totalLength, location + r));

        // 计算P1和P2
        Coordinate P1 = getPointOnLine(rideg, locStart);
        Coordinate P2 = getPointOnLine(rideg, locEnd);

        // 2. 计算P1和P2的垂线，找到与封闭多边形的交点
        List<Coordinate> D1D2 = findIntersectionsWithBoundary(P1);
        List<Coordinate> D3D4 = findIntersectionsWithBoundary(P2);


        List<Coordinate> allCoordinates = new ArrayList<>();
        allCoordinates.addAll(D1D2);
        allCoordinates.addAll(D3D4);

        // 3. 使用ConvexHull计算凸包
        GeometryFactory geomFactory = new GeometryFactory();
        Geometry geom = geomFactory.createGeometryCollection(allCoordinates.stream()
                .map(coord -> geomFactory.createPoint(coord))
                .toArray(Geometry[]::new));

        ConvexHull convexHull = new ConvexHull(geom);
        Polygon hull = (Polygon) convexHull.getConvexHull();

        // 将计算得到的凸包转换为WB_Polygon
        List<WB_Point> points = new ArrayList<>();
        for (Coordinate coord : hull.getCoordinates()) {
            points.add(new WB_Point(coord.x, coord.y));
        }

        return new WB_Polygon(points.toArray(new WB_Point[0]));
    }


    // 根据给定的location计算LineString2上的点
    private Coordinate getPointOnLine(LineString line, double location) {
        // 使用线段的比例来插值计算指定位置的点
        Coordinate[] coords = line.getCoordinates();
        double lengthSoFar = 0.0;

        for (int i = 0; i < coords.length - 1; i++) {
            Coordinate start = coords[i];
            Coordinate end = coords[i + 1];
            double segmentLength = start.distance(end);

            if (lengthSoFar + segmentLength >= location) {
                // 在该段中找到了对应的点
                double segmentRatio = (location - lengthSoFar) / segmentLength;
                double x = start.x + segmentRatio * (end.x - start.x);
                double y = start.y + segmentRatio * (end.y - start.y);
                return new Coordinate(x, y);
            }
            lengthSoFar += segmentLength;
        }
        return coords[coords.length - 1];  // 如果找不到，返回最后一个点
    }

    // 查找垂线与封闭多边形的交点
    private List<Coordinate> findIntersectionsWithBoundary(Coordinate point) {
        List<Coordinate> intersectionPoints = new ArrayList<>();
        GeometryFactory geomFactory = new GeometryFactory();

        // 获取point在LineString2上所在的线段
        LineSegment lineSegment = getLineSegmentContainingPoint(point);

        // 创建一条从point出发的垂线
        LineString verticalLine = createPerpendicularLine(point, lineSegment);

        // 遍历多边形的每一条边，查找交点
        for (int i = 0; i < boundary.getNumPoints() - 1; i++) {
            Coordinate start = boundary.getCoordinates()[i];
            Coordinate end = boundary.getCoordinates()[i + 1];
            LineString boundaryLine = geomFactory.createLineString(new Coordinate[]{start, end});

            // 如果垂线与边界相交，则计算交点
            if (verticalLine.intersects(boundaryLine)) {
                Geometry intersection = verticalLine.intersection(boundaryLine);
                if (!intersection.isEmpty()) {
                    intersectionPoints.addAll(Arrays.asList(intersection.getCoordinates()));
                }
            }
        }
        return intersectionPoints;  // 返回交点列表
    }

    // 获取包含point的线段
    private LineSegment getLineSegmentContainingPoint(Coordinate point) {
        Coordinate[] coords = rideg.getCoordinates();
        for (int i = 0; i < coords.length - 1; i++) {
            Coordinate start = coords[i];
            Coordinate end = coords[i + 1];
            if (isPointOnSegment(start, end, point)) {
                return new LineSegment(start, end);
            }
        }
        return null;  // 如果没有找到，返回null
    }

    // 判断点是否在给定的线段上
    private boolean isPointOnSegment(Coordinate start, Coordinate end, Coordinate point) {
        double crossProduct = (point.y - start.y) * (end.x - start.x) - (point.x - start.x) * (end.y - start.y);
        if (Math.abs(crossProduct) > 1e-6) return false;  // 判断是否在同一条直线上

        double dotProduct = (point.x - start.x) * (end.x - start.x) + (point.y - start.y) * (end.y - start.y);
        if (dotProduct < 0) return false;  // 判断是否在起点和终点之间

        double squaredLength = (end.x - start.x) * (end.x - start.x) + (end.y - start.y) * (end.y - start.y);
        if (dotProduct > squaredLength) return false;  // 判断是否在终点和起点之间

        return true;
    }

    // 创建与线段垂直的线
    private LineString createPerpendicularLine(Coordinate point, LineSegment lineSegment) {
        // 计算线段的方向向量
        double dx = lineSegment.p1.x - lineSegment.p0.x;
        double dy = lineSegment.p1.y - lineSegment.p0.y;

        double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0) throw new IllegalArgumentException("A ridge segment must have positive length");
        double ratio = 1.0 / length;

        // 垂直于线段的方向
        double perpendicularDx = -dy * ratio;
        double perpendicularDy = dx * ratio;

        // 确定垂线的两个端点，假设延长线的长度为10
        Coordinate start = new Coordinate(point.x + 100*perpendicularDx, point.y + 100*perpendicularDy);
        Coordinate end = new Coordinate(point.x - 100*perpendicularDx, point.y - 100*perpendicularDy);

        return new GeometryFactory().createLineString(new Coordinate[]{start, end});
    }

    // 获取计算出的Plot
    public WB_Polygon getPolygon() {
        return calculatePlot();
    }

    //获取开敞边
    public LineString getopenEdge(List<LineString> ridegs) {
        Coordinate[] coordinates = polygon.getCoordinates();
        index = getopenEdgeindex(ridegs);
        return gf.createLineString(new Coordinate[]{coordinates[index],coordinates[(index+1)%4]});
    }

    //获取开敞边的索引
    public int getopenEdgeindex(List<LineString> ridegs) {
        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coordinates = polygon.getCoordinates();
        Coordinate[] midPoints = new Coordinate[4];
        midPoints[0] = new Coordinate((coordinates[0].x+coordinates[1].x)/2,(coordinates[0].y+coordinates[1].y)/2,0);
        midPoints[1] = new Coordinate((coordinates[1].x+coordinates[2].x)/2,(coordinates[1].y+coordinates[2].y)/2,0);
        midPoints[2] = new Coordinate((coordinates[2].x+coordinates[3].x)/2,(coordinates[2].y+coordinates[3].y)/2,0);
        midPoints[3] = new Coordinate((coordinates[3].x+coordinates[0].x)/2,(coordinates[3].y+coordinates[0].y)/2,0);

        Point[] points = new Point[4];
        for (int i = 0; i < 4; i++) {
            points[i] = gf.createPoint(midPoints[i]);
        }

        int index = 0;
        double maxDistance = 0;
        for (int i = 0; i < coordinates.length -1; i++) {
            double mindist = Double.MAX_VALUE;
            for (LineString line : ridegs) {
                double dist = line.distance(points[i]);
                if (dist < mindist) {mindist = dist;}
            }
            if (mindist > maxDistance) {
                maxDistance = mindist;
                index = i;
            }
        }
        return (index+2)%4;
    }

    public double calculateOpenness(Coordinate point) {
        Coordinate P1 = openEdge.getCoordinates()[0];
        Coordinate P2 = openEdge.getCoordinates()[1];

        // 计算向量 PP1 和 PP2
        double dx1 = point.x - P1.x;
        double dy1 = point.y - P1.y;
        double dx2 = point.x - P2.x;
        double dy2 = point.y - P2.y;

        // 计算点积（PP1 · PP2）
        double dotProduct = dx1 * dx2 + dy1 * dy2;

        // 计算模长（|PP1| 和 |PP2|）
        double magnitude1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
        double magnitude2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);

        // 计算夹角的余弦值
        double cosAngle = dotProduct / (magnitude1 * magnitude2);

        // 限制余弦值范围，避免误差
        cosAngle = Math.min(Math.max(cosAngle, -1.0), 1.0);

        // 通过反余弦计算角度（以度为单位）
        double angle = Math.toDegrees(Math.acos(cosAngle));

        return angle;
    }



    // 计算地块的开敞度：所有点的角度总和除以点数
    public double calculateOpenness(double distance) {
        // 获取地块的所有点
        List<Coordinate> points = generatePointsByDistance(distance); // 假设生成 10x10 格点
        double totalAngle = 0;
        int numPoints = points.size();

        // 对每个点计算角度并累加
        for (Coordinate point : points) {
            totalAngle += calculateOpenness(point);
        }

        // 计算并返回平均角度
        return totalAngle / numPoints;
    }


    // 根据给定的间距生成规律排列的点，并检查每个点是否在多边形内部
    public List<Coordinate> generatePointsByDistance(double distance) {
        List<Coordinate> points = new ArrayList<>();
        Envelope envelope = polygon.getEnvelopeInternal();

        // 计算X和Y方向需要的点数
        int cols = (int) Math.floor((envelope.getMaxX() - envelope.getMinX()) / distance);
        int rows = (int) Math.floor((envelope.getMaxY() - envelope.getMinY()) / distance);

        // 计算每个点的间距
        double xStep = (envelope.getMaxX() - envelope.getMinX()) / cols;
        double yStep = (envelope.getMaxY() - envelope.getMinY()) / rows;

        // 遍历每行和每列来生成点
        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j <= cols; j++) {
                double x = envelope.getMinX() + j * xStep;
                double y = envelope.getMinY() + i * yStep;

                Coordinate point = new Coordinate(x, y, 0);

                // 判断点是否在多边形内
                if (isPointInPolygon(point)) {
                    points.add(point);
                }
            }
        }

        return points;
    }

    // 生成规律排列的点，并检查每个点是否在多边形内部
    public List<Coordinate> generateRegularPoints(int numPoints) {
        List<Coordinate> points = new ArrayList<>();
        Envelope envelope = polygon.getEnvelopeInternal();

        // 计算点的行数和列数
        int rows = (int) Math.sqrt(numPoints);  // 行数
        int cols = (int) Math.ceil((double) numPoints / rows);  // 列数

        // 遍历每行和每列来生成点
        double xStep = (envelope.getMaxX() - envelope.getMinX()) / (cols - 1);
        double yStep = (envelope.getMaxY() - envelope.getMinY()) / (rows - 1);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double x = envelope.getMinX() + j * xStep;
                double y = envelope.getMinY() + i * yStep;

                Coordinate point = new Coordinate(x, y, 0);

                // 判断点是否在多边形内
                if (isPointInPolygon(point)) {
                    points.add(point);
                }
            }
        }

        return points;
    }

    // 判断点是否在多边形内部
    private boolean isPointInPolygon(Coordinate point) {
        // 使用JTS库的Geometry.contains()方法检查点是否在多边形内
        return polygon.contains(new GeometryFactory().createPoint(point));
    }



    //生成并评估点的开敞度
    public void setOpenEdge(double distance) {
        List<Coordinate> points = this.generatePointsByDistance(distance);
        for (Coordinate point : points) {
            double openness = this.calculateOpenness(point);
            double colorValue =(openness-20)/160;
            new IPoint(point.x,point.y,point.z).clr(0,colorValue, 1 - colorValue);
        }
    }

    //with layer
    public void setOpenEdgewithlayer(double distance, String layer) {
        List<Coordinate> points = this.generatePointsByDistance(distance);
        for (Coordinate point : points) {
            double openness = this.calculateOpenness(point);
            double colorValue =(openness-20)/160;
            new IPoint(point.x,point.y,point.z).clr(0,colorValue, 1 - colorValue).layer(layer);
        }
    }

    //计算plot到每个地块的平均距离
    public double averdist(ICurve[] plotcrvs, List<LineString> ridegs){
        double[] dists = getPlotsDistance(plotcrvs,ridegs);
        int length = dists.length;
        double sum = 0;
        for (int i = 0; i < length; i++) {
            sum += dists[i];
        }
        return sum/length;
    }

    //计算plot到每个地块的距离
    public double[] getPlotsDistance(ICurve[] plotcrvs, List<LineString> ridegs) {
        Coordinate[] coordinates = polygon.getCoordinates();
        int pointindex = this.getopenEdgeindex(ridegs);
        Point[] thispoints = new Point[2];
        thispoints[0] = gf.createPoint(new Coordinate(coordinates[(pointindex+2)%4].x,coordinates[(pointindex+2)%4].y,0));
        thispoints[1] = gf.createPoint(new Coordinate(coordinates[(pointindex+3)%4].x,coordinates[(pointindex+3)%4].y,0));

        List<LineString> newridegs = new ArrayList<>();
        newridegs.addAll(ridegs);

        Point thispoint0 = gf.createPoint(getshortestDistancepoint(thispoints[0],newridegs));
        Point thispoint1 = gf.createPoint(getshortestDistancepoint(thispoints[1],newridegs));
        Coordinate[] newline0 = new Coordinate[]{
                new Coordinate(thispoint0.getX(), thispoint0.getY(), 0),
                new Coordinate(thispoints[0].getX(), thispoints[0].getY(), 0)
        };
        Coordinate[] newline1 = new Coordinate[]{
                new Coordinate(thispoint1.getX(), thispoint1.getY(), 0),
                new Coordinate(thispoints[1].getX(), thispoints[1].getY(), 0)
        };
        LineString newlineString0 = gf.createLineString(newline0);
        LineString newlineString1 = gf.createLineString(newline1);
        newridegs.add(newlineString0);
        newridegs.add(newlineString1);
        newridegs = cuttedLineStrings(newridegs);

        Map<Coordinate, List<Spacesyntax_00.Edge>> graph = buildGraphWithIntersections(newridegs);


        double[] minidist = new double[plotcrvs.length];
        Coordinate[] plotcoords = new Coordinate[plotcrvs.length];
        WB_Polygon[] plotpolygons = new WB_Polygon[plotcrvs.length];
        for (int i = 0; i < plotcrvs.length; i++) {
            plotpolygons[i] = ICurveToWB_Polygon(plotcrvs[i]);
        }
        for(int i = 0; i < plotcrvs.length; i++) {
            WB_Point p = plotpolygons[i].getCenter();
            plotcoords[i] = new Coordinate(p.xd(),p.yd(), 0);
            double minidistance = findShortestPath(graph,coordinates[(pointindex+2)%4], plotcoords[i]);
            double distance = findShortestPath(graph,coordinates[(pointindex+3)%4], plotcoords[i]);
            if (distance < minidistance) {minidistance = distance;}
            minidist[i] = minidistance;
        }
        return minidist;
    }

    //将地块距离可视化
    public void plotVisiable(ICurve[] plotcrvs, List<LineString> ridegs, String layer) {
        double[] minidist = getPlotsDistance(plotcrvs,ridegs);
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

        WB_Polygon[] plotpolygons = new WB_Polygon[plotcrvs.length];
        for (int i = 0; i < plotcrvs.length; i++) {
            plotpolygons[i] = ICurveToWB_Polygon(plotcrvs[i]);
        }

        for (int j = 0; j < plotcrvs.length; j++) {
            drawWB_Polygonwithclr(plotpolygons[j], (minidist[j]-min_minidist)/(max_minidist-min_minidist), (max_minidist-minidist[j])/(max_minidist-min_minidist), 0, layer);
        }

    }

    public double plotarea(){
        return polygon.getArea();
    }
}