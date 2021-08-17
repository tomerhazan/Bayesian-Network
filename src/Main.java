import java.io.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        File inputFile = new File("input.txt");
        Read read = new Read();
        Algorithms algorithms = new Algorithms();

        try {
            Scanner scanner1 = new Scanner(inputFile);
            read.firstLane(scanner1.nextLine());
            read.secondLane(scanner1.nextLine());
            while (scanner1.hasNext()){
                read.readLane(scanner1.nextLine());
            }
            algorithms.calcAlgorithms(read);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}