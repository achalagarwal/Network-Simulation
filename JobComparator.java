import java.util.Comparator;

public class JobComparator implements Comparator<Job> {
    @Override
    public int compare(Job o1, Job o2) {
        if(o1.startTime<=o2.startTime)
            return -1;
        else
            return +1;
    }
}
