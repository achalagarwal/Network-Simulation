package Mobility;

import java.util.ArrayList;
import java.util.Random;

public class Control {
    static ArrayList<Cell> cells = new ArrayList<>();
    static ArrayList<Host> hosts = new ArrayList<>();
    static double time =0.0;
    Control(){

    }
    static public Cell getCell(int id){
        return cells.get(id);
    }
    static public int nearestCell(double x, double y){
        Cell cmin = cells.get(0);
        for(Cell c:cells){
            if(c.distance(x,y)<cmin.distance(x,y))
                cmin = c;
        }
        return cmin.id;
    }
    public static void main(String args[]){
        int cells = 7;
        int hosts = 2000;
        double arrayPos[][] = new double[10][3];
        arrayPos[0][0] = 0.0;
        arrayPos[0][1] = 0.0;
        arrayPos[0][2] = 50000;
        arrayPos[1][0] = 80000;
        arrayPos[1][1] = 0.0;
        arrayPos[1][2] = 50000;
        arrayPos[2][0] = -80000;
        arrayPos[2][1] = 0.0;
        arrayPos[2][2] = 50000;
        arrayPos[3][0] = 80000*Math.cos(Math.PI/4);
        arrayPos[3][1] = 80000*Math.sin(Math.PI/4);
        arrayPos[3][2] = 50000;
        arrayPos[4][0] = 80000*Math.cos(3*Math.PI/4);
        arrayPos[4][1] = 80000*Math.sin(3*Math.PI/4);
        arrayPos[4][2] = 50000;
        arrayPos[5][0] = 80000*Math.cos(-Math.PI/4);
        arrayPos[5][1] = 80000*Math.sin(-Math.PI/4);
        arrayPos[5][2] = 50000;
        arrayPos[6][0] = 80000*Math.cos(5*Math.PI/4);
        arrayPos[6][1] = 80000*Math.sin(5*Math.PI/4);
        arrayPos[6][2] = 50000;
//        arrayPos[7][0] = 0.0;
//        arrayPos[7][1] = 0.0;
//        arrayPos[7][2] = 50000;
//        arrayPos[8][0] = 0.0;
//        arrayPos[8][1] = 0.0;
//        arrayPos[8][2] = 50000;
//        arrayPos[9][0] = 0.0;
//        arrayPos[9][1] = 0.0;
//        arrayPos[9][2] = 50000;
        for(int i = 0;i<cells;i++){
            Control.cells.add(new Cell(i,arrayPos[i][0],arrayPos[i][1],arrayPos[i][2]));
        }
        double maxX = 0;
        double maxY = 0;
        //assumed symmetrical hence only max
        for(Cell c:Control.cells){
            if(c.centreY + c.radius > maxY)
                maxY =  c.centreY + c.radius;
            if(c.centreX + c.radius > maxX)
                maxX =  c.centreX + c.radius;
        }
        for(int i = 0;i<2000;i++){
            Control.hosts.add(new Host(maxX,maxY));
        }
        while(Control.time<10000){
            for(Host h:Control.hosts)
                h.update(1);
            Control.time+=1;
        }
        System.out.println("DONE");
    }
}
