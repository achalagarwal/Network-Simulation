import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.Comparator;

class com implements Comparator<Integer> {
    @Override
    public int compare(Integer o1, Integer o2) {
        if (o1.intValue() < o2.intValue())
            return -1;
        else
            return 1;
    }

}

    public class Test {
        public static double getNext(int lambda) {
            return  Math.log(1-Math.random()/(-lambda));
        }
        public static void main(String[] args) {
            ArrayList<Integer> ints = new ArrayList<>();
            ints.add(new Integer(1));
            ints.add(4);
            ints.add(7);
            ints.add(2);
            ints.remove(new Integer(1));
            ints.sort(new com());
            ints.add(1, 3);
            System.out.println("Hi");
            for (int i = 0;i<80;i++)
                System.out.println(getNext(100));
        }
    }

