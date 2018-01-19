import java.util.ArrayList;

public class CellControl {
    static ArrayList<Cell> cells = new ArrayList<>();
    public void addCell(Cell c){
        cells.add(c);
    }
    static Cell getCell(int id){
        for(Cell c:cells){
            if(c.getCellId() == id )
                return c;
        }
        return null;
    }

}
