public class BSCDriver {
    public static void main(String[] args) {
        BSC_Control control = new BSC_Control();
        BSC[] BSCs = new BSC[19];
        for (int i = 0; i < 19; i++) {
            BSCs[i] = new BSC(i);
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
    }
}

