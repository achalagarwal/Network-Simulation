import java.util.ArrayList;

public class BSC_Control {
    static ArrayList<BSC> bscs = new ArrayList<>();
    public void addBSC(BSC c){
        bscs.add(c);
    }
    static BSC getBSC(int id){
        for(BSC c:bscs){
            if(c.id == id )
                return c;
        }
        return null;
    }

}
