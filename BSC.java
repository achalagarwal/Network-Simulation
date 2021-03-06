
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
import java.util.Iterator;
import java.util.Random;
class Channel{
    boolean vacant;
    Priority p;
    Channel(){
        this.vacant = true;
        p = Priority.BACKGROUND;
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
    int simNumber;
    int guardChannels[];
    int handoffDrops[];
    int ongoingCalls[];
    double probabilities[][];
    int totalHandoffs[];
    int totalNewCalls[];
    int newCallDrops[];
    int occupiedGuardCells[];
    //parameters
    int vacant[];
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



        if (id == 0) {

            data = new double[14][500000]; //change this range in handin also 20000000
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
        vacant = new int[4];
        consecutiveHandoffsLimit = new int[3]; //set values
        consecutiveHandoffs = new int[3];
        probabilities = new double[3][3];
        guardChannels = new int[3];
        ongoingCalls = new int[3];
        occupiedGuardCells = new int[3];
        totalChannels = 300;
        channels = new ArrayList[4];
        for (int i = 0; i < 4; i++)
            channels[i] = new ArrayList<>();
        for (int i = 0; i < totalChannels; i++)
            channels[3].add(new Channel());
        vacant[3] = totalChannels;
        int currentSize = 0;
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


    int getCurrentChannelsInUse() {
        int size = 0;
        for (int i = 0; i < 4; i++) {
            size += getOccupiedChannels(i);
        }
        return size;
    }

    @Deprecated
    int getTotalOccupied() {
        return occupiedGuardCells[0] + occupiedGuardCells[1] + occupiedGuardCells[2];
    }

    int getOccupiedChannels(int flag) {
        int count = 0;
        for (int i = 0; i < channels[flag].size(); i++) {
            if (channels[flag].get(i) != null)
                if (!(channels[flag].get(i).vacant))
                    count++;
        }
        return count;
    }

    int getTotalOccupiedCells() {
        int count = 0;
        for (int i = 0; i < 3; i++)
            count += getOccupiedChannels(i);
        return count;
    }

    public boolean addNewHandIn(int flag) {
        Priority p = getPriority(flag);
        int a;
        if(vacant[flag]>0) {
            a = getVacantChannel(flag);
            if (a != -1) {
                channels[flag].get(a).vacant = false;
                channels[flag].get(a).p = p;
                vacant[flag]--;
                return true;
            }
        }
        if(vacant[3]>0) {
             a = getVacantChannel(3);
            if (a != -1) {
                channels[3].get(a).vacant = false;
                vacant[3]--;
                channels[3].get(a).p = p;
                return true;
            }
        }

        //check vacant[flag]>0

        double r = Math.random();
        if(flag<2) {
            if(r<0.3){
                if(vacant[flag+1]>0){
                    a = getVacantChannel(flag+1);
                    if(a!=-1) {
                        channels[flag + 1].get(a).vacant = false;
                        channels[flag + 1].get(a).p = p;
                        vacant[flag]--;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Deprecated
    public boolean checkHandin(int flag) {
        if (totalChannels - getTotalGuardChannels() - (getTotalOngoingCalls() - getTotalOccupied()) > 0)
            return true;
        if (guardChannels[flag] - occupiedGuardCells[flag] > 0) {
            occupiedGuardCells[flag]++;
            return true;
        }
        //NEW CODE incorporates PROBABILITY

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
            if (presentRatio >= handoffThreshold[flag]) { //include alpha1
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
        if (this.id == 0) {
            if (check[flag] == 5) {
                if (counter[flag] < 500000) {
                    check[flag] = 0;
                    data[flag * 2][counter[flag * 2]] = channels[flag].size();
                    data[flag * 2 + 1][counter[flag * 2 + 1]] = t;
                    data[6 + flag * 2][counter[6 + flag * 2]] = handoffDrops[flag] * 100.0 / totalHandoffs[flag];
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
        if (this.id == -1 &&simNumber%2==0) {
            pw.println(t + "      " + "HANDIN"+ "    "+ getPriority(flag));
            pw.println("VACANT CHANNELS  -> " + vacant[0] + " " + + vacant[1] + " "+ vacant[2] + " "+ vacant[3] + " ");
            pw.println("GUARD CHANNELS -> " + channels[0].size() + " " + channels[1].size() + " " + channels[2].size() + " " + channels[3].size());
            pw.println("ONGOING CALLS -> " + getOngoingCalls(0) + "  " + getOngoingCalls(1) + "   " + getOngoingCalls(2));
            pw.println("***********************");
        }
    }

    @Deprecated
    void removeCall(int flag) {
        if (occupiedGuardCells[flag] > 0) {
            occupiedGuardCells[flag]--;
        }
    }

    Priority getPriority(int flag) {
        if (flag == 0)
            return Priority.REALTIME;
        else if (flag == 1)
            return Priority.STREAMING;
        else if (flag == 2)
            return Priority.BACKGROUND;
        else
            return null;
    }

    int getPriority(Priority p) {
        int flag;
        if (job.priority == Priority.REALTIME)
            flag = 0;
        else if (job.priority == Priority.STREAMING)
            flag = 1;
        else if (job.priority == Priority.BACKGROUND)
            flag = 2;
        else
            flag = -1;
        return flag;
    }

    // vacants a channel when a call terminates or handoffs

    boolean freeChannel(int flag) {
        Priority p = getPriority(flag);
        for (int i = 0; i < 3; i++) {
            if (i != flag && channels[i].size()>vacant[i]) {
                for (Channel c : channels[i]) {
                    if (!c.vacant && c.p==p) {
                        vacant[i]++;
                        c.p = null;
                        c.vacant = true;
                        return true;
                    }
                }
            }
        }
        if(channels[flag].size()>vacant[flag]){
            for (Channel c : channels[flag]) {
                if (!c.vacant && c.p == p) {
                    c.p = null;
                    vacant[flag]++;
                    c.vacant = true;
                    return true;
                }
            }
        }
        if(channels[3].size()>vacant[3]){
            for (Channel c : channels[3]) {
                if (!c.vacant && c.p == p) {
                    c.p = null;
                    vacant[3]++;
                    c.vacant = true;
                    return true;
                }
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
                synchronized (BSC_Control.getBSC(neighbours[random]).channels) {
                    BSC_Control.getBSC(neighbours[random]).handin(this.id, flag, job.startTime);
                }
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

    public int getTotalGuardCells() {
        int count = 0;
        for (int i = 0; i < 3; i++)
            count += channels[i].size();
        return count;
    }

    int getVacantChannel(int flag) {
        for (int i = 0; i < channels[flag].size(); i++) {
            if (channels[flag].get(i) != null)
                if (channels[flag].get(i).vacant)
                    return i;
        }
        return -1;
    }

    public boolean addNewCall(int flag) {
        Priority p = getPriority(flag);
        int a;
        if(vacant[3]>0) {
             a = getVacantChannel(3);
            if (a != -1) {
                channels[3].get(a).vacant = false;
                channels[3].get(a).p = p;
                vacant[3]--;
                return true;
            }
        }
        double r = new Random().nextDouble();
        double tp;
        for (int i = 2; i >= 0; i--) {
           // tp = vacant[i]*0.01+probabilities[flag][i];
            tp = probabilities[flag][i];
            if (r < tp) {

                if(vacant[i]>0) {
                    a = getVacantChannel(i);
                    if (a != -1) {
                        channels[i].get(a).vacant = false;
                        channels[i].get(a).p = p;
                        vacant[i]--;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Deprecated
    public boolean newCallCheck(int flag) {
        if (totalChannels - getTotalGuardChannels() - (getTotalOngoingCalls() - getTotalOccupied()) > 0)
            return true;
        else {
            double r = new Random().nextDouble();
            for (int i = 0; i < 3; i++) {
                if (r < probabilities[flag][i]) {
                    if (guardChannels[i] - occupiedGuardCells[i] > 0) {
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
            t = t+StdRandom.exp(callTerminationRate[flag]);
            Control.addJob(new Job(this.id,Event.DISCONNECT,t,job.priority));
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
        if (this.id == 0 && flag == 0) {
            if (check[3] == 10) {
                check[3] = 0;
                if (counter[12] < 500000 && counter[13] < 500000) {
                    data[12][counter[12]++] = newCallDrops[0]*100.0/totalNewCalls[0];
                    //data[12][counter[12]++] =totalChannels-vacant[0]-vacant[1]-vacant[2]-vacant[3];

                    data[13][counter[13]++] = t;
                }
            } else
                check[3]++;
        }
    }


    public void disconnect() {

        int flag = getPriority(job.priority);
       // double t = job.startTime + StdRandom.exp(callTerminationRate[flag]);
        //Control.addJob(new Job(this.id, Event.DISCONNECT, t, flag));
        freeChannel(flag);
        //print();
        //  pw.println("Call Termination in " + Thread.currentThread().getName());
//                if(ongoingCalls == 0){
//                    resetHandoff();
//                    resetTermination();
//                }

    }

    public void addGuardCell(int flag) {

        if (channels[3].size() == 0)
            return; //do something about this
        if(vacant[3]>0) {
            Iterator i = channels[3].iterator();
            Channel c;
            while (i.hasNext()) {
                c = (Channel) i.next();
                if (c.vacant) {
                    vacant[flag]++;
                    vacant[3]--;
                    channels[flag].add(c);
                    i.remove();
                    return;
                }
            }
        }
        channels[flag].add(channels[3].get(0));
        // assert c!=null;
        channels[3].remove(0);
//
//        Channel c = null;
//        for (int i = 3; i > flag; i--) {
//            if (channels[i].size() > 0) {
//                channels[flag].add(channels[i].get(0));
//                // assert c!=null;
//                channels[i].remove(0);
//                return;
//            }
//        }
        // channels[flag].add(c);
    }

    @Deprecated
    public void incrementGuardChannel(int flag) {
        if (totalChannels - getTotalGuardChannels() > 0) {
            guardChannels[flag]++;
            occupiedGuardCells[flag]++;
            //  pw.println("Guard Channels increased to" + guardChannels);
        }
    }

    public void freeGuardCell(int flag) {
        //Channel c = null;
        if (channels[flag].size() == 0)
            return;
        if(vacant[flag]>0) {
            Iterator i = channels[flag].iterator();
            Channel c;
            while (i.hasNext()) {
                c = (Channel) i.next();
                if (c.vacant) {
                    channels[3].add(c);
                    i.remove();
                    vacant[flag]--;
                    vacant[3]++;
                    return;
                }
            }
        }
            channels[3].add(channels[flag].get(0));
            // assert c!=null;
            channels[flag].remove(0);
            // channels[3].add(c);
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
        if (flag == 1) {
            lastReset = job.startTime;
            for (int i = 0; i < 3; i++) {
                periodNewCalls[i] = 0;
                periodNewCallDrops[i] = 0;
                periodHandoffs[i] = 0;
                periodHandoffDrops[i] = 0;
            }
        }
    }

    public int getOngoingCalls(int flag) {
        int count = 0;
        Priority p = getPriority(flag);
        for (int i = 0; i < 4; i++) {
            for (Channel c : channels[i]) {
                if (!c.vacant) {
                    if(c.p == p)
                    count++;
                }
            }
        }
        return count;
    }

    public void print0() {

//        pw.println(  "**************************************" );
//        pw.println("Time " + job.startTime);
//        pw.println("Event " + job.event);
//        pw.println("Ongoing Calls "+ ongoingCalls);
//        pw.println(  "**************************************" );
        if (this.id == 0 &&simNumber%2==0) {
            pw.println(job.startTime + "      " + job.event + "    "+ job.priority);
            pw.println("VACANT CHANNELS  -> " + vacant[0] + " " + + vacant[1] + " "+ vacant[2] + " "+ vacant[3] + " ");
            pw.println("GUARD CHANNELS -> " + channels[0].size() + " " + channels[1].size() + " " + channels[2].size() + " " + channels[3].size());
            pw.println("ONGOING CALLS -> " + getOngoingCalls(0) + "  " + getOngoingCalls(1) + "   " + getOngoingCalls(2));
            pw.println("***********************");
        }
        //pw.println(job.startTime + " " + job.event + " " + ongoingCalls[0] + " " + ongoingCalls[1] + " " + ongoingCalls[2] + " " + handoffDrops[0] + " " + handoffDrops[0] + " " + handoffDrops[1] + " " + handoffDrops[2] + " " + guardChannels[0] + " " + guardChannels[1] + " " + guardChannels[2] + " " + newCallDrops[0] + " " + newCallDrops[1] + " " + newCallDrops[2]);
        // pw.println("Event " + job.event);
        //pw.println("Ongoing Calls "+ ongoingCalls);


    }

    public void initParams() {
        if(simNumber < 2) {
            consecutiveHandoffsLimit = new int[]{10, 5, 3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.002, 0.05, 0.5};
            callTerminationRate = new double[]{0.005, 0.005, 0.005};
            handoffRate = new double[]{0.01, 0.01, 0.01};
            callArrivalRate = new double[]{1.0, 1.0, 1.0};
        }
        else if(simNumber <4){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.002, 0.05, 0.5};
            callTerminationRate = new double[]{0.005, 0.005, 0.005};
            handoffRate = new double[]{0.01, 0.01, 0.01};
            callArrivalRate = new double[]{0.5, 0.5, 0.5};
        }
        else if(simNumber< 6){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.002, 0.05, 0.5};
            callTerminationRate = new double[]{0.005, 0.005, 0.005};
            handoffRate = new double[]{0.01, 0.01, 0.01};
            callArrivalRate = new double[]{0.5, 0.5, 0.5};
        }
        else if(simNumber<8){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.002, 0.01, 0.3};
            callTerminationRate = new double[]{0.005, 0.008, 0.008};
            handoffRate = new double[]{0.1, 0.05, 0.05};
            callArrivalRate = new double[]{0.5, 0.3, 0.3};
        }
        else if(simNumber < 10){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.001, 0.03, 0.2};
            callTerminationRate = new double[]{0.0005, 0.006, 0.006};
            handoffRate = new double[]{0.01, 0.01, 0.01};
            callArrivalRate = new double[]{0.05, 0.03, 0.03};
        }
        else if(simNumber <12){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.001, 0.01, 0.1};
            callTerminationRate = new double[]{0.001, 0.0005, 0.0005};
            handoffRate = new double[]{0.01, 0.01, 0.01};
            callArrivalRate = new double[]{0.1, 0.05, 0.05};
        }
        else if(simNumber<14){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.001, 0.01, 0.1};
            callTerminationRate = new double[]{0.01, 0.005, 0.005};
            handoffRate = new double[]{0.3, 0.01, 0.01};
            callArrivalRate = new double[]{1.0, 0.1, 0.1};
        }
        else if(simNumber <16){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.001, 0.01, 0.1};
            callTerminationRate = new double[]{0.005, 0.005, 0.005};
            handoffRate = new double[]{0.01, 0.05, 0.02};
            callArrivalRate = new double[]{0.2, 0.1, 0.1};
        }
        else if(simNumber <18){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.001, 0.03, 0.2};
            callTerminationRate = new double[]{0.0005, 0.006, 0.006};
            handoffRate = new double[]{0.01, 0.01, 0.01};
            callArrivalRate = new double[]{0.05, 0.03, 0.03};
        }
        else if(simNumber <20){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.001, 0.01, 0.1};
            callTerminationRate = new double[]{0.005, 0.005, 0.005};
            handoffRate = new double[]{0.01, 0.01, 0.01};
            callArrivalRate = new double[]{0.1, 0.1, 0.1};
        }
        else if(simNumber <22){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.001, 0.01, 0.1};
            callTerminationRate = new double[]{0.001, 0.001, 0.001};
            handoffRate = new double[]{0.01, 0.01, 0.01};
            callArrivalRate = new double[]{0.1, 0.1, 0.1};
        }
        else if(simNumber <24){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.001, 0.01, 0.1};
            callTerminationRate = new double[]{0.001, 0.005, 0.02};
            handoffRate = new double[]{0.03, 0.05, 0.05};
            callArrivalRate = new double[]{0.3, 0.5, 0.5};
        }
        else if(simNumber<26){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.0, 0.0, 0.00}};
            handoffThreshold = new double[]{0.001, 0.1, 0.5};
            callTerminationRate = new double[]{0.003, 0.005, 0.02};
            handoffRate = new double[]{0.01, 0.05, 0.05};
            callArrivalRate = new double[]{0.3, 0.5, 0.5};
        }
        else if(simNumber <28){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.001, 0.1, 0.5};
            callTerminationRate = new double[]{0.003, 0.005, 0.02};
            handoffRate = new double[]{0.01, 0.05, 0.05};
            callArrivalRate = new double[]{0.3, 0.5, 0.5};
        }
        else if(simNumber <30){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.001, 0.1, 0.5};
            callTerminationRate = new double[]{0.003, 0.01, 0.02};
            handoffRate = new double[]{0.01, 0.05, 0.05};
            callArrivalRate = new double[]{0.3, 0.5, 0.5};
        }
        else if(simNumber <32){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.001, 0.2, 0.5};
            callTerminationRate = new double[]{0.0005, 0.006, 0.006};
            handoffRate = new double[]{0.01, 0.01, 0.01};
            callArrivalRate = new double[]{0.05, 0.03, 0.03};
        }
        else if(simNumber <34){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.001, 0.2, 0.5};
            callTerminationRate = new double[]{0.01, 0.0005, 0.0005};
            handoffRate = new double[]{0.3, 0.04, 0.04};
            callArrivalRate = new double[]{0.8, 0.1, 0.1};
        }
        else if(simNumber <36){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.002, 0.01, 0.3};
            callTerminationRate = new double[]{0.005, 0.008, 0.008};
            handoffRate = new double[]{0.1, 0.05, 0.05};
            callArrivalRate = new double[]{0.5, 0.3, 0.3};
        }
        else if(simNumber <38){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.002, 0.05, 0.5};
            callTerminationRate = new double[]{0.005, 0.005, 0.005};
            handoffRate = new double[]{0.1, 0.01, 0.01};
            callArrivalRate = new double[]{0.5, 0.3, 0.3};
        }
        else if(simNumber <40){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.002, 0.05, 0.5};
            callTerminationRate = new double[]{0.005, 0.005, 0.005};
            handoffRate = new double[]{0.1, 0.1, 0.1};
            callArrivalRate = new double[]{0.5, 0.5, 0.5};
        }
        else if(simNumber<42){
            consecutiveHandoffsLimit = new int[]{10,5,3}; //set values
            probabilities = new double[][]{{0.05,0.2,0.4}, {0.00,0.05,0.1}, {0.00,0.00,0.05}};
            handoffThreshold = new double[]{0.001, 0.2, 0.5};
            callTerminationRate = new double[]{0.0005, 0.006, 0.006};
            handoffRate = new double[]{0.1, 0.2, 0.2};
            callArrivalRate = new double[]{3.1, 2.1, 2.1};
        }
        else {
            Random generator = new Random((long)(1000000000*Math.random()));
            consecutiveHandoffsLimit = new int[]{(int)(6+10*generator.nextDouble()),(int)(2+6*generator.nextDouble()),(int)(1+4*generator.nextDouble())}; //set values
            generator.setSeed((long)(1000000000*Math.random()));
            probabilities = new double[][]{{0 + 0.2*generator.nextDouble(),0.20 + 0.2*generator.nextDouble(),0.4 + 0.2*generator.nextDouble()}, {0.00,0 + 0.2*generator.nextDouble(),0.20 + 0.2*generator.nextDouble()}, {0.00,0.00,0.2*generator.nextDouble()}};
            generator.setSeed((long)(1000000000*Math.random()));
            handoffThreshold = new double[]{0.001, 0.02, 0.1};
            callTerminationRate = new double[]{0.001+generator.nextDouble()/80,0.001+generator.nextDouble()/200,0.001+generator.nextDouble()/200};
            generator.setSeed((long)(1000000000*Math.random()));
            handoffRate = new double[]{0.004 + (0.1-0.004)*Math.pow(generator.nextDouble(),2), 0.004 + (0.1-0.004)*Math.pow(generator.nextDouble(),2), 0.004 + (0.1-0.004)*Math.pow(generator.nextDouble(),2)};
            generator.setSeed((long)(1000000000*Math.random()));
            callArrivalRate = new double[]{0.1+generator.nextDouble()*2,0.1+generator.nextDouble(),0.1+generator.nextDouble()};
        }
        PrintWriter qw = null;
        try {
            qw = new PrintWriter(this.simNumber+".txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        qw.println("consecutiveHandoffsLimit");
        printArray(qw,consecutiveHandoffsLimit);
        for(int i = 0;i<3;i++) {
            qw.println("probabilities for "+ i);
            printArray(qw, probabilities[i]);
        }

        qw.println("handoffThreshold");
        printArray(qw,handoffThreshold);

        qw.println("callTerminationRate");
        printArray(qw,callTerminationRate);

        qw.println("handoffRate");
        printArray(qw,handoffRate);

        qw.println("callArrivalRate");
        printArray(qw,callArrivalRate);

        qw.close();
    }
    public void printArray(PrintWriter pw, double[] arr){
        for(int i = 0;i<arr.length;i++){
            pw.print(arr[i]+"  ");
        }
        pw.println();
    }
    public void printArray(PrintWriter pw, int[] arr){
        for(int i = 0;i<arr.length;i++){
            pw.print(arr[i]+"  ");
        }
        pw.println();
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
        if(this.id == 0) {
            try {
                pw = new PrintWriter("./" + "log_" + id + simNumber + ".txt");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        //pw.println("job.startTime job.event ongoingCalls handoffDropPercent guardCells newCallDrop% ");
       // pw.println("job.startTime  job.event  ongoingCalls[0]   ongoingCalls[1]   ongoingCalls[2]    handoffDrops[0]    handoffDrops[1]   handoffDrops[2]    guardChannels[0]    guardChannels[1   guardChannels[2]         newCallDrops[0]+      newCallDrops[1]+ newCallDrops[2]");
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

                if (job.startTime - lastReset >= 20000) {
                    reset(1);
                    //     print0();
                }
                synchronized (this.channels) {

                    if (job.event == Event.HANDOFF)
                        handoff();
                    else if (job.event == Event.CONNECT)
                        connect();
                    else if (job.event == Event.DISCONNECT)
                        disconnect();
                    else if (job.event == Event.TERMINATE) {
                        job = null;
                        break;
                    }
                   // print0();
                    job = null;

                }
//                if(id==0) {
//                    initdata[0][0] = guardChannels[0];
//                    initdata[1][1] = job.startTime;
//                   // chart.updateXYSeries("sine",initdata[1],initdata[0]);
//                }
                    // printer System.out.println(this.id + " has ended job");
                    this.notifyAll();

            }
        }
        if(this.id==0) {
            pw.flush();
            pw.close();
        }

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
            if(counter[i]== 500000)
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
                        BitmapEncoder.saveBitmapWithDPI(chart[i], "./"+simNumber+"Sample_Chart_300_DPI" + i, BitmapEncoder.BitmapFormat.PNG, 600);
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
