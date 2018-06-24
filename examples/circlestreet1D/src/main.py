from matplotlib import pyplot
from matplotlib import animation
from matplotlib import cm
import numpy as np

import microtrafficsim as mts


class VelocityImage:
    """
    Contains all velocities.
    """

    def __init__(self, init_street, t=100, cmap_name='cool'):
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
        self._cmap.set_bad(color='white')
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


    def shift(self):
        del self._street_vals[-1]
        self._street_vals.insert(0, self._street_vals[0])


    def add_new_street(self, new_street):
        if len(self._street_vals) == self._t:
            raise ValueError("too many streets")
        self._street_vals.insert(0, new_street)


def animate(i, v_img):
    if (i == 0):
        print("init step")
    else:
        print("step", i)
        v_img.shift()


    v_img.plot.set_data(v_img.to_array())
    return (v_img.plot,)


def main():
    # init street
    street = mts.Street(5)
    vehicle = mts.Vehicle()
    street[1] = vehicle

    fig = pyplot.figure()
    v_img = VelocityImage(street, t=6)

    anim = animation.FuncAnimation(
        fig,
        func=animate,
        fargs=[v_img],
        interval=1000,
        blit=True
    )
    pyplot.show()


if __name__ == "__main__":
    main()
