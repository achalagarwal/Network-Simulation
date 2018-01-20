import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;

import java.util.ArrayList;

public class Control extends Thread{
    int num;
    boolean run;
    static ArrayList<Job> jobs = new ArrayList<>(70);
    static ArrayList<Integer> hList = new ArrayList<>(20);
    static double time = 0;
    static double totalSim = 720;
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
        System.out.println("Entered remove "+ BSC);
        synchronized (hList) {
            hList.remove(new Integer(BSC));
        }
    }
    public static boolean waitForHandoff(int BSC) {
        synchronized (hList) {
            for (Integer i : hList) {
                if (BSC_Control.getBSC(i).hasNeighbour(BSC)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void run(){
        while(time<totalSim) {
            Job j = getJob();
            if(j!=null){
            BSC c = BSC_Control.getBSC(j.cell);
                synchronized (c) {

                    while (c.job!=null) {
                        try {
                            c.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                while (waitForHandoff(j.cell)) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //System.out.println("hi");
                }
                if (j.event == Event.HANDOFF) {
                    synchronized (hList) {
                        hList.add(new Integer(j.cell));
                    }
                }
                synchronized (c) {
                    c.job  = j;
                    time = j.startTime;
                    removeJob(j);
                    c.notifyAll();
                }
                System.out.println(jobs.size());
                synchronized (hList) {
                    for (Integer e : hList)
                        System.out.println("hList " + e.toString());
                }

            }
        }



        for(int i = 19;i<21;i++){
            BSC c = BSC_Control.getBSC(i);
            synchronized (c) {
                c.job = new Job(i,Event.TERMINATE,time);
                System.out.println("Terminate command sent");
                c.notifyAll();
            }

        }
        System.out.println("Hii");
    }
}
