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
    street_length = 100
    density = 0.2
    t = 100
    fps = 1
    cmap_name = 'cool'

    # calculated params from init params
    vehicle_count = max(1, int(density * street_length))
    millis_per_frame = 1000 * max(1, int(1.0 / fps)) 

    # init street
    crossroad = mts.Crossroad()
    street = mts.Street(street_length, crossroad, v_max=5)
    crossroad.incoming = street
    crossroad.leaving = street

    # create vehicles
    for index in random.sample(range(street_length), vehicle_count):
        street[index] = mts.Vehicle(street, random.random())

    # plotting
    fig = pyplot.figure()
    v_img = VelocityImage(street, t=t, cmap_name=cmap_name)

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
