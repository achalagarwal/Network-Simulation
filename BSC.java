
import javafx.scene.chart.XYChart;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import sun.plugin.net.protocol.jar.CachedJarURLConnection;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
class Channel{
    boolean vacant;
    Priority p;
    Channel(){
        this.vacant = true;
        p = null;
    }}

public class BSC implements Runnable {
    Job job;
    ArrayList<Channel>[] channels;

    //stats
    double[][] data;
    int counter[];
    int check[];
    org.knowm.xchart.XYChart chart[] = new org.knowm.xchart.XYChart[7];
    int totalChannels;
    int guardChannels[];
    int handoffDrops[];
    int ongoingCalls[];
    double probabilities[][];
    int totalHandoffs[];
    int totalNewCalls[];
    int newCallDrops[];
    int occupiedGuardCells[];
    //parameters
    double handoffThreshold[];
    double callArrivalRate[];
    double callTerminationRate[];
    double handoffRate[];
    boolean status;
    int[] neighbours;
    int id;
    SwingWrapper<org.knowm.xchart.XYChart> sw[] = new SwingWrapper[7];

    int consecutiveHandoffs[];
    int consecutiveHandoffsLimit[];
    double alpha1[];
    double alpha2[];
    //controls
    double lastReset;
    int periodHandoffs[];
    int periodHandoffDrops[];
    int periodNewCalls[];
    int periodNewCallDrops[];
    PrintWriter pw;

    BSC(int id) {

        try {
            pw = new PrintWriter("./" + "log_" + Integer.toString(id) + ".txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(id==0) {

            data = new double[14][10000]; //change this range in handin also 20000000
//            for (int i = 0; i < 6; i++)
//                for (int j = 0; j < 10000000; j++)
//                    data[i][j] = 0;


            counter = new int[14];
            check = new int[4];
            for (int i = 0; i < 4; i++)
                check[i] = 0;
            for (int i = 0; i < 14; i++)
                counter[i] = 0;
        }

        consecutiveHandoffsLimit = new int[3]; //set values
        consecutiveHandoffs = new int[3];
        probabilities = new double[3][3];
        guardChannels = new int[3];
        ongoingCalls = new int[3];
        occupiedGuardCells = new int[3];
        totalChannels = 2000;
        channels= new ArrayList[4];
        for(int i = 0;i<4;i++)
            channels[i] = new ArrayList<>();
        for(int i = 0;i<totalChannels;i++)
            channels[3].add(new Channel());
        int currentSize =0 ;
        handoffDrops = new int[3];
        totalHandoffs = new int[3];
        totalNewCalls = new int[3];
        newCallDrops = new int[3];
        handoffThreshold = new double[3];
        callTerminationRate = new double[3];

        status = false;
        callArrivalRate = new double[3];
        this.id = id;
        lastReset = 0.0;
        periodHandoffs = new int[3];
        periodHandoffDrops = new int[3];
        periodNewCalls = new int[3];
        periodNewCallDrops = new int[3];
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
    @Deprecated
    int getTotalOngoingCalls() {
        return ongoingCalls[0] + ongoingCalls[1] + ongoingCalls[2];
    }


    int getCurrentChannelsInUse(){
        int size = 0;
        for(int i =0;i<4;i++) {
            size+=getOccupiedChannels(i);
        }
        return size;
    }
    @Deprecated
    int getTotalOccupied(){
        return occupiedGuardCells[0] + occupiedGuardCells[1] + occupiedGuardCells[2];
    }

    int getOccupiedChannels(int flag){
        int count = 0;
        for(int i = 0;i<channels[flag].size();i++){
            if(channels[flag].get(i)!=null)
            if(!(channels[flag].get(i).vacant))
                count++;
        }
        return count;
    }
    int getTotalOccupiedCells(){
        int count = 0;
        for(int i = 0;i <3;i++)
            count+=getOccupiedChannels(i);
        return count;
    }
    public boolean addNewHandIn(int flag) {
        Priority p = getPriority(flag);
        int a = getVacantChannel(3);
        if (a != -1) {
            channels[3].get(a).vacant = false;
            channels[3].get(a).p = p;
            return true;
        }
        a = getVacantChannel(flag);
        if (a != -1) {
            channels[flag].get(a).vacant = false;
            channels[flag].get(a).p = p;
            return true;
        }
        return false;
    }
    @Deprecated
    public boolean checkHandin(int flag) {
        if(totalChannels-getTotalGuardChannels()-(getTotalOngoingCalls()-getTotalOccupied())>0)
            return true;
        if(guardChannels[flag]-occupiedGuardCells[flag]>0){
            occupiedGuardCells[flag]++;
            return true;
        }
        return false;





        /*
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
        */




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
        if (!addNewHandIn(flag)) {
            periodHandoffDrops[flag]++;//here present ratio does not use the currently dropped handoff, reconfirm
            handoffDrops[flag]++;
            presentRatio = periodHandoffDrops[flag] * 1.0 / periodHandoffs[flag];
            if (presentRatio >=  handoffThreshold[flag]) { //include alpha1
                addGuardCell(flag);
            }
            consecutiveHandoffs[flag] = 0;
        } else {// if (ongoingCalls[flag] < totalChannels)  //update algo
            presentRatio = periodHandoffDrops[flag] * 1.0 / periodHandoffs[flag];
            consecutiveHandoffs[flag]++;
            if (presentRatio <= handoffThreshold[flag] && consecutiveHandoffs[flag] == consecutiveHandoffsLimit[flag] - 1) //include alpha2
                freeGuardCell(flag);
            //ongoingCalls[flag]++;
        }
        Control.removeFromHList(cell);
        if (this.id == 0 ) {
            if (check[flag] == 10) {
                if (counter[flag] < 10000000) {
                    check[flag] = 0;
                    data[flag * 2][counter[flag * 2]] = channels[flag].size();
                    data[flag * 2 + 1][counter[flag * 2 + 1]] = t;
                    data[6 + flag * 2][counter[6 + flag * 2]] = presentRatio * 100.0;
                    data[6 + flag * 2 + 1][counter[6 + flag * 2 + 1]] = t;
                    counter[2 * flag]++;
                    counter[flag * 2 + 1]++;
                    counter[6 + flag * 2]++;
                    counter[6 + flag * 2 + 1]++;
                    //   chart(flag);
                }
//            else
//                System.out.println("Data Array insufficient");
            } else
                check[flag]++;
        }
        if (this.id == 0) {
            pw.println(t + "      " + "HANDIN" + "    "+ getPriority(flag));
            pw.println("OCCUPIED CHANNELS  -> " + getOccupiedChannels(0) + " " + getOccupiedChannels(1) + " " + getOccupiedChannels(2) +" " + getOccupiedChannels(3));
            pw.println("GUARD CHANNELS -> " + channels[0].size() + " " + channels[1].size() + " " + channels[2].size() + " " + channels[3].size());
            //pw.println("OCCUPIED CHANNELS -> " + occupiedGuardCells[0] + occupiedGuardCells[1] + occupiedGuardCells[2]);
            pw.println("***********************");
        }
    }
    @Deprecated
    void removeCall(int flag) {
        if (occupiedGuardCells[flag] > 0){
            occupiedGuardCells[flag]--;
        }
    }
    Priority getPriority(int flag){
        if(flag == 0)
            return Priority.REALTIME;
        else if(flag == 1)
            return Priority.STREAMING;
        else if(flag == 2)
            return Priority.BACKGROUND;
        else
            return null;
    }
    int getPriority(Priority p){
        int flag;
        if (job.priority == Priority.REALTIME)
            flag = 0;
        else if (job.priority == Priority.STREAMING)
            flag = 1;
        else if(job.priority == Priority.BACKGROUND)
            flag = 2;
        else
            flag = -1;
        return flag;
    }
    boolean freeChannel(int flag){
        Priority p = getPriority(flag);
        for(int i = 0;i<3;i++){
            if(i!=flag){
                for(Channel c: channels[i]) {
                    if (!c.vacant) {
                        c.p = null;
                        c.vacant = true;
                        return true;
                    }
                }
            }
        }
        for(Channel c:channels[flag]){
            if (!c.vacant) {
                c.p = null;
                c.vacant = true;
                return true;
            }
        }
        for(Channel c:channels[3]){
            if (!c.vacant) {
                c.p = null;
                c.vacant = true;
                return true;
            }
        }
        return false;
    }
    public void handoff() {
        int flag = getPriority(job.priority);
        double t = job.startTime + StdRandom.exp(handoffRate[flag]);
        Control.addJob(new Job(this.id, Event.HANDOFF, t, job.priority));
        if (freeChannel(flag)) {

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
        } else { //should never reach this line as all cells have neighbours
            //pw.println("No calls to Handoff");
            Control.removeFromHList(id);
//            this.data.nextHandoff = totalSimulationTime + 1;
//            this.data.nextTermination = totalSimulationTime +1;
        }
    }
    @Deprecated
    public int getTotalGuardChannels() {
        return guardChannels[0] + guardChannels[1] + guardChannels[2];
    }

    public int getTotalGuardCells(){
        int count = 0;
        for(int i = 0;i<3;i++)
            count+=channels[i].size();
        return count;
    }
    int getVacantChannel(int flag){
        for(int i = 0;i<channels[flag].size();i++){
            if(channels[flag].get(i)!=null)
            if(channels[flag].get(i).vacant)
                return i;
        }
        return -1;
    }
    public boolean addNewCall(int flag){
        Priority p = getPriority(flag);
        int a = getVacantChannel(3);
        if(a!=-1) {
            channels[3].get(a).vacant = false;
            channels[3].get(a).p = p;
            return true;
        }
        double r = new Random().nextDouble();
        for(int i = 2;i>=0;i--) {
            if (r < probabilities[flag][i]) {
                a = getVacantChannel(i);
                if(a!=-1){
                    channels[i].get(a).vacant = false;
                    channels[i].get(a).p = p;
                    return true;
                }
            }
        }
        return false;
    }
    @Deprecated
    public boolean newCallCheck(int flag) {
        if(totalChannels-getTotalGuardChannels()-(getTotalOngoingCalls()-getTotalOccupied())>0)
            return true;
        else{
            double r = new Random().nextDouble();
            for(int i = 0;i<3;i++) {
                if (r < probabilities[flag][i]) {
                    if (guardChannels[i] - occupiedGuardCells[i]>0){
                        occupiedGuardCells[i]++;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void connect() {

        int flag = getPriority(job.priority);
        totalNewCalls[flag]++;
        periodNewCalls[flag]++;
        double t = job.startTime + StdRandom.exp(callArrivalRate[flag]);
        Control.addJob(new Job(this.id, Event.CONNECT, t, job.priority));
        if (addNewCall(flag)) {// total - guard = Ca

//            if(ongoingCalls == 0){
//                resetTermination();
//                resetHandoff();
//            }
           // ongoingCalls[flag]++;
            // print();
            //pw.println("New Connection Request in " + Thread.currentThread().getName());

        } else {
            newCallDrops[flag]++;
            periodNewCallDrops[flag]++;
            //print();
            //  pw.println("New Connection Failed in " + Thread.currentThread().getName());

        }
        if (this.id == 0  && flag==0) {
            if(check[3] == 20) {
                check[3] = 0;
                if (counter[12] < 10000000 && counter[13] < 10000000) {
                    data[12][counter[12]++] = getCurrentChannelsInUse();
                    data[13][counter[13]++] = t;
                }
            }
            else
                check[3]++;
        }
    }


    public void disconnect() {

        int flag = getPriority(job.priority);
        double t = job.startTime + StdRandom.exp(callTerminationRate[flag]);
        Control.addJob(new Job(this.id, Event.DISCONNECT, t, flag));
        freeChannel(flag);
            //print();
            //  pw.println("Call Termination in " + Thread.currentThread().getName());
//                if(ongoingCalls == 0){
//                    resetHandoff();
//                    resetTermination();
//                }

    }
    public void addGuardCell(int flag){
        Channel c=null;
        for(int i = 3;i>flag;i--) {
            if (channels[i].size() > 0) {
                channels[flag].add(channels[i].get(0));
                // assert c!=null;
                channels[i].remove(0);
                return;
            }
        }
           // channels[flag].add(c);
    }
    @Deprecated
    public void incrementGuardChannel(int flag) {
        if (totalChannels-getTotalGuardChannels()>0) {
            guardChannels[flag]++;
            occupiedGuardCells[flag]++;
            //  pw.println("Guard Channels increased to" + guardChannels);
        }
    }
    public void freeGuardCell(int flag){
        Channel c=null;
        if(channels[flag].size()==0)
            return;
        if(channels[flag].size()>0) {
            channels[3].add(channels[flag].get(0));
            // assert c!=null;
            channels[flag].remove(0);
           // channels[3].add(c);
        }
    }
    @Deprecated
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
                periodNewCalls[i] = 0;
                periodNewCallDrops[i] = 0;
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
        if (this.id == 0) {
            pw.println(job.startTime + "      " + job.event + "    "+ job.priority);
            //pw.println("OCCUPIED CHANNELS  -> " + getOccupiedChannels(0) + " " + getOccupiedChannels(1) + " " + getOccupiedChannels(2) +" " + getOccupiedChannels(3));
            pw.println("GUARD CHANNELS -> " + channels[0].size() + " " + channels[1].size() + " " + channels[2].size() + " " + channels[3].size());
            //pw.println("OCCUPIED CHANNELS -> " + occupiedGuardCells[0] + occupiedGuardCells[1] + occupiedGuardCells[2]);
            pw.println("***********************");
        }
        //pw.println(job.startTime + " " + job.event + " " + ongoingCalls[0] + " " + ongoingCalls[1] + " " + ongoingCalls[2] + " " + handoffDrops[0] + " " + handoffDrops[0] + " " + handoffDrops[1] + " " + handoffDrops[2] + " " + guardChannels[0] + " " + guardChannels[1] + " " + guardChannels[2] + " " + newCallDrops[0] + " " + newCallDrops[1] + " " + newCallDrops[2]);
        // pw.println("Event " + job.event);
        //pw.println("Ongoing Calls "+ ongoingCalls);


    }

    public void initParams() {
        consecutiveHandoffsLimit = new int[]{4, 2, 2}; //set values
        probabilities = new double[][]{{0.05, 0.5, 0.8}, {0.0, 0.05, 0.2}, {0.0, 0.0, 0.05}};
        handoffThreshold = new double[]{0.01, 0.1, 0.5};
        callTerminationRate = new double[]{0.05, 0.05, 0.05};
        handoffRate = new double[]{0.01, 0.01, 0.01};
        callArrivalRate = new double[]{0.5, 0.5, 0.5};
    }

    public void initJobs() {
        Priority p;
        for (int i = 0; i < 3; i++) {
//            if (i == 0)
//                p = Priority.REALTIME;
//            else if (i == 1)
//                p = Priority.STREAMING;
//            else
//                p = Priority.BACKGROUND;
//            ongoingCalls[i] = 1;
            p = getPriority(i);
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
        if(this.id == 0)
            System.out.println("in cell 0");
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
                print0();
                if (job.startTime - lastReset >= 2500) {
                    reset(1);
               //     print0();
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
        double tempData[][] = new double[14][];
        for(int i = 0;i<14;i+=2){
            if(counter[i]<counter[i+1])
                counter[i+1] = counter[i];
            else
                counter[i] = counter[i+1];
        }
        for(int i =0;i<14;i++){
            tempData[i] = new double[counter[i]];
            if(counter[i]== 10000000)
                System.out.println("Overflow of data in "+i);
            for(int j = 0;j<counter[i];j++)
                tempData[i][j] = data[i][j];
           // tempData[i] = Arrays.copyOf(data[i],counter[i]);
        }

            for (int i = 0; i < 7; i++) {
            if(tempData[2*i+1].length == tempData[2*i].length)
                chart[i] = QuickChart.getChart("Chart " + i, "Time", "Data" + i, "graph" + i, tempData[2 * i + 1], tempData[2 * i]);
            else {
                System.out.println("Data arrays not same for i = " + i + " the values are: data[i].l = " + tempData[2 * i + 1].length + " and data[i+1].l = " + tempData[2 * i].length);
                chart[i] = QuickChart.getChart("Chart " + i, "Time", "Data" + i, "graph" + i, new double[]{1,2}, new double[]{1,2});
            }
                //sw[i] = new SwingWrapper(chart[i]);
                //sw[i].displayChart();
            }
            for (int i = 0; i < 7; i++) {
                System.out.println("Printing Chart "+ (i+1));
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
