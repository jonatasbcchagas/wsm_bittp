import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TTPInstance {

    private static TTPInstance instance = null;

    public String instanceFileName;
    public String problemName;
    public String knapsackDataType;
    public int numberOfNodes;
    public int numberOfItems;
    public int capacityOfKnapsack;
    public double minSpeed;
    public double maxSpeed;
    public double rentingRatio;
    public String edgeWeightType;
    public double[][] nodes;
    public int[][] items;

    private TTPInstance() { }

    public static TTPInstance getInstance() {
        if (instance == null) instance = new TTPInstance();
        return instance;
    }

    public void readTTPInstance(String instanceFileName) {

        this.instanceFileName = instanceFileName;

        BufferedReader br = null;

        try {
            File file = new File(this.instanceFileName);
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                // process the line
                if (line.startsWith("PROBLEM NAME")) {
                    line = line.substring(line.indexOf(":") + 1);
                    line = line.replaceAll("\\s+", "");
                    this.problemName = line;
                }
                if (line.startsWith("KNAPSACK DATA TYPE")) {
                    line = line.substring(line.indexOf(":") + 1);
                    line = line.replaceAll("\\s+", "");
                    this.knapsackDataType = line;
                }
                if (line.startsWith("DIMENSION")) {
                    line = line.substring(line.indexOf(":") + 1);
                    line = line.replaceAll("\\s+", "");
                    this.numberOfNodes = Integer.parseInt(line);
                }
                if (line.startsWith("NUMBER OF ITEMS")) {
                    line = line.substring(line.indexOf(":") + 1);
                    line = line.replaceAll("\\s+", "");
                    this.numberOfItems = Integer.parseInt(line);
                }
                if (line.startsWith("CAPACITY OF KNAPSACK")) {
                    line = line.substring(line.indexOf(":") + 1);
                    line = line.replaceAll("\\s+", "");
                    this.capacityOfKnapsack = Integer.parseInt(line);
                }
                if (line.startsWith("MIN SPEED")) {
                    line = line.substring(line.indexOf(":") + 1);
                    line = line.replaceAll("\\s+", "");
                    this.minSpeed = Double.parseDouble(line);
                }
                if (line.startsWith("MAX SPEED")) {
                    line = line.substring(line.indexOf(":") + 1);
                    line = line.replaceAll("\\s+", "");
                    this.maxSpeed = Double.parseDouble(line);
                }
                if (line.startsWith("RENTING RATIO")) {
                    line = line.substring(line.indexOf(":") + 1);
                    line = line.replaceAll("\\s+", "");
                    this.rentingRatio = Double.parseDouble(line);
                }
                if (line.startsWith("EDGE_WEIGHT_TYPE")) {
                    line = line.substring(line.indexOf(":") + 1);
                    line = line.replaceAll("\\s+", "");
                    this.edgeWeightType = line;
                }
                if (line.startsWith("NODE_COORD_SECTION")) {
                    this.nodes = new double[this.numberOfNodes][3];
                    for (int i = 0; i < this.numberOfNodes; ++i) {
                        line = br.readLine();
                        String[] splittedLine = line.split("\\s+");
                        for (int j = 0; j < splittedLine.length; ++j) {
                            double temp = Double.parseDouble(splittedLine[j]);
                            // adjust city number by 1
                            if (j == 0) temp = temp - 1;
                            this.nodes[i][j] = temp;
                        }
                    }
                }
                if (line.startsWith("ITEMS SECTION")) {
                    this.items = new int[this.numberOfItems][4];
                    for (int i = 0; i < this.numberOfItems; ++i) {
                        line = br.readLine();
                        String[] splittedLine = line.split("\\s+");
                        for (int j = 0; j < splittedLine.length; ++j) {
                            int temp = Integer.parseInt(splittedLine[j]);
                            // adjust city number by 1
                            if (j == 0) temp = temp - 1; // item numbers start here with 0 --> in TTP files with 1
                            if (j == 3) temp = temp - 1; // city numbers start here with 0 --> in TTP files with 1
                            this.items[i][j] = temp;
                        }
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            System.err.println("ERROR:  TTP file \"" + instanceFileName + "\" not found.");
            System.exit(0);
        }
    }

    public int distance(int i, int j) {
        return (int) Math.ceil(Math.sqrt((this.nodes[i][1] - this.nodes[j][1]) * (this.nodes[i][1] - this.nodes[j][1]) + (this.nodes[i][2] - this.nodes[j][2]) * (this.nodes[i][2] - this.nodes[j][2])));
    }
    
    public double getAverageEgdeLength() {
        double avgEdgeLength = 0.0;
        for (int ini = 0; ini < numberOfNodes; ++ini) {
            for (int fin = ini+1; fin < numberOfNodes; ++fin) {                
                avgEdgeLength += distance(ini, fin);
            }
        }
        avgEdgeLength /= numberOfNodes;
        avgEdgeLength /= (numberOfNodes - 1);
        avgEdgeLength *= 2.0;
        return avgEdgeLength;        
    }
}