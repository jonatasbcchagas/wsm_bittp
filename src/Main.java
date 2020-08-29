import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

public class Main {

    private static void usage() {
        
        System.out.println("java -jar wsm.jar [parameters]\n");
        System.out.println("parameters:");
        System.out.println("            --inputfile <ttp_file>                   or     -i <ttp_file>                   (path to the file with TTP data)");
        System.out.println("            --distrib <prob_distrib> <v1> <v2>       or     -d <prob_distrib> <v1> <v2>     (0: Uniform distribution with a=v1 and b=v2");
        System.out.println("                                                                                             1: Gaussian distribution with mu=v1 and sigma=v2");
        System.out.println("                                                                                             2: Beta distribution with alpha=v1 and beta=v2)");
        System.out.println("            --eta <eta_value>                        or     -e <eta_value>                  (any positive non-zero integer number)");
        System.out.println("            --rho <rho_value>                        or     -r <rho_value>                  (any positive non-zero integer number)");
        System.out.println("            --gamma <gamma_value>                    or     -g <gamma_value>                (any positive non-zero integer number)");
        System.out.println("            --beta <beta_value>                      or     -b <beta_value>                 (any number)");
        System.out.println("            --lambda <lambda_value>                  or     -l <lambda_value>               (any positive number)");
        System.out.println("            --time <runtime_in_seconds>              or     -t <runtime_in_seconds>         (runtime in seconds)");
        System.out.println("            --seed <random_seed>                     or     -s <random_seed>                (seed for random numbers)");
        System.out.println("            --outputfile <output_file>               or     -o <output_file>                (<output_file> + \".bittp.f\" stores the non-dominated solutions (time and profit)");
        System.out.println("                                                                                             <output_file> + \".bittp.x\" stores the non-dominated solutions (tours and packing plans)");
        System.out.println("                                                                                             <output_file> + \".ttp.sol\" stores the best TTP solution found)");
        System.out.println("            --donotsavef                                                                    (do not save <output_file> + \".bittp.f\")");
        System.out.println("            --donotsavex                                                                    (do not save <output_file> + \".bittp.x\")");
        System.out.println("            --donotsavettpsol                                                               (do not save <output_file> + \".ttp.sol\")");
        System.out.println();
        System.out.println("default values:");
        System.out.println("                    prob_distrib         0  (v1=0, v2=1)");
        System.out.println("                    eta_value                         10");
        System.out.println("                    rho_value                          1");
        System.out.println("                    gamma_value                       10");
        System.out.println("                    beta_value                      -INF     (no 2-opt moves)");
        System.out.println("                    lambda_value                       0     (no bit-flip moves)");
        System.out.println("                    runtime_in_seconds               600");
        System.out.println("                    random_seed                 11235813");
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {

        Locale.setDefault(new Locale("en", "US"));

        if (args.length == 0) usage();
        
        String inputFileName = null, outputFileName = null;

        double v1 = 0.0, v2 = 1.0, lambda = 0.0, beta = -987654321.0;
        int probDistrib = 0, eta = 10, rho = 1, gamma = 10, runtime = 600;
        long randomSeed = 11235813;
        boolean doNotSaveF = false, doNotSaveX = false, doNotSaveTTPSol = false;
        
        int checkParameters = 0;

        try {
            for (int i = 0; i < args.length; i += 2) {
                if (args[i].equals("--inputfile") || args[i].equals("-i")) {
                    inputFileName = args[i + 1]; checkParameters += 1;
                }
                else if (args[i].equals("--distrib") || args[i].equals("-d")) {
                    probDistrib = Integer.parseInt(args[i + 1]);
                    if (!(probDistrib == 0 || probDistrib == 1 || probDistrib == 2)) { checkParameters = -1; break; }
                    v1 = Double.parseDouble(args[i + 2]); v2 = Double.parseDouble(args[i + 3]); i += 2;
                    if ((probDistrib == 0 || probDistrib == 1) && (v1 < 0.0 || v2 < 0.0 || v1 > 1.0 || v2 > 1.0)) { checkParameters = -1; break; }
                    if (probDistrib == 0 && v1 > v2) { checkParameters = -1; break; }
                }
                else if (args[i].equals("--eta") || args[i].equals("-e")) {
                    eta = Integer.parseInt(args[i + 1]); 
                    if (eta <= 0) { checkParameters = -1; break; }
                }
                else if (args[i].equals("--rho") || args[i].equals("-r")) {
                    rho = Integer.parseInt(args[i + 1]); 
                    if (rho <= 0) { checkParameters = -1; break; }
                }
                else if (args[i].equals("--gamma") || args[i].equals("-g")) {
                    gamma = Integer.parseInt(args[i + 1]); 
                    if (gamma <= 0) { checkParameters = -1; break; }
                }
                else if (args[i].equals("--beta") || args[i].equals("-b")) {
                    beta = Double.parseDouble(args[i + 1]);
                }
                else if (args[i].equals("--lambda") || args[i].equals("-l")) {
                    lambda = Double.parseDouble(args[i + 1]);
                    if (lambda < 0.0) { checkParameters = -1; break; }
                }
                else if (args[i].equals("--time") || args[i].equals("-t")) {
                    runtime = Integer.parseInt(args[i + 1]);
                    if (runtime <= 0) { checkParameters = -1; break; }
                }
                else if (args[i].equals("--seed") || args[i].equals("-s")) {
                    randomSeed = Long.parseLong(args[i + 1]);
                    if (randomSeed < 0) { checkParameters = -1; break; }
                }
                else if (args[i].equals("--outputfile") || args[i].equals("-o")) {
                    outputFileName = args[i + 1]; checkParameters += 1;
                }
                else if (args[i].equals("--donotsavef")) { doNotSaveF = true; i -= 1; }
                else if (args[i].equals("--donotsavex")) { doNotSaveX = true; i -= 1; }
                else if (args[i].equals("--donotsavettpsol")) { doNotSaveTTPSol = true; i -= 1; }
                else { checkParameters = -1; break; }
            }
        } catch (NumberFormatException e) {
            Main.usage();
        }
        
        if (checkParameters != 2) Main.usage();

        if (outputFileName != null && Files.isDirectory(Paths.get((new File(outputFileName)).getAbsolutePath()).getParent()) == false) {
            System.err.println("ERROR:  path to output file \"" + (new File(outputFileName)).getAbsolutePath().toString() + "\" not found.");
            System.exit(0);
        }

        TTPInstance.getInstance().readTTPInstance(inputFileName);
        
        BITTPAlgorithm.solve(probDistrib, v1, v2, eta, rho, gamma, beta, lambda, runtime, randomSeed, outputFileName, doNotSaveF, doNotSaveX, doNotSaveTTPSol);
    }
}
