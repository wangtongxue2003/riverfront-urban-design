package Typologies;

import igeo.IGeometry;
import igeo.IVec;
import org.locationtech.jts.geom.*;

import java.util.List;

public class Site {
    private Polygon site;
    private LineString openEdge;
    private double sitea;
    private double siteb;
    private IVec OA;
    private IVec OB;
    private Coordinate[] coordinates;
    GeometryFactory gf = new GeometryFactory();
    private double entrance;
    private IGeometry[][] geometries;


    public Site(Polygon site, LineString openEdge, double entrance, IGeometry[][] geometries) {

        this.openEdge = openEdge;
        this.coordinates = reorderPolygon(site,openEdge);
        // JTS 2D coordinates leave z as NaN; iGeo surfaces require a finite elevation.
        for (Coordinate coordinate : coordinates) {
            if (!Double.isFinite(coordinate.getZ())) coordinate.setZ(0);
        }
        this.site = gf.createPolygon(coordinates);
        this.sitea = coordinates[0].distance(coordinates[3]);  //进深方向大小
        this.siteb = coordinates[0].distance(coordinates[1]);  //面宽方向大小
        this.OA = new IVec(coordinates[3].x - coordinates[0].x, coordinates[3].y - coordinates[0].y,0);
        this.OB = new IVec(coordinates[1].x - coordinates[0].x, coordinates[1].y - coordinates[0].y,0);
        this.entrance = entrance;
        this.geometries = geometries;
    }

    public Site(){}

    //get
    public Polygon getSite() {return this.site;}
    public LineString getOpenEdge() {return this.openEdge;}
    public double getSitea() {return this.sitea;}
    public double getSiteb() {return this.siteb;}
    public IVec getOA() {return this.OA;}
    public IVec getOB() {return this.OB;}
    public double getEntrance() {return this.entrance;}
    public IGeometry[][] getGeometries() {return this.geometries;}



    public static Coordinate[] reorderPolygon(Polygon site, LineString entranEdge) {
        GeometryFactory gf = new GeometryFactory();

        // 获取 Polygon 和 LineString 的点
        Coordinate[] siteCoords = site.getCoordinates();
        Coordinate[] edgeCoords = entranEdge.getCoordinates();
       /* System.out.println("Site coordinates: " + Arrays.toString(siteCoords));*/

        // 检查 Polygon 点序是否为逆时针，必要时反转
        boolean isPolygonCCW = isCounterClockwise(siteCoords);
        if (isPolygonCCW) {
            siteCoords = reverseCoordinates(siteCoords);
        }

       /* System.out.println("sitecoordslength: " + siteCoords.length);*/

        // 调整 LineString 点序以与 Polygon 匹配
        edgeCoords = alignEdgeWithPolygon(siteCoords, edgeCoords);

        /*System.out.println("edgecoordslength: " + Arrays.toString(edgeCoords));*/

        // 找到 LineString 的端点在 Polygon 中的索引
        int firstIndex = findCoordinateIndex(siteCoords, edgeCoords[0]);
        int secondIndex = findCoordinateIndex(siteCoords, edgeCoords[1]);

     /*   System.out.println("firstindex: " + firstIndex);
        System.out.println("secondindex: " + secondIndex);*/

        if (firstIndex == -1 || secondIndex == -1) {
            throw new IllegalArgumentException("The entranEdge is not a part of the site.");
        }

        // 调整 Polygon 点序，使 LineString 的端点位于点序的起始位置
        siteCoords = reorderCoordinates(siteCoords, firstIndex);

        Coordinate[] newsiteCoords = new Coordinate[5];
        newsiteCoords[0] = siteCoords[0];
        newsiteCoords[1] = siteCoords[1];
        newsiteCoords[2] = siteCoords[2];
        newsiteCoords[3] = siteCoords[3];
        newsiteCoords[4] = siteCoords[0];

        Polygon returnpolygon = gf.createPolygon(newsiteCoords);


        /*System.out.println("newsiteCoords: " + Arrays.toString(newsiteCoords));*/

       /* // 检查 Polygon 点序是否为逆时针，必要时反转
        boolean isPolygonCCW1 = isCounterClockwise(siteCoords);
        if (isPolygonCCW1) {
            siteCoords = reverseCoordinates(siteCoords);
        }*/

      /*  System.out.println("sitecoordslength: " + Arrays.toString(siteCoords));*/
        // 创建新的 Polygon
        return newsiteCoords;
    }

    // 检查点序是否逆时针
    private static boolean isCounterClockwise(Coordinate[] coords) {
        double sum = 0;
        for (int i = 0; i < coords.length - 1; i++) {
            sum += (coords[i + 1].x - coords[i].x) * (coords[i + 1].y + coords[i].y);
        }
        return sum > 0;
    }

    // 调整 LineString 点序以与 Polygon 的顺序匹配
    private static Coordinate[] alignEdgeWithPolygon(Coordinate[] siteCoords, Coordinate[] edgeCoords) {
        // 找到端点在 Polygon 中的索引
        int firstIndex = findCoordinateIndex(siteCoords, edgeCoords[0]);
        int secondIndex = findCoordinateIndex(siteCoords, edgeCoords[1]);

        if (firstIndex == -1 || secondIndex == -1) {
            throw new IllegalArgumentException("The entranEdge is not a part of the site.");
        }

        // 检查 LineString 点序是否符合 Polygon 的方向
        boolean isEdgeAligned = (secondIndex == (firstIndex + 1) % (siteCoords.length - 1));

        // 如果不一致，反转 LineString 点序
        if (!isEdgeAligned) {
            edgeCoords = reverseCoordinates(edgeCoords);
        }

        return edgeCoords;
    }

    // 反转坐标点序
    private static Coordinate[] reverseCoordinates(Coordinate[] coords) {
        Coordinate[] reversed = new Coordinate[coords.length];
        for (int i = 0; i < coords.length; i++) {
            reversed[i] = coords[coords.length - 1 - i];
        }
        return reversed;
    }

    // 找到某个坐标点在坐标数组中的索引
    private static int findCoordinateIndex(Coordinate[] coords, Coordinate target) {
        for (int i = 0; i < coords.length; i++) {
            if (coords[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }

    // 根据索引调整坐标点序（处理首尾点闭合）
    private static Coordinate[] reorderCoordinates(Coordinate[] coords, int startIndex) {
        int n = coords.length - 1; // 忽略最后一个重复的点
        Coordinate[] reordered = new Coordinate[n + 1];

        // 从 startIndex 开始重新排列点序
        int j = 0;
        for (int i = startIndex; i < n; i++) {
            reordered[j] = coords[i];
            j++;
        }
        for (int i = 0; i < startIndex; i++) {
            reordered[j] = coords[i];
            j++;
        }

        // 添加首尾闭合点
        reordered[n] = reordered[0];
        return reordered;
    }

    public Site getSiteanother() {
        Coordinate[] newopenedge = new Coordinate[2];
        newopenedge[0] = this.coordinates[1];
        newopenedge[1] = this.coordinates[2];
        LineString newOpenEdge = gf.createLineString(newopenedge);
        Site newsite = new Site(this.site, newOpenEdge, 0.5, geometries);
        return newsite;
    }

    public Polygon[] get2Site(){
        Coordinate[] oldsite = this.coordinates;
        Coordinate[] newsite1 = new Coordinate[5];
        Coordinate[] newsite2 = new Coordinate[5];
        newsite1[0] = oldsite[0];
        newsite1[1] = oldsite[1];
        newsite1[2] = new Coordinate((coordinates[1].x + coordinates[2].x)/2, (coordinates[1].y + coordinates[2].y)/2, 0);
        newsite1[3] = new Coordinate((coordinates[0].x + coordinates[3].x)/2, (coordinates[0].y + coordinates[3].y)/2, 0);
        newsite1[4] = newsite1[0];

        newsite2[0] = oldsite[2];
        newsite2[1] = oldsite[3];
        newsite2[2] = newsite1[3];
        newsite2[3] = newsite1[2];
        newsite2[4] = newsite2[0];

        LineString newOpenEdge1 = gf.createLineString(new Coordinate[]{newsite1[0], newsite1[1]});
        if ((newsite1[1].distance(newsite1[2])) < (newsite1[0].distance(newsite1[1]))) {
            newOpenEdge1 = gf.createLineString(new Coordinate[]{newsite2[1], newsite2[2]});
        }
        LineString newOpenEdge2 = gf.createLineString(new Coordinate[]{newsite2[0], newsite2[1]});
        if ((newsite2[1].distance(newsite2[2])) < (newsite2[0].distance(newsite2[1]))) {
            newOpenEdge2 = gf.createLineString(new Coordinate[]{newsite2[1], newsite2[2]});
        }
        Polygon polygon1 = gf.createPolygon(newsite1);
        Polygon polygon2 = gf.createPolygon(newsite2);

        return new Polygon[]{polygon1, polygon2};
    }

    public Site getRealSite(List<LineString> roads){
        Coordinate[] siteCoords = this.coordinates;
        LineString[] siteLings = new LineString[4];
        Site realSite = new Site(this.site, this.openEdge, 0.5, geometries);
        for (int i = 0; i < 4; i++) {
            siteLings[i] = gf.createLineString(new Coordinate[]{siteCoords[i], siteCoords[(i + 1) % 4]});
            for (LineString road : roads) {
                if (road.intersects(siteLings[i])) {
                    Geometry inter = road.intersection(siteLings[i]);
                    Coordinate[] intersection = inter.getCoordinates();
                    LineString newOpenEdge = siteLings[i];
                    entrance = (siteCoords[i].distance(intersection[0])) / (siteCoords[i].distance(siteCoords[(i + 1) % 4]));
                    if (entrance > 0 && entrance < 1) {
                        realSite = new Site(site, newOpenEdge, entrance, geometries);
                        break;  //找到就不找了
                    }
                }
            }
        }
        return realSite;
    }


}
