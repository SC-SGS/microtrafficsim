import argparse

from matplotlib import pyplot
from matplotlib import animation
from matplotlib import cm
import random
import numpy as np

import microtrafficsim as mts


class VelocityImage:
    """
    Contains all velocities.
    """

    def __init__(self, init_street, t, cmap_name, bg, v_max):
        # init street values
        self._t = t
        n = init_street.length
        self._street_vals = [[np.nan] * n for _ in range(t)]

        # set initial street state
        self._street_vals[0] = init_street.to_v_list()

        # init imgplot
        pyplot.gcf()
        self._imgplot = pyplot.imshow(
            self.to_array(), origin='lower', animated=True
        )

        # init plot
        pyplot.title("Single Laned Nagel-Schreckenberg-Model")
        pyplot.xlabel("street cell")
        pyplot.ylabel("street age")

        # init colormap
        self._cmap = cm.get_cmap(cmap_name)
        self._cmap.set_bad(color=bg)
        self._imgplot.set_cmap(self.cmap)
        pyplot.clim(0, v_max)
        pyplot.colorbar()



    @property
    def cmap(self):
        return self._cmap


    @property
    def plot(self):
        return self._imgplot


    def to_array(self):
        return np.array(self._street_vals)
        # return np.array([s.to_array() for s in self._street_vals])
        # return np.array(np.arange(6) * np.arange(5)[:, np.newaxis])


    def shift(self, new_street):
        del self._street_vals[-1]
        self._street_vals.insert(0, new_street.to_v_list())


class StreetWrapper:
    """
    Should wrap multiple streets for easier interaction with the visualization.
    """

    def __init__(self, streets):
        self._length = sum([s.length for s in streets])
        self._streets = streets


    @property
    def length(self):
        return len(self)


    def __len__(self):
        return self._length


    def to_v_list(self):
        v_list = []
        for s in self._streets:
            v_list += s.to_v_list()
        return v_list


    @property
    def vehicles(self):
        v_list = []
        for s in self._streets:
            v_list += s.vehicles
        return v_list


class Config:
    """
    TODO
    """

    def __init__(self):
        # streets
        self.street_length = 100
        self.compound_streets = False
        self.town_part = 0.3

        # vehicles
        self.density = 0.16

        # time
        self.t = 100
        self.fps = 5

        # plotting
        self.cmap_name = 'cool'
        self.bg = 'lightgray'


    @property
    def vehicle_count(self):
        return max(1, int(self.density * self.street_length))


    @property
    def millis_per_frame(self):
        return max(1, int(1000.0 / self.fps))


    @property
    def town_street_length(self):
        return int(self.street_length * self.town_part)


    @property
    def motorway_length(self):
        return self.street_length - self.town_street_length


def animate(i, v_img, street):
    if (i == 0):
        pass # init step
    else:
        for vehicle in street.vehicles:
            vehicle.accelerate()

        for vehicle in street.vehicles:
            vehicle.brake()

        for vehicle in street.vehicles:
            vehicle.dawdle()

        for vehicle in street.vehicles:
            vehicle.move()

        v_img.shift(street)


    v_img.plot.set_data(v_img.to_array())
    return (v_img.plot,)


def main(cfg):
    """
    Config object cfg
    """
    town_street_v_max = 2
    motorway_v_max = 5
    v_max = 5

    if not cfg.compound_streets:
        # init street
        crossroad = mts.Crossroad()

        # stick graph parts together
        street = mts.Street(cfg.street_length, crossroad, v_max=5)
        crossroad.leaving = street
        crossroad.incoming = street


        # create vehicles
        for index in random.sample(range(cfg.street_length), cfg.vehicle_count):
            street[index] = mts.Vehicle(street, random.random())
    else:
        # init street
        crossroad_left = mts.Crossroad()
        crossroad_mid = mts.Crossroad()

        # stick graph parts together
        town_street = mts.Street(
            cfg.town_street_length, crossroad_mid, v_max=town_street_v_max
        )
        crossroad_left.leaving = town_street
        crossroad_mid.incoming = town_street
        motorway = mts.Street(
            cfg.motorway_length, crossroad_left, v_max=motorway_v_max
        )
        crossroad_mid.leaving = motorway
        crossroad_left.incoming = motorway


        # create vehicles
        for index in random.sample(range(cfg.street_length), cfg.vehicle_count):
            # get correct street
            if index < len(town_street):
                town_street[index] = mts.Vehicle(town_street, random.random())
            else:
                index -= len(town_street)
                motorway[index] = mts.Vehicle(motorway, random.random())


        # street wrapper for better interaction
        street = StreetWrapper([town_street, motorway])


    # plotting
    fig = pyplot.figure()
    v_img = VelocityImage(
        street, t=cfg.t, cmap_name=cfg.cmap_name, bg=cfg.bg, v_max=v_max
    )

    anim = animation.FuncAnimation(
        fig,
        func=animate,
        fargs=[v_img, street],
        interval=cfg.millis_per_frame,
        blit=True
    )
    pyplot.show()


if __name__ == "__main__":
    # defaults
    cfg = Config()


    # cmdline parsing
    parser = argparse.ArgumentParser(description="1D-Microtrafficsim")

    # streets
    parser.add_argument("-n", "--street_length", type=int,
        help="number of cells in the 1D-street (default: {})"
             .format(cfg.street_length),
        default=cfg.street_length
    )
    parser.add_argument("--compound_streets", action="store_true",
        help="if set, {}%% of the street will be in town".format(cfg.town_part),
        default=cfg.compound_streets
    )

    # vehicles
    parser.add_argument("--density", type=float,
        help="percentage of filled street cells (default: {})"
             .format(cfg.density),
        default=cfg.density
    )

    # time
    parser.add_argument("-t", "--steps", type=int,
        help="remembered time steps (plot's y axis) (default: {})"
             .format(cfg.t),
        default=cfg.t
    )
    parser.add_argument("-fps", "--fps", type=int,
        help="time steps per second (default: {})".format(cfg.fps),
        default=cfg.fps
    )

    # plotting
    parser.add_argument("-cm", "--colormap", type=str,
        help="name of colormap (default: {})".format(cfg.cmap_name),
        default=cfg.cmap_name
    )
    parser.add_argument("-bg", "--background", type=str,
        help="plot's background color (e.g. #D3D3D3 or lightgray) (default: {})"
             .format(cfg.bg),
        default=cfg.bg
    )

    args = parser.parse_args()


    # setup config
    cfg.street_length = args.street_length
    cfg.compound_streets = args.compound_streets
    cfg.density = args.density
    cfg.t = args.steps
    cfg.fps = args.fps
    cfg.cmap_name = args.colormap
    cfg.bg = args.background

    main(cfg)
