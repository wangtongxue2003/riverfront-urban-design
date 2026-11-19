package NSGAII;

import Typologies.Site;
import Typologies.TypeA;
import Typologies.TypeB;
import igeo.ICurve;
import igeo.IGeometry;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;
import wblut.geom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static NSGAII.test01.parallelAverageDistanceFunction;
import static Spacesyntax.dataTransfer.*;

public class Plots {
    private LineString boundary;  // 封闭多边形的边界
    private LineString rideg; // 中间的LineString
    private double[] locations;  // LineString上的位置
    private List<LineString> river;
    private WB_Polygon[] wbPolygons;
    private List<LineString> cutlines;
    private char[] typologies;
    private float[] floor;
    private Polygon[] plotsRectangle;

    public Plots(LineString boundary, LineString rideg, Double[] locations, char[] typologies, float[] floor, List<LineString> river) {
        double[] locs = new double[locations.length];
        for (int i = 0 ; i < locations.length ; i++) {
            locs[i] = locations[i];
        }
        this.boundary = boundary;
        this.rideg = rideg;
        this.locations = locs;
        this.river = river;
        this.typologies = typologies;
        this.floor = floor;
        this.wbPolygons = this.calculatePlots();
        this.cutlines = this.getCutLines();
        this.plotsRectangle = this.getPlotsRectangle();

    }

    public Plots(LineString boundary, LineString rideg, double[] locations, char[] typologies, float[] floor, List<LineString> river) {
        this.boundary = boundary;
        this.rideg = rideg;
        this.locations = locations;
        this.river = river;
        this.typologies = typologies;
        this.floor = floor;
        this.wbPolygons = this.calculatePlots();
        this.cutlines = this.getCutLines();
        System.out.println(cutlines.size());
        this.plotsRectangle = this.getPlotsRectangle();

    }

    //set get
    public WB_Polygon[] getWbPolygons() {return wbPolygons;}
    public List<LineString> getcutlines() {return cutlines;}
    public float getFloor(int index) {return floor[index];}
    public WB_Polygon getWbPolygon(int index) {return wbPolygons[index];}
    public Polygon getPlotRectangle(int index) {return plotsRectangle[index];}
    public List<LineString> getRiver() {return river;}
    public char[] getTypologies() {return typologies;}


    // 计算Plots的核心方法
    public WB_Polygon[] calculatePlots() {
        WB_Polygon[] wbpolygons = new WB_Polygon[locations.length];  //记录生成的多边形的数组
        WB_Polygon wb_boundary = LineStringToWB_Polygon(boundary);
        WB_Point[] locations_coord = new WB_Point[locations.length];
        for (int i = 0; i < locations.length; i++) {
            Coordinate locationi = getPointOnLine(rideg, locations[i]);
            locations_coord[i] = new WB_Point(locationi.x, locationi.y,0);
        }
        List<WB_VoronoiCell2D> voronoiCells = WB_VoronoiCreator.getClippedVoronoi2D(locations_coord, wb_boundary,0).getCells();

        int a = 0;
        for (WB_VoronoiCell2D cell : voronoiCells){
            wbpolygons[a] = cell.getPolygon();
            a++;
        }

        // 创建一个List来存储排序后的多边形
        List<WB_Polygon> sortedPolygons = new ArrayList<>();

        // 创建一个布尔数组来标记哪些多边形已经排序
        boolean[] visited = new boolean[wbpolygons.length];
        for (int i = 0; i < wbpolygons.length; i++) {
            visited[i] = false;
        }
        // 遍历locations_coord的顺序
        for (WB_Point point : locations_coord) {
            // 在wbpolygons中找到包含当前点的多边形
            for (int i = 0; i < wbpolygons.length; i++) {
                if (!visited[i]) {
                    if (isPointInPolygon(point, wbpolygons[i])) {
                        // 将找到的多边形添加到排序后的数组中
                        sortedPolygons.add(wbpolygons[i]);
                        visited[i] = true;  // 标记此多边形已处理
                        break;  // 一旦找到包含点的多边形，跳出内层循环
                    }
                }
            }
        }
        WB_Polygon[] sortedWbPolygons = new WB_Polygon[sortedPolygons.size()];
        sortedPolygons.toArray(sortedWbPolygons);

        return sortedWbPolygons;
    }

    public Polygon[] getPlotsRectangle(){
        Polygon[] plotsrectangle = new Polygon[wbPolygons.length];
        WB_Polygon[] wbplotsrectangle = new WB_Polygon[wbPolygons.length];
        plotsrectangle[0] = getrectanglefrompoint( rideg.getStartPoint(),cutlines.getFirst().getCentroid());
        plotsrectangle[wbPolygons.length-1] = getrectanglefrompoint( cutlines.getLast().getCentroid(),rideg.getEndPoint());
        for (int i = 0; i < wbPolygons.length-2; i++) {

            plotsrectangle[i+1] = getrectanglefromplot(i);

        }
       /* for (int i = 0; i < wbPolygons.length; i++) {
            ZLargestRectangle largestRectangle = new ZLargestRectangle(wbPolygons[i]);
            largestRectangle.cal();
            wbplotsrectangle[i] = largestRectangle.getRectResult_WB();
            plotsrectangle[i] = WB_PolygonToPolygon(wbplotsrectangle[i]);
        }*/
        return plotsrectangle;
    }

    public Polygon getrectanglefrompoint(Point M1, Point M2){
        GeometryFactory gf = new GeometryFactory();
        List<Coordinate> intersectionPoints = new ArrayList<>();

        /*if (M1.distance(M2) < 5){return gf.createPolygon(
                new Coordinate[]{new Coordinate(5,0,0),new Coordinate(5,5,0),new Coordinate(0,5,0),new Coordinate(0,0,0),new Coordinate(5,0,0)});}
      */
        Point O = gf.createPoint(new Coordinate((M1.getX()+M2.getX())/2, (M1.getY()+M2.getY())/2));

        Vector2D OM2 = new Vector2D(M2.getX()-O.getX(),M2.getY()-O.getY());
        Vector2D ON1nor = OM2.rotate(Math.PI/2).normalize();

        Coordinate ON1max = new Coordinate(O.getX() + 1000*ON1nor.getX(), O.getY() + 1000*ON1nor.getY());
        Coordinate ON2max = new Coordinate(O.getX() - 1000*ON1nor.getX(), O.getY() - 1000*ON1nor.getY());

        LineString N2N1 = gf.createLineString(new Coordinate[] {ON1max, ON2max});

        for (int i = 0; i < boundary.getCoordinates().length-1; i++) {
            Coordinate start = boundary.getCoordinates()[i];
            Coordinate end = boundary.getCoordinates()[i+1];
            LineString boundaryline = gf.createLineString(new Coordinate[] {start, end});

            if (N2N1.intersects(boundaryline)) {
                Geometry N = N2N1.intersection(boundaryline);
                if (!N.isEmpty()){intersectionPoints.addAll(Arrays.asList(N.getCoordinates()));}
            }
        }
        Coordinate N1 = intersectionPoints.get(0);
        Coordinate N2 = intersectionPoints.get(1);
        Vector2D ON1 = new Vector2D(N1.x-O.getX(),N1.y-O.getY());
        Vector2D ON2 = new Vector2D(N2.x-O.getX(),N2.y-O.getY());

        Coordinate[] rectangleCoordinate = new Coordinate[5];
        rectangleCoordinate[0] = new Coordinate(O.getX() + OM2.getX() + ON1.getX(), O.getY() + OM2.getY() + ON1.getY(), 0);
        rectangleCoordinate[1] = new Coordinate(O.getX() - OM2.getX() + ON1.getX(), O.getY() - OM2.getY() + ON1.getY(), 0);
        rectangleCoordinate[2] = new Coordinate(O.getX() - OM2.getX() + ON2.getX(), O.getY() - OM2.getY() + ON2.getY(), 0);
        rectangleCoordinate[3] = new Coordinate(O.getX() + OM2.getX() + ON2.getX(), O.getY() + OM2.getY() + ON2.getY(), 0);
        rectangleCoordinate[4] = new Coordinate(O.getX() + OM2.getX() + ON1.getX(), O.getY() + OM2.getY() + ON1.getY(), 0);
        return gf.createPolygon(rectangleCoordinate);
    }

    public Polygon getrectanglefromplot(int index){
        GeometryFactory gf = new GeometryFactory();
        List<Coordinate> intersectionPoints = new ArrayList<>();

        Point M1 = cutlines.get(index).getCentroid();
        Point M2 = cutlines.get(index+1).getCentroid();

        return getrectanglefrompoint(M1, M2);
    }

    //根据char决定地块类型并完成后续操作
    public void DecideTypology(String layer, IGeometry[][] geometries){
       for (int i = 0; i < locations.length; i++) {
           if (typologies[i] == 'A'){
               TypeA typeA = new TypeA(this, i);
               typeA.drawTypeA(layer);
           }
           if (typologies[i] == 'B'){
               TypeB typeB = new TypeB(this, i,geometries);
               typeB.drawTypeB(layer);
           }
           if (typologies[i] == 'C'){
          /*     TypeC typeC = new TypeC(this, i);
               typeC.drawTypeC(layer);*/
           }
           if (typologies[i] == 'D'){}
       }
    }

    // 根据给定的location计算LineString上的点
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

    public boolean isPointInPolygon(WB_Point point, WB_Polygon polygon) {
        GeometryFactory gf = new GeometryFactory();
        // 转换 WB_Polygon 为 JTS Polygon
        Polygon jtsPolygon = WB_PolygonToPolygon(polygon);

        // 创建 JTS Point
        Point jtsPoint = gf.createPoint(new Coordinate(point.xd(), point.yd(),point.zd()));

        // 判断点是否在多边形内
        return jtsPolygon.contains(jtsPoint);
    }

    public List<LineString> getCutLines() {
        GeometryFactory gf = new GeometryFactory();
        List<LineString> cutLines = new ArrayList<>();
        double[] v2x = new double[locations.length-1];
        double[] v2y = new double[locations.length-1];
        Polygon[] polygons = new Polygon[locations.length];

        for (int i = 0; i < locations.length; i++) {
            polygons[i] = WB_PolygonToPolygon(wbPolygons[i]);
        }

        for (int i = 0; i < locations.length-1; i++) {
            v2x[i] = getPointOnLine(rideg, locations[i+1]).x-getPointOnLine(rideg, locations[i]).x;
            v2y[i] = getPointOnLine(rideg, locations[i+1]).y-getPointOnLine(rideg, locations[i]).y;

           /* v2x[i] = polygons[i+1].getCentroid().getX() - polygons[i].getCentroid().getX();
            v2y[i] = polygons[i+1].getCentroid().getY() - polygons[i].getCentroid().getY();*/
            double v2length = Math.sqrt(v2x[i]*v2x[i] + v2y[i]*v2y[i]);
            v2x[i] /= v2length;
            v2y[i] /= v2length;

            Coordinate[] coordinates = polygons[i].getCoordinates();
            for (int j = 0; j < coordinates.length-1; j++) {
                double v1x = coordinates[j+1].x-coordinates[j].x;
                double v1y = coordinates[j+1].y-coordinates[j].y;
                double v1length = Math.sqrt(v1x*v1x + v1y*v1y);
                v1x /= v1length;
                v1y /= v1length;

                double dotProduct = v2x[i] * v1x + v2y[i] * v1y;
                Coordinate[] newCoordinates = new Coordinate[2];
                newCoordinates[0] = coordinates[j];
                newCoordinates[1] = coordinates[j+1];

                LineString line = gf.createLineString(newCoordinates);


                if (Math.abs(dotProduct) < 0.0001){
       /*             System.out.println(line);*/
                    boolean alreadyExists = false;

                    for (LineString ls : cutLines) {
                        if (ls.equals(line)) alreadyExists = true;
                    }

                    if (!alreadyExists) {cutLines.add(line);
                   }
                }
            }
        }
        return cutLines;
    }


    public double getSumBuildingArea() {
        double sumArea = 0.0;
        for (int i = 0; i < wbPolygons.length; i++) {
            if (typologies[i] == 'A'){
                sumArea += new TypeA(this, i).getBuildingArea();
            }
            if (typologies[i] == 'B'){
                sumArea += new TypeB(this, i).getBuildingArea();
            }
           /* if (typologies[i] == 'C'){
                sumArea += new TypeC(this, i).getBuildingArea();
            }*/
            if (typologies[i] == 'D'){}
        }
        return sumArea;
    }

    public double getAArea(){
        double sumArea = 0.0;
        for (int i = 0; i < wbPolygons.length; i++) {
            if (typologies[i] == 'A'){
                sumArea += new TypeA(this, i).getTypeArea();
            }
        }
        return sumArea;
    }
    public static Plots getsortedPlots(LineString boundary, LineString rideg, Double[] locations, char[] typologies, float[] floor, List<LineString> river){
        double[] locs = new double[locations.length];
        for (int i = 0; i < locations.length; i++){
            locs[i] = locations[i];
        }

        IndexValue[] plotsvars = new IndexValue[locs.length];
        for (int i = 0; i < locs.length; i++){
            plotsvars[i] = new IndexValue(locs[i], i);
        }
        Arrays.sort(plotsvars, Comparator.comparingDouble(IndexValue::getValue));

        double[] plotslocs = new double[locs.length];
        for (int i = 0; i < locs.length; i++){
            plotslocs[i] = plotsvars[i].getValue();
            System.out.println(plotslocs[i]);
        }
        char[] plotstypologies = new char[typologies.length];
        for (int i = 0; i < typologies.length; i++){
            plotstypologies[i] = typologies[plotsvars[i].index];
            System.out.println(plotstypologies[i]);
        }
        float[] plotsfloors = new float[floor.length];
        for (int i = 0; i < floor.length; i++){
            plotsfloors[i] = floor[plotsvars[i].index];
        }

        Plots plots = new Plots(boundary,rideg,plotslocs,plotstypologies, plotsfloors,river);
        return plots;
    }

    public static double getSumBuildingArea(LineString boundary, LineString rideg, Double[] locations, char[] typologies, float[] floor, List<LineString> river){
        Plots plots = getsortedPlots(boundary, rideg, locations, typologies, floor, river);
        return plots.getSumBuildingArea();
    }

    public static double getSumAArea(LineString boundary, LineString rideg, Double[] locations, char[] typologies, float[] floor, List<LineString> river){
        Plots plots = getsortedPlots(boundary, rideg, locations, typologies, floor, river);
        return plots.getAArea();
    }

    public static double getAverDist(LineString boundary, LineString rideg, Double[] locations, char[] typologies, float[] floor, List<LineString> river, ICurve[] plotcrvs, List<LineString> ridegs){
        Plots plots = getsortedPlots(boundary, rideg, locations, typologies, floor, river);
        List<Plot> Aplots = new ArrayList<Plot>();
        List<Integer> indexes = new ArrayList<>();
        for (int j = 0; j < plots.getTypologies().length; j++){
            if (plots.getTypologies()[j] == 'A'){
                indexes.add(j);
            }
        }
        for (int i = 0; i < indexes.size(); i++){
            Plot plot = new Plot(plots.getPlotRectangle(indexes.get(i)), river);
            Aplots.add(plot);
        }

        return parallelAverageDistanceFunction(Aplots, plotcrvs, ridegs);
    }

    public static double deltaHeight(LineString boundary, LineString rideg, Double[] locations, char[] typologies, float[] floor, List<LineString> river){
        Plots plots = getsortedPlots(boundary, rideg, locations, typologies, floor, river);
        double sumDeltaHeight = 0;
        for (int j = 0; j < plots.getTypologies().length-1; j++){
            sumDeltaHeight += Math.abs(plots.getFloor(j+1)-plots.getFloor(j));
        }
        return sumDeltaHeight;
    }

    // 自定义类用于保存元素值和索引
    public static class IndexValue {
        private final double value;
        private final int index;

        public IndexValue(double value, int index) {
            this.value = value;
            this.index = index;
        }

        public double getValue() {return value;}
        public int getIndex() {return index;}
    }

   /* static double siteblength(LineString boundary, LineString rideg, Double[] locations, char[] typologies, int[] floor, List<LineString> river){
        double siteblength = -1000.0;
        Plots plots = getsortedPlots(boundary, rideg, locations, typologies, floor, river);
        for (int j = 0; j < plots.getTypologies().length; j++){
            if (plots.getTypologies()[j] == 'B'){
                Site site = new Site(plots.getPlotRectangle(j), new Plot(plots.getPlotRectangle(j), river).getopenEdge(river));
                if (site.getSiteb() > 15) siteblength = 1000.0;
            }
            if (plots.getTypologies()[j] == 'C'){
                Site site = new Site(plots.getPlotRectangle(j), new Plot(plots.getPlotRectangle(j), river).getopenEdge(river));
                if (site.getSiteb() > 20) siteblength = 1000.0;
            }
        }
        return siteblength;
    }*/

    public static double siteblength(LineString boundary, LineString rideg, Double[] locations, char[] typologies, float[] floor, List<LineString> river){
        double penalty = 0; // 初始化惩罚值

        Plots plots = getsortedPlots(boundary, rideg, locations, typologies, floor, river);
        for (int j = 0; j < plots.getTypologies().length; j++){
            IGeometry[][] geometries = null;
            Site site = new Site(plots.getPlotRectangle(j), new Plot(plots.getPlotRectangle(j), river).getopenEdge(river), 0.5,  geometries);

            if (plots.getTypologies()[j] == 'B'){
                if (site.getSiteb() > 20) {
                    penalty += Math.pow(site.getSiteb() - 15, 2); // 对违反约束的情况添加惩罚
                }
                if (site.getSiteb() < 4) {
                    penalty += Math.pow(site.getSiteb() - 4, 2);
                }
            }
            if (plots.getTypologies()[j] == 'C'){
                if (site.getSiteb() > 30 || site.getSitea() > 30) {
                    penalty += Math.pow(site.getSiteb() - 20, 2); // 对违反约束的情况添加惩罚
                }
            }
        }
        return penalty; // 返回惩罚值
    }
}
