import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

public class Cell implements  Runnable {
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
    double totalSimulationTime;
    double startTime;
    int neighbours[];
    int cellId;
    int consecutiveHandoffs;
    int handoffsPending;
    double alpha1;
    double alpha2;
    final Object handoffLock;
    int N; //consecutive handoffs
    Elements data;
    PrintWriter pw;
    Cell(int id){
        handoffLock = new Object();
        try {
            pw = new PrintWriter("./"+"log_"+Integer.toString(id)+".txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
        totalSimulationTime = 720;
        cellId = id;
        setNeighbours();
        data = new Elements();
        handoffsPending=0;
    }


    public int getTotalChannels () {
        return totalChannels;
    }

    public void setTotalChannels(int totalChannels) {
        this.totalChannels = totalChannels;
    }

    public int getHandoffDrops() {
        return handoffDrops;
    }

    public void setHandoffDrops(int handoffDrops) {
        this.handoffDrops = handoffDrops;
    }

    public int getOngoingCalls() {
        return ongoingCalls;
    }

    public void setOngoingCalls(int ongoingCalls) {
        this.ongoingCalls = ongoingCalls;
    }

    public int getTotalHandoffs() {
        return totalHandoffs;
    }

    public void setTotalHandoffs(int totalHandoffs) {
        this.totalHandoffs = totalHandoffs;
    }

    public int getTotalNewCalls() {
        return totalNewCalls;
    }

    public void setTotalNewCalls(int totalNewCalls) {
        this.totalNewCalls = totalNewCalls;
    }

    public int getNewCallDrops() {
        return newCallDrops;
    }

    public void setNewCallDrops(int newCallDrops) {
        this.newCallDrops = newCallDrops;
    }

    public double getHandoffThreshold() {
        return handoffThreshold;
    }

    public void setHandoffThreshold(double handoffThreshold) {
        this.handoffThreshold = handoffThreshold;
    }

    public double getCallArrivalRate() {
        return callArrivalRate;
    }

    public void setCallArrivalRate(double callArrivalRate) {
        this.callArrivalRate = callArrivalRate;
    }

    public double getCallTerminationRate() {
        return callTerminationRate;
    }

    public void setCallTerminationRate(double callTerminationRate) {
        this.callTerminationRate = callTerminationRate;
    }

    public double getHandoffRate() {
        return handoffRate;
    }

    public void setHandoffRate(double handoffRate) {
        this.handoffRate = handoffRate;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public double getTotalSimulation() {
        return totalSimulationTime;
    }

    public void setTotalSimulation(double totalSimulation) {
        this.totalSimulationTime = totalSimulation;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }
    public int getNeighbour(int id){
        if(id<6)
            return neighbours[id];
        else return -1;
    }
 //getters and setters


    private void setNeighbours(){
        if(cellId == 0){
            neighbours = new int[]{1,2,3,4,5,6};
        }
        if(cellId == 1){
            neighbours = new int[]{2,8,7,18,6,0};
        }
        if(cellId == 2){
            neighbours = new int[]{1,0,3,8,9,10};
        }
        if(cellId == 3){
            neighbours = new int[]{0,2,4,10,11,12};
        }
        if(cellId == 4){
            neighbours = new int[]{0,3,5,12,13,14};
        }
        if(cellId == 5){
            neighbours = new int[]{0,4,6,14,15,16};
        }
        if(cellId == 6){
            neighbours = new int[]{0,1,5,16,17,18};
        }
        if(cellId == 7){
            neighbours = new int[]{1,8,18};
        }
        if(cellId == 8){
            neighbours = new int[]{1,2,7,9};
        }
        if(cellId == 9){
            neighbours = new int[]{2,8,10};
        }
        if(cellId == 10){
            neighbours = new int[]{2,3,9,11};
        }
        if(cellId == 11){
            neighbours = new int[]{3,10,12};
        }
        if(cellId == 12){
            neighbours = new int[]{3,4,11,13};
        }
        if(cellId == 13){
            neighbours = new int[]{4,12,14};
        }
        if(cellId == 14){
            neighbours = new int[]{4,5,13,15};
        }
        if(cellId == 15){
            neighbours = new int[]{5,14,16};
        }
        if(cellId == 16){
            neighbours = new int[]{5,6,15,17};
        }
        if(cellId == 17){
            neighbours = new int[]{6,16,18};
        }
        if(cellId == 18){
            neighbours = new int[]{1,6,7,17};
        }
    }
    public synchronized void turnOn(){
        startTime = System.nanoTime();
        status = true;
        this.notifyAll();
    }
    public void turnOff() {
        status = false;
    }
    public void handoffHelper(){
        synchronized (this.handoffLock) {
            handoffsPending++;
            //pw.println("Handoff Connection Request in " + this.cellId);
        }
    }
    private void resetTermination() {
        if(data.nextTermination > totalSimulationTime){
            data.nextTermination = data.currentTime + data.getExponentTime(callTerminationRate);
        }
    }
    private void resetHandoff(){
        if(data.nextHandoff > totalSimulationTime){
            data.nextHandoff = data.currentTime + data.getExponentTime(handoffRate);
        }
    }
    public  void requestHandoff(int id) { //a new handoff call request in this cell
        synchronized (this.handoffLock) {
            pw.println(" New handoff connection request received from Cell Number - " + id);
            //pw.println("Handoff block entered in " + this.cellId);
            double presentRatio = handoffDrops * 1.0 / totalHandoffs;
            if (ongoingCalls == totalChannels) {
                handoffDrops++; //here present ratio does not use the currently dropped handoff, reconfirm
                if (presentRatio >= handoffThreshold) { //include alpha1
                    incrementGuardChannel();
                } else if (presentRatio <= handoffThreshold && consecutiveHandoffs == N - 1) { //include alpha2
                    decrementGuardChannel();
                }
                consecutiveHandoffs = 0;
            }
            else if (ongoingCalls < totalChannels) {
                totalHandoffs++;
                consecutiveHandoffs++;
                ongoingCalls++;
            }
            handoffsPending--;
        }
    }
    public void sendHandoff() {//sciencedirect ieee springer
        if (ongoingCalls > 0) {
            data.currentEvent = Event.HANDOFF;
            this.data.nextHandoff = data.currentTime + data.getExponentTime(handoffRate);
            ongoingCalls--;
            if(ongoingCalls == 0){
                resetTermination();
                resetHandoff();
            }
            print();
            if(neighbours.length>0) {
                int random = new Random().nextInt(neighbours.length);
                CellControl.getCell(neighbours[random]).requestHandoff(this.cellId);
                pw.println("Handoff Call sent to Cell Number - " + neighbours[random]);
            }
        }
        else{ //should never reach this line
            pw.println("No calls to Handoff");
            this.data.nextHandoff = totalSimulationTime + 1;
            this.data.nextTermination = totalSimulationTime +1;
        }
    }
    public void newConnection() {
        this.data.nextArrival = data.currentTime + data.getExponentTime(callArrivalRate);
        if (ongoingCalls < totalChannels - guardChannels) {// total - guard = Ca
            totalNewCalls++;
            if(ongoingCalls == 0){
                resetTermination();
                resetHandoff();
            }
            ongoingCalls++;
            data.currentEvent = Event.CONNECT;
            print();
            pw.println("New Connection Request in " + Thread.currentThread().getName());

        }
        else {
            newCallDrops++;
            //print();
          //  pw.println("New Connection Failed in " + Thread.currentThread().getName());

        }
    }
    public void reset(){
        data.resetTime = 0;
        totalHandoffs = 0;
        handoffDrops = 0;
    }
    public void disconnect(){

        if(ongoingCalls>0) {
            data.currentEvent = Event.DISCONNECT;
            ongoingCalls--;
            this.data.nextTermination = data.currentTime + data.getExponentTime(callTerminationRate);
            print();
            pw.println("Call Termination in " + Thread.currentThread().getName());
            if(ongoingCalls == 0){
                resetHandoff();
                resetTermination();
            }
        }
        else { //should never reach this line during simulation
            pw.println("No calls to terminate in " + Thread.currentThread().getName());
            this.data.nextTermination = totalSimulationTime + 1;
            this.data.nextHandoff = totalSimulationTime + 1;
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
        pw.println("Time " + data.currentTime);
        pw.println("Event " + data.currentEvent);
        pw.println("Ongoing Calls "+ ongoingCalls);
        pw.println("Handoff Call drops " + handoffDrops );

    }
    public void run(){
        synchronized (this) {
            while (!this.status) {
                try {
                //    pw.println(Thread.currentThread().getName() + " is waiting to be turned On");
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        pw.println(Thread.currentThread().getName() + " has been turned On");
        data.currentTime = 0;
        //newConnection();
        ongoingCalls = 1; //initial
        data.currentEvent = Event.CONNECT; // initial
        data.nextArrival = data.currentTime + data.getExponentTime(callArrivalRate);
        data.nextTermination = data.currentTime + data.getExponentTime(callTerminationRate);
        data.nextHandoff = data.currentTime + data.getExponentTime(handoffRate);
        print();
        data.setNextEvent();
        while (data.currentTime < totalSimulationTime) {
            synchronized (this) {
                if(data.resetTime>22)
                    reset();
//                while (handoffsPending>0) {
//                    this.requestHandoff(cellId);
//                }
                if (data.nextEvent == Event.CONNECT)
                    newConnection();
                else if (data.nextEvent == Event.DISCONNECT) {
                    disconnect();
                } else if (data.nextEvent == Event.HANDOFF) {
                    sendHandoff();
                }
                this.notifyAll();
                data.setNextEvent();
            }
        }
        pw.println(this.data.currentTime);
        pw.println(this.handoffDrops);
        pw.println(this.totalHandoffs);
        pw.flush();
        pw.close();
    }
}
