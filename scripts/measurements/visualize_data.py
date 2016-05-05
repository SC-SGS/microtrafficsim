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
    visualize_data.py [options]
    visualize_data.py (anger | total_anger | spawned_count | t_despawn | t_lifetime) [options]
    visualize_data.py -h | --help
    visualize_data.py --version

Options:
    -h --help           Show this screen.
    -s --show           Shows the created plot after saving the picture.
    -i <f> --in=<f>     Specify the input file (including path) TODO tut noch nicht; einfach in Ordner kopieren
    -o <f> --out=<f>    Specify the output file (including path) [default: data.pdf]
"""


class VisTypeObj:
    type = -1
    csv_filename = ''

    def __init__(self, type, filename):
        self.type = type
        self.csv_filename = filename

    def __eq__(self, other):
        return self.type == other.type


class VisType:
    def __init__(self):
        pass

    anger = VisTypeObj(0, 'anger_per_timestep.csv')
    total_anger = VisTypeObj(1, 'total_anger_when_despawning.csv')
    spawned_count = VisTypeObj(2, 'spawned_count_per_timestep.csv')
    t_despawn = VisTypeObj(3, 't_spawn_despawn_depending_on_vehicleID.csv')
    t_lifetime = VisTypeObj(4, 't_spawn_despawn_depending_on_vehicleID.csv')


def visualize(vistype, output_filename, show):
    f = open(vistype.csv_filename)
    values = f.read().split(';')
    f.close()
    title = ''
    xlabel = ''
    ylabel = ''
    x = []
    y = []
    
    # # # # # # # # # # # # # # # # # #
    # # # # # # # # # # # # # # # # # #
    if vistype == VisType.anger:
        iterValues = iter(values)
        # 2 loops, but in O(n)
        for n in iterValues:
            n = int(n)
            if n > 0:
                avg = 0
                for _ in range(n):
                    avg += next(iterValues)
                y.append(float(avg) / n)
            else:
                y.append(0)
        x = range(len(y))
        title = 'avg anger per timestep'
        xlabel = 'timestep'
        ylabel = 'avg anger'
    # # # # # # # # # # # # # # # # # #
    # # # # # # # # # # # # # # # # # #
    elif vistype == VisType.total_anger:
        dict = {}
        totalDespawned = 0
        for v in values:
            v = int(v)
            totalDespawned += 1
            if v not in dict:
                dict[v] = 1
            else:
                dict[v] += 1
        # plotting preparation
        if totalDespawned > 0:
            x = range(1 + max(dict.keys()))
            for xx in x:
                if xx in dict:
                    y.append(dict[xx] / float(totalDespawned))
                else:
                    y.append(0)
        title = 'total anger when despawning (%i despawned)' % totalDespawned
        xlabel = 'total anger when despawning'
        ylabel = '#vehicles per #despawned_vehicles'
    # # # # # # # # # # # # # # # # # #
    # # # # # # # # # # # # # # # # # #
    elif vistype == VisType.spawned_count:
        # 2 loops, but in O(n)
        for count in values:
            y.append(count)
        x = range(len(y))
        title = '# spawned vehicles per timestep'
        xlabel = 'timestep'
        ylabel = '# spawned vehicles'
    # # # # # # # # # # # # # # # # # #
    # # # # # # # # # # # # # # # # # #
    elif vistype == VisType.t_despawn:
        dict = {}
        totalDespawned = 0
        toggle = False
        for v in values:
            if toggle:
                v = int(v)
                totalDespawned += 1
                if v not in dict:
                    dict[v] = 1
                else:
                    dict[v] += 1
            toggle = not toggle
        # plotting preparation
        if totalDespawned > 0:
            x = range(1 + max(dict.keys()))
            for xx in x:
                if xx in dict:
                    y.append(dict[xx] / float(totalDespawned))
                else:
                    y.append(0)
        title = '# despawning vehicles per timestep (%i despawned)' % totalDespawned
        xlabel = 'timestep'
        ylabel = '# despawning vehicles'
    # # # # # # # # # # # # # # # # # #
    # # # # # # # # # # # # # # # # # #
    elif vistype == VisType.t_lifetime:
        dict = {}
        totalDespawned = 0
        iterValues = iter(values)
        # t_spawn = iter(values[:, 0])
        # t_despawn = iter(values[:, 1])
        for s in iterValues:
            v = int(next(iterValues)) - int(s)
            totalDespawned += 1
            if v not in dict:
                dict[v] = 1
            else:
                dict[v] += 1
        # plotting preparation
        if totalDespawned > 0:
            x = range(1 + max(dict.keys()))
            for xx in x:
                if xx in dict:
                    y.append(dict[xx] / float(totalDespawned))
                else:
                    y.append(0)
        title = '# vehicles per lifetime (%i despawned)' % totalDespawned
        xlabel = 'vehicle\'s lifetime'
        ylabel = '# vehicles'
    # # # # # # # # # # # # # # # # # #
    # # # # # # # # # # # # # # # # # #

    # fig = plt.figure()
    # pylab.rc("axes", lw=3)
    # pylab.rc("lines", mew=3, lw=3)
    # pylab.rc("font", size=20, weight='bold')
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    plt.plot(x, y, 'x')
    plt.legend()
    plt.grid()
    plt.yticks(fontsize='x-large')
    plt.xticks(fontsize='x-large')
    plt.title(title, fontsize='x-large')
    plt.tight_layout()
    plt.savefig(output_filename)
    if show:
        plt.show()

if __name__ == '__main__':
    args = docopt(usage, version='OSM data visualization 1.0')

    i = 0
    vistype = VisType.anger
    if args['anger']:
        i += 1
        vistype = VisType.anger
    elif args['total_anger']:
        i += 1
        vistype = VisType.total_anger
    elif args['spawned_count']:
        i += 1
        vistype = VisType.spawned_count
    elif args['t_despawn']:
        i += 1
        vistype = VisType.t_despawn
    elif args['t_lifetime']:
        i += 1
        vistype = VisType.t_lifetime

    if i > 1:
        print('Error: Only one argument allowed.')
    elif i == 1:
        filename = args['--out']
        legalDataEndings = [
            'png',
            'jpg',
            'jpeg',
            'JPEG',
            'pdf',
            'PDF'
        ]
        if any(filename.split('.')[-1] == ending for ending in legalDataEndings):
            visualize(vistype, filename, args['--show'])
        else:
            print 'Illegal data ending. Accepted:', legalDataEndings













