import numpy as np
import os
import math
import multiprocessing

random_seeds = [ 269070,  99470, 126489, 644764, 547617, 642580,  73456, 462018, 858990, 756112, 
                 701531, 342080, 613485, 131654, 886148, 909040, 146518, 782904,   3075, 974703, 
                 170425, 531298, 253045, 488197, 394197, 519912, 606939, 480271, 117561, 900952, 
                 968235, 345118, 750253, 420440, 761205, 130467, 928803, 768798, 640300, 871462, 
                 639622,  90614, 187822, 594363, 193911, 846042, 680779, 344008, 759862, 661168, 
                 223420, 959508,  62985, 349296, 910428, 964420, 422964, 384194, 985214,  57575, 
                 639619,  90505, 435236, 465842, 102567, 189997, 741017, 611828, 699223, 335142, 
                  52119,  49256, 324523, 348215, 651525, 517999, 830566, 958538, 880422, 390645, 
                 148265, 807740, 934464, 524847, 408760, 668587, 257030, 751580,  90477, 594476, 
                 571216, 306614, 308010, 661191, 890429, 425031,  69108, 435783,  17725, 335928, ]

configs = {
("eil51", 1, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 93, "--rho": 9, "--gamma": 59, "--beta": 10, "--lambda": 0.35},
("eil51", 1, "uncorr-similar-weights") : {"--distrib": "1 0.5 0.2", "--eta": 51, "--rho": 2, "--gamma": 142, "--beta": 1, "--lambda": 0.32},
("eil51", 1, "uncorr") : {"--distrib": "0 0 1", "--eta": 127, "--rho": 1, "--gamma": 160, "--beta": 1, "--lambda": 0.38},
("eil51", 3, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 49, "--rho": 10, "--gamma": 23, "--beta": 1, "--lambda": 0.35},
("eil51", 3, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 87, "--rho": 3, "--gamma": 37, "--beta": 1, "--lambda": 0.48},
("eil51", 3, "uncorr") : {"--distrib": "0 0 1", "--eta": 90, "--rho": 3, "--gamma": 140, "--beta": 1, "--lambda": 0.48},
("eil51", 5, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 7, "--rho": 87, "--gamma": 76, "--beta": 10, "--lambda": 0.4},
("eil51", 5, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 2, "--rho": 24, "--gamma": 19, "--beta": 10, "--lambda": 0.47},
("eil51", 5, "uncorr") : {"--distrib": "0 0 1", "--eta": 71, "--rho": 3, "--gamma": 125, "--beta": 1, "--lambda": 0.5},
("eil51", 10, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 26, "--rho": 39, "--gamma": 27, "--beta": 10, "--lambda": 0.39},
("eil51", 10, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 37, "--rho": 5, "--gamma": 47, "--beta": 1, "--lambda": 0.43},
("eil51", 10, "uncorr") : {"--distrib": "0 0 1", "--eta": 4, "--rho": 44, "--gamma": 23, "--beta": 100, "--lambda": 0.44},
("pr152", 1, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 84, "--rho": 47, "--gamma": 15, "--beta": 100, "--lambda": 0.44},
("pr152", 1, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 105, "--rho": 1, "--gamma": 148, "--beta": 1, "--lambda": 0.43},
("pr152", 1, "uncorr") : {"--distrib": "0 0 1", "--eta": 71, "--rho": 8, "--gamma": 5, "--beta": 1, "--lambda": 0.34},
("pr152", 3, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 70, "--rho": 26, "--gamma": 17, "--beta": 100, "--lambda": 0.42},
("pr152", 3, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 43, "--rho": 4, "--gamma": 33, "--beta": 0.00001, "--lambda": 0.49},
("pr152", 3, "uncorr") : {"--distrib": "0 0 1", "--eta": 67, "--rho": 12, "--gamma": 32, "--beta": 0, "--lambda": 0.47},
("pr152", 5, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 146, "--rho": 12, "--gamma": 29, "--beta": 0.000001, "--lambda": 0.25},
("pr152", 5, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 40, "--rho": 2, "--gamma": 87, "--beta": 0.01, "--lambda": 0.43},
("pr152", 5, "uncorr") : {"--distrib": "0 0 1", "--eta": 44, "--rho": 5, "--gamma": 81, "--beta": 0.00001, "--lambda": 0.37},
("pr152", 10, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 38, "--rho": 20, "--gamma": 19, "--beta": 1, "--lambda": 0.18},
("pr152", 10, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 60, "--rho": 3, "--gamma": 164, "--beta": 0.1, "--lambda": 0.39},
("pr152", 10, "uncorr") : {"--distrib": "0 0 1", "--eta": 36, "--rho": 19, "--gamma": 18, "--beta": 0.01, "--lambda": 0.25},
("a280", 1, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 75, "--rho": 20, "--gamma": 2, "--beta": 0.1, "--lambda": 0.25},
("a280", 1, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 14, "--rho": 40, "--gamma": 15, "--beta": 10, "--lambda": 0.45},
("a280", 1, "uncorr") : {"--distrib": "0 0 1", "--eta": 26, "--rho": 33, "--gamma": 50, "--beta": 1, "--lambda": 0.49},
("a280", 3, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 75, "--rho": 8, "--gamma": 6, "--beta": 1, "--lambda": 0.28},
("a280", 3, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 49, "--rho": 2, "--gamma": 32, "--beta": 0.001, "--lambda": 0.29},
("a280", 3, "uncorr") : {"--distrib": "0 0 1", "--eta": 6, "--rho": 26, "--gamma": 18, "--beta": 0.1, "--lambda": 0.36},
("a280", 5, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 25, "--rho": 20, "--gamma": 3, "--beta": 1, "--lambda": 0.31},
("a280", 5, "uncorr-similar-weights") : {"--distrib": "1 0.5 0.2", "--eta": 62, "--rho": 3, "--gamma": 6, "--beta": 0.001, "--lambda": 0.23},
("a280", 5, "uncorr") : {"--distrib": "0 0 1", "--eta": 19, "--rho": 6, "--gamma": 6, "--beta": 0.1, "--lambda": 0.2},
("a280", 10, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 81, "--rho": 9, "--gamma": 21, "--beta": 0.0001, "--lambda": 0.25},
("a280", 10, "uncorr-similar-weights") : {"--distrib": "1 0.5 0.2", "--eta": 123, "--rho": 1, "--gamma": 38, "--beta": 0.01, "--lambda": 0.07},
("a280", 10, "uncorr") : {"--distrib": "0 0 1", "--eta": 43, "--rho": 3, "--gamma": 7, "--beta": 0.001, "--lambda": 0.08},
("dsj1000", 1, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 165, "--rho": 9, "--gamma": 8, "--beta": 0.00001, "--lambda": 0.27},
("dsj1000", 1, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 94, "--rho": 5, "--gamma": 16, "--beta": -987654321, "--lambda": 0.29},
("dsj1000", 1, "uncorr") : {"--distrib": "0 0 1", "--eta": 132, "--rho": 24, "--gamma": 7, "--beta": 0.000001, "--lambda": 0.04},
("dsj1000", 3, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 136, "--rho": 4, "--gamma": 29, "--beta": 0.01, "--lambda": 0.18},
("dsj1000", 3, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 137, "--rho": 9, "--gamma": 9, "--beta": 0.000001, "--lambda": 0.42},
("dsj1000", 3, "uncorr") : {"--distrib": "0 0 1", "--eta": 183, "--rho": 21, "--gamma": 9, "--beta": -987654321, "--lambda": 0.31},
("dsj1000", 5, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 197, "--rho": 10, "--gamma": 17, "--beta": 0.001, "--lambda": 0.21},
("dsj1000", 5, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 168, "--rho": 10, "--gamma": 56, "--beta": 0.01, "--lambda": 0.1},
("dsj1000", 5, "uncorr") : {"--distrib": "0 0 1", "--eta": 166, "--rho": 4, "--gamma": 35, "--beta": 0.000001, "--lambda": 0.05},
("dsj1000", 10, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 156, "--rho": 2, "--gamma": 50, "--beta": 0.00001, "--lambda": 0.16},
("dsj1000", 10, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 159, "--rho": 2, "--gamma": 26, "--beta": 0.000001, "--lambda": 0.1},
("dsj1000", 10, "uncorr") : {"--distrib": "0 0 1", "--eta": 97, "--rho": 4, "--gamma": 8, "--beta": 0, "--lambda": 0.22},
("fnl4461", 1, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 77, "--rho": 32, "--gamma": 7, "--beta": -987654321, "--lambda": 0.14},
("fnl4461", 1, "uncorr-similar-weights") : {"--distrib": "1 0.5 0.2", "--eta": 187, "--rho": 6, "--gamma": 9, "--beta": 0.001, "--lambda": 0.04},
("fnl4461", 1, "uncorr") : {"--distrib": "0 0 1", "--eta": 86, "--rho": 21, "--gamma": 16, "--beta": 0, "--lambda": 0.06},
("fnl4461", 3, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 200, "--rho": 3, "--gamma": 28, "--beta": 0.001, "--lambda": 0.33},
("fnl4461", 3, "uncorr-similar-weights") : {"--distrib": "1 0.5 0.2", "--eta": 158, "--rho": 6, "--gamma": 43, "--beta": 0.000001, "--lambda": 0.09},
("fnl4461", 3, "uncorr") : {"--distrib": "0 0 1", "--eta": 135, "--rho": 10, "--gamma": 59, "--beta": 0.00001, "--lambda": 0.35},
("fnl4461", 5, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 195, "--rho": 2, "--gamma": 48, "--beta": 0.01, "--lambda": 0.2},
("fnl4461", 5, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 194, "--rho": 5, "--gamma": 67, "--beta": 0.001, "--lambda": 0.1},
("fnl4461", 5, "uncorr") : {"--distrib": "0 0 1", "--eta": 162, "--rho": 8, "--gamma": 41, "--beta": -987654321, "--lambda": 0.33},
("fnl4461", 10, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 143, "--rho": 3, "--gamma": 4, "--beta": 0, "--lambda": 0.02},
("fnl4461", 10, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 164, "--rho": 6, "--gamma": 41, "--beta": 0.01, "--lambda": 0.17},
("fnl4461", 10, "uncorr") : {"--distrib": "0 0 1", "--eta": 80, "--rho": 8, "--gamma": 23, "--beta": 0.000001, "--lambda": 0.01},
("usa13509", 1, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 200, "--rho": 8, "--gamma": 17, "--beta": -987654321, "--lambda": 0.04},
("usa13509", 1, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 157, "--rho": 6, "--gamma": 8, "--beta": 0.00001, "--lambda": 0.48},
("usa13509", 1, "uncorr") : {"--distrib": "0 0 1", "--eta": 172, "--rho": 14, "--gamma": 7, "--beta": 0.00001, "--lambda": 0.3},
("usa13509", 3, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 144, "--rho": 4, "--gamma": 71, "--beta": 0.000001, "--lambda": 0.05},
("usa13509", 3, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 63, "--rho": 5, "--gamma": 36, "--beta": -987654321, "--lambda": 0.21},
("usa13509", 3, "uncorr") : {"--distrib": "0 0 1", "--eta": 76, "--rho": 18, "--gamma": 14, "--beta": 0.001, "--lambda": 0.29},
("usa13509", 5, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 156, "--rho": 3, "--gamma": 124, "--beta": 0, "--lambda": 0.26},
("usa13509", 5, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 184, "--rho": 4, "--gamma": 77, "--beta": 0.001, "--lambda": 0.03},
("usa13509", 5, "uncorr") : {"--distrib": "0 0 1", "--eta": 55, "--rho": 11, "--gamma": 53, "--beta": 0.001, "--lambda": 0.22},
("usa13509", 10, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 147, "--rho": 2, "--gamma": 41, "--beta": 0.00001, "--lambda": 0.01},
("usa13509", 10, "uncorr-similar-weights") : {"--distrib": "2 1.5 3", "--eta": 97, "--rho": 2, "--gamma": 141, "--beta": -987654321, "--lambda": 0.06},
("usa13509", 10, "uncorr") : {"--distrib": "0 0 1", "--eta": 113, "--rho": 7, "--gamma": 17, "--beta": 0, "--lambda": 0.25},
("pla33810", 1, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 136, "--rho": 17, "--gamma": 16, "--beta": 0.0001, "--lambda": 0.13},
("pla33810", 1, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 169, "--rho": 3, "--gamma": 68, "--beta": -987654321, "--lambda": 0.3},
("pla33810", 1, "uncorr") : {"--distrib": "0 0 1", "--eta": 186, "--rho": 25, "--gamma": 12, "--beta": 0.0001, "--lambda": 0.18},
("pla33810", 3, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 200, "--rho": 19, "--gamma": 39, "--beta": 0.01, "--lambda": 0.22},
("pla33810", 3, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 193, "--rho": 9, "--gamma": 97, "--beta": 0.01, "--lambda": 0.01},
("pla33810", 3, "uncorr") : {"--distrib": "0 0 1", "--eta": 196, "--rho": 30, "--gamma": 7, "--beta": -987654321, "--lambda": 0.11},
("pla33810", 5, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 176, "--rho": 4, "--gamma": 47, "--beta": 0.001, "--lambda": 0.26},
("pla33810", 5, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 186, "--rho": 4, "--gamma": 56, "--beta": 0.001, "--lambda": 0},
("pla33810", 5, "uncorr") : {"--distrib": "2 1.5 3", "--eta": 161, "--rho": 5, "--gamma": 3, "--beta": -987654321, "--lambda": 0.03},
("pla33810", 10, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 183, "--rho": 6, "--gamma": 27, "--beta": 100, "--lambda": 0.03},
("pla33810", 10, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 174, "--rho": 6, "--gamma": 50, "--beta": 100, "--lambda": 0.17},
("pla33810", 10, "uncorr") : {"--distrib": "0 0 1", "--eta": 166, "--rho": 5, "--gamma": 18, "--beta": 0.000001, "--lambda": 0.01},
("pla85900", 1, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 181, "--rho": 9, "--gamma": 6, "--beta": 0.01, "--lambda": 0.06},
("pla85900", 1, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 123, "--rho": 6, "--gamma": 65, "--beta": -987654321, "--lambda": 0.11},
("pla85900", 1, "uncorr") : {"--distrib": "0 0 1", "--eta": 162, "--rho": 19, "--gamma": 19, "--beta": 0.00001, "--lambda": 0.07},
("pla85900", 3, "bounded-strongly-corr") : {"--distrib": "0 0 1", "--eta": 168, "--rho": 5, "--gamma": 16, "--beta": 100, "--lambda": 0.07},
("pla85900", 3, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 186, "--rho": 8, "--gamma": 16, "--beta": 0.000001, "--lambda": 0.11},
("pla85900", 3, "uncorr") : {"--distrib": "0 0 1", "--eta": 176, "--rho": 6, "--gamma": 11, "--beta": 10, "--lambda": 0.02},
("pla85900", 5, "bounded-strongly-corr") : {"--distrib": "2 1.5 3", "--eta": 170, "--rho": 6, "--gamma": 34, "--beta": 0, "--lambda": 0.1},
("pla85900", 5, "uncorr-similar-weights") : {"--distrib": "2 1.5 3", "--eta": 171, "--rho": 3, "--gamma": 69, "--beta": 10, "--lambda": 0.24},
("pla85900", 5, "uncorr") : {"--distrib": "0 0 1", "--eta": 106, "--rho": 6, "--gamma": 16, "--beta": 10, "--lambda": 0.18},
("pla85900", 10, "bounded-strongly-corr") : {"--distrib": "2 1.5 3", "--eta": 179, "--rho": 1, "--gamma": 60, "--beta": 0.0001, "--lambda": 0.22},
("pla85900", 10, "uncorr-similar-weights") : {"--distrib": "0 0 1", "--eta": 182, "--rho": 2, "--gamma": 33, "--beta": 1, "--lambda": 0.03},
("pla85900", 10, "uncorr") : {"--distrib": "0 0 1", "--eta": 88, "--rho": 4, "--gamma": 7, "--beta": 0.001, "--lambda": 0.23},
}

def launcher(tsp_base, n_items_per_city, type_of_items, knapsack_size, run, time_limit):
    number_of_items = (int(''.join(filter(lambda x: x.isdigit(), tsp_base))) - 1) * int(n_items_per_city)
    instance = "instances/%s-ttp/%s_n%s_%s_%02d.ttp" % (tsp_base, tsp_base, number_of_items, type_of_items, knapsack_size)
    output = "%s_n%s_%s_%02d_%02d" % (tsp_base, number_of_items, type_of_items, knapsack_size, run)
    os.system("java -jar wsm.jar --inputfile %s %s --time %d --seed %d --outputfile %s --donotsavex" % 
                      (
                         instance,
                         ' '.join("%s %s" % (k, v) for k, v in configs[tsp_base, n_items_per_city, type_of_items].items()),
                         time_limit,
                         random_seeds[run - 1],
                         output
                      )
             )
    
if __name__ == "__main__":

    tsp_bases = ["eil51", "pr152", "a280", "dsj1000", "fnl4461", "usa13509", "pla33810", "pla85900", ]

    number_of_items_per_city = [1, 3, 5, 10, ]

    types_of_items = ["bounded-strongly-corr", "uncorr", "uncorr-similar-weights", ]

    knapsack_sizes = list(range(1, 11))

    runs = list(range(1, 31))

    time_limit = 600

    pool = multiprocessing.Pool(processes=max(1, multiprocessing.cpu_count() - 2))

    for tsp_base in tsp_bases:
        for n_items_per_city in number_of_items_per_city:            
            for type_of_items in types_of_items:
                for knapsack_size in knapsack_sizes:                    
                    for run in runs:
                        pool.apply_async(launcher, args=(tsp_base, n_items_per_city, type_of_items, knapsack_size, run, time_limit))
    
    pool.close()
    pool.join()
