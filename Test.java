
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;
class Solution {

    public static void main(String[] args) {
//            ArrayList<Integer> ints = new ArrayList<>();
//            ints.add(new Integer(1));
//            ints.add(4);
//            ints.add(7);
//            ints.add(2);
//            ints.remove(new Integer(1));
//            ints.sort(new com());
//            ints.add(1, 3);
//            System.out.println("Hi");
//            for (int i = 0;i<80;i++)
//                System.out.println(getNext(100));

        try {
     //       File f = new File("./in.txt");
     //       PrintWriter pw = new PrintWriter(f);
            Scanner br = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
            int t =br.nextInt();
            int c = 1;
            while (t-- > 0) {
           //     pw.println("TEST CASE "+ (t+1));
                boolean flag = false;
                int a = br.nextInt();
                int length;
                if(a%3==0)
                    length = a/3;
                else
                    length = a/3+1;
                int arr [][] = new int[length][3];
                //hard code for first 3
                while(true){
                    System.out.println("2 2");
                    System.out.flush();
                    int ir = br.nextInt();
                    int ic = br.nextInt();
              //      pw.println("Recieved this: " +ir + " "+ic);
                    if(ir==-1||ir==0) {
                        flag = true;
                        break;
                    }
                    arr[ir-1][ic-1] =1;
                    if(arr[0][0] == 1 && arr[0][1] == 1 && arr[0][2] == 1)
                        break;
                }
        //        pw.println("Flag is: "+ flag + " after exiting from Row 1");
                for(int j = 0 ;j<length;j++){
       //             pw.println(arr[j][0]+ " " + arr[j][1] + " "+ arr[j][2]);
                }
                if(flag)
                    continue;
                int i;
                for( i = 3;i<=length-1;){
                    System.out.println(i+" 2");
                    System.out.flush();
                    int ir = br.nextInt();
                    int ic = br.nextInt();
              //      pw.println("Recieved this: " +ir + " "+ic);
                    if(ir==-1||ir ==0 ){
                        flag = true;
                        break;
                    }
                    arr[ir-1][ic-1] = 1;
                    if(arr[i-2][0] == 1 && arr[i-2][1] == 1 && arr[i-2][2] == 1) {
                  //      pw.println("Another Row Completed : " + (i-1));
                        i++;
                    }

                }
//                pw.println("Flag is: "+ flag + " after exiting from Row: "+i);
//                for(int j = 0 ;j<length;j++){
//                    pw.println(arr[j][0]+ " " + arr[j][1] + " "+ arr[j][2]);
//                }
                if(flag)
                    continue;
                while(true){
                    System.out.println((length-1)+" 2");
                    System.out.flush();
                    int ir = br.nextInt();
                    int ic = br.nextInt();
                   // pw.println("Recieved this: " +ir + " "+ic);
                    if(ir==-1 || ir == 0) {
                        flag = true;
                        break;
                    }
                    arr[ir-1][ic-1] =1;
                    if(arr[length-1][0] == 1 && arr[length-1][1] == 1 && arr[length-1][2] == 1 && arr[length-2][0] == 1 && arr[length-2][1] == 1 && arr[length-2][2] == 1)
                        break;
                }
               // pw.println("DONE ACCORDING TO CODE");
//                for(int j = 0 ;j<length;j++){
//                    pw.println(arr[j][0]+ " " + arr[j][1] + " "+ arr[j][2]);
//                }
               // pw.println("****************");

            }
//        pw.close();
//            pw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
