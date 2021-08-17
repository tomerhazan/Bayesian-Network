import java.io.*;
import java.util.ArrayList;

public class Algorithms {

    public void calcAlgorithms(Read read) throws IOException {
        ArrayList<String> output = new ArrayList<>();
        for (int i=0; i<read.network.queries.length;i++){
            double ans = 0;
            if (read.network.queries[i].algoNum == 1){
                Algorithm1 algorithm1 = new Algorithm1();
                ans = algorithm1.calcAlgo(read.network.Variables,read.network.queries[i],0,0,true);
                output.add(String.format("%.5f",ans) + "," + algorithm1.numberOfAdd + "," + algorithm1.numberOfMult) ;
            }else if(read.network.queries[i].algoNum == 2){
                Algorithm2 algorithm2 = new Algorithm2();
                ans = algorithm2.calcAlgo(read.network.Variables,read.network.queries[i]);
                output.add(String.format("%.5f",ans) + "," + algorithm2.numberOfAdd + "," + algorithm2.numberOfMult) ;
            }else if(read.network.queries[i].algoNum == 3){
                Algorithm3 algorithm3 = new Algorithm3();
                ans = algorithm3.calcAlgo(read.network.Variables,read.network.queries[i]);
                output.add(String.format("%.5f",ans) + "," + algorithm3.numberOfAdd + "," + algorithm3.numberOfMult) ;
            }
        }
        try {
            FileWriter fileWriter = new FileWriter("output.txt");
            for (int i = 0; i < output.size(); i++) {
                fileWriter.write(output.get(i) + "\n");
            }
            fileWriter.close();
        }catch (IOException error){
            System.out.println(error);
        }
    }
}
