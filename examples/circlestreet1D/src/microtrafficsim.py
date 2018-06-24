from matplotlib import cm
import random
import numpy as np


class Street:
    """
    TODO
    """

    def __init__(self, n):
        self._cells = {}
        self._n = n


    def __index_check(self, index):
        if index < 0 or self.length <= index:
            raise IndexError()


    def __getitem__(self, cell):
        self.__index_check(cell)
        return self._cells[cell]


    def __setitem__(self, cell, vehicle):
        self.__index_check(cell)
        self._cells[cell] = vehicle


    @property
    def length(self):
        return self._n


    @property
    def vehicles(self):
        return self._cells.values()


    def to_v_list(self):
        list = [np.nan] * self.length

        for cell, vehicle in self._cells.items():
            list[cell] = vehicle.v

        return list


class Vehicle:
    """
    TODO
    """

    def __init__(self, street, seed, dawdle_factor=0.2, colormap_name='cubehelix'):
        self._street = street
        self._random = random.Random(seed)
        self._dawdle_factor = dawdle_factor
        self._cmap = cm.get_cmap(colormap_name)
        self._max_v = 5
        self._v = 0


    @property
    def v(self):
        return self._v


    @property
    def max_v(self):
        return self._max_v


    @property
    def dawdle_factor(self):
        return self._dawdle_factor


    def accelerate(self):
        self._v = min(self.v + 1, self.max_v)


    def brake(self):
        pass


    def dawdle(self):
        if self._random.random() < self.dawdle_factor:
            self._v = max(self.v - 1, 0)


    def move(self):
        pass
