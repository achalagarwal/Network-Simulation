import java.util.ArrayList;

public class BSCDriver {
    public static void reset(){
        Control.jobs = new ArrayList<>(70);
        Control.hList = new ArrayList<>(20);
        Control.time = 0;
        BSC_Control.bscs = new ArrayList<>();
    }
    public static void main(String[] args) {
        for (int j = 0; j < 41; j++) {
            System.out.println("SIMULATION "+j+" HAS BEGUN");
            BSC_Control control = new BSC_Control();
            BSC[] BSCs = new BSC[19];
            for (int i = 0; i < 19; i++) {
                BSCs[i] = new BSC(i);
                BSCs[i].simNumber = j;
                control.addBSC(BSCs[i]);
            }
            Thread[] threads = new Thread[19];

            Control con = new Control();
            con.start();
            System.out.println("hi");
            for (BSC c : BSCs) {
                c.turnOn();
            }
            for (int i = 0; i < 19; i++) {
                threads[i] = new Thread(BSCs[i], Integer.toString(i));
                threads[i].start();
            }
            try {
                threads[0].join();
                con.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            reset();

        }
    }
}

