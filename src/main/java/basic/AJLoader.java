package basic;

import com.google.gson.*;
import processing.core.PApplet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * ArchiJSON文件的读取类
 *
 * @author Baizhou Zhang zhangbz
 * @project archijson-reader
 * @date 2024/11/24
 * @time 9:05
 */
public class AJLoader {
    // 读入的archijson文件路径
    private final String filePath;

    // 地块的基本信息，需要调用时请使用对应的“get....”方法
    private Vec[][] siteVecs;
    private Vec[][] plotVecs;

    // 几何图元信息


    public AJLoader(String path) {
        this.filePath = path;


        Gson gson = new Gson();

        try {
            // 使用 BufferedReader 读取 JSON 文件
            BufferedReader br = new BufferedReader(new FileReader(filePath));

            // 使用 Gson 解析 JSON 数据并映射到指定的 ARCHIJSON 类
            ARCHIJSON archijson = gson.fromJson(br, ARCHIJSON.class);

            // 打印测试一下解析的properties属性
            System.out.println(">>> AJLoader --- .archijson file open: " + filePath);
            System.out.println(">>> AJLoader --- Test print - properties: " + archijson.properties);
            System.out.println(">>> AJLoader --- Test print - geometry num: " + archijson.geometryElements.size());

            // 处理并提取所有數数据
            JsonObject properties = archijson.properties;
            List<JsonElement> geometryElements = archijson.geometryElements;

            if (properties.get("app").getAsString().equals("flexurban")) {
                int siteNum = properties.get("siteNum").getAsInt();
                int plotNum = properties.get("plotNum").getAsInt();

                this.siteVecs = new Vec[siteNum][];
                this.plotVecs = new Vec[plotNum][];

                // site
                for (int i = 0; i < siteNum; i++) {
                    JsonObject siteObject = geometryElements.get(i).getAsJsonObject();

                    int size = siteObject.get("size").getAsInt();
                    JsonArray coordArray = siteObject.get("coordinates").getAsJsonArray();
                    int ptNum = coordArray.size() / size;

                    Vec[] vecs = new Vec[ptNum];
                    for (int j = 0; j < ptNum; ++j) {
                        float[] pt = new float[size];
                        for (int k = 0; k < size; ++k) {
                            pt[k] = coordArray.get(j * size + k).getAsFloat();
                        }
                        vecs[j] = new Vec(pt[0], pt[1], pt[2]);
                    }

                    siteVecs[i] = vecs;
                }

                // plot
                for (int i = siteNum; i < siteNum + plotNum; i++) {
                    JsonObject siteObject = geometryElements.get(i).getAsJsonObject();

                    int size = siteObject.get("size").getAsInt();
                    JsonArray coordArray = siteObject.get("coordinates").getAsJsonArray();
                    int ptNum = coordArray.size() / size;

                    Vec[] vecs = new Vec[ptNum];
                    for (int j = 0; j < ptNum; ++j) {
                        float[] pt = new float[size];
                        for (int k = 0; k < size; ++k) {
                            pt[k] = coordArray.get(j * size + k).getAsFloat();
                        }
                        vecs[j] = new Vec(pt[0], pt[1], pt[2]);
                    }

                    plotVecs[i - siteNum] = vecs;
                }

            } else {
                throw new IOException("archijson file only support app flexurban");
            }

            // 关闭 BufferedReader
            br.close();
        } catch (JsonSyntaxException | JsonIOException | IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 绘制所有场地轮廓
     *
     * @param pApplet
     * @return void
     */
    public void drawSite(PApplet pApplet) {
        pApplet.pushStyle();

        pApplet.stroke(255, 0, 0);
        pApplet.strokeWeight(3);
        pApplet.noFill();

        for (int i = 0; i < this.siteVecs.length; i++) {
            pApplet.beginShape();
            for (int j = 0; j < this.siteVecs[i].length; j++) {
                pApplet.vertex(siteVecs[i][j].x, siteVecs[i][j].y, siteVecs[i][j].z);
            }
            pApplet.endShape();
        }

        pApplet.popStyle();
    }

    /**
     * 绘制所有地块
     *
     * @param pApplet
     * @return void
     */
    public void drawPlot(PApplet pApplet) {
        pApplet.pushStyle();

        pApplet.stroke(0);
        pApplet.strokeWeight(1);
        pApplet.fill(200);

        for (int i = 0; i < this.plotVecs.length; i++) {
            pApplet.beginShape();
            for (int j = 0; j < this.plotVecs[i].length; j++) {
                pApplet.vertex(plotVecs[i][j].x, plotVecs[i][j].y, plotVecs[i][j].z);
            }
            pApplet.endShape();
        }

        pApplet.popStyle();
    }

    /* ------------- getter ------------- */

    public Vec[][] getSiteVecs() {
        return siteVecs;
    }

    public Vec[][] getPlotVecs() {
        return plotVecs;
    }

    /* ------------- inner classes ------------- */

    /**
     *
     */
    private class ARCHIJSON {
        JsonObject properties;
        List<JsonElement> geometryElements;
    }
}
