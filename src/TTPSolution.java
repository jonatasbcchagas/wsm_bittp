import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TTPSolution {

    public int[] tspTour;
    public boolean[] packingPlan;
 
    public int totalProfit = Integer.MIN_VALUE;
    public double totalTime = Double.POSITIVE_INFINITY;
    public int totalDistance = Integer.MAX_VALUE;
    public int totalWeight = Integer.MAX_VALUE;
    public double singleObjectiveScore = Double.NEGATIVE_INFINITY;
    public double weightedSumScore = Double.NEGATIVE_INFINITY;
    public List<Double> objectives = null;
    public double itsAlpha;

    public static double alpha = 0.5;

    public TTPSolution(int[] tspTour, boolean[] packingPlan) {
        this.tspTour = tspTour;
        this.packingPlan = packingPlan;
        this.evaluate();
    }

    public void evaluate() {

        TTPInstance instance = TTPInstance.getInstance();

        int capacityOfKnapsack = instance.capacityOfKnapsack;
        double rentRate = instance.rentingRatio;
        double vmin = instance.minSpeed;
        double vmax = instance.maxSpeed;
        double v = (vmax - vmin) / capacityOfKnapsack;

        this.totalWeight = 0;
        this.totalProfit = 0;
        this.totalDistance = instance.distance(this.tspTour[0], this.tspTour[1]);
        this.totalTime = this.totalDistance / (vmax - v * this.totalWeight);

        int itemsPerCity = this.packingPlan.length / (this.tspTour.length - 2);

        for (int i = 1; i < this.tspTour.length - 1; ++i) {
            int currentCity = this.tspTour[i] - 1; 
            for (int itemNumber = 0; itemNumber < itemsPerCity; itemNumber++) {
                int itemIndex = currentCity + itemNumber * (instance.numberOfNodes - 1);
                if (this.packingPlan[itemIndex]) {
                    this.totalProfit = this.totalProfit + instance.items[itemIndex][1];
                    this.totalWeight = this.totalWeight + instance.items[itemIndex][2];
                }
            }
            int distance = instance.distance(this.tspTour[i], this.tspTour[(i + 1) % (this.tspTour.length - 1)]);
            this.totalDistance += distance;
            this.totalTime = this.totalTime + (distance / (vmax - v * this.totalWeight));
        }
        
        this.weightedSumScore = alpha * this.totalProfit - (1.0 - alpha) * rentRate * this.totalTime;
        this.singleObjectiveScore = this.totalProfit - this.totalTime * rentRate;
        this.objectives = Arrays.asList(1.0 * this.totalTime, -1.0 * this.totalProfit);
        this.itsAlpha = alpha;
    }

    public static double calculateWeightedSumScore(int[] tspTour, boolean[] packingPlan) {

        TTPInstance instance = TTPInstance.getInstance();

        int capacityOfKnapsack = instance.capacityOfKnapsack;
        double rentRate = instance.rentingRatio;
        double vmin = instance.minSpeed;
        double vmax = instance.maxSpeed;
        double v = (vmax - vmin) / capacityOfKnapsack;

        int _totalWeight = 0;
        int _totalProfit = 0;
        double _totalTime = instance.distance(tspTour[0], tspTour[1]) / (vmax - v * _totalWeight);

        int itemsPerCity = packingPlan.length / (tspTour.length - 2);

        for (int i = 1; i < tspTour.length - 1; ++i) {
            int currentCity = tspTour[i] - 1; 
            for (int itemNumber = 0; itemNumber < itemsPerCity; itemNumber++) {
                int itemIndex = currentCity + itemNumber * (instance.numberOfNodes - 1);
                if (packingPlan[itemIndex]) {
                    _totalProfit = _totalProfit + instance.items[itemIndex][1];
                    _totalWeight = _totalWeight + instance.items[itemIndex][2];
                }
            }
            int distance = instance.distance(tspTour[i], tspTour[i + 1]);
            _totalTime = _totalTime + (distance / (vmax - v * _totalWeight));
        }
        return alpha * _totalProfit - (1.0 - alpha) * rentRate * _totalTime;
    }

    public boolean checkFeasibility() {

        // correctness check: does the tour start and end in the same city
        if (this.tspTour[0] != this.tspTour[this.tspTour.length - 1]) {
            System.err.println("ERROR: The last city is not the same as the first city.");
            return false;
        }

        // correctness check: does the tour contains all cities
        Set<Integer> hashSet = new HashSet<>();
        for (int i = 0; i < tspTour.length; ++i)
            hashSet.add(tspTour[i]);
        if (hashSet.size() != tspTour.length - 1) {
            System.err.println("ERROR: tour is not complete.");
            return false;
        }

        TTPInstance instance = TTPInstance.getInstance();

        int capacityOfKnapsack = instance.capacityOfKnapsack;
        double vmin = instance.minSpeed;
        double vmax = instance.maxSpeed;
        double v = (vmax - vmin) / capacityOfKnapsack;

        if (totalWeight > capacityOfKnapsack) {
            System.err.println("ERROR: knapsack capacity extrapolated.");
            return false;
        }

        int _totalWeight = 0;
        int _totalProfit = 0;
        double _totalTime = instance.distance(tspTour[0], tspTour[1]) / (vmax - v * _totalWeight);

        int itemsPerCity = this.packingPlan.length / (this.tspTour.length - 2);

        for (int i = 1; i < tspTour.length - 1; ++i) {
            int currentCity = tspTour[i] - 1; 
            for (int itemNumber = 0; itemNumber < itemsPerCity; itemNumber++) {
                int itemIndex = currentCity + itemNumber * (instance.numberOfNodes - 1);
                if (packingPlan[itemIndex]) {
                    _totalProfit = _totalProfit + instance.items[itemIndex][1];
                    _totalWeight = _totalWeight + instance.items[itemIndex][2];
                }
            }
            int distance = instance.distance(tspTour[i], tspTour[i + 1]);
            _totalTime = _totalTime + (distance / (vmax - v * _totalWeight));
        }

        if (_totalWeight != totalWeight || _totalProfit != totalProfit || Double.compare(_totalTime, totalTime) != 0) {
            System.err.println("ERROR: wrong solution evaluation.");
            return false;
        }

        return true;
    }

    public void save(BufferedWriter writerX) {

        try {
            writerX.write(String.format("[%d", tspTour[0] + 1));
            for (int i = 1; i < tspTour.length - 1; ++i) {
                writerX.write(String.format(",%d", tspTour[i] + 1));
            }
            writerX.write("]\n[");
            boolean firstItem = true;
            for (int i = 0; i < packingPlan.length; ++i) {
                if (packingPlan[i]) {
                    if (!firstItem) writerX.write(",");
                    writerX.write(String.format("%d", i + 1));
                    firstItem = false;
                }
            }
            writerX.write("]\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}