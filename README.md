# A weighted-sum method for solving the bi-objective traveling thief problem

This repository contains the source code and data associated to the paper ["A weighted-sum method for solving the bi-objective traveling thief problem"](https://cdn.shopify.com/s/files/1/0787/7841/products/3_563d6937-2fb2-42e8-a0e4-aacc75daf4f0.png?v=1557918086) by Jonatas B. C. Chagas and Markus Wagner. In this paper, we study a bi-objective formulation of the traveling thief problem, which has as components the traveling salesperson problem and the knapsack problem. We present a weighted-sum method that makes use of randomized versions of existing heuristics. Our algorithm and the results achieved are described in detail in the aforementioned paper.

### Usage:

```console
$ java -jar wsm.jar [parameters]

parameters:
                    --inputfile <ttp_file>                   or     -i <ttp_file>                   (path to the file with TTP data)
                    --distrib <prob_distrib> <v1> <v2>       or     -d <prob_distrib> <v1> <v2>     (0: Uniform distribution with a=v1 and b=v2
                                                                                                     1: Gaussian distribution with mu=v1 and sigma=v2
                                                                                                     2: Beta distribution with alpha=v1 and beta=v2)
                    --eta <eta_value>                        or     -e <eta_value>                  (any positive non-zero integer number)
                    --rho <rho_value>                        or     -r <rho_value>                  (any positive non-zero integer number)
                    --gamma <gamma_value>                    or     -g <gamma_value>                (any positive non-zero integer number)
                    --beta <beta_value>                      or     -b <beta_value>                 (any number)
                    --lambda <lambda_value>                  or     -l <lambda_value>               (any positive number)
                    --time <runtime_in_seconds>              or     -t <runtime_in_seconds>         (runtime in seconds)
                    --seed <random_seed>                     or     -s <random_seed>                (seed for random numbers)
                    --outputfile <output_file>               or     -o <output_file>                (<output_file> + ".bittp.f" stores the non-dominated solutions (time and profit)
                                                                                                     <output_file> + ".bittp.x" stores the non-dominated solutions (tours and packing plans)
                                                                                                     <output_file> + ".ttp.sol" stores the best TTP solution found)
                                                                                                     <output_file> + ".ttp.log" stores the best TTP score found over the runtime)
                    --donotsavef                                                                    (do not save <output_file> + ".bittp.f")
                    --donotsavex                                                                    (do not save <output_file> + ".bittp.x")
                    --donotsavettpsol                                                               (do not save <output_file> + ".ttp.sol")
                    --donotsavettplog                                                               (do not save <output_file> + ".ttp.log")
        
default values:
                    prob_distrib         0  (v1=0, v2=1)
                    eta_value                         10
                    rho_value                          1
                    gamma_value                       10
                    beta_value                      -INF     (no 2-opt moves)
                    lambda_value                       0     (no bit-flip moves)
                    runtime_in_seconds               600
                    random_seed                 11235813

```

We provide a python script (see "run_wsm_on_all_instances.py") for running our algorithm on all test problems.
