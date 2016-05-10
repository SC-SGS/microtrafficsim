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
    age_when_despawning.py [options]
    age_when_despawning.py -h | --help
    age_when_despawning.py --version

Options:
    -h --help           Show this screen.
    -s --show           Shows the created plot after saving the picture.
    -i <f> --in=<f>     Specify the path to the csv file folder [default: ../data/csv/linear_distance_per_despawn_age.csv]
    -o <f> --out=<f>    Specify the output file (including path) [default: ../data/pdf/linear_distance_per_despawn_age.pdf]
"""



def visualize(input_filepath, output_filepath, show):
    f = open(input_filepath)
    values = f.read().split(';')
    f.close()
    title = ''
    xlabel = ''
    ylabel = ''
    x = []
    y = []
    
    iterValues = iter(values)
    totalDespawned = float(next(iterValues))
    # plotting preparation
    if totalDespawned > 0:
        for v in iterValues:
            x.append(float(v))
            y.append(100 * float(next(iterValues)))

        title = 'linear distance per age [m/s]\n(%i despawned)' % totalDespawned
        xlabel = 'm/s; all values rounded to one digit'
        ylabel = '# vehicles with \'avg speed\' <= x m/s (in %)'

#    fig = plt.figure()
#    pylab.rc("axes", lw=3)
#    pylab.rc("lines", mew=3, lw=3)
#    pylab.rc("font", size=20, weight='bold')
    plt.xticks(np.arange(min(x), max(x)+1, 2))
    plt.yticks(np.arange(0, 101, 5))
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    plt.plot(x, y, 'x-')
    plt.legend()
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













