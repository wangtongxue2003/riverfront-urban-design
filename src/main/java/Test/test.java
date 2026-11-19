package Test;

import peasy.PeasyCam;
import processing.core.PApplet;
import wblut.geom.*;

import java.util.ArrayList;
import java.util.List;

public class test extends PApplet {
    private PeasyCam cam;

    int pointsnum = 20;
    private float[][] newpoints = new float[pointsnum][2];
    private float[][] oldpoints = new float[pointsnum][2];
    private long lastUpdateTime = 0; // 上次更新的时间
    private long updateInterval = 5000; // 更新间隔为5秒

    float max_x = 1000;
    float max_y = 1000;
    float maxTransitionSpeed = 0.1f; // 最大平滑过渡速度
    float minTransitionSpeed = 0.005f; // 最小平滑过渡速度
    WB_Coord[] points = new WB_Coord[pointsnum];

    WB_Polygon site = new WB_Polygon();

    float[] oldHeight = new float[pointsnum]; // 旧的高度
    float[] newHeight = new float[pointsnum];



    public void settings(){
        size(1300, 900, P3D);
    }

    public static void main(String[] args) {
        PApplet.main("Test.test");
    }

    public void setup() {

        cam = new PeasyCam(this, 2000);
        // 初始化点的位置
        for (int i = 0; i < pointsnum; i++) {
            newpoints[i][0] = random(0, max_x);
            newpoints[i][1] = random(0, max_y);
            newHeight[i] = random(50, 200);
        }

        // 初始化旧点位置
        for (int i = 0; i < pointsnum; i++) {
            oldpoints[i][0] = newpoints[i][0];
            oldpoints[i][1] = newpoints[i][1];
            oldHeight[i] = newHeight[i];
        }
        smooth();

        WB_Point p1 = new WB_Point(0, 0);
        WB_Point p2 = new WB_Point(max_x, 0);
        WB_Point p3 = new WB_Point(0, max_y);
        WB_Point p4 = new WB_Point(max_x, max_y);

        List<WB_Point> sitepoints = new ArrayList<>();
        sitepoints.add(p1);
        sitepoints.add(p2);
        sitepoints.add(p4);
        sitepoints.add(p3);

        // 使用四个点创建一个WB_Polygon
        site = new WB_Polygon(sitepoints);

    }

    public void draw() {
        background(255);
        stroke(0);
        strokeWeight(2);

        // 绘制一个边长为 1000 的正方形
        beginShape(LINES); // 开始绘制线段

        // 正方形的四条边
        vertex(0, 0, 0);
        vertex(max_x, 0, 0);

        vertex(0, 0, 0);
        vertex(0, max_y, 0);

        vertex(max_x, 0, 0);
        vertex(max_x, max_y, 0);

        vertex(0, max_y, 0);
        vertex(max_x, max_y, 0);

        endShape(); // 结束绘制

        // 检查时间差，如果超过5秒，则更新点的位置
        if (millis() - lastUpdateTime > updateInterval) {
            lastUpdateTime = millis(); // 更新上次更新时间
            // 随机生成新点的位置
            for (int i = 0; i < pointsnum; i++) {
                newpoints[i][0] = random(0, max_x);  // 随机生成新的 x 坐标
                newpoints[i][1] = random(0, max_y);  // 随机生成新的 y 坐标
                newHeight[i] = random(50, 200);
            }
        }

        for (int i = 0; i < pointsnum; i++) {
            fill(0, 0, 0); // 设置点的颜色为黑色
            noStroke(); // 不绘制边框
            // 计算每个点到目标点的距离
            float deltaX = newpoints[i][0] - oldpoints[i][0];
            float deltaY = newpoints[i][1] - oldpoints[i][1];
            float deltaheight = newHeight[i] - oldHeight[i];
            float distance = dist(oldpoints[i][0], oldpoints[i][1], newpoints[i][0], newpoints[i][1]);

            float transitionSpeed = map(distance, 0, max_x, minTransitionSpeed, maxTransitionSpeed);

            oldpoints[i][0] += deltaX * transitionSpeed;
            oldpoints[i][1] += deltaY * transitionSpeed;
            oldHeight[i] += deltaheight * transitionSpeed;

            points[i] = new WB_Point(oldpoints[i][0], oldpoints[i][1]);

            // 绘制点
            ellipse(oldpoints[i][0], oldpoints[i][1], 10, 10);
        }

        List<WB_VoronoiCell2D> voronoiCells = WB_VoronoiCreator.getClippedVoronoi2D(points, site,20).getCells();

        for (WB_VoronoiCell2D cell : voronoiCells) {
            fill(255);
            stroke(0);
            strokeWeight(2);

            // 通过其边界绘制Voronoi单元(底面)
            beginShape();
            for (int i = 0; i < cell.getPolygon().getNumberOfPoints(); i++) {
                WB_Point pt = cell.getPolygon().getPoint(i);
                vertex(pt.xf(), pt.yf());
            }
            endShape(CLOSE);

            //顶面
            beginShape();
            for (int i = 0; i < cell.getPolygon().getNumberOfPoints(); i++) {
                WB_Point pt = cell.getPolygon().getPoint(i);
                vertex(pt.xf(), pt.yf(),oldHeight[cell.getIndex()]);
            }
            endShape(CLOSE);

            // 绘制侧面（连接底面和顶面）
            for (int i = 0; i < cell.getPolygon().getNumberOfPoints(); i++) {
                WB_Point pt1 = cell.getPolygon().getPoint(i);
                WB_Point pt2 = cell.getPolygon().getPoint((i + 1) % cell.getPolygon().getNumberOfPoints());

                // 画出侧面
                beginShape(QUADS);
                vertex(pt1.xf(), pt1.yf(), 0);        // 底面
                vertex(pt2.xf(), pt2.yf(), 0);        // 底面
                vertex(pt2.xf(), pt2.yf(), oldHeight[cell.getIndex()]);  // 顶面
                vertex(pt1.xf(), pt1.yf(), oldHeight[cell.getIndex()]);  // 顶面
                endShape(CLOSE);
            }

        }
    }
}
