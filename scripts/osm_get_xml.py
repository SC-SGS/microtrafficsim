#!/usr/bin/env python
"""
OpenStreetMap XML file downloader.

Requires python 3, urllib and docopt

Note: The download-size is limited, for larger areas refer to
'http://download.geofabrik.de/'
"""
__author__ = "Maximilian Luz"

import os
import urllib3
from docopt import docopt


# Note: openstreetmap.org seems unable to handle large areas, thus use overpass
USAGE = """OpenStreetMap XML downloader.

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

    # open file and connection
    http = urllib3.PoolManager()
    has_failed = False
    try:
        print("downloading '{0}' to '{1}'".format(url, filename))
        with open(filename, "wb") as target:
            remote = http.request('GET', url)
            target.write(remote.data)

    except Exception as err:
        has_failed = True
        os.remove(filename)
        raise err

    if not has_failed:
        print("download finished successfully!")


def run(args):
    """run the downloader with the specified arguments"""
    bounds = args['--bounds'].split(",")
    left = float(bounds[0].strip())
    bottom = float(bounds[1].strip())
    right = float(bounds[2].strip())
    top = float(bounds[3].strip())

    osm_get_region_xml(args['--api'], left, bottom, right, top, args['--out'])


if __name__ == '__main__':
    run(docopt(USAGE, version='OSM XML downloader 1.0'))
