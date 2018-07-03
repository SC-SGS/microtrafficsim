#!/usr/bin/env python
'''
OpenStreetMap XML downloader for multiple (hardcoded) files, using the
overpass API.

Requires Python 3 and the urllib3 package.
'''

import argparse


API = 'http://overpass-api.de/api'
PREDEFINED = [
    ('stuttgart',              8.9679, 48.6622,    9.3363, 48.9135),
    ('stuttgart-small',        9.0565, 48.7027,    9.2876, 48.8561),
    ('karlsruhe',              8.2356, 48.9164,    8.5408, 49.1195),
    ('london',                -0.5679, 51.2447,    0.3117, 51.7275),
    ('london-small',          -0.3433, 51.3400,    0.1428, 51.6159),
    ('paris-small',            2.2058, 48.7863,    2.4809, 48.9209),
    ('barcelona',              1.9470, 41.2608,    2.3332, 41.5122),
    ('barcelona-small',        2.0328, 41.3369,    2.2570, 41.4610),
    ('rome',                  12.1632, 41.6385,   12.7091, 42.0015),
    ('rome-small',            12.3610, 41.7739,   12.6381, 41.9990),
    ('new-york',             -74.3273, 40.5075,  -73.4361, 40.8990),
    ('new-york-small',       -74.1474, 40.5610,  -73.7238, 40.9348),
    ('manhatten',            -74.0568, 40.6999,  -73.8068, 40.9008),
    ('boston',               -71.3445, 42.1827,  -70.5727, 42.6951),
    ('boston-small',         -71.2759, 42.1934,  -70.8227, 42.5450),
    ('san-francisco',       -122.5278, 37.2106, -121.6393, 38.0578),
    ('san-francisco-small', -122.5168, 37.2183, -121.7889, 37.9052),
    ('sacramento',          -121.6022, 37.8686, -121.0374, 38.9169),
    ('tokyo',                139.3341, 35.3689,  140.1622, 35.9108),
    ('tokyo-small',          139.5916, 35.5210,  140.1601, 35.8579),
    ('osaka',                135.0728, 34.1442,  135.8727, 35.0851),
    ('osaka-small',          135.0742, 34.3054,  135.6592, 34.8775),
    ('bejing',               116.0307, 39.6702,  116.7339, 40.1862),
    ('bejing-small',         116.1893, 39.7526,  116.5509, 40.0252),
    ('bangkok',              100.3924, 13.5946,  100.7240, 14.1958),
    ('bangkok-small',        100.4010, 13.6133,  100.6476, 13.8599)
]


def download_and_save_predefined(maps, api_url=API):
    '''
    TODO
    '''
    pass


def download_and_save_region(bounds, filename, api_url=API):
    '''
    TODO
    '''
    pass


def main(args):
    print([float(x) for x in args.coord.split(',')])


if __name__ == '__main__':
    # cmdline parsing
    parser = argparse.ArgumentParser(description='OpenStreetMap XML downloader')


    # list of predefined files
    help = 'List of predefined maps ('
    if len(PREDEFINED) > 0:
        help += ', '.join([download[0] for download in PREDEFINED])
    else:
        help += '<no maps defined>'
    help += ")"

    parser.add_argument('-p', '--predefined', nargs='+', type=str,
        help=help
    )


    # -c --coord list of rectangular coordinates
    help = 'List of rectangular coordinates: left,bottom,right,top'
    parser.add_argument('-c', '--coord', type=str,
        help=help
    )


    # -o --out list of own files containing filenames


    # finished preparing parser
    args = parser.parse_args()

    main(args)
