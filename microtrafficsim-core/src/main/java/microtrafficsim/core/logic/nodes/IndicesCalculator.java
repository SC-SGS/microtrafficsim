package microtrafficsim.core.logic.nodes;


import microtrafficsim.core.logic.streets.DirectedEdge;

public class IndicesCalculator {
    public static final byte NO_MATCH = -42;

    /**
     * TODO no strings are used, but bytes
     *
     * <p>
     * This method assumes to get two strings containing sorted indices like
     * they appear in traffic logic ({@link DirectedEdge}) to calculate two
     * vehicles' priorities at a crossroad.
     *
     * <p>
     * Because the indices are sorted in an ascending order, there is only one
     * unique or no matching. Therefore, this algorithm just go through the
     * strings from left to right repeatedly and returns the first found
     * matching char. IT DOES NOT DETECT ANY DIFFERENCE FROM THIS, so you have
     * to know whether the indices are sorted.
     *
     * <p>
     * Runtime (circa):<br>
     * The runtime of worst case is O(n^2), but n is the count of all indices in
     * the given strings. The indices are limited by the number of streets per
     * node, so it's quite small.<br>
     * Let len1 := s1.length(); len2 := s2.length(); n := len1 + len2;<br>
     * INIT_TIME := n;<br>
     * WORST_CASE (= no match) := INIT_TIME + n(n-1)/2;<br>
     * BEST_CASE := INIT_TIME + 1 <br>
     * <br>
     * Example:<br>
     * len1 = 4 = len2; n = 8; INIT_TIME = 8; WORST_CASE = 8 + 28 = 36;
     * BEST_CASE := 9
     *
     *
     * @return The first index of this unique matching or
     * {@link IndicesCalculator}.NO_MATCH, if there is no matching.
     */
    public static byte leftmostIndexInMatching(byte origin1, byte destination1, byte origin2, byte destination2,
                                               byte supremum) {
        byte[] s1 = getIndices(origin1, destination1, supremum);
        byte[] s2 = getIndices(origin2, destination2, supremum);
        int len1  = s1.length;
        int len2  = s2.length;
        int n     = len1 + len2;
        // set two arrays for comparison
        byte[] c1 = new byte[n];
        byte[] c2 = new byte[n];
        for (int i = 0; i < n; i++)
            if (i < len1) {
                c1[i] = s1[i];
                c2[i] = NO_MATCH;
            } else {    // i = len2 + len1; i < n
                c1[i] = NO_MATCH;
                c2[i] = s2[i - len1];
            }
        // compare
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n - i; j++)
                if (c1[j] == c2[j + i])
                    if (c1[j] != NO_MATCH) return c1[j];
        }

        return NO_MATCH;
    }

    private static byte[] getIndices(byte origin, byte destination, byte supremum) {
        int delta = destination - origin;
        if (delta < 0) { delta = supremum + delta; }
        byte[] indices         = new byte[delta + 1];

        byte index      = origin;
        int  arrayIndex = 0;
        while (index != destination) {
            indices[arrayIndex++] = index;
            index                 = (byte) ((index + 1) % supremum);
        }
        indices[arrayIndex] = destination;

        return indices;
    }

    public static boolean areIndicesCrossing(byte origin1, byte destination1, byte origin2, byte destination2,
                                             byte supremum) {
        int i = origin1;

        // DFA: A out of {start1,end1}; B out of {start2,end2}
        // edge 0: A -A-> false
        // edge 1: A -B-> AB
        // edge 2: AB -B-> false
        // edge 3: AB -A-> true
        // => 2 states => boolean
        // if common destination: true should be returned => order of the if-statements is relevant
        boolean stateA            = true;
        int     irrelevantCounter = 0;
        while (irrelevantCounter++ < 2 * supremum) {
            i = (i + 1) % supremum;
            if (stateA) {                                                 // state A
                if (i == origin2 || i == destination2)                    // -B->
                    stateA = false;                                       // state AB
                else if (i == destination1)                               // -A->
                    return false;                                         // AA
            } else {                                                      // state AB
                if (i == destination1 || destination1 == destination2)    // -A->
                    return true;                                          // ABA
                else if (i == origin2 || i == destination2)               // -B->
                    return false;                                         // ABB
            }
        }
        return false;    // should never be reached if method parameters are correct
    }
}