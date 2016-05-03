package math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.math.HaversineDistanceCalculator;


/**
 * Test for {@link HaversineDistanceCalculator#getDistance(Coordinate, Coordinate)}.
 * 
 * <p>
 * 	Uses randomly chosen coordinates validated against pythons 'haversine' package.
 * </p>
 * 
 * @author Maximilian Luz
 */
public class HaversineDistanceCalculatorTest {
	
	/**
	 * Maximum accepted relative error for the tiny-distance test.
	 */
	public static final double TEST_ERROR_TINY   = 0.0175;
	
	/**
	 * Maximum accepted relative error for the small-distance test.
	 */
	public static final double TEST_ERROR_SMALL  = 0.000_9;
	
	/**
	 * Maximum accepted relative error for the medium-distance and special-value test.
	 */
	public static final double TEST_ERROR_MEDIUM = 0.000_000_95;
	
	/**
	 * Maximum accepted relative error for the large-distance test.
	 */
	public static final double TEST_ERROR_LARGE  = 0.000_000_175;
	
	
	
	@Test
	public void tinyDistanceTest() {
		testTiny(new Coordinate( 89.9415283795f, 109.7875355590f), new Coordinate( 89.9415338314f, 109.8594900320f), 8.18726257704f);
		testTiny(new Coordinate( 77.3830318267f, -99.6133859133f), new Coordinate( 77.3830152360f, -99.6133142808f), 2.53581603076f);
		testTiny(new Coordinate(-89.9704685383f, -49.9835735805f), new Coordinate(-89.9705461336f, -49.9363734596f), 9.04126810485f);
	}

	@Test
	public void smallDistanceTest() {
		testSmall(new Coordinate(-88.4984791520f, -10.0154931803f), new Coordinate(-88.4976165803f, -10.0185151131f), 96.3171424604f);
		testSmall(new Coordinate(-22.7537225095f, 128.7989056000f), new Coordinate(-22.7532004733f, 128.7986889730f), 62.1528014133f);
		testSmall(new Coordinate(-89.1264976224f, 173.3994391860f), new Coordinate(-89.1262069285f, 173.4255448040f), 54.8070118852f);
	}

	@Test
	public void mediumDistanceTest() {
		testMedium(new Coordinate(-85.5472820916f, -41.5173553867f), new Coordinate(-87.3000142268f, 174.7355772440f), 758259.320052f);
		testMedium(new Coordinate( 88.1227964648f,  68.1695242816f), new Coordinate( 86.0508824430f, 143.9006708240f), 437207.315526f);
		testMedium(new Coordinate(-80.6328602117f, -86.8554249613f), new Coordinate(-76.9611456019f, -98.3796353781f), 476269.642714f);
	}

	@Test
	public void largeDistanceTest() {
		testLarge(new Coordinate( 84.9070630931f, -107.50983647300f), new Coordinate( 33.7680211039f, -145.62378544f),    5814115.3273f);
		testLarge(new Coordinate(-11.0070857856f,  166.94540132700f), new Coordinate(-28.6281817616f,   14.3016381006f), 14717574.4805f);
		testLarge(new Coordinate( 82.1689155817f,    4.30831453137f), new Coordinate( 80.8273511993f, -125.473361061f),   1712102.2492f);
	}

	@Test
	public void specialValuesTest() {
		testMedium(new Coordinate( 36.7706f,   0.0000f), new Coordinate(  0.0000f, 326.8710f),  5322827.78638f);
		testMedium(new Coordinate(  0.0000f, 352.2007f), new Coordinate( 12.8311f,   0.0000f),  1665864.25705f);
		testMedium(new Coordinate( 90.0000f,   0.0000f), new Coordinate(-90.0000f,   0.0000f), 20015086.79600f);
		testMedium(new Coordinate(107.8402f,  82.3843f), new Coordinate(107.8402f,  82.3843f),        0.00000f);
		testMedium(new Coordinate(307.3582f, 160.6513f), new Coordinate(112.6830f,  62.8048f), 14960385.93980f);
	}

	@Test
	public void zeroDistanceTest() {
		testExact(new Coordinate( 33.8921262476f,  13.0502576083f), new Coordinate( 33.8921262476f,  13.0502576083f), 0.0f);
		testExact(new Coordinate( 10.7292734782f, -91.9645526538f), new Coordinate( 10.7292734782f, -91.9645526538f), 0.0f);
		testExact(new Coordinate(-69.7723269397f, 135.7376675200f), new Coordinate(-69.7723269397f, 135.7376675200f), 0.0f);
	}
	
	
	private void testExact(Coordinate a, Coordinate b, double distance) {
		test(a, b, distance, 0);
	}
	
	private void testTiny(Coordinate a, Coordinate b, double distance) {
		test(a, b, distance, TEST_ERROR_TINY);
	}
	
	private void testSmall(Coordinate a, Coordinate b, double distance) {
		test(a, b, distance, TEST_ERROR_SMALL);
	}
	
	private void testMedium(Coordinate a, Coordinate b, double distance) {
		test(a, b, distance, TEST_ERROR_MEDIUM);
	}
	
	private void testLarge(Coordinate a, Coordinate b, double distance) {
		test(a, b, distance, TEST_ERROR_LARGE);
	}
	
	private void test(Coordinate a, Coordinate b, double distance, double error) {
		assertEquals(HaversineDistanceCalculator.getDistance(a, b), distance, distance * error);
	}
}
