import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class NonDominatedSet {

    private List<TTPSolution> solutions = null;
    private static double EPS = 0.00001;

    public NonDominatedSet() {
        this.solutions = new LinkedList<>();
    }

    public int getRelation(TTPSolution s1, TTPSolution s2) {
        int val = 0;
        for (int i = 0; i < s1.objectives.size(); i++) {
            if (s1.objectives.get(i) + EPS < s2.objectives.get(i)) {
                if (val == -1) return 0;
                val = 1;
            }
            else if (s1.objectives.get(i) > s2.objectives.get(i) + EPS) {
                if (val == 1) return 0;
                val = -1;
            }
        }
        return val;
    }

    public boolean equalsInDesignSpace(TTPSolution s1, TTPSolution s2) {
        // for simplification and efficiency, I've decided to consider that two solutions are equals if their objectives are the same.
        for (int i = 0; i < s1.objectives.size(); ++i) {
            if (Math.abs(s1.objectives.get(i) - s2.objectives.get(i)) > EPS) return false;
        }
        return true;
    }

    public void add(TTPSolution s1) {

        boolean isAdded = true;

        for (Iterator<TTPSolution> it = this.solutions.iterator(); it.hasNext();) {
            TTPSolution s2 = it.next();
            int rel = getRelation(s1, s2);
            if (rel == -1 || (rel == 0 && equalsInDesignSpace(s1, s2))) {
                isAdded = false;
                break;
            }
            else if (rel == 1) it.remove();
        }

        if (isAdded) this.solutions.add(s1);
    }

    public TTPSolution getBestSolutionForTheNewWeightSumScore() {

        double alpha = TTPSolution.alpha;
        double rentingRatio = TTPInstance.getInstance().rentingRatio;

        Iterator<TTPSolution> it = this.solutions.iterator();
        TTPSolution s = it.next();
        double fs = alpha * s.totalProfit - (1.0 - alpha) * rentingRatio * s.totalTime;
        while (it.hasNext()) {
            TTPSolution sLine = it.next();
            if (alpha * sLine.totalProfit - (1.0 - alpha) * rentingRatio * sLine.totalTime > fs) {
                fs = alpha * sLine.totalProfit - (1.0 - alpha) * rentingRatio * sLine.totalTime;
                s = sLine;
            }
        }
        return s;
    }

    public void saveNDSBITTPSolutions(String outputFileName, boolean doNotSaveF, boolean doNotSaveX) {

        Comparator<TTPSolution> newComp = new Comparator<TTPSolution>() {
            @Override
            public int compare(TTPSolution left, TTPSolution right) {
                return Double.compare(left.objectives.get(0), right.objectives.get(0));
            }
        };
        this.solutions.sort(newComp);

        if (!doNotSaveF) {
            BufferedWriter writerF = null;
            // BufferedWriter writerFAlpha = null;
            try {
                writerF = new BufferedWriter(new FileWriter(outputFileName + ".bittp.f", false));
                // writerFAlpha = new BufferedWriter(new FileWriter(outputFileName + ".bittp.f.alpha", false));
                for (Iterator<TTPSolution> it = this.solutions.iterator(); it.hasNext();) {
                    TTPSolution s = it.next();
                    if (!s.checkFeasibility()) {
                        writerF.write("ERROR: infeasible solutions!");
                        writerF.flush();
                        writerF.close();
                        // writerFAlpha.write("ERROR: infeasible solutions!");
                        // writerFAlpha.flush();
                        // writerFAlpha.close();
                        System.exit(0);
                    }
                    writerF.write(String.format("%.5f %d\n", s.totalTime, s.totalProfit));
                    writerF.flush();
                    // writerFAlpha.write(String.format("%.5f %d %.5f\n", s.totalTime, s.totalProfit, s.itsAlpha));
                    // writerFAlpha.flush();
                }
                writerF.close();
                // writerFAlpha.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!doNotSaveX) {
            BufferedWriter writerX = null;
            try {
                writerX = new BufferedWriter(new FileWriter(outputFileName + ".bittp.x", false));
                for (Iterator<TTPSolution> it = this.solutions.iterator(); it.hasNext();) {
                    TTPSolution s = it.next();
                    if (doNotSaveF && !s.checkFeasibility()) {
                        writerX.write("ERROR: infeasible solutions!");
                        writerX.flush();
                        writerX.close();
                        System.exit(0);
                    }
                    s.save(writerX);
                    writerX.flush();
                }
                writerX.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void saveBestTTPSolution(String outputFileName) {

        Iterator<TTPSolution> it = this.solutions.iterator();
        TTPSolution s = it.next();
        while (it.hasNext()) {
            TTPSolution sLine = it.next();
            if (sLine.singleObjectiveScore > s.singleObjectiveScore) {
                s = sLine;
            }
        }
        
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outputFileName + ".ttp.sol", false));
            if (!s.checkFeasibility()) {
                writer.write("ERROR: infeasible solutions!");
                writer.flush();
                writer.close();
                System.exit(0);
            }
            s.save(writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
