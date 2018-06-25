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

    def __init__(self, length, crossroad, v_max):
        # vehicles
        self._cells = {}

        # params
        self._length = length
        self._crossroad = crossroad
        self._v_max = v_max


    def __cell_check(self, cell):
        """
        Raises an IndexError if cell < 0 or length <= cell.

        Returns true if the cell contains a vehicle
        """
        if cell < 0 or self._length <= cell:
            raise IndexError("Index is ", cell)
        return cell in self._cells


    def __getitem__(self, cell):
        contains_vehicle = self.__cell_check(cell)
        if not contains_vehicle:
            raise ValueError("There should be a vehicle at {}, but is not.".format(cell))
        return self._cells[cell]


    def __setitem__(self, cell, vehicle):
        contains_vehicle = self.__cell_check(cell)
        if contains_vehicle:
            raise ValueError(
                "There should be no vehicle (at {}) to fill cell {}." \
                .format(vehicle._pos, cell)
            )
        self._cells[cell] = vehicle
        vehicle._pos = cell


    def __delitem__(self, cell):
        del self._cells[cell]


    def _move(self, vehicle, dest):
        del self._cells[vehicle._pos]
        contains_vehicle = self.__cell_check(dest)
        if contains_vehicle:
            raise ValueError(
                "There should be no vehicle (at {}) to fill cell {}." \
                .format(vehicle._pos, dest)
            )
        self._cells[dest] = vehicle
        vehicle._pos = dest


    @property
    def _last_pos(self):
        # if no vehicles in the street
        if not self._cells:
            return self._length
        else:
            key = sorted(self._cells.keys())[0]
            return self._cells[key]._pos


    def _vehicle_in_front(self, vehicle):
        keys = sorted(self._cells.keys())
        idx = 1 + keys.index(vehicle._pos)

        if idx == len(keys):
            return None
        else:
            return self._cells[keys[idx]]


    @property
    def length(self):
        return self._length


    @property
    def vehicles(self):
        return [vehicle for vehicle in self._cells.values()]


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
        self._pos = -1

        # dawdle factor
        self._random = random.Random(seed)
        self._dawdle_factor = dawdle_factor

        # velocity
        self._max_v = 5
        self._v = 0


    def accelerate(self):
        # accelerate, own max v, street max v
        self._v = min(self._v + 1, self._max_v, self._street._v_max)


    def brake(self):
        front = self._street._vehicle_in_front(self)

        # if no front vehicle
        # -> self is frontmost
        # -> brake for next street
        if front == None:
            # distance to end of the current street
            distance = self._street._length - self._pos
            # distance to the last free position of the next street
            distance += self._street._crossroad._leaving._last_pos
        # if front vehicle
        # -> brake for front vehicle
        else:
            distance = front._pos - self._pos

        self._v = min(self._v, distance - 1)


    def dawdle(self):
        if self._random.random() < self._dawdle_factor:
            self._v = max(self._v - 1, 0)


    def move(self):
        distance = self._street.length - self._pos
        # if vehicle leaves current street
        # -> enter next street
        if self._v >= distance:
            del self._street[self._pos]
            self._street = self._street._crossroad.leaving
            self._street[self._v - distance] = self
        else:
            self._street._move(self, self._pos + self._v)
