#!/usr/bin/env python
"""
OpenStreetMap XML file downloader.

Requires: urllib2, docopt
Not testet with python 3 yet.

Note: The download-size is limited, for larger areas refer to
'http://download.geofabrik.de/'
"""
__author__ = "Maximilian Luz"

from docopt import docopt
import urllib2
import os


# Note: openstreetmap.org seems unable to handle large areas, thus use overpass
usage = """OpenStreetMap XML downloader.

Usage:
  osm_get_xml.py [options]
  osm_get_xml.py -h | --help
  osm_get_xml.py --version

Options:
  -h --help                Show this screen.
  --api=<url>              Specify the base-api URL
                             [default: http://overpass-api.de/api]
  --bounds=<bbox>          Specify the bounds of the area to be downloaded as
                             latitude/longitude in the following order:
                             LEFT,BOTTOM,RIGHT,TOP.
                             [default: 9.0788,48.7074,9.2804,48.8218]
  -o <file> --out=<file>   Specify the output file [default: map.osm]
"""


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
    args = docopt(usage, version='OSM XML downloader 1.0')
    bounds = args['--bounds'].split(",")
    left = float(bounds[0].strip())
    bottom = float(bounds[1].strip())
    right = float(bounds[2].strip())
    top = float(bounds[3].strip())

    osm_get_region_xml(args['--api'], left, bottom, right, top, args['--out'])
