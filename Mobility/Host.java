package Mobility;

import java.util.ArrayList;
import java.util.Random;

public class Host {
    double time;
    double velocity;
    double direction;
    int cell;
    int id;
    double x;
    double y;
    double dx;
    double dy;
    double distance;
    double waitFor;
    double moveFor;
    double rangeX;
    double rangeY;
    boolean isMoving;
    ArrayList<Pair> pairs;
    class Pair{
        double x;
        double y;
        Pair(double x, double y){
            this.x = x;
            this.y = y;
        }
    }
    public Host(int i,double rx,double ry) {
        rangeX = rx;
        rangeY = ry;
        time = 0.0;
        id = i;
        setStartPosition();
        setCell();
        randomDecide();
        pairs = new ArrayList<>();

    }
    public void randomDecide(){
        setMoving();
        if(isMoving){
            setMovingParams();
        }
        else
            setWaitTime();
    }
    public void setStartPosition(){
         x = -rangeX + (new Random().nextDouble())*2*rangeX;
         y = -rangeY + (new Random().nextDouble())*2*rangeY;
    }
    public void setMovingParams(){
        setVelocity();
        setDistance();
        setDirection();
        setDestination();
        moveFor = distance/velocity;
    }
    public void setVelocity(){
        velocity = new Random().nextDouble()*200;

    }
    public void setDistance(){
        distance = new Random().nextDouble()*1000;
    }
    public void setWaitTime(){
        waitFor = new Random().nextDouble()*100;
    }
    public void setCell(){
       cell = Control.nearestCell(x,y);
    }
    public void setDirection(){
        direction = new Random().nextDouble()*2*Math.PI;
    }
    public void setDestination(){
        dx = distance*Math.cos(direction);
        dy = distance*Math.sin(direction);
    }
    public void setMoving(){
        int r = new Random().nextInt(2);
        if(r==0)
            isMoving = false;
        else
            isMoving = true;
    }
    public void move(double dt){
        x += dt*velocity*Math.cos(direction);
        y += dt*velocity*Math.sin(direction);
        if(Math.abs(x)>rangeX||Math.abs(y)>rangeY){
            setStartPosition();
            setCell();
            return;
        }
        int oCell = cell;
        setCell();
        if(cell!=oCell){
            Control.getCell(oCell).handover();
        }
    }
    public void update(double dt){ //returns cell
        if(!isMoving){
            waitFor-=dt;
            if(waitFor <0){
                dt = -waitFor; //as remaining time
                waitFor = 0;
                randomDecide();
                update(dt); //scope for tail call elimination
            }

            //            else if(waitFor>=0) //already reduced waitfor
//            return;
        }
        else if(isMoving){
            moveFor-=dt;
            if(moveFor<0){
                dt = -moveFor;
                move(dt);
                moveFor = 0;
                randomDecide();
                update(dt);
            }
            else if(moveFor>=0)
                move(dt);
               // return;

        }
        pairs.add(new Pair(x,y));
    }
}
