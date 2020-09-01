"""
This script is an adaptation of the script used to evaluate the BITTP competition at GECCO2019.

GECCO2019 competition info: https://www.egr.msu.edu/coinlab/blankjul/gecco19-thief
Original script: https://github.com/julesy89/gecco19-thief

"""

from non_dominated_sorting import fast_non_dominated_sort
import os.path
import numpy as np
import matplotlib.pyplot as plt
from hv import Hypervolume
from normalization import normalize
import pandas as pd

# the result folder as a path
folder = os.getcwd()

# all submissions received
participants = ["ALLAOUI", 
                #"jomar", 
                "NDS-BRKGA", 
                "shisunzhang", 
                "faria", 
                "HPI", 
                "NTGA", 
                "SSteam", 
                "SamirO-ETF-ba", 
                "FRA", 
                "sinc", 
                "JG",                
                #"wsm-00600", 
                #"wsm-01200", 
                #"wsm-01800", 
                #"wsm-03600", 
                #"wsm-07200", 
                "wsm-10800",                    
                #"wsm-18000",               
               ]

# all the problems to be solved
problems = ["a280_n279", "a280_n1395", "a280_n2790", "fnl4461_n4460", "fnl4461_n22300", "fnl4461_n44600", "pla33810_n33809", "pla33810_n169045", "pla33810_n338090"]

data = {}
for problem in problems:
    _entry = {}
    for participant in participants:        
        # check for the corresponding file
        fname = "%s_%s.f" % (participant, problem)   
        path_to_file = os.path.join(folder,participant, fname)        
        # in case the wrong delimiter was used
        if not os.path.isfile(path_to_file):
            fname = "%s_%s.f" % (participant, problem.replace("_", "-"))
            path_to_file = os.path.join(folder,participant, fname)         
        # load the values in the objective space - first column is time, second profit
        _F = np.loadtxt(path_to_file)        
        # modify it to a min-min problem by multiplying the profit by -1
        _entry[participant] = _F * [1, -1]        
    data[problem] = _entry

'''
import matplotlib.cm as cm
cmap = cm.get_cmap('tab20')

for problem in problems:    
    for k, participant in enumerate(participants):
        _F = data[problem][participant]
        plt.scatter(_F[:,0], _F[:,1], label=participant, s=10, facecolors='none', edgecolors=cmap(k))
    _all = np.row_stack([data[problem][participant] for participant in participants])
    I = fast_non_dominated_sort(_all)[0]
    _non_dom = _all[I]    
    _min = _non_dom.min(axis=0)
    _max = _non_dom.max(axis=0)
    _range = _max - _min        
    print("=" * 60)
    print(problem)
    print("=" * 60)
    plt.xlabel("time")
    plt.ylabel("negative profit")
    plt.xlim(_min[0] - 0.05 * _range[0], _max[0] + 0.05 * _range[0])
    plt.ylim(_min[1] - 0.05 * _range[1], _max[1] + 0.05 * _range[1])
    plt.legend()
    plt.show()
'''        

ideal_point = {}
nadir_point = {}
ndf = {}
for problem in problems:    
    # the merged non-dominated solutions for the specific problem
    M = []
    for participant in participants:    
        _F = data[problem][participant]
        M.append(_F)        
    M = np.vstack(M)    
    I = fast_non_dominated_sort(M)[0]
    M = M[I, :]    
    ideal_point[problem] = np.min(M, axis=0)
    nadir_point[problem] = np.max(M, axis=0)
    ndf[problem] = M
    
results = []
for problem in problems:    
    z = ideal_point[problem]
    z_nad = nadir_point[problem] 
    for participant in participants:    
        _F = data[problem][participant]
        _N = normalize(_F, z, z_nad)
        _hv = Hypervolume(np.array([1,1])).calc(_N)
        results.append({'problem' : problem, 'participant' : participant, 'hv' : _hv})
        
df = pd.DataFrame(results, columns=["problem", "participant", "hv"])

for problem in problems:
    print("=" * 60)
    print(problem)
    print("=" * 60)    
    _df = df[df["problem"] == problem].copy()
    _df.sort_values("hv", ascending=False, inplace=True)
    _df.reset_index(drop=True, inplace=True)
    print(_df)

# the final ranking. And add zero points initially (sum is later taken anyway...)
ranking = []
for participant in participants:
    ranking.append({'participant': participant, 'points' : 0})

# one more time loop through problem wise
for problem in problems:
    _df = df[df["problem"] == problem].copy()
    # sort descending by hv
    _df.sort_values("hv", ascending=False, inplace=True)
    # 3 points for the 1st place
    first = _df.iloc[0]["participant"]
    ranking.append({'participant': first, 'points' : 3})
    # 2 points for the 2nd place
    second = _df.iloc[1]["participant"]
    ranking.append({'participant': second, 'points' : 2})
    # 1 point for the 3rd place
    third = _df.iloc[2]["participant"]
    ranking.append({'participant': third, 'points' : 1})
    
ranking = pd.DataFrame(ranking, columns=["participant", "points"])
print("\n")
print(ranking.groupby('participant').sum().sort_values("points", ascending=False))
