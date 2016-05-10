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
    -i <f> --in=<f>     Specify the path to the csv file folder [default: ../data/csv/spawned_count_per_timestep.csv]
    -o <f> --out=<f>    Specify the output file (including path) [default: ../data/pdf/spawned_count_per_timestep.pdf]
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
    # plotting preparation
    for v in iterValues:
        x.append(int(v)) # timestep
        y.append(float(next(iterValues))) # counter avg

    title = '# spawned vehicles per timestep'
    xlabel = 'timestep'
    ylabel = '# spawned vehicles'

#    fig = plt.figure()
#    pylab.rc("axes", lw=3)
#    pylab.rc("lines", mew=3, lw=3)
#    pylab.rc("font", size=20, weight='bold')
    plt.xticks(np.arange(0, max(x)+1, max(500, 1000 * (int(max(x) / 5) / 1000) ) ))
    plt.yticks(np.arange(0, max(y), max(500, 1000 * (int(max(y) / 5) / 1000) ) ))
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













