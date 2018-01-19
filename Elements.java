
public class Elements {
    Event nextEvent;
    Event currentEvent;
    double currentTime;
    double nextTime;
    double nextArrival;
    double nextHandoff;
    double nextTermination;
    double resetTime;
    Elements(){
        nextEvent = null;
        currentEvent = null;
        currentTime = 0.0;
        nextTime =0.0;
        nextArrival = 0.0;
        nextTermination = 0;
        nextHandoff = 0.0;
        resetTime = 0.0;
    }
    double getExponentTime(double lambda){
        return StdRandom.exp(lambda);
    }
    double getPoissonTime(double lambda){
        return StdRandom.poisson(lambda);
    }
    public double getNextTime(){
        return nextTime;
    }
    public Event getNextEvent(){
        return nextEvent;
    }
    public void setNextEvent(){
        if(nextArrival<=nextHandoff){
            if(nextArrival<=nextTermination){
                nextEvent = Event.CONNECT;
                nextTime = nextArrival;
            }
            else{
                nextEvent = Event.DISCONNECT;
                nextTime = nextTermination;
            }
        }
        else{
            if(nextHandoff<=nextTermination){
                nextEvent = Event.HANDOFF;
                nextTime = nextHandoff;
            }
            else{
                nextEvent = Event.DISCONNECT;
                nextTime = nextTermination;
            }
        }
        resetTime+=nextTime - currentTime;
        currentTime = nextTime;

    }
}