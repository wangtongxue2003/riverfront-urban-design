package Spacesyntax;

import igeo.*;
import org.locationtech.jts.geom.*;
import processing.core.PApplet;
import wblut.geom.WB_Coord;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.hemesh.HEC_FromFacelist;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Mesh;
import wblut.hemesh.HE_Vertex;

import java.util.ArrayList;
import java.util.List;

public class dataTransfer {

    public dataTransfer() {

    }


    //Polygon -> ICurve
    public static ICurve PolygonToICurve(Polygon p) {
        Coordinate[] coordinates = p.getCoordinates();
        IVec[] iVecs = new IVec[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            iVecs[i] = new IVec(coordinates[i].x, coordinates[i].y, 0);
        }
        return new ICurve(iVecs);
    }

    // IVertex -> WB_Point
    public static WB_Point IVertexToWB_Point(final IVertex vertex)
    {
        return new WB_Point(vertex.x(), vertex.y(), vertex.z());
    }


    public static Coordinate IPointToCoordinate(final IPoint point){
        return new Coordinate(point.x(), point.y(), point.z());
    }

    // ICurve -> WB_Point
    public static WB_Point[] ICurveToWB_Point(final ICurve curve) {
        WB_Point[] points = new WB_Point[curve.cpNum()];
        for (int i = 0; i < curve.cpNum(); i++) {
            points[i] = new WB_Point(curve.cp(i).x(), curve.cp(i).y(), curve.cp(i).z());
        }
        return points;
    }

    // ICurve -> WB_Polygon
    public static WB_Polygon ICurveToWB_Polygon(final ICurve curve) {
        WB_Point[] points = new WB_Point[curve.cpNum()];
        for (int i = 0; i < curve.cpNum(); i++) {
            points[i] = new WB_Point(curve.cp(i).x(), curve.cp(i).y(), curve.cp(i).z());
        }
        return new WB_Polygon(points);
    }


    //ICurve -> LineString
    public static LineString ICurveToLineString(final ICurve curve) {
        GeometryFactory gf = new GeometryFactory();
        WB_Point[] points = ICurveToWB_Point( curve );
        Coordinate[] coordinates = new Coordinate[points.length];
        for (int i = 0; i < points.length; i++) {
            coordinates[i] = new Coordinate(points[i].xd(), points[i].yd(), points[i].zd());
        }
        return gf.createLineString(coordinates);
    }

    //LineString -> WB_Polygon
    public static WB_Polygon LineStringToWB_Polygon(final LineString lineString) {
        WB_Point[] points = new WB_Point[lineString.getNumPoints()];
        for (int i = 0; i < lineString.getNumPoints(); i++) {
            points[i] = new WB_Point(lineString.getPointN(i).getX(),lineString.getPointN(i).getY(),0);
        }
        return new WB_Polygon(points);
    }

    //WB_polygon -> Polygon
    public static Polygon WB_PolygonToPolygon(WB_Polygon wbPolygon) {
        // 获取WB_Polygon的顶点
        List<WB_Point> wbPoints =new ArrayList<>();
        int num = wbPolygon.getNumberOfPoints();
        for (int i = 0; i < num; i++) {
            wbPoints.add(wbPolygon.getPoint(i));
        }
        // 将WB_Point转为Coordinate数组
        List<Coordinate> coordinates = new ArrayList<>();
        for (WB_Point wbPoint : wbPoints) {
            coordinates.add(new Coordinate(wbPoint.xd(), wbPoint.yd(), 0));
        }

        // 将Coordinate数组转换为JTS的Polygon
        Coordinate[] coords = coordinates.toArray(new Coordinate[0]);
        GeometryFactory geometryFactory = new GeometryFactory();
        Polygon polygon = geometryFactory.createPolygon(coords);

        return polygon;
    }

    //Polygon -> WB_Plygon  z为0
    public static WB_Polygon PolygonToWB_Polygon(Polygon polygon) {
        List<WB_Point> wbPoints =new ArrayList<>();
        int num = polygon.getNumPoints();
        Coordinate[] coordinates = polygon.getCoordinates();
        for (int i = 0; i < num; i++) {
            wbPoints.add(new WB_Point(coordinates[i].x, coordinates[i].y,0));
        }
        return new WB_Polygon(wbPoints);
    }

    // WB_polygon -> Surface（绘制方法，无返回值）
    public static void drawWB_Polygon (WB_Polygon p) {
        WB_Coord[] vertices = p.getPoints().toArray();
        double[][] points = new double[vertices.length][3];
        for (int i = 0; i < vertices.length; i++) {
            points[i][0] = vertices[i].xd();
            points[i][1] = vertices[i].yd();
            points[i][2] = vertices[i].zd();
        }
        IVec[] pnts = new IVec[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            pnts[i] = new IVec(points[i][0], points[i][1], points[i][2]);
        }
        new ISurface(pnts);

    }

    public static void drawWB_Polygonwithlayer (WB_Polygon p, String layer) {
        WB_Coord[] vertices = p.getPoints().toArray();
        double[][] points = new double[vertices.length][3];
        for (int i = 0; i < vertices.length; i++) {
            points[i][0] = vertices[i].xd();
            points[i][1] = vertices[i].yd();
            points[i][2] = vertices[i].zd();
        }
        IVec[] pnts = new IVec[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            pnts[i] = new IVec(points[i][0], points[i][1], points[i][2]);
        }
        new ISurface(pnts).layer(layer);

    }

    // WB_polygon -> Surface with color（绘制方法，无返回值）
    public static void drawWB_Polygonwithclr (WB_Polygon p, double r, double g, double b, String layer) {
        WB_Coord[] vertices = p.getPoints().toArray();
        double[][] points = new double[vertices.length][3];
        for (int i = 0; i < vertices.length; i++) {
            points[i][0] = vertices[i].xd();
            points[i][1] = vertices[i].yd();
            points[i][2] = vertices[i].zd();
        }
        IVec[] pnts = new IVec[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            pnts[i] = new IVec(points[i][0], points[i][1], points[i][2]);
        }
        new ISurface(pnts).clr(r, g, b).layer(layer);
    }

    // Polygon -> Surface with color（绘制方法，无返回值）
    public static void drawPolygonwithclr (Polygon p, double r, double g, double b, String layer) {
        Coordinate[] vertices = p.getCoordinates();
        double[][] points = new double[vertices.length][3];
        for (int i = 0; i < vertices.length; i++) {
            points[i][0] = vertices[i].x;
            points[i][1] = vertices[i].y;
            points[i][2] = vertices[i].z;
        }
        IVec[] pnts = new IVec[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            pnts[i] = new IVec(points[i][0], points[i][1], points[i][2]);
        }
        new ISurface(pnts).clr(r, g, b).layer(layer);
    }

    // IMesh -> HE_Mesh
    public static HE_Mesh IMeshToHE_Mesh(final IMesh imesh)
    {
        ArrayList<IVertex> vertices = imesh.vertices();
        List<WB_Point> pts = new ArrayList<>();
        for (IVertex v : vertices) {
            pts.add(IVertexToWB_Point(v));
        }

        List<int[]> faceList = new ArrayList<>();
        for (int i = 0; i < imesh.faceNum(); i++) {
            IFace face = imesh.face(i);
            IVertex[] vInFace = face.vertices;
            int[] id = new int[vInFace.length];
            for (int j = 0; j < vInFace.length; j++) {
                id[j] = vertices.indexOf(vInFace[j]);
            }
            faceList.add(id);
        }

        HEC_FromFacelist creator = new HEC_FromFacelist();
        creator.setVertices(pts);
        creator.setFaces(faceList);
        return new HE_Mesh(creator);
    }

    /*public static IMesh HE_MeshToIMesh(final HE_Mesh heMesh) {
        // 初始化 IGEO 的网格对象
        IMesh iMesh = new IMesh();

        // 提取 HE_Mesh 中的顶点
        List<HE_Vertex> vertices = heMesh.getVertices();
        IVec[] iGeoVertices = new IVec[vertices.size()];

        for (int i = 0; i < vertices.size(); i++) {
            WB_Point point = vertices.get(i).getPosition();
            // 将 WB_Point 转换为 IGEO 的 IVec
            iGeoVertices[i] = new IVec(point.xd(), point.yd(), point.zd());
        }

        // 将顶点添加到 IGEO 的网格
        for (IVec v : iGeoVertices) {
            iMesh.addVertex(new IVertex(v));
        }

        // 提取 HE_Mesh 中的面
        for (HE_Face face : heMesh.getFaces()) {
            List<HE_Vertex> faceVertices = face.getFaceVertices();
            // 获取面中顶点的索引
            int[] faceIndices = new int[faceVertices.size()];

            for (int i = 0; i < faceVertices.size(); i++) {
                faceIndices[i] = vertices.indexOf(faceVertices.get(i));
            }

            // 使用顶点索引创建 IGEO 的面
            iMesh.addFace(faceIndices);
        }

        return iMesh;
    }*/


    public static LineString[] WB_PointtoLineStrings(WB_Point[] wbPoints) {
        // 创建 GeometryFactory 用于生成 JTS 线
        GeometryFactory geometryFactory = new GeometryFactory();

        // 计算 LineString 数组的大小
        int numLineStrings = wbPoints.length - 1;
        LineString[] lineStrings = new LineString[numLineStrings];

        // 遍历每一对点，生成 LineString
        for (int i = 0; i < numLineStrings; i++) {
            Coordinate[] coordinates = new Coordinate[] {
                    new Coordinate(wbPoints[i].xd(), wbPoints[i].yd()),
                    new Coordinate(wbPoints[i + 1].xd(), wbPoints[i + 1].yd())
            };
            lineStrings[i] = geometryFactory.createLineString(coordinates);
        }

        return lineStrings;
    }
}
