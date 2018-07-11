"""
A small module for 1D microscopic traffic simulation.
"""
__author__ = "Dominic Parga Cacheiro"

################################################################################

from matplotlib import cm
import random
import numpy as np

################################################################################

class Crossroad:
    """
    A container for intersection between two streets.
    """

    def __init__(self):
        """
        Sets its incoming and leaving street to None. Due to circle references
        they have to be set after the streets are initialized (which need this
        crossroad for init)
        """
        self._incoming = None
        self._leaving = None

    ############################################################################

    @property
    def incoming(self):
        """
        RETURN incoming street: Street
        """
        return self._incoming

    @incoming.setter
    def incoming(self, incoming):
        """
        PARAM incoming: Street
        """
        self._incoming = incoming

    @property
    def leaving(self):
        """
        RETURN leaving street: Street
        """
        return self._leaving

    @leaving.setter
    def leaving(self, leaving):
        """
        PARAM leaving: Street
        """
        self._leaving = leaving

################################################################################

class Street:
    """
    Kind of a container class storing its vehicles in a sparse array (-> dict).
    """

    def __init__(self, length, crossroad, v_max):
        """
        Default initialization.

        PARAM length: int
        The number of cells of this street.

        PARAM crossroad: Crossroad
        The upcoming (destination) crossroad

        PARAM v_max: int
        Speed limit of this street needed by vehicles
        """
        # vehicles
        self._cells = {}

        # params
        self._length = length
        self._crossroad = crossroad
        self._v_max = v_max

    ############################################################################

    @property
    def length(self):
        """
        RETURN length of this street: int
        """
        return len(self)

    def __len__(self):
        """
        RETURN length of this street: int
        """
        return self._length

    def __cell_check(self, cell):
        """
        Raises an IndexError if cell < 0 or length <= cell.

        RETURN true if the cell contains a vehicle: bool
        """
        if cell < 0 or self._length <= cell:
            raise IndexError("Index is ", cell)
        return cell in self._cells

    def __getitem__(self, cell):
        """
        PARAM cell: int
        Index/position

        THROWS ValueErro
        if there is no vehicle. Helps a lot with debugging.
        """
        contains_vehicle = self.__cell_check(cell)
        if not contains_vehicle:
            err_msg = "There should be a vehicle at {}, but is not."
            err_msg = err_msg.format(cell)
            raise ValueError(err_msg)
        return self._cells[cell]

    def __setitem__(self, cell, vehicle):
        """
        PARAM cell: int
        Index/position

        PARAM vehicle: Vehicle
        The new vehicle of the given cell position

        THROWS ValueError:
        if the given cell already contains a vehicle
        """
        contains_vehicle = self.__cell_check(cell)
        if contains_vehicle:
            raise ValueError(
                "There should be no vehicle (at {}) to fill cell {}." \
                .format(vehicle._pos, cell)
            )
        self._cells[cell] = vehicle
        vehicle._pos = cell

    def __delitem__(self, cell):
        """
        Removes a vehicle at the given cell position (if not empty)
        """
        del self._cells[cell]

    ############################################################################

    def _move(self, vehicle, dest):
        """
        Removes the vehicle from its position and sets the cell of index 'dest'
        to this vehicle before updating its' position as well.

        THROWS ValueError
        if 'dest' contains a vehicle
        """
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
        """
        RETURN max insertion index: int
        If a vehicle crosses a crossroad, it has to check for space. Relevant
        for it is the last vehicle in the next street and (if empty) its'
        length.
        """
        # if no vehicles in the street
        if not self._cells:
            return self._length
        else:
            key = sorted(self._cells.keys())[0]
            return self._cells[key]._pos

    def _vehicle_in_front(self, vehicle):
        """
        Sorts all vehicles by their position and immediately gets the front
        vehicle

        RETURN the vehicle in front of the given one: Vehicle
        or None, if given vehicle is frontmost
        """
        keys = sorted(self._cells.keys())
        idx = 1 + keys.index(vehicle._pos)

        if idx == len(keys):
            return None
        else:
            return self._cells[keys[idx]]

    ############################################################################

    @property
    def vehicles(self):
        """
        RETURN a list of vehicles in this street (no fix order): list
        """
        return [vehicle for vehicle in self._cells.values()]

    def to_v_list(self):
        """
        RETURN list of velocities (or np.nan if no vehicle) for visualization
        purposes
        """
        list = [np.nan] * self._length

        for cell, vehicle in self._cells.items():
            list[cell] = vehicle._v

        return list

################################################################################

class Vehicle:
    """
    A vehicle containing information about machine (v_max) and driver
    (street.v_max) at once.
    """

    def __init__(self, street, seed, dawdle_factor=0.2):
        """
        Default initialization. v_max is set to 5

        PARAM street: Street
        The street where this vehicle drives on.

        PARAM seed: int
        Dawdling needs a random object, which is initialized with this seed.

        PARAM dawdle_factor: float
        Default is 0.2
        """
        # street stuff
        self._street = street
        self._pos = -1

        # dawdle factor
        self._random = random.Random(seed)
        self._dawdle_factor = dawdle_factor

        # velocity
        self._v_max = 5
        self._v = 0

    ############################################################################

    def accelerate(self):
        """
        Increases the velocity up to v_max of the vehicle and the street's v_max
        """
        # accelerate, own max v, street max v
        self._v = min(self._v + 1, self._v_max, self._street._v_max)

    def brake(self):
        """
        Braking for the front vehicle (if existing) or for the next street (if
        self is frontmost vehicle in current street)
        """
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
        """
        If random, the velocity is decremented.
        """
        if self._random.random() < self._dawdle_factor:
            self._v = max(self._v - 1, 0)

    def move(self):
        """
        Moves the vehicle at the street or switches its street if crossing a
        crossroad.
        """
        distance = self._street.length - self._pos
        # if vehicle leaves current street
        # -> enter next street
        if self._v >= distance:
            del self._street[self._pos]
            self._street = self._street._crossroad.leaving
            self._street[self._v - distance] = self
        else:
            self._street._move(self, self._pos + self._v)
