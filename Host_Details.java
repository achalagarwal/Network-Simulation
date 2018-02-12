import java.util.Random;
public class Host_Details {

    double connection_strength;
    double  speed;
    int neighbourId;
    Host_Details(){

        Random r = new Random();

        connection_strength = - r.nextDouble()*r.nextInt(90);

        neighbourId = r.nextInt(6);

        int p = r.nextInt(10);

        if(p<2)
            speed = 0;
        else if(p<4)
            speed = r.nextDouble()*r.nextInt(10);
        else if(p<5)
            speed = r.nextDouble()*r.nextInt(30);
        else if(p<6)
            speed = r.nextDouble()*r.nextInt(40);
        else if(p<7)
            speed = r.nextDouble()*r.nextInt(60);
        else if(p<8)
            speed = r.nextDouble()*r.nextInt(80);
        else if(p<9)
            speed = r.nextDouble()*r.nextInt(100);
        else if(p<10)
            speed = r.nextDouble()*r.nextInt(150);
    }
    public void update(){
        
    }

}
