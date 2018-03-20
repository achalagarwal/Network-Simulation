
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

public class BSC implements Runnable {
    Job job;
    //stats
    int totalChannels;
    int guard1Channels;
    int guard2Channels;
    int guard3Channels;
    int handoffDrops;
    int ongoingCalls;
    int totalHandoffs;
    int totalNewCalls;
    int newCallDrops;
    //parameters
    double handoffThreshold;
    double callArrivalRate;
    double callTerminationRate;
    double handoffRate;
    boolean status;
    int[] neighbours;
    int id;
    int consecutiveHandoffs;
    int consecutiveHandoffsLimit;
    double alpha1;
    double alpha2;
    //controls
    double lastReset;
    int periodHandoffs;
    int periodHandoffDrops;
    PrintWriter pw;
    BSC(int id){
        
        try {
            pw = new PrintWriter("./"+"log_"+Integer.toString(id)+".txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        consecutiveHandoffsLimit = 6;
        consecutiveHandoffs = 0;
        guard1Channels = 0;
        guard2Channels = 0;
        guard3Channels = 0;
        ongoingCalls = 0;
        totalChannels = 150;
        handoffDrops = 0;
        totalHandoffs = 0;
        totalNewCalls = 0;
        newCallDrops = 0;
        handoffThreshold = 0.1;
        callTerminationRate = 0.2;
        handoffRate = 0.1;
        status = false;
        callArrivalRate = 0.4;
        this.id = id;
        lastReset = 0.0;
        setNeighbours();
    }
    public synchronized void turnOn(){
        status = true;
        this.notifyAll();
    }
    public void turnOff() {
        status = false;
    }
    private void setNeighbours(){
        if(id == 0){
            neighbours = new int[]{1,2,3,4,5,6};
        }
        if(id == 1){
            neighbours = new int[]{2,8,7,18,6,0};
        }
        if(id == 2){
            neighbours = new int[]{1,0,3,8,9,10};
        }
        if(id == 3){
            neighbours = new int[]{0,2,4,10,11,12};
        }
        if(id == 4){
            neighbours = new int[]{0,3,5,12,13,14};
        }
        if(id == 5){
            neighbours = new int[]{0,4,6,14,15,16};
        }
        if(id == 6){
            neighbours = new int[]{0,1,5,16,17,18};
        }
        if(id == 7){
            neighbours = new int[]{1,8,18};
        }
        if(id == 8){
            neighbours = new int[]{1,2,7,9};
        }
        if(id == 9){
            neighbours = new int[]{2,8,10};
        }
        if(id == 10){
            neighbours = new int[]{2,3,9,11};
        }
        if(id == 11){
            neighbours = new int[]{3,10,12};
        }
        if(id == 12){
            neighbours = new int[]{3,4,11,13};
        }
        if(id == 13){
            neighbours = new int[]{4,12,14};
        }
        if(id == 14){
            neighbours = new int[]{4,5,13,15};
        }
        if(id == 15){
            neighbours = new int[]{5,14,16};
        }
        if(id == 16){
            neighbours = new int[]{5,6,15,17};
        }
        if(id == 17){
            neighbours = new int[]{6,16,18};
        }
        if(id == 18){
            neighbours = new int[]{1,6,7,17};
        }
        if(id == 19){
            neighbours = new int[]{20};
        }
        if(id == 20){
            neighbours = new int[]{19};
        }
    }
    public boolean hasNeighbour(int x){
        for(int i = 0;i<neighbours.length;i++)
            if(x==neighbours[i])
                return true;
        return false;
    }
    public void handin(int cell){//remove from hlist
        totalHandoffs++;
        periodHandoffs++;
        //pw.println(" New handoff connection request received from Cell Number - " + cell);
        //pw.println("Handoff block entered in " + this.cellId);
        double presentRatio = periodHandoffDrops * 1.0 / periodHandoffs;
        if (ongoingCalls == totalChannels) {
            periodHandoffDrops++;//here present ratio does not use the currently dropped handoff, reconfirm
            handoffDrops++;
            if (presentRatio >= 0.5*handoffThreshold) { //include alpha1
                incrementGuardChannel();
            } else if (presentRatio <= 0.3*handoffThreshold && consecutiveHandoffs == consecutiveHandoffsLimit - 1) { //include alpha2
                decrementGuardChannel();
            }
            consecutiveHandoffs = 0;
        }
        else if (ongoingCalls < totalChannels) {
            consecutiveHandoffs++;
            ongoingCalls++;
        }
        Control.removeFromHList(cell);
    }
    public void handoff() {
        if (job.priority != Priority.BACKGROUND) {
            double t = job.startTime + StdRandom.exp(handoffRate);
            Control.addJob(new Job(this.id, Event.HANDOFF, t));
            if (ongoingCalls > 0) {
                ongoingCalls--;
//            if(ongoingCalls == 0){
//                resetTermination();
//                resetHandoff();
//            }
                //   print();
                if (neighbours.length > 0) {
                    int random = new Random().nextInt(neighbours.length);
                    //pw.println("Handoff Call sent to Cell Number - " + neighbours[random]);
                    BSC_Control.getBSC(neighbours[random]).handin(this.id);
                }
            } else { //should never reach this line
                //pw.println("No calls to Handoff");
                Control.removeFromHList(id);
//            this.data.nextHandoff = totalSimulationTime + 1;
//            this.data.nextTermination = totalSimulationTime +1;
            }
        }
        else{
            double t = job.startTime + StdRandom.exp(callTerminationRate);
            Control.addJob(new Job(this.id, Event.DISCONNECT, t));
            Control.removeFromHList(id);
        }
    }
    public void connect() {
        if(job.priority != Priority.BACKGROUND) {
            totalNewCalls++;
            double t = job.startTime + StdRandom.exp(callArrivalRate);
            Control.addJob(new Job(this.id, Event.CONNECT, t));
            if (ongoingCalls < totalChannels - guardChannels) {// total - guard = Ca

//            if(ongoingCalls == 0){
//                resetTermination();
//                resetHandoff();
//            }
                ongoingCalls++;
                // print();
                //pw.println("New Connection Request in " + Thread.currentThread().getName());

            } else {
                newCallDrops++;
                //print();
                //  pw.println("New Connection Failed in " + Thread.currentThread().getName());

            }
        }
        //else handle background calls
        else{
            double t = job.startTime + StdRandom.exp(callArrivalRate);
            Control.addJob(new Job(this.id, Event.CONNECT, t));
        }
    }
    public void disconnect() {
        if (job.priority != Priority.BACKGROUND) {
            double t = job.startTime + StdRandom.exp(callTerminationRate);
            Control.addJob(new Job(this.id, Event.DISCONNECT, t));
            if (ongoingCalls > 0) {
                ongoingCalls--;
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
        //else handle background calls
        else{
            double t = job.startTime + StdRandom.exp(callTerminationRate);
            Control.addJob(new Job(this.id, Event.DISCONNECT, t));
        }
    }
    public void incrementGuardChannel(){
        if(guardChannels<totalChannels) {
            guardChannels++;
          //  pw.println("Guard Channels increased to" + guardChannels);
        }
    }
    public void decrementGuardChannel(){
        if(guardChannels>0) {
            guardChannels--;
           // pw.println("Guard Channels decreased to" + guardChannels);
        }
    }
    public void reset(){
        lastReset = job.startTime;
        periodHandoffs = 0;
        periodHandoffDrops = 0;
    }
    public void print(){

//        pw.println(  "**************************************" );
//        pw.println("Time " + job.startTime);
//        pw.println("Event " + job.event);
//        pw.println("Ongoing Calls "+ ongoingCalls);
//        pw.println(  "**************************************" );
            if(this.id == 0)
          pw.println(job.startTime+" "+job.event+" "+ongoingCalls+" "+handoffDrops * 1.0 / totalHandoffs+" "+guardChannels+ " " + newCallDrops*1.0/totalNewCalls);
         // pw.println("Event " + job.event);
         //pw.println("Ongoing Calls "+ ongoingCalls);


    }
    public void run() {
        pw.println("job.startTime job.event ongoingCalls handoffDropPercent guardCells newCallDrop% ");
        ongoingCalls = 1;
        double t =  StdRandom.exp(callArrivalRate);
        Control.addJob(new Job(this.id,Event.CONNECT,t));
        t =  StdRandom.exp(callTerminationRate);
        Control.addJob(new Job(this.id, Event.DISCONNECT, t));
        t =  StdRandom.exp(handoffRate);
        Control.addJob(new Job(this.id,Event.HANDOFF,t));
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
                System.out.println( Control.BLUE + this.id + " has started job" + Control.RESET);
                print();
                if(job.startTime-lastReset>= 1000)
                    reset();
                if (job.event == Event.HANDOFF)
                    handoff();
                else if (job.event == Event.CONNECT)
                    connect();
                else if (job.event == Event.DISCONNECT)
                    disconnect();
                else if(job.event == Event.TERMINATE)
                    break;
                job = null;
                System.out.println(this.id + " has ended job");
                this.notifyAll();
            }
        }
        pw.flush();
        pw.close();
    }
}

