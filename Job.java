import java.util.Random;
public class Job {
    Event event;
    double startTime;
    double endTime;
    int cell;
    Priority priority;
    @Deprecated
    Job(int cell, Event event, double startTime){
        this.cell = cell;
        this.event = event;
        this.startTime = startTime;
        this.endTime = -1;
        generatePriority();
    }
    Job(int cell, Event event, double start, double end){
        this.cell = cell;
        this.event = event;
        this.startTime = start;
        this.endTime = end;
        generatePriority();
    }
    Job(int cell,Event event, double start, Priority p){
        this.cell = cell;
        this.event = event;
        this.startTime = start;
        this.endTime = -1;
        this.priority = p;
    }
    public void generatePriority(){
        int a = new Random().nextInt(3);
        if(a == 0)
            this.priority = Priority.BACKGROUND;
        else if(a == 1)
            this.priority = Priority.REALTIME;
        else
            this.priority = Priority.STREAMING;
    }
}
