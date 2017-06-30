#!/usr/bin/env python
"""
OpenStreetMap XML to MicroTrafficSim ExchangeFormat Batch Converter

Requires Python 3
"""
__author__ = "Maximilian Luz"
import os
import subprocess


FILES = [
    ("map.osm", True)
]


def change_suffix(s):
    base = s.rsplit(".osm", 1)
    return ".mtsmap".join(base)

def convert(file, driving_on_the_right):
    out_file = change_suffix(file)

    args = "-Dexec.args=\"-i {} -o {} --driving-right={}\"".format(file, out_file, driving_on_the_right)
    command = "./gradlew :tools:exfmtconv:run"

    subprocess.call(command + " " + args, shell=True)


def run():
    """Convert all files"""

    for (file, right) in FILES:
        convert(file, right)


if __name__ == '__main__':
    run()
