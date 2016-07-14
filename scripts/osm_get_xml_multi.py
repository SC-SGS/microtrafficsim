#!/usr/bin/env python
"""
OpenStreetMap XML downloader for multiple (hardcoded) files, using the
overpass API.

Not tested with python 3 yet.
"""
__author__ = "Maximilian Luz"

import urllib2
import os


def osm_get_region_xml(api_url, bb_left, bb_bottom, bb_right, bb_top, filename):
    """downloads the specified OSM region to the specified file"""

    # construct url (
    url = api_url + "/map?bbox={0},{1},{2},{3}"
    url = url.format(str(bb_left), str(bb_bottom), str(bb_right), str(bb_top))

    # open file
    f = open(filename, 'wb')

    print("downloading '{0}' to '{1}'".format(url, filename))

    error = False
    try:
        # open connection
        remote = urllib2.urlopen(url)
        buffer_size = 8129

        # download and write file
        buffer = remote.read(buffer_size)
        while buffer:
            f.write(buffer)
            buffer = remote.read(buffer_size)

    except urllib2.URLError as e:
        error = True
        if e.code == '400':
            print("HTTP Error 400: 'Any of the node/way/relation limits are "
                  + "crossed'")
        elif e.code == '':
            print("HTTP Error 509: 'You have downloaded too much data. "
                  + "Please try again later'")
        else:
            print("HTTP Error {0}".format(e.code))

    # close file, cleanup on error
    f.close()
    if error:
        os.remove(filename)
    else:
        print("download finished successfully!")


if __name__ == '__main__':
    api = 'http://overpass-api.de/api'
    download = [
        ('stuttgart.osm',              8.9679, 48.6622,    9.3363, 48.9135),
        ('stuttgart-small.osm',        9.0565, 48.7027,    9.2876, 48.8561),
        ('karlsruhe.osm',              8.2356, 48.9164,    8.5408, 49.1195),
        ('london.osm',                -0.5679, 51.2447,    0.3117, 51.7275),
        ('london-small.osm',          -0.3433, 51.3400,    0.1428, 51.6159),
        ('paris-small.osm',            2.2058, 48.7863,    2.4809, 48.9209),
        ('barcelona.osm',              1.9470, 41.2608,    2.3332, 41.5122),
        ('barcelona-small.osm',        2.0328, 41.3369,    2.2570, 41.4610),
        ('rome.osm',                  12.1632, 41.6385,   12.7091, 42.0015),
        ('rome-small.osm',            12.3610, 41.7739,   12.6381, 41.9990),
        ('new-york.osm',             -74.3273, 40.5075,  -73.4361, 40.8990),
        ('new-york-small.osm',       -74.1474, 40.5610,  -73.7238, 40.9348),
        ('manhatten.osm',            -74.0568, 40.6999,  -73.8068, 40.9008),
        ('boston.osm',               -71.3445, 42.1827,  -70.5727, 42.6951),
        ('boston-small.osm',         -71.2759, 42.1934,  -70.8227, 42.5450),
        ('san-francisco.osm',       -122.5278, 37.2106, -121.6393, 38.0578),
        ('san-francisco-small.osm', -122.5168, 37.2183, -121.7889, 37.9052),
        ('sacramento.osm',          -121.6022, 37.8686, -121.0374, 38.9169),
        ('tokyo.osm',                139.3341, 35.3689,  140.1622, 35.9108),
        ('tokyo-small.osm',          139.5916, 35.5210,  140.1601, 35.8579),
        ('osaka.osm',                135.0728, 34.1442,  135.8727, 35.0851),
        ('osaka-small.osm',          135.0742, 34.3054,  135.6592, 34.8775),
        ('bejing.osm',               116.0307, 39.6702,  116.7339, 40.1862),
        ('bejing-small.osm',         116.1893, 39.7526,  116.5509, 40.0252),
        ('bangkok.osm',              100.3924, 13.5946,  100.7240, 14.1958),
        ('bangkok-small.osm',        100.4010, 13.6133,  100.6476, 13.8599)
    ]

    for x in download:
        osm_get_region_xml(api, x[1], x[2], x[3], x[4], x[0])
