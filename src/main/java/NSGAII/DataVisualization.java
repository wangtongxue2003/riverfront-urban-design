package NSGAII;

import peasy.PeasyCam;
import processing.core.PApplet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DataVisualization extends PApplet {
    private PeasyCam cam;

    ArrayList<Float> opennessValues = new ArrayList<>();
    ArrayList<Float> avgDistanceValues = new ArrayList<>();
    ArrayList<Float> areaValues = new ArrayList<>();

    public static void main(String[] args) {
        PApplet.main("NSGAII.DataVisualization");
    }

    public void settings() {
        size(600, 600,P3D);
    }

    public void setup() {
        cam = new PeasyCam(this, 500);
        // 读取CSV文件
        String csvFile = "private-data/output_data.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            // 跳过第一行（列名）
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                // 提取每个目标值
                opennessValues.add(Float.parseFloat(values[0]));
                avgDistanceValues.add(Float.parseFloat(values[1]));
                areaValues.add(Float.parseFloat(values[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Openness Values: " + opennessValues);
        System.out.println("Avg Distance Values: " + avgDistanceValues);
        System.out.println("Area Values: " + areaValues);

    }

    public void draw() {
        background(255);

        // 设置坐标轴的颜色
        stroke(0);
        strokeWeight(1);

        // 绘制三条轴：X 轴, Y 轴, Z 轴
        drawAxis();  // 绘制坐标轴

        // 绘制目标值的散点图
        stroke(0);
        fill(0);
        strokeWeight(2);
        for (int i = 0; i < opennessValues.size(); i++) {
            // 使用三个参数分别映射到 X, Y, Z 轴
            float x = map(opennessValues.get(i), 100000, 300000, -300, 0);  // X轴
            float y = map(avgDistanceValues.get(i), 180, 210, 0, 300);  // Y轴
            float z = map(areaValues.get(i), 1000, 2500, 0, 300);  // Z轴

            pushMatrix();
            translate(x, y, z);  // 移动到对应的坐标
            ellipse(0, 10, 10, 10);  // 绘制数据点
            popMatrix();
        }

        // 绘制图例
        fill(0);
        textAlign(LEFT);
        text("Openness vs Average Distance vs Area", 20, 40);
    }

    // 绘制三维坐标轴
    private void drawAxis() {
        // X轴（红色）
        stroke(255, 0, 0);
        line(0, 0, 0, -300, 0, 0);  // 从原点到X轴终点
        text("-Openness", -310, 0, 0);  // X轴标签
        // 绘制X轴刻度
        for (int i = -50; i >= -250; i -= 50) {
            line(i, 0, 0, i, 5, 0);  // 绘制刻度
            text(str(i), i, 10, 0);  // 刻度标签
        }

        // Y轴（绿色）
        stroke(0, 255, 0);
        line(0, 0, 0, 0, 300, 0);  // 从原点到Y轴终点
        text("Avg Distance", 0, 310, 0);  // Y轴标签
        // 绘制Y轴刻度
        for (int i = 50; i <= 250; i += 50) {
            line(0, i, 0, 5, i, 0);  // 绘制刻度
            text(str(i * -1), 10, i, 0);  // 刻度标签
        }

        // Z轴（蓝色）
        stroke(0, 0, 255);
        line(0, 0, 0, 0, 0, 300);  // 从原点到Z轴终点
        text("Area", 0, 0, 310);  // Z轴标签
        // 绘制Z轴刻度
        for (int i = 50; i <= 250; i += 50) {
            line(0, 0, i, 5, 0, i);  // 绘制刻度
            text(str(i), 10, 0, i);  // 刻度标签
        }
    }
}
