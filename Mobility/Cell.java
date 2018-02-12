package Mobility;

import java.util.ArrayList;

public class Cell {
    double centreX;
    double centreY;
    double radius;
    int id;
    int handovers;
    Cell(int id,double x,double y, double r){
        centreX = x;
        centreY = y;
        radius = r;
        this.id = id;
    }
    public void handover(){
        handovers ++;
    }
    public double distance(double x,double y){
        return Math.sqrt(Math.pow((centreX - x),2)+Math.pow((centreY - y),2));
    }

}
