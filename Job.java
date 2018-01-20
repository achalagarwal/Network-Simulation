public class Job {
    Event event;
    double startTime;
    double endTime;
    int cell;
    Job(int cell, Event event, double startTime){
        this.cell = cell;
        this.event = event;
        this.startTime = startTime;
        this.endTime = -1;
    }
    Job(int cell, Event event, double start, double end){
        this.cell = cell;
        this.event = event;
        this.startTime = start;
        this.endTime = end;
    }
}
