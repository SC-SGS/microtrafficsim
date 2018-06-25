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
    print(street.vehicles[0]._pos)
    print(street.vehicles[0]._v)

    if (i == 0):
        print("init step")
    else:
        print("step", i)

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
    # init general
    street_length = 6
    t = 10
    random.seed(42)

    # init street
    crossroad = mts.Crossroad()
    street = mts.Street(street_length, crossroad)
    crossroad.incoming = street
    crossroad.leaving = street

    # vehicles
    street[0] = mts.Vehicle(street, random.random())
    # street[2] = mts.Vehicle(street, random.random())

    fig = pyplot.figure()
    v_img = VelocityImage(street, t=t)

    anim = animation.FuncAnimation(
        fig,
        func=animate,
        fargs=[v_img, street],
        interval=2000,
        blit=True
    )
    pyplot.show()


if __name__ == "__main__":
    main()
