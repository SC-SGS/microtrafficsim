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

    def __init__(self, init_street, t=100, cmap_name='cool', bg='white'):
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
        pyplot.clim(0, 5)
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


def main():
    # init params
    compound_streets = False

    # streets
    street_length = 100
    town_part = 0.3

    # vehicles
    density = 0.2

    # time
    t = 100
    fps = 5

    # plotting
    cmap_name = 'cool'
    bg = 'lightgrey'


    # calculated params from init params
    vehicle_count = max(1, int(density * street_length))
    millis_per_frame = max(1, int(1000.0 / fps))

    if not compound_streets:
        # init street
        crossroad = mts.Crossroad()

        # stick graph parts together
        street = mts.Street(street_length, crossroad, v_max=5)
        crossroad.leaving = street
        crossroad.incoming = street


        # create vehicles
        for index in random.sample(range(street_length), vehicle_count):
            street[index] = mts.Vehicle(street, random.random())
    else:
        town_street_length = int(street_length * town_part)
        motorway_length = street_length - town_street_length

        # init street
        crossroad_left = mts.Crossroad()
        crossroad_mid = mts.Crossroad()

        # stick graph parts together
        town_street = mts.Street(town_street_length, crossroad_mid, v_max=2)
        crossroad_left.leaving = town_street
        crossroad_mid.incoming = town_street
        motorway = mts.Street(motorway_length, crossroad_left, v_max=5)
        crossroad_mid.leaving = motorway
        crossroad_left.incoming = motorway


        # create vehicles
        for index in random.sample(range(street_length), vehicle_count):
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
    v_img = VelocityImage(street, t=t, cmap_name=cmap_name, bg=bg)

    anim = animation.FuncAnimation(
        fig,
        func=animate,
        fargs=[v_img, street],
        interval=millis_per_frame,
        blit=True
    )
    pyplot.show()


if __name__ == "__main__":
    main()
