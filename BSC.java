
import javafx.scene.chart.XYChart;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class BSC implements Runnable {
    Job job;
    //stats
    double[][] data;
    int counter[];
    org.knowm.xchart.XYChart chart[] = new org.knowm.xchart.XYChart[6];
    int totalChannels;
    int guardChannels[];
    int handoffDrops[];
    int ongoingCalls[];
    double probabilities[][];
    int totalHandoffs[];
    int totalNewCalls[];
    int newCallDrops[];
    //parameters
    double handoffThreshold[];
    double callArrivalRate[];
    double callTerminationRate[];
    double handoffRate[];
    boolean status;
    int[] neighbours;
    int id;
    SwingWrapper<org.knowm.xchart.XYChart> sw[] = new SwingWrapper[6];

    int consecutiveHandoffs[];
    int consecutiveHandoffsLimit[];
    double alpha1[];
    double alpha2[];
    //controls
    double lastReset;
    int periodHandoffs[];
    int periodHandoffDrops[];
    PrintWriter pw;

    BSC(int id) {

        try {
            pw = new PrintWriter("./" + "log_" + Integer.toString(id) + ".txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(id==0) {

            data = new double[12][20000000]; //change this range in handin also
//            for (int i = 0; i < 6; i++)
//                for (int j = 0; j < 10000000; j++)
//                    data[i][j] = 0;
        }

        counter = new int[12];
        for(int i =0;i<12;i++)
            counter[i] = 0;
        consecutiveHandoffsLimit = new int[3]; //set values
        consecutiveHandoffs = new int[3];
        probabilities = new double[3][3];
        guardChannels = new int[3];
        ongoingCalls = new int[3];
        totalChannels = 3000;
        handoffDrops = new int[3];
        totalHandoffs = new int[3];
        totalNewCalls = new int[3];
        newCallDrops = new int[3];
        handoffThreshold = new double[3];
        callTerminationRate = new double[3];
        handoffRate = new double[3];
        status = false;
        callArrivalRate = new double[3];
        this.id = id;
        lastReset = 0.0;
        periodHandoffs = new int[3];
        periodHandoffDrops = new int[3];
        setNeighbours();
    }

    public synchronized void turnOn() {
        status = true;
        this.notifyAll();
    }

    public void turnOff() {
        status = false;
    }

    private void setNeighbours() {
        if (id == 0) {
            neighbours = new int[]{1, 2, 3, 4, 5, 6};
        }
        if (id == 1) {
            neighbours = new int[]{2, 8, 7, 18, 6, 0};
        }
        if (id == 2) {
            neighbours = new int[]{1, 0, 3, 8, 9, 10};
        }
        if (id == 3) {
            neighbours = new int[]{0, 2, 4, 10, 11, 12};
        }
        if (id == 4) {
            neighbours = new int[]{0, 3, 5, 12, 13, 14};
        }
        if (id == 5) {
            neighbours = new int[]{0, 4, 6, 14, 15, 16};
        }
        if (id == 6) {
            neighbours = new int[]{0, 1, 5, 16, 17, 18};
        }
        if (id == 7) {
            neighbours = new int[]{1, 8, 18};
        }
        if (id == 8) {
            neighbours = new int[]{1, 2, 7, 9};
        }
        if (id == 9) {
            neighbours = new int[]{2, 8, 10};
        }
        if (id == 10) {
            neighbours = new int[]{2, 3, 9, 11};
        }
        if (id == 11) {
            neighbours = new int[]{3, 10, 12};
        }
        if (id == 12) {
            neighbours = new int[]{3, 4, 11, 13};
        }
        if (id == 13) {
            neighbours = new int[]{4, 12, 14};
        }
        if (id == 14) {
            neighbours = new int[]{4, 5, 13, 15};
        }
        if (id == 15) {
            neighbours = new int[]{5, 14, 16};
        }
        if (id == 16) {
            neighbours = new int[]{5, 6, 15, 17};
        }
        if (id == 17) {
            neighbours = new int[]{6, 16, 18};
        }
        if (id == 18) {
            neighbours = new int[]{1, 6, 7, 17};
        }
        if (id == 19) {
            neighbours = new int[]{20};
        }
        if (id == 20) {
            neighbours = new int[]{19};
        }
    }

    public boolean hasNeighbour(int x) {
        for (int i = 0; i < neighbours.length; i++)
            if (x == neighbours[i])
                return true;
        return false;
    }

    int getTotalOngoingCalls() {
        return ongoingCalls[0] + ongoingCalls[1] + ongoingCalls[2];
    }

    public boolean checkHandin(int flag) {
        if(guardChannels[flag]>ongoingCalls[flag])
            return true;
        else if(getTotalOngoingCalls()>=totalChannels)
            return false;
        int free = totalChannels-getTotalOngoingCalls();
        for(int i = 0;i<flag;i++){
            if(ongoingCalls[i]<guardChannels[i])
            free-=guardChannels[i]-ongoingCalls[i];
        }
        return free>0;
        /*
        if(getTotalOngoingCalls()<totalChannels)
            return true;
        else if()
        if(totalChannels < getTotalGuardChannels())
            return true;
        else if(false){
            for(int i = 2;i>flag;i--){
                if(ongoingCalls[i]<guardChannels[i]) {
                    decrementGuardChannel(i);
                    return true;
                }
            }
        }
        //else
        return false;
        */
    }

    public void handin(int cell, int flag, double t) {//shift the remove from hlist to caller and use only flag as parameter?
        totalHandoffs[flag]++;
        periodHandoffs[flag]++;
        //pw.println(" New handoff connection request received from Cell Number - " + cell);
        //pw.println("Handoff block entered in " + this.cellId);
        double presentRatio;

        if (!checkHandin(flag)) {
            periodHandoffDrops[flag]++;//here present ratio does not use the currently dropped handoff, reconfirm
            handoffDrops[flag]++;
            presentRatio = periodHandoffDrops[flag] * 1.0 / periodHandoffs[flag];
            if (presentRatio >=  handoffThreshold[flag]) { //include alpha1
                incrementGuardChannel(flag);
            }
            consecutiveHandoffs[flag] = 0;
        } else {// if (ongoingCalls[flag] < totalChannels)  //update algo
            presentRatio = periodHandoffDrops[flag] * 1.0 / periodHandoffs[flag];
            consecutiveHandoffs[flag]++;
            if (presentRatio <= handoffThreshold[flag] && consecutiveHandoffs[flag] == consecutiveHandoffsLimit[flag] - 1) //include alpha2
                decrementGuardChannel(flag);
            ongoingCalls[flag]++;
        }
        Control.removeFromHList(cell);
        if (this.id == 0 ) {
            if(counter[flag]<20000000) {
                data[flag * 2][counter[flag*2]] = guardChannels[flag];
                data[flag * 2 + 1][counter[flag*2+1]] = t;
                data[6 + flag * 2][counter[6+flag*2]] = presentRatio * 100.0;
                data[6 + flag * 2 + 1][counter[6+flag*2+1]] = t;
                counter[2*flag]++;
                counter[flag*2 +1]++;
                counter[6 + flag*2]++;
                counter[6 + flag*2+1]++;
                //   chart(flag);
            }
            else
                System.out.println("Data Array insufficient");
        }
        if (this.id == -1)
            pw.println(t + " " + "HAND-IN" + " " + ongoingCalls[0] + " " + ongoingCalls[1] + " " + ongoingCalls[2] + " " + " " + handoffDrops[0] + " " + handoffDrops[1] + " " + handoffDrops[2] + " " + guardChannels[0] + " " + guardChannels[1] + " " + guardChannels[2] + " " + newCallDrops[0] + " " + newCallDrops[1] + " " + newCallDrops[2]);

    }

    public void handoff() {
        int flag;
        if (job.priority == Priority.REALTIME)
            flag = 0;
        else if (job.priority == Priority.STREAMING)
            flag = 1;
        else  //if(job.priority == Priority.BACKGROUND)
            flag = 2;
        double t = job.startTime + StdRandom.exp(handoffRate[flag]);
        Control.addJob(new Job(this.id, Event.HANDOFF, t, job.priority));
        if (ongoingCalls[flag] > 0) {
            ongoingCalls[flag]--;
//            if(ongoingCalls == 0){
//                resetTermination();
//                resetHandoff();
//            }
            //   print();
            if (neighbours.length > 0) {
                int random = new Random().nextInt(neighbours.length);
                //pw.println("Handoff Call sent to Cell Number - " + neighbours[random]);
                BSC_Control.getBSC(neighbours[random]).handin(this.id, flag, job.startTime);
            }
        } else { //should never reach this line
            //pw.println("No calls to Handoff");
            Control.removeFromHList(id);
//            this.data.nextHandoff = totalSimulationTime + 1;
//            this.data.nextTermination = totalSimulationTime +1;
        }
    }

    public int getTotalGuardChannels() {
        return guardChannels[0] + guardChannels[1] + guardChannels[2];
    }

    public boolean newCallCheck(int flag) {

        if (totalChannels - getTotalGuardChannels() > getTotalOngoingCalls())
            return true;
        else{
            double r = new Random().nextDouble();
            for(int i = 0;i<3;i++) {
                if (guardChannels[i] - ongoingCalls[i]>0){
                    if (r < probabilities[flag][i]) {
                        ongoingCalls[i]++;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void connect() {

        int flag;
        if (job.priority == Priority.REALTIME)
            flag = 0;
        else if (job.priority == Priority.STREAMING)
            flag = 1;
        else  //if(job.priority == Priority.BACKGROUND)
            flag = 2;
        totalNewCalls[flag]++;
        double t = job.startTime + StdRandom.exp(callArrivalRate[flag]);
        Control.addJob(new Job(this.id, Event.CONNECT, t, job.priority));
        if (newCallCheck(flag)) {// total - guard = Ca

//            if(ongoingCalls == 0){
//                resetTermination();
//                resetHandoff();
//            }
            ongoingCalls[flag]++;
            // print();
            //pw.println("New Connection Request in " + Thread.currentThread().getName());

        } else {
            newCallDrops[flag]++;
            //print();
            //  pw.println("New Connection Failed in " + Thread.currentThread().getName());

        }
    }

    public void disconnect() {

        int flag;
        if (job.priority == Priority.REALTIME)
            flag = 0;
        else if (job.priority == Priority.STREAMING)
            flag = 1;
        else  //if(job.priority == Priority.BACKGROUND)
            flag = 2;
        double t = job.startTime + StdRandom.exp(callTerminationRate[flag]);
        Control.addJob(new Job(this.id, Event.DISCONNECT, t, flag));
        if (ongoingCalls[flag] > 0) {
            ongoingCalls[flag]--;
            //print();
            //  pw.println("Call Termination in " + Thread.currentThread().getName());
//                if(ongoingCalls == 0){
//                    resetHandoff();
//                    resetTermination();
//                }
        } else {
            //    pw.println("No calls to terminate in " + Thread.currentThread().getName());
        }

    }

    public void incrementGuardChannel(int flag) {
        if (guardChannels[flag] < totalChannels) {
            guardChannels[flag]++;
            //  pw.println("Guard Channels increased to" + guardChannels);
        }
    }

    public void decrementGuardChannel(int flag) {
        if (guardChannels[flag] > 0) {
            guardChannels[flag]--;
            // pw.println("Guard Channels decreased to" + guardChannels);
        }
    }

    public void reset(int flag) {
        // pw.println("job.startTime  job.event  job.priority  ongoingCalls[0]   ongoingCalls[1]   ongoingCalls[2]  handoffDrops[0]   handoffDrops[0]    handoffDrops[1]   handoffDrops[2]    guardChannels[0]    guardChannels[1   guardChannels[2]         newCallDrops[0]+      newCallDrops[1]+ newCallDrops[2]");
        if(flag == 1) {
            lastReset = job.startTime;
            for (int i = 0; i < 3; i++) {
                periodHandoffs[i] = 0;
                periodHandoffDrops[i] = 0;
            }
        }
    }

    public void print0() {

//        pw.println(  "**************************************" );
//        pw.println("Time " + job.startTime);
//        pw.println("Event " + job.event);
//        pw.println("Ongoing Calls "+ ongoingCalls);
//        pw.println(  "**************************************" );
        if (this.id == 0)
            pw.println(job.startTime + " " + job.event + " " + ongoingCalls[0] + " " + ongoingCalls[1] + " " + ongoingCalls[2] + " " + handoffDrops[0] + " " + handoffDrops[0] + " " + handoffDrops[1] + " " + handoffDrops[2] + " " + guardChannels[0] + " " + guardChannels[1] + " " + guardChannels[2] + " " + newCallDrops[0] + " " + newCallDrops[1] + " " + newCallDrops[2]);
        // pw.println("Event " + job.event);
        //pw.println("Ongoing Calls "+ ongoingCalls);


    }

    public void initParams() {
        consecutiveHandoffsLimit = new int[]{6, 4, 2}; //set values
        probabilities = new double[][]{{0.05, 0.2, 0.4}, {0.0, 0.05, 0.2}, {0.0, 0.0, 0.05}};
        handoffThreshold = new double[]{0.002, 0.05, 0.5};
        callTerminationRate = new double[]{0.005, 0.1, 0.005};
        handoffRate = new double[]{0.01, 0.05, 0.1};
        callArrivalRate = new double[]{0.5, 0.3, 0.3};
    }

    public void initJobs() {
        Priority p;
        for (int i = 0; i < 3; i++) {
            if (i == 0)
                p = Priority.REALTIME;
            else if (i == 1)
                p = Priority.STREAMING;
            else
                p = Priority.BACKGROUND;
            ongoingCalls[i] = 1;
            double t = StdRandom.exp(callArrivalRate[i]);
            Control.addJob(new Job(this.id, Event.CONNECT, t, p));
            t = StdRandom.exp(callTerminationRate[i]);
            Control.addJob(new Job(this.id, Event.DISCONNECT, t, p));
            t = StdRandom.exp(handoffRate[i]);
            Control.addJob(new Job(this.id, Event.HANDOFF, t, p));
        }
    }

    public void chart(int flag) {
        if (counter[flag] == 1) {
            chart[0] = QuickChart.getChart("Simple XChart Real-time Demo", "Time", "Guard Channels for Real Time", "graph", data[1], data[0]);
            sw[0] = new SwingWrapper(chart[0]);
            sw[0].displayChart();
        } else {
            chart[0].updateXYSeries("graph", data[1], data[0], (double[]) null);
            sw[0].repaintChart();
        }
    }

    public void run() {
        // pw.println("job.startTime job.event ongoingCalls handoffDropPercent guardCells newCallDrop% ");
        pw.println("job.startTime  job.event  ongoingCalls[0]   ongoingCalls[1]   ongoingCalls[2]  handoffDrops[0]   handoffDrops[0]    handoffDrops[1]   handoffDrops[2]    guardChannels[0]    guardChannels[1   guardChannels[2]         newCallDrops[0]+      newCallDrops[1]+ newCallDrops[2]");
        initParams();
        initJobs();
        while (true) {
            synchronized (this) {
                if (!status) {
                    break;
                }

                while (job == null) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // printer System.out.println(Control.BLUE + this.id + " has started job" + Control.RESET);
                //print0();
                if (job.startTime - lastReset >= 25000) {
                    reset(1);
                    print0();
                }
                if (job.event == Event.HANDOFF)
                    handoff();
                else if (job.event == Event.CONNECT)
                    connect();
                else if (job.event == Event.DISCONNECT)
                    disconnect();
                else if (job.event == Event.TERMINATE) {
                    break;
                }
                job = null;
//                if(id==0) {
//                    initdata[0][0] = guardChannels[0];
//                    initdata[1][1] = job.startTime;
//                   // chart.updateXYSeries("sine",initdata[1],initdata[0]);
//                }
                // printer System.out.println(this.id + " has ended job");
                this.notifyAll();
            }
        }
        pw.flush();
        pw.close();
        if (id == 0) {
        double tempData[][] = new double[12][];

        for(int i =0;i<12;i++){
            //tempData[i] = new double[counter[i]];
            if(counter[i]== 20000000)
                System.out.println("Overflow of data in "+i);
            tempData[i] = Arrays.copyOf(data[i],counter[i]);
        }

            for (int i = 0; i < 6; i++) {
                chart[i] = QuickChart.getChart("Chart " + i, "Time", "Data" + i, "graph" + i, tempData[2 * i + 1], tempData[2 * i]);
                //sw[i] = new SwingWrapper(chart[i]);
                //sw[i].displayChart();
            }
            for (int i = 0; i < 6; i++) {
                try {
                    // BitmapEncoder.saveBitmap(chart, "./Sample_Chart", BitmapEncoder.BitmapFormat.PNG);
                    BitmapEncoder.saveBitmapWithDPI(chart[i], "./Sample_Chart_300_DPI" + i, BitmapEncoder.BitmapFormat.PNG, 600);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            /*
            double[] xData = new double[] { 0.0, 1.0, 2.0 };
            double[] yData = new double[] { 2.0, 1.0, 0.0 };

            // Create Chart
            org.knowm.xchart.XYChart charts = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", xData, yData);
            try {
                // BitmapEncoder.saveBitmap(chart, "./Sample_Chart", BitmapEncoder.BitmapFormat.PNG);
                BitmapEncoder.saveBitmapWithDPI(charts, "./SSSS", BitmapEncoder.BitmapFormat.PNG, 600);
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
        }
    }
}
//print metadata
