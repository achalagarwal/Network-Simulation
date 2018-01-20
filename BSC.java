import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

public class BSC implements Runnable {
    Job job;
    int totalChannels;
    int guardChannels;
    int handoffDrops;
    int ongoingCalls;
    int totalHandoffs;
    int totalNewCalls;
    int newCallDrops;
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
    PrintWriter pw;
    BSC(int id){
        
        try {
            pw = new PrintWriter("./"+"log_"+Integer.toString(id)+".txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        consecutiveHandoffsLimit = 10;
        consecutiveHandoffs = 0;
        guardChannels = 0;
        ongoingCalls = 0;
        totalChannels = 100;
        handoffDrops = 0;
        totalHandoffs = 0;
        totalNewCalls = 0;
        newCallDrops = 0;
        handoffThreshold = 0.02;
        callTerminationRate = 0.2;
        handoffRate = 0.1;
        status = false;
        callArrivalRate = 0.5;
        this.id = id;
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
        pw.println(" New handoff connection request received from Cell Number - " + cell);
        //pw.println("Handoff block entered in " + this.cellId);
        double presentRatio = handoffDrops * 1.0 / totalHandoffs;
        if (ongoingCalls == totalChannels) {
            handoffDrops++; //here present ratio does not use the currently dropped handoff, reconfirm
            if (presentRatio >= handoffThreshold) { //include alpha1
                incrementGuardChannel();
            } else if (presentRatio <= handoffThreshold && consecutiveHandoffs == consecutiveHandoffsLimit - 1) { //include alpha2
                decrementGuardChannel();
            }
            consecutiveHandoffs = 0;
        }
        else if (ongoingCalls < totalChannels) {
            totalHandoffs++;
            consecutiveHandoffs++;
            ongoingCalls++;
        }
        Control.removeFromHList(cell);
    }
    public void handoff(){
        double t = job.startTime + StdRandom.exp(handoffRate);
        Control.addJob(new Job(this.id,Event.HANDOFF,t));
        if (ongoingCalls > 0) {
            ongoingCalls--;
//            if(ongoingCalls == 0){
//                resetTermination();
//                resetHandoff();
//            }
         //   print();
            if(neighbours.length>0) {
                int random = new Random().nextInt(neighbours.length);
                pw.println("Handoff Call sent to Cell Number - " + neighbours[random]);
                BSC_Control.getBSC(neighbours[random]).handin(this.id);
            }
        }
        else{ //should never reach this line
            pw.println("No calls to Handoff");
//            this.data.nextHandoff = totalSimulationTime + 1;
//            this.data.nextTermination = totalSimulationTime +1;
        }
    }
    public void connect() {
        double t = job.startTime + StdRandom.exp(callArrivalRate);
        Control.addJob(new Job(this.id,Event.CONNECT,t));
        if (ongoingCalls < totalChannels - guardChannels) {// total - guard = Ca
            totalNewCalls++;
//            if(ongoingCalls == 0){
//                resetTermination();
//                resetHandoff();
//            }
            ongoingCalls++;
           // print();
            pw.println("New Connection Request in " + Thread.currentThread().getName());

        }
        else {
            newCallDrops++;
            //print();
            //  pw.println("New Connection Failed in " + Thread.currentThread().getName());

        }
    }
    public void disconnect(){
        double t = job.startTime + StdRandom.exp(callTerminationRate);
        Control.addJob(new Job(this.id, Event.DISCONNECT, t));
        if(ongoingCalls>0) {
            ongoingCalls--;
            //print();
            pw.println("Call Termination in " + Thread.currentThread().getName());
//                if(ongoingCalls == 0){
//                    resetHandoff();
//                    resetTermination();
//                }
        }
        else {
            pw.println("No calls to terminate in " + Thread.currentThread().getName());
        }
    }
    public void incrementGuardChannel(){
        if(guardChannels<totalChannels) {
            guardChannels++;
            pw.println("Guard Channels increased to" + guardChannels);
        }
    }
    public void decrementGuardChannel(){
        if(guardChannels>0) {
            guardChannels--;
            pw.println("Guard Channels decreased to" + guardChannels);
        }
    }
    public void print(){

        pw.println(  "**************************************" );
        pw.println("Time " + job.startTime);
        pw.println("Event " + job.event);
        pw.println("Ongoing Calls "+ ongoingCalls);
        

    }
    public void run() {
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
                print();
                if (job.event == Event.HANDOFF)
                    handoff();
                else if (job.event == Event.CONNECT)
                    connect();
                else if (job.event == Event.DISCONNECT)
                    disconnect();
                else if(job.event == Event.TERMINATE)
                    break;
                job = null;
                this.notifyAll();
            }
        }
        pw.flush();
        pw.close();
    }
}

