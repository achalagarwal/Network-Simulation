import java.util.Random;

public class Connection {
    double startTime;
    double endTime;
    boolean handoff;
    double holdTime;
    int cellId;
    Connection(int id){
        cellId = id;
        startTime = System.nanoTime();
        endTime = this.startTime + StdRandom.exp(CellControl.getCell(cellId).getCallTerminationRate());
        holdTime = this.startTime + StdRandom.exp(1/CellControl.getCell(cellId).getHandoffRate());
        if(holdTime<endTime){
            handoff = true;
        }

    }
    public void handoff(){
        int number = new Random().nextInt(6);
        int next = CellControl.getCell(cellId).getNeighbour(number);
        cellId = next;
    }
    public void setEndTime(double lambda){
        endTime = this.startTime + StdRandom.exp(lambda);
    }
    public void setHandoff(){

    }
}
