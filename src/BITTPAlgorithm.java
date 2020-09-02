import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

public class BITTPAlgorithm {

    private static long startTime = 0;
    private static int timeLimit = 600;
    private static RandomGenerator rg = null;
    private static NonDominatedSet pareto = new NonDominatedSet();
    private static TTPInstance instance = null;

    private static int[] cityIndex = null;
    private static int[] cityTourIndex = null;
    private static double[] dSoFar = null;
    private static double[] dToGo = null;
    private static double[] scores = null;
    private static double[][] sortData = null;
    
    private static int[] solveTSPLinKernighanHeuristic() {
        
        int[] tour = new int[instance.numberOfNodes + 1];

        String instanceFileName = instance.instanceFileName;
        
        String tspTourFileName = String.format("%s_%08d_%08d_%08d.tsp.sol", instanceFileName, rg.nextInt(100000000), Math.round(rg.nextInt(100000000) * Math.random()), Math.round(rg.nextInt(100000000) * Math.random()));

        try {
            List<String> command = new ArrayList<>();
            command.add(String.format("%s/tools/linkern", System.getProperty("user.dir")));            
            command.add("-o");
            command.add(tspTourFileName);
            command.add("-s");
            command.add(String.valueOf(rg.nextInt(987654321)));
            command.add("-Q");
            command.add(instanceFileName);

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            final Process process = builder.start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) { }
            br.close();
            process.waitFor();
            
            br = new BufferedReader(new FileReader(tspTourFileName));
            br.readLine(); // discard the first line

            for (int i = 0; i < tour.length - 1; ++i) {
                line = br.readLine();
                int number = Integer.parseInt(line.split("\\s+")[0]);
                tour[i] = number;
            }
            tour[tour.length - 1] = tour[0];
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        (new File(tspTourFileName)).delete();

        return tour;
    }

    private static void generateNewTTPSolutionsFromTwoOptMoves(TTPSolution s, double avgEdgeLength, double beta) {

        if (beta == -987654321) return;
                
        if (System.currentTimeMillis() - BITTPAlgorithm.startTime >= BITTPAlgorithm.timeLimit * 1000.0) return;

        int[] newTour = new int[instance.numberOfNodes + 1];

        int bestIni = -1, bestFin = -1, pos;
        double bestScore = Double.MIN_VALUE;

        for (int ini = 1; ini < s.tspTour.length - 1; ++ini) {
            for (int fin = ini + 1; fin < s.tspTour.length - 1; ++fin) {                
                if (System.currentTimeMillis() - BITTPAlgorithm.startTime >= BITTPAlgorithm.timeLimit * 1000.0) break;                
                int newDistance = s.totalDistance - instance.distance(s.tspTour[ini - 1], s.tspTour[ini]) - instance.distance(s.tspTour[fin], s.tspTour[fin + 1]) + instance.distance(s.tspTour[ini - 1], s.tspTour[fin]) + instance.distance(s.tspTour[ini], s.tspTour[fin + 1]);
                if (newDistance - s.totalDistance <= avgEdgeLength * beta) {
                    for (pos = 0; pos < ini; ++pos) newTour[pos] = s.tspTour[pos];
                    for (int i = fin; i >= ini; --i, ++pos) newTour[pos] = s.tspTour[i];
                    for (int i = fin + 1; i < s.tspTour.length; ++i, ++pos) newTour[pos] = s.tspTour[i];
                    double score = TTPSolution.calculateWeightedSumScore(newTour, s.packingPlan);
                    if (score > bestScore) { bestScore = score; bestIni = ini; bestFin = fin; }
                }
            }
        }

        if (bestIni + bestFin < 0) return;
        for (pos = 0; pos < bestIni; ++pos) newTour[pos] = s.tspTour[pos];
        for (int i = bestFin; i >= bestIni; --i, ++pos) newTour[pos] = s.tspTour[i];
        for (int i = bestFin + 1; i < s.tspTour.length; ++i, ++pos) newTour[pos] = s.tspTour[i];

        BITTPAlgorithm.pareto.add(new TTPSolution(newTour, s.packingPlan.clone()));
    }

    private static void generateNewTTPSolutionsFromBitFlipMoves(TTPSolution s, double lambda) {

        if (lambda <= 10E-5) return; 
        
        if (System.currentTimeMillis() - BITTPAlgorithm.startTime >= BITTPAlgorithm.timeLimit * 1000.0) return;

        int originalWeight = s.totalWeight;
        for (int i = 0; i < s.packingPlan.length; ++i) {
            if (System.currentTimeMillis() - BITTPAlgorithm.startTime >= BITTPAlgorithm.timeLimit * 1000.0) break;
            if (rg.nextDouble() > lambda) continue;
            if (s.packingPlan[i] || (!s.packingPlan[i] && originalWeight + instance.items[i][2] <= instance.capacityOfKnapsack)) {
                s.packingPlan[i] = !s.packingPlan[i];
                BITTPAlgorithm.pareto.add(new TTPSolution(s.tspTour.clone(), s.packingPlan.clone()));
                s.packingPlan[i] = !s.packingPlan[i];
            }
        }
    }
    
    private static boolean[] randomizedPackingAlgorithm(int[] tour, int rho, int gamma) {

        // Calculate information ready for heuristics
        dSoFar[0] = 0;
        cityTourIndex[0] = 0;

        for (int i = 1; i < tour.length - 1; ++i) {
            dSoFar[i] = dSoFar[i - 1] + instance.distance(tour[i - 1], tour[i]);
            cityIndex[tour[i]] = i;
        }

        for (int i = 0; i < instance.numberOfNodes; ++i) {
            dToGo[i] = dSoFar[instance.numberOfNodes - 1] - dSoFar[i];
        }

        for (int i = 0; i < instance.numberOfItems; ++i) {
            cityTourIndex[i] = cityIndex[instance.items[i][3]];
        }
        
        boolean[] packingPlan = new boolean[instance.numberOfItems];
        boolean[] packingPlanLine = new boolean[instance.numberOfItems];
        
        boolean[] packingPlanBest = new boolean[instance.numberOfItems];
        double weightedSumScoreBest = TTPSolution.calculateWeightedSumScore(tour, packingPlanBest);
        
        double weightedSumScoreEmptyKnapsack = weightedSumScoreBest;
       
        for(int rhoLine = 1; rhoLine <= rho; ++rhoLine) {
            
            if (System.currentTimeMillis() - BITTPAlgorithm.startTime >= BITTPAlgorithm.timeLimit * 1000.0) break;
                    
            double powerA = rg.nextDouble();
            double powerB = rg.nextDouble();
            double powerC = rg.nextDouble();
    
            double sumPowers = powerA + powerB + powerC;
    
            powerA = powerA / sumPowers;
            powerB = powerB / sumPowers;
            powerC = powerC / sumPowers;
    
            for (int i = 0; i < instance.numberOfItems; ++i) {
                BITTPAlgorithm.scores[i] = Math.pow(instance.items[i][1], powerA) / (Math.pow(BITTPAlgorithm.dToGo[BITTPAlgorithm.cityTourIndex[i]], powerB) * Math.pow(BITTPAlgorithm.instance.items[i][2], powerC));
            }

            for (int i = 0; i < instance.numberOfItems; ++i) {
                BITTPAlgorithm.sortData[i][0] = i;
                BITTPAlgorithm.sortData[i][1] = BITTPAlgorithm.scores[i];
            }

            Comparator<double[]> newComp = new Comparator<double[]>() {
                @Override
                public int compare(double[] left, double[] right) {
                    return -Double.compare(left[1], right[1]);
                }
            };

            Arrays.sort(BITTPAlgorithm.sortData, newComp);

            int varphi = (int) Math.ceil(instance.numberOfItems / (double)gamma * TTPSolution.alpha + Math.pow(10, -5));
            
            for (int i = 0; i < instance.numberOfItems; ++i) {
                packingPlan[i] = packingPlanLine[i] = false;
            }
            int k = 0, kLine = 0;
            
            int totalWeight = 0, totalWeightLine = 0;
            
            double weightedSumScoreLine, weightedSumScore = weightedSumScoreEmptyKnapsack;
            
            boolean hasNewPackingPlan = false;
            
            while (kLine < instance.numberOfItems && varphi >= 1) {
         
                if (System.currentTimeMillis() - BITTPAlgorithm.startTime >= BITTPAlgorithm.timeLimit * 1000.0) break;
                
                int itemIndex = (int)BITTPAlgorithm.sortData[kLine][0];
                if (totalWeightLine + instance.items[itemIndex][2] <= instance.capacityOfKnapsack) {
                    packingPlanLine[itemIndex] = true;
                    totalWeightLine += instance.items[itemIndex][2];
                    hasNewPackingPlan = true;
                }
                if ((kLine+1) % varphi == 0 && hasNewPackingPlan == true) {
                    weightedSumScoreLine = TTPSolution.calculateWeightedSumScore(tour, packingPlanLine);                    
                    if (weightedSumScoreLine > weightedSumScore) {
                        weightedSumScore = weightedSumScoreLine;
                        System.arraycopy(packingPlanLine, 0, packingPlan, 0, packingPlanLine.length);
                        totalWeight = totalWeightLine;
                        k = kLine;                           
                    }
                    else {
                        System.arraycopy(packingPlan, 0, packingPlanLine, 0, packingPlan.length);
                        totalWeightLine = totalWeight;
                        kLine = k;
                        varphi = (int)Math.floor(varphi / 2.0);
                    }
                    hasNewPackingPlan = false;
                }
                kLine++;
            }
            if(weightedSumScore > weightedSumScoreBest) {
                weightedSumScoreBest = weightedSumScore;
                System.arraycopy(packingPlan, 0, packingPlanBest, 0, packingPlan.length);                
            }            
        }
       
        return packingPlanBest;
    }

    public static void solve(int probabilityDistribution, double v1, double v2, int eta, int rho, int gamma, double beta, double lambda, int timeLimit, long seed, String outputFileName, boolean doNotSaveF, boolean doNotSaveX, boolean doNotSaveTTPSol, boolean doNotSaveTTPLog) {

        BITTPAlgorithm.instance = TTPInstance.getInstance();

        BITTPAlgorithm.startTime = System.currentTimeMillis();
        BITTPAlgorithm.timeLimit = timeLimit;
        
        double avgEdgeLength = -1;
        // avgEdgeLength just matters when beta is not INF (-987654321) neither 0 
        if (beta != -987654321 && beta != 0) avgEdgeLength = instance.getAverageEgdeLength();
                
        BITTPAlgorithm.rg = new Well19937c();
        BITTPAlgorithm.rg.setSeed(seed);
        
        AbstractRealDistribution randomDistribution = null;
        if(probabilityDistribution == 0) randomDistribution = new UniformRealDistribution(rg, v1, v2);
        else if(probabilityDistribution == 1) randomDistribution = new NormalDistribution(rg, v1, v2);
        else if(probabilityDistribution == 2) randomDistribution = new BetaDistribution(rg, v1, v2);        

        BITTPAlgorithm.cityIndex = new int[instance.numberOfNodes];
        BITTPAlgorithm.cityTourIndex = new int[instance.numberOfItems];
        BITTPAlgorithm.dSoFar = new double[instance.numberOfNodes];
        BITTPAlgorithm.dToGo = new double[instance.numberOfNodes];
        BITTPAlgorithm.scores = new double[instance.numberOfItems];
        BITTPAlgorithm.sortData = new double[instance.numberOfItems][2];
        
        TTPSolution s = null;
        
        List<double[]> bestTTPScoresOverTime = new ArrayList<>();
        double bestTTPScore = -987654321.0;

        while (true) {

            if (System.currentTimeMillis() - BITTPAlgorithm.startTime >= BITTPAlgorithm.timeLimit * 1000.0) break;
            
            int[] tour = BITTPAlgorithm.solveTSPLinKernighanHeuristic();
                        
            for(int k = 0; k < eta; ++k) {
                if (System.currentTimeMillis() - BITTPAlgorithm.startTime >= BITTPAlgorithm.timeLimit * 1000.0) break;                
                TTPSolution.alpha = Math.max(0.0, Math.min(randomDistribution.sample(), 1.0));                                
                boolean[] packingPlan = BITTPAlgorithm.randomizedPackingAlgorithm(tour, rho, gamma);
                double currentTTPScore = BITTPAlgorithm.pareto.add(new TTPSolution((k == 0) ? tour : tour.clone(), packingPlan));
                if(currentTTPScore > bestTTPScore) {
                    bestTTPScore = currentTTPScore;
                    bestTTPScoresOverTime.add(new double[]{bestTTPScore, (System.currentTimeMillis() - BITTPAlgorithm.startTime) / 1000.0});
                }
            }
            
            if (beta == -987654321 && lambda <= 10E-5) continue; 
            
            TTPSolution.alpha = Math.max(0.0, Math.min(randomDistribution.sample(), 1.0));
                    
            BITTPAlgorithm.pareto.getBestSolutionForTheNewWeightSumScore();
            
            BITTPAlgorithm.generateNewTTPSolutionsFromTwoOptMoves(s, avgEdgeLength, beta);
            
            BITTPAlgorithm.generateNewTTPSolutionsFromBitFlipMoves(s, lambda);
        }

        BITTPAlgorithm.pareto.saveNDSBITTPSolutions(outputFileName, doNotSaveF, doNotSaveX);
        if(!doNotSaveTTPSol) BITTPAlgorithm.pareto.saveBestTTPSolution(outputFileName);
        
        if(!doNotSaveTTPLog) {
            BufferedWriter writerTTPLog;
            try {
                writerTTPLog = new BufferedWriter(new FileWriter(outputFileName + ".ttp.log", false));                           
                for (Iterator<double[]> it = bestTTPScoresOverTime.iterator(); it.hasNext();) {
                    double [] scoreAndRuntime = it.next();                
                    writerTTPLog.write(String.format("%.5f %.5f\n", scoreAndRuntime[0], scoreAndRuntime[1]));
                }
                writerTTPLog.flush();
                writerTTPLog.close();
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
    }
}
