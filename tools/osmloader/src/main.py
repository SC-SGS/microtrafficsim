#!/usr/bin/env python
'''
OpenStreetMap XML downloader for multiple (hardcoded) files, using the
overpass API.
'''
__author__ = 'Maximilian Luz, Dominic Parga Cacheiro'

import argparse
import os
import urllib3


API = 'http://overpass-api.de/api/map?bbox={l},{b},{r},{t}'
# new predefined maps can be added here
# in ALPHABETICAL ORDER for better overview
PREDEFINED = {
    'backnang':            (   9.3767, 48.9132,    9.4905, 48.9753),
    'bangkok':             ( 100.3924, 13.5946,  100.7240, 14.1958),
    'bangkok-small':       ( 100.4010, 13.6133,  100.6476, 13.8599),
    'barcelona':           (   1.9470, 41.2608,    2.3332, 41.5122),
    'barcelona-small':     (   2.0328, 41.3369,    2.2570, 41.4610),
    'bejing':              ( 116.0307, 39.6702,  116.7339, 40.1862),
    'bejing-small':        ( 116.1893, 39.7526,  116.5509, 40.0252),
    'boston':              ( -71.3445, 42.1827,  -70.5727, 42.6951),
    'boston-small':        ( -71.2759, 42.1934,  -70.8227, 42.5450),
    'karlsruhe':           (   8.2356, 48.9164,    8.5408, 49.1195),
    'london':              (  -0.5679, 51.2447,    0.3117, 51.7275),
    'london-small':        (  -0.3433, 51.3400,    0.1428, 51.6159),
    'manhatten':           ( -74.0568, 40.6999,  -73.8068, 40.9008),
    'new-york':            ( -74.3273, 40.5075,  -73.4361, 40.8990),
    'new-york-small':      ( -74.1474, 40.5610,  -73.7238, 40.9348),
    'osaka':               ( 135.0728, 34.1442,  135.8727, 35.0851),
    'osaka-small':         ( 135.0742, 34.3054,  135.6592, 34.8775),
    'paris-small':         (   2.2058, 48.7863,    2.4809, 48.9209),
    'rome':                (  12.1632, 41.6385,   12.7091, 42.0015),
    'rome-small':          (  12.3610, 41.7739,   12.6381, 41.9990),
    'sacramento':          (-121.6022, 37.8686, -121.0374, 38.9169),
    'san-francisco':       (-122.5278, 37.2106, -121.6393, 38.0578),
    'san-francisco-small': (-122.5168, 37.2183, -121.7889, 37.9052),
    'stuttgart':           (   8.9679, 48.6622,    9.3363, 48.9135),
    'stuttgart-small':     (   9.0565, 48.7027,    9.2876, 48.8561),
    'tokyo':               ( 139.3341, 35.3689,  140.1622, 35.9108),
    'tokyo-small':         ( 139.5916, 35.5210,  140.1601, 35.8579),
}


class Region:
    '''
    Using this wrapper class allows to set the coordinates in an order
    (left, bottom, right, top) independent of the coordinates' access.
    '''

    def __init__(self, name='map', left=0.0, bottom=0.0, right=0.0, top=0.0):
        '''
        PARAM left,bottom,right,top : float\n
        Default is 0.0
        '''
        self._name = name
        self._left = left
        self._bottom = bottom
        self._right = right
        self._top = top

    def __str__(self):
        return '({n}, {l}, {b}, {r}, {t})'.format(
            n=self.name,
            l=self.left,
            b=self.bottom,
            r=self.right,
            t=self.top
        )

    @property
    def name(self):
        return self._name

    @name.setter
    def name(self, name):
        self._name = name

    @property
    def left(self):
        return self._left

    @left.setter
    def left(self, left):
        self._left = left

    @property
    def bottom(self):
        return self._bottom

    @bottom.setter
    def bottom(self, bottom):
        self._bottom = bottom

    @property
    def right(self):
        return self._right

    @right.setter
    def right(self, right):
        self._right = right

    @property
    def top(self):
        return self._top

    @top.setter
    def top(self, top):
        self._top = top


def download_and_save_region(map_path, region):
    '''
    PARAM map_path : str\n
    Absolute path (e.g. starting with /) or relative to the file's location\n
    \n
    PARAM region : Region\n
    The wrapper class in this module for unified parameter access\n
    '''

    # construct url
    url = API.format(
        l=region.left,
        b=region.bottom,
        r=region.right,
        t=region.top
    )

    # open file and connection
    http = urllib3.PoolManager()
    filename = os.path.abspath(os.path.join(map_path, region.name + '.osm'))
    has_failed = False

    try:
        print(
            'downloading \'{1}\' from \'{0}\''.format(url, region.name),
            flush=True
        )
        with open(filename, 'wb') as target:
            # streaming is better, see https://bugs.python.org/issue24658
            remote = http.request('GET', url, preload_content=False)
            for chunk in remote.stream():
                target.write(chunk)
    except Exception:
        has_failed = True

    # close connection
    remote.release_conn()

    # handle error
    if not has_failed:
        print("download finished successfully!", flush=True)
    else:
        os.remove(filename)
        print('Error occurred', flush=True)


def parse_cmdline():
    # cmdline parsing
    parser = argparse.ArgumentParser(description='OpenStreetMap XML downloader')


    help = 'List of predefined maps ('
    if len(PREDEFINED) > 0:
        help += ', '.join([map_name for map_name in PREDEFINED.keys()])
    else:
        help += '<no maps defined>'
    help += ")"
    parser.add_argument('-p', '--predefined',
        metavar=('MAP_NAME'),
        type=str,
        nargs='+',
        default=[],
        help=help
    )

    help = 'Downloads all predefined maps ('
    if len(PREDEFINED) > 0:
        help += ', '.join([map_name for map_name in PREDEFINED.keys()])
    else:
        help += '<no maps defined>'
    help += ")"
    parser.add_argument('--all-predefined',
        action='store_true',
        help=help
    )


    help = 'Adds a map name related to the custom bounding box'
    parser.add_argument('-m', '--maps',
        metavar=('MAP_NAME'),
        type=str,
        action='append',
        default=[],
        help=help
    )


    help = 'Adds a custom bounding box'
    parser.add_argument('-b', '--bounds',
        metavar=('LEFT', 'BOTTOM', 'RIGHT', 'TOP'),
        type=float,
        nargs=4,
        action='append',
        default=[],
        help=help
    )


    help = 'Path to map folder. '
    help += 'Relative paths are relative to this file\'s location. '
    help += 'Absolute paths (e.g. starting with /) are working as well.'
    parser.add_argument('-o', '--out',
        metavar=('PATH'),
        type=str,
        default='.',
        help=help
    )


    # finished preparing parser
    args = parser.parse_args()


    # prepare download files
    regions = []


    # download all?
    if args.all_predefined:
        for map_name, bounds in PREDEFINED.items():
            regions.append(Region(
                map_name,
                bounds[0],
                bounds[1],
                bounds[2],
                bounds[3],
            ))
    else:
        # remember predefined maps and their related coordinates
        for map_name in args.predefined:
            try:
                bounds = PREDEFINED[map_name]
                regions.append(Region(
                    map_name,
                    bounds[0],
                    bounds[1],
                    bounds[2],
                    bounds[3],
                ))
            except KeyError:
                err_msg = '\'' + map_name + '\''
                err_msg += ' is expected to be predefined, but isn\'t.'
                exit(err_msg)

    # remember custom maps and their related coordinates
    if len(args.maps) > len(args.bounds):
        err_msg = 'There are more custom map names than bounds.'
        exit(err_msg)
    if len(args.maps) < len(args.bounds):
        err_msg = 'There are more bounds than custom map names.'
        exit(err_msg)

    for map_name, bounds in zip(args.maps, args.bounds):
        region = Region(
            map_name,
            bounds[0],
            bounds[1],
            bounds[2],
            bounds[3],
        )
        regions.append(region)

    return args.out, regions


def main():
    map_path, regions = parse_cmdline()
    map_path = os.path.abspath(map_path)

    if not os.path.isdir(map_path):
        raise ValueError('Wrong map path')
    if not regions:
        exit()

    print('downloading to \'{0}\''.format(map_path))
    print()
    for region in regions:
        download_and_save_region(map_path, region)
        print()


if __name__ == '__main__':
    main()
