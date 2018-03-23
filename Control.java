import com.sun.org.apache.regexp.internal.RE;
import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
/*
    Random Factor prevalence:
    If a cell needs to generate a handoff, it must check its neightbours for their current status, and not start the handoff process if any neighbour is busy.
    This perfection may not be desired, owing to random chances and the time difference is too small to care.
 */

import java.util.ArrayList;

public class Control extends Thread{
    public static  String BLACK = "\u001B[30m";
    public static  String RED = "\u001B[31m";
    public static  String GREEN = "\u001B[32m";
    public static  String YELLOW = "\u001B[33m";
    public static  String BLUE = "\u001B[34m";
    public static  String PURPLE = "\u001B[35m";
    public static  String CYAN = "\u001B[36m";
    public static  String WHITE = "\u001B[37m";
    //Reset code
    public static  String RESET = "\u001B[0m";
    int num;
    boolean run;
    static ArrayList<Job> jobs = new ArrayList<>(70);
    static ArrayList<Integer> hList = new ArrayList<>(20);
    static double time = 0;
    static double totalSim = 172000;
    public  static void addJob(Job n) {
        synchronized (jobs) {
            int i = 0;
            for (Job j : jobs) {
                if (j.startTime > n.startTime) {
                    jobs.add(i, n);
                    return;
                }
                i++;
            }
            jobs.add(i, n);
        }
    }
    public static Job getJob() {
        synchronized (jobs) {
            if (jobs.isEmpty())
                return null;
            return jobs.get(0);
        }
    }
    public static void removeJob(Job j) {
        synchronized (jobs) {
            if (jobs.isEmpty())
                return;
             jobs.remove(j);
        }
    }
    public static  void removeFromHList(int BSC){
        System.out.println(YELLOW + "Removed "+ BSC + " from H List"+RESET);
        synchronized (hList) {
            hList.remove(new Integer(BSC));
            hList.notifyAll();
        }
    }
    public static boolean waitForHandoff(int BSC) {
        for (Integer i : hList) {
            if (BSC_Control.getBSC(i).hasNeighbour(BSC)) {
                System.out.println(RED+BSC+ " waiting for neighbour cell " + i+RESET);
                return true;
            }
        }
        return false;
    }

    public void run(){
        while(time<totalSim) {
            Job j = getJob();
            if(j!=null){
            BSC c = BSC_Control.getBSC(j.cell);
                synchronized (c) {

                    while (c.job!=null) {
                        try {
                            System.out.println("Waiting for cell  "+ j.cell +" to finish pending job");
                            c.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Cell  "+ j.cell +" finished pending job, now can be offered new job");
                synchronized (hList) {
                    while (waitForHandoff(j.cell)) {
                        try {
                            System.out.println( j.cell+" waiting for neighbours");
                            hList.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //System.out.println("hi");
                    }
                    System.out.println( j.cell+" has finished waiting for neighbours");
                    if (j.event == Event.HANDOFF) {
                        System.out.println(YELLOW+  j.cell+ " has entered the H List" + RESET);
                        hList.add(new Integer(j.cell));
                    }
                    hList.notifyAll();
                }
                    c.job  = j;
                    System.out.println(BLUE + "Job given to "+ j.cell + RESET);
                    time = j.startTime;
                    removeJob(j);
                    c.notifyAll();
                }
               // System.out.println(jobs.size());

            }
        }


        for(int i = 0;i<19;i++) {
            BSC c = BSC_Control.getBSC(i);
            synchronized (c) {
                c.job = new Job(i, Event.TERMINATE, time);
                System.out.println("Terminate command sent");
                c.notifyAll();
            }

        }
        System.out.println("Hii");
    }
}
