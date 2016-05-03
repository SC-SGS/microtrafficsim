"""
Test generator for the HaversineDistanceCalculator
"""

from haversine import haversine
import random


def random_coord():
    return (random.uniform(-90, 90), random.uniform(-180, 180))


def fmtprt(fname, a, b):
    for i in range(0, len(a)):
        print "\t\t{}(new Coordinate({}f, {}f), new Coordinate({}f, {}f), {}f);".format(fname, a[i][0], a[i][1], b[i][0], b[i][1], haversine(a[i], b[i])*1000)

def zero():
    a = []
    b = []

    for i in range(0, 3):
        c = random_coord()
        a.append(c)
        b.append(c)

    print "\n\t@Test"
    print "\tpublic void zeroDistanceTest() {"
    fmtprt("testExact", a, b)
    print "\t}"


def special_values():
    a = [
        ( 36.7706,   0.0000),
        (  0.0000, 352.2007),
        ( 90.0000,   0.0000),
        (107.8402,  82.3843),
        (307.3582, 160.6513)
    ]

    b = [
        (  0.0000, 326.8710),
        ( 12.8311,   0.0000),
        (-90.0000 ,  0.0000),
        (107.8402,  82.3843),
        (112.6830,  62.8048)
    ]

    print "\n\t@Test"
    print "\tpublic void specialValuesTest() {"
    fmtprt("testMedium", a, b)
    print "\t}"


def large():
    a = []
    b = []

    while len(a) < 3:
        c1 = random_coord()
        c2 = random_coord()

        if haversine(c1, c2) >= 1000:
            a.append(c1)
            b.append(c2)

    print "\n\t@Test"
    print "\tpublic void largeDistanceTest() {"
    fmtprt("testLarge", a, b)
    print "\t}"


def medium():
    a = []
    b = []

    while len(a) < 3:
        c1 = random_coord()
        c2 = random_coord()

        if haversine(c1, c2) >= 0.1 and haversine(c1, c2) < 1000:
            a.append(c1)
            b.append(c2)

    print "\n\t@Test"
    print "\tpublic void mediumDistanceTest() {"
    fmtprt("testMedium", a, b)
    print "\t}"


def small():
    a = []
    b = []

    while len(a) < 3:
        c1 = random_coord()
        c2 = (c1[0] + random.uniform(-0.1, 0.1), c1[1] + random.uniform(-0.1, 0.1))

        if haversine(c1, c2) < 0.1 and haversine(c1, c2) > 0.01:
            a.append(c1)
            b.append(c2)

    print ""
    print "\t@Test"
    print "\tpublic void smallDistanceTest() {"
    fmtprt("testSmall", a, b)
    print "\t}"


def tiny():
    a = []
    b = []

    while len(a) < 3:
        c1 = random_coord()
        c2 = (c1[0] + random.uniform(-0.1, 0.1),
              c1[1] + random.uniform(-0.1, 0.1))

        if haversine(c1, c2) < 0.01:
            a.append(c1)
            b.append(c2)

    print "\n\t@Test"
    print "\tpublic void tinyDistanceTest() {"
    fmtprt("testTiny", a, b)
    print "\t}"


if __name__ == '__main__':
    tiny()
    small()
    medium()
    large()
    special_values()
    zero()
