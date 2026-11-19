package basic;
import processing.core.PApplet;

public class Vec {
    public float x;
    public float y;
    public float z;

    //构造方法
    public Vec(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec(double xd, double yd, double zd) {
        this.x = (float) xd;
        this.y = (float) yd;
        this.z = (float) zd;
    }

    public Vec set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    //向量加法（修改原对象）
    public void add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public Vec add(Vec v){
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return this;
    }

    //向量加法（不修改原对象）
    public Vec addNew(Vec v){
        float x = this.x + v.x;
        float y = this.y + v.y;
        float z = this.z + v.z;
        return new Vec(x,y,z);
    }

    public Vec addNew(float x, float y, float z) {
        float xx = this.x + x;
        float yy = this.y + y;
        float zz = this.z + z;
        return new Vec(xx,yy,zz);
    }

    //向量减法（修改原对象）
    public Vec sub(Vec v){
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        return this;
    }

    public void sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
    }

    //向量减法（不修改原对象）
    public Vec subNew(Vec v){
        float x = this.x - v.x;
        float y = this.y - v.y;
        float z = this.z - v.z;
        return new Vec(x,y,z);
    }

    public Vec subNew(float x, float y, float z) {
        float xx = this.x - x;
        float yy = this.y - y;
        float zz = this.z - z;
        return new Vec(xx,yy,zz);
    }

    //重点
    public Vec mean(Vec v){
        return new Vec((this.x+v.x)/2f, (this.y+v.y)/2f, (this.z+v.z)/2f);
    }

    public Vec scale(float s){
        this.x *= s;
        this.y *= s;
        this.z *= s;
        return this;
    }

    public Vec scaleNew(float s){
        float xx = this.x*s;
        float yy = this.y*s;
        float zz = this.z*s;
        return new Vec(xx,yy,zz);
    }

    public Vec between(Vec v, float s){
        float t_x = (1-s)*this.x + s*v.x;
        float t_y = (1-s)*this.y + s*v.y;
        float t_z = (1-s)*this.z + s*v.z;
        return new Vec(t_x,t_y,t_z);
    }

    //向量模长
    public float mag() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    // 向量的叉积
    public Vec crossProduct(Vec v) {
        float cx = this.y * v.z - this.z * v.y; // 计算 X 分量
        float cy = this.z * v.x - this.x * v.z; // 计算 Y 分量
        float cz = this.x * v.y - this.y * v.x; // 计算 Z 分量
        return new Vec(cx, cy, cz); // 返回叉积结果
    }

    //求单位向量
    public Vec nor(){
        float len = this.mag();
        this.x /= len;
        this.y /= len;
        this.z /= len;
        return this;
    }

    public Vec norNew(){
        float len = this.mag();
        float x = this.x / len;
        float y = this.y / len;
        float z = this.z / len;
        return new Vec(x,y,z);
    }

    //向量点乘
    public float dot(Vec v){
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    //向量叉乘
    public Vec cross(Vec v){
        float x = this.y * v.z - this.z * v.y;
        float y = this.z * v.x - this.x * v.z;
        float z = this.x * v.y - this.y * v.x;
        return new Vec(x,y,z);
    }

    //向量相交角
    public float angle(Vec v){
        float dotPQ = this.dot(v);
        float mag_p = this.mag();
        float mag_q = v.mag();
        float cosAngle = dotPQ/(mag_p*mag_q);
        return (float) Math.acos(cosAngle);
    }

    //MIRO
    public Vec mirovec(){
        float x = - this.x;
        float y = this.y;
        float z = this.z;
        return new Vec(x,y,z);
    }

    //距离
    public float distance(Vec v){
        float dx = this.x - v.x;
        float dy = this.y - v.y;
        float dz = this.z - v.z;
        return (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    public float _toRadian(float angle){
        return (float) (angle*Math.PI/180.);
    }

    public float _toAngle(float radian) {
        return (float) (radian*180/Math.PI);
    }

    public void draw(PApplet app){
        app.pushStyle();
        app.line(0,0,0,this.x,this.y,this.z);

        app.pushMatrix();
        app.translate(this.x,this.y,this.z);
        app.box(5);
        app.popMatrix();

        app.popStyle();
    }

    public Vec interpolation(Vec v, float r){
        float xx = (1-r)*this.x + r*v.x;
        float yy = (1-r)*this.y + r*v.y;
        float zz = (1-r)*this.z + r*v.z;
        return new Vec(xx,yy,zz);
    }

    public void printInfo() {
        System.out.println(x+" "+y+" "+z);
    }



}
