import numpy as np
import matplotlib.pyplot as plt
from docopt import docopt

# import urllib2
# import os

"""
OpenStreetMap data visualization                  (not tested with python3 yet)

Requires: urllib2, docopt
"""

usage = """OpenStreetMap data visualization.

Usage:
    anger_per_timestep.py [options]
    anger_per_timestep.py -h | --help
    anger_per_timestep.py --version

Options:
    -h --help           Show this screen.
    -s --show           Shows the created plot after saving the picture.
    -i <f> --in=<f>     Specify the path to the csv file folder [default: ../data/csv/anger_per_timestep.csv]
    -o <f> --out=<f>    Specify the output file (including path) [default: ../data/pdf/anger_per_timestep.pdf]
"""



def boxfake(list):
    list.sort()
    n = len(list)
    if n > 0:
        return list[0], list[n / 4], list[n/2], list[n * 3/4], list[n-1]
    else:
        return -1, -1, -1, -1, -1


def visualize(input_filepath, output_filepath, show):
    f = open(input_filepath)
    values = iter(f.read().split(';'))
    f.close()
    
    x = []
    y_mean = []
    y_min = []
    y_lowQuartile = []
    y_median = []
    y_highQuartile = []
    y_max = []
    
    iterValues = iter(values)
    for v in iterValues:
        x.append(v)
        y_mean.append(float(next(iterValues)))
        y_min.append(int(next(iterValues)))
        y_lowQuartile.append(int(next(iterValues)))
        y_median.append(int(next(iterValues)))
        y_highQuartile.append(int(next(iterValues)))
        y_max.append(int(next(iterValues)))

    title = 'anger per timestep'
    xlabel = 'timestep'
    ylabel = 'anger'

#    fig = plt.figure()
#    pylab.rc("axes", lw=3)
#    pylab.rc("lines", mew=3, lw=3)
#    pylab.rc("font", size=20, weight='bold')
    plt.yticks(np.arange(0, max(y_max), 200))
    plt.xlabel(xlabel, fontsize=18)
    plt.ylabel(ylabel, fontsize=18)
    plt.plot(x, y_min, 'x--b', linewidth=2, markersize=10, markeredgewidth=2, label='min, max')
    plt.plot(x, y_lowQuartile, '^-k', linewidth=2, markersize=7, markeredgewidth=2, label='25%, 75%\nquantile')
    plt.plot(x, y_median, 'o-b', linewidth=2, markersize=7, markeredgewidth=2, label='median')
    plt.plot(x, y_highQuartile, '^-k', linewidth=2, markersize=7, markeredgewidth=2)
    plt.plot(x, y_max, 'x--b', linewidth=2, markersize=10, markeredgewidth=2)
    plt.plot(x, y_mean, 'x-r', linewidth=2, markersize=10, markeredgewidth=2, label='average')
    plt.legend(loc="upper left")
    plt.grid()
    plt.yticks(fontsize='x-large')
    plt.xticks(fontsize='x-large')
    plt.title(title, fontsize='x-large')
    plt.tight_layout()
    plt.savefig(output_filepath)
    if show:
        plt.show()

if __name__ == '__main__':
    args = docopt(usage, version='OSM data visualization 1.0')

    input_filepath = args['--in']
    legalInputEndings = [
        'csv'
    ]

    output_filepath = args['--out']
    legalOutputEndings = [
        'png',
        'jpg',
        'jpeg',
        'JPEG',
        'pdf',
        'PDF'
    ]
    
    okayOut = False
    if any(output_filepath.split('.')[-1] == ending for ending in legalOutputEndings):
        okayOut = True
    else:
        print 'Illegal output file ending. Accepted:', legalOutputEndings

    okayIn = False
    if any(input_filepath.split('.')[-1] == ending for ending in legalInputEndings):
        okayIn = True
    else:
        print 'Illegal input file ending. Accepted:', legalInputEndings

    if okayOut & okayIn:
        visualize(input_filepath, output_filepath, args['--show'])













