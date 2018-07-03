#!/usr/bin/env python
'''
OpenStreetMap XML downloader for multiple (hardcoded) files, using the
overpass API.

Requires Python 3 and the urllib3 package.
'''

import argparse
import urllib3


API = 'http://overpass-api.de/api'
PREDEFINED = {
    'stuttgart':           (   8.9679, 48.6622,    9.3363, 48.9135),
    'stuttgart-small':     (   9.0565, 48.7027,    9.2876, 48.8561),
    'karlsruhe':           (   8.2356, 48.9164,    8.5408, 49.1195),
    'london':              (  -0.5679, 51.2447,    0.3117, 51.7275),
    'london-small':        (  -0.3433, 51.3400,    0.1428, 51.6159),
    'paris-small':         (   2.2058, 48.7863,    2.4809, 48.9209),
    'barcelona':           (   1.9470, 41.2608,    2.3332, 41.5122),
    'barcelona-small':     (   2.0328, 41.3369,    2.2570, 41.4610),
    'rome':                (  12.1632, 41.6385,   12.7091, 42.0015),
    'rome-small':          (  12.3610, 41.7739,   12.6381, 41.9990),
    'new-york':            ( -74.3273, 40.5075,  -73.4361, 40.8990),
    'new-york-small':      ( -74.1474, 40.5610,  -73.7238, 40.9348),
    'manhatten':           ( -74.0568, 40.6999,  -73.8068, 40.9008),
    'boston':              ( -71.3445, 42.1827,  -70.5727, 42.6951),
    'boston-small':        ( -71.2759, 42.1934,  -70.8227, 42.5450),
    'san-francisco':       (-122.5278, 37.2106, -121.6393, 38.0578),
    'san-francisco-small': (-122.5168, 37.2183, -121.7889, 37.9052),
    'sacramento':          (-121.6022, 37.8686, -121.0374, 38.9169),
    'tokyo':               ( 139.3341, 35.3689,  140.1622, 35.9108),
    'tokyo-small':         ( 139.5916, 35.5210,  140.1601, 35.8579),
    'osaka':               ( 135.0728, 34.1442,  135.8727, 35.0851),
    'osaka-small':         ( 135.0742, 34.3054,  135.6592, 34.8775),
    'bejing':              ( 116.0307, 39.6702,  116.7339, 40.1862),
    'bejing-small':        ( 116.1893, 39.7526,  116.5509, 40.0252),
    'bangkok':             ( 100.3924, 13.5946,  100.7240, 14.1958),
    'bangkok-small':       ( 100.4010, 13.6133,  100.6476, 13.8599)
}


class Bounds:
    '''
    Using this wrapper class allows to set the coordinates in an order
    (left, bottom, right, top) independent of the coordinates' access.
    '''

    def __init__(self, left=0.0, bottom=0.0, right=0.0, top=0.0):
        '''
        PARAM left,bottom,right,top : float\n
        Default is 0.0
        '''
        self._left = left
        self._bottom = bottom
        self._right = right
        self._top = top

    def __str__(self):
        return '({0}, {1}, {2}, {3})'.format(
            self.left,
            self.bottom,
            self.right,
            self.top
        )

    @property
    def left(self):
        return self._left

    @left.setter
    def left(self, value):
        self._left = value

    @property
    def bottom(self):
        return self._bottom

    @bottom.setter
    def bottom(self, value):
        self._bottom = value

    @property
    def right(self):
        return self._right

    @right.setter
    def right(self, value):
        self._right = value

    @property
    def top(self):
        return self._top

    @top.setter
    def top(self, value):
        self._top = value


def download_and_save_region(bounds, filename, api_url=API):
    '''
    TODO
    '''

    # open file and connection
    http = urllib3.PoolManager()


def parse_cmdline():
    # cmdline parsing
    parser = argparse.ArgumentParser(description='OpenStreetMap XML downloader')


    # list of predefined files
    help = 'List of predefined maps ('
    if len(PREDEFINED) > 0:
        help += ', '.join([map_name for map_name in PREDEFINED.keys()])
    else:
        help += '<no maps defined>'
    help += ")"

    parser.add_argument('-m', '--maps',
        metavar=('MAP_NAME'),
        type=str,
        nargs='+',
        default=[],
        help=help
    )


    # -b --bounds list of rectangular coordinates
    help = 'List of rectangular coordinates'
    parser.add_argument('-b', '--bounds',
        metavar=('LEFT', 'BOTTOM', 'RIGHT', 'TOP'),
        type=float,
        nargs=4,
        action='append',
        default=[],
        help=help
    )


    # -o --out list of own files containing filenames
    help = 'List of custom map-names related to the list of coordinates. '
    parser.add_argument('-o', '--out',
        metavar=('MAP_NAME'),
        type=str,
        nargs='+',
        default=[],
        help=help
    )


    # finished preparing parser
    args = parser.parse_args()


    # prepare download files
    downloads = []

    # remember predefined maps and their related coordinates
    for map_name in args.maps:
        try:
            downloads.append((
                map_name,
                PREDEFINED[map_name]
            ))
        except KeyError:
            err_msg = '\'' + map_name + '\''
            err_msg += ' is expected to be predefined, but isn\'t.'
            exit(err_msg)

    # remember custom maps and their related coordinates
    if len(args.out) > len(args.bounds):
        err_msg = 'There are more custom map names than bounds.'
        exit(err_msg)
    if len(args.out) < len(args.bounds):
        err_msg = 'There are more bounds than custom map names.'
        exit(err_msg)

    for map_name, bounds in zip(args.out, args.bounds):
        bounds = Bounds(
            bounds[0],
            bounds[1],
            bounds[2],
            bounds[3],
        )
        downloads.append((map_name, bounds))

    return downloads


def main():
    downloads = parse_cmdline()
    print(downloads)


if __name__ == '__main__':
    main()
