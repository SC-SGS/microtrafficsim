from matplotlib import cm
import random
import numpy as np


class Crossroad:
    """
    TODO
    """

    def __init__(self):
        self._incoming = None
        self._leaving = None


    @property
    def incoming(self):
        return self._incoming


    @incoming.setter
    def incoming(self, incoming):
        self._incoming = incoming


    @property
    def leaving(self):
        return self._leaving


    @leaving.setter
    def leaving(self, leaving):
        self._leaving = leaving


class Street:
    """
    TODO
    """

    def __init__(self, length, crossroad):
        # vehicles
        self._cells = {}
        self._last_vehicle = None

        # params
        self._length = length
        self._crossroad = crossroad


    def __cell_check(self, cell):
        """
        Raises an IndexError if cell < 0 or length <= cell.

        Returns true if the cell contains a vehicle
        """
        if cell < 0 or self._length <= cell:
            raise IndexError()
        return cell in self._cells


    def __getitem__(self, cell):
        contains_vehicle = self.__cell_check(cell)
        if not contains_vehicle:
            raise ValueError("There should be a vehicle, but is not.")
        return self._cells[cell]


    def __setitem__(self, cell, vehicle):
        self.__cell_check(cell)
        contains_vehicle = self.__cell_check(cell)
        if contains_vehicle:
            raise ValueError("There should be no vehicle to fill cell.")
        self._cells[cell] = vehicle
        vehicle._pos = cell


    @property
    def _last_pos(self):
        if self._last_vehicle is not None:
            return self._last_vehicle.pos
        else:
            return self._length


    @property
    def length(self):
        return self._length


    @property
    def vehicles(self):
        return self._cells.values()


    def to_v_list(self):
        list = [np.nan] * self._length

        for cell, vehicle in self._cells.items():
            list[cell] = vehicle._v

        return list


class Vehicle:
    """
    TODO
    """

    def __init__(self, street, seed, dawdle_factor=0.2):
        # street stuff
        self._street = street
        self._front = None
        self._pos = -1

        # dawdle factor
        self._random = random.Random(seed)
        self._dawdle_factor = dawdle_factor

        # velocity
        self._max_v = 5
        self._v = 0


    def accelerate(self):
        self._v = min(self._v + 1, self._max_v)


    def brake(self):
        # if no front vehicle
        # -> self is frontmost
        # -> brake for next street
        if self._front == None:
            distance = self._street._length - self._pos
            distance += self._street._crossroad._leaving._last_pos
        # if front vehicle
        # -> brake for front vehicle
        else:
            distance = self._front._pos - self._pos

        self._v = min(self._v, distance - 1)


    def dawdle(self):
        if self._random.random() < self._dawdle_factor:
            self._v = max(self._v - 1, 0)


    def move(self):
        pass
