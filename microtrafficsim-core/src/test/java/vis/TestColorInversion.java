package vis;

import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.math.Vec3f;
import microtrafficsim.math.Vec4f;
import microtrafficsim.math.random.distributions.impl.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the inversion for {@value noOfColors} random generated {@link Color}s with precision of {@link #delta} =
 * {@value delta}.
 *
 * @author Dominic Parga Cacheiro
 */
public class TestColorInversion {

    private static final int noOfColors = 10000;
    private static final double delta = 0.0000001;

    private Random rdm = new Random(1234567890L);
    private Color[] baseColors, invertedColors;

    /*
    |=======|
    | setup |
    |=======|
    */
    @Before
    public void setup() {
        rdm.reset();
        baseColors = new Color[noOfColors];
        invertedColors = new Color[noOfColors];
    }

    @After
    public void testAll() {
        testCorrectInversion();
        testCorrectDoubleInversion();
    }

    private void testCorrectInversion() {
        for (int i = 0; i < baseColors.length; i++) {
            Color baseColor = baseColors[i];
            Color invertedColor = invertedColors[i];

            assertNotEquals(baseColor, invertedColor);
            // r
            assertEquals(1.f - baseColor.r, invertedColor.r, delta);
            assertEquals(baseColor.r, 1.f - invertedColor.r, delta);
            // g
            assertEquals(1.f - baseColor.g, invertedColor.g, delta);
            assertEquals(baseColor.g, 1.f - invertedColor.g, delta);
            // b
            assertEquals(1.f - baseColor.b, invertedColor.b, delta);
            assertEquals(baseColor.b, 1.f - invertedColor.b, delta);
            // a
            assertEquals(baseColor.a, invertedColor.a, 0);
            assertEquals(baseColor.a, invertedColor.a, 0);
        }
    }

    private void testCorrectDoubleInversion() {
        for (int i = 0; i < baseColors.length; i++) {
            Color baseColor     = baseColors[i];
            Color invertedColor = Color.inverse(invertedColors[i]);

            // r
            assertEquals(baseColor.r, invertedColor.r, delta);
            assertEquals(baseColor.r, invertedColor.r, delta);
            // g
            assertEquals(baseColor.g, invertedColor.g, delta);
            assertEquals(baseColor.g, invertedColor.g, delta);
            // b
            assertEquals(baseColor.b, invertedColor.b, delta);
            assertEquals(baseColor.b, invertedColor.b, delta);
            // a
            assertEquals(baseColor.a, invertedColor.a, 0);
            assertEquals(baseColor.a, invertedColor.a, 0);
        }
    }

    /*
    |=====================|
    | test cases: inverse |
    |=====================|
    */
    @Test
    public void invert() {
        for (int i = 0; i < noOfColors; i++) {
            baseColors[i] = new Color(rdm.nextFloat(), rdm.nextFloat(), rdm.nextFloat(), rdm.nextFloat());
            invertedColors[i] = new Color(baseColors[i]).invert();
        }
    }

    @Test
    public void inverse() {
        for (int i = 0; i < noOfColors; i++) {
            baseColors[i]     = new Color(rdm.nextFloat(), rdm.nextFloat(), rdm.nextFloat(), rdm.nextFloat());
            invertedColors[i] = Color.inverse(baseColors[i]);
        }
    }

    @Test
    public void inverseFromByteRGB() {
        for (int i = 0; i < invertedColors.length; i++) {
            byte[] rgb = rdm.nextByte(3);
            baseColors[i]     = Color.from(rgb[0], rgb[1], rgb[2]);
            invertedColors[i] = Color.inverseFrom(rgb[0], rgb[1], rgb[2]);
        }
    }

    @Test
    public void inverseFromByteRGBA() {
        for (int i = 0; i < invertedColors.length; i++) {
            byte[] rgba = rdm.nextByte(4);
            baseColors[i]     = Color.from(rgba[0], rgba[1], rgba[2], rgba[3]);
            invertedColors[i] = Color.inverseFrom(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
    }

    @Test
    public void inverseFromIntRGB() {
        for (int i = 0; i < invertedColors.length; i++) {
            int[] rgb = { rdm.nextInt(256), rdm.nextInt(256), rdm.nextInt(256) };
            baseColors[i]     = Color.from(rgb[0], rgb[1], rgb[2]);
            invertedColors[i] = Color.inverseFrom(rgb[0], rgb[1], rgb[2]);
        }
    }

    @Test
    public void inverseFromIntRGBA() {
        for (int i = 0; i < invertedColors.length; i++) {
            int[] rgba = { rdm.nextInt(256), rdm.nextInt(256), rdm.nextInt(256), rdm.nextInt(256) };
            baseColors[i]     = Color.from(rgba[0], rgba[1], rgba[2], rgba[3]);
            invertedColors[i] = Color.inverseFrom(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
    }

    @Test
    public void inverseFromFloatRGBA() {
        for (int i = 0; i < invertedColors.length; i++) {
            float[] rgba = { rdm.nextFloat(), rdm.nextFloat(), rdm.nextFloat(), rdm.nextFloat() };
            baseColors[i]     = Color.from(rgba[0], rgba[1], rgba[2], rgba[3]);
            invertedColors[i] = Color.inverseFrom(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
    }

    @Test
    public void inverseFromVec3f() {
        for (int i = 0; i < invertedColors.length; i++) {
            Vec3f v = new Vec3f(rdm.nextFloat(), rdm.nextFloat(), rdm.nextFloat());
            baseColors[i]     = Color.from(v);
            invertedColors[i] = Color.inverseFrom(v);
        }
    }

    @Test
    public void inverseFromVec4f() {
        for (int i = 0; i < invertedColors.length; i++) {
            Vec4f v = new Vec4f(rdm.nextFloat(), rdm.nextFloat(), rdm.nextFloat(), rdm.nextFloat());
            baseColors[i]     = Color.from(v);
            invertedColors[i] = Color.inverseFrom(v);
        }
    }

    /*
    |========================================|
    | test cases: inverse from one parameter |
    |========================================|
    */
    @Test
    public void inverseFromOneIntRGB() {
        for (int i = 0; i < invertedColors.length; i++) {
            int rgb = rdm.nextInt(0xFFFFFF);
            baseColors[i]     = Color.fromRGB(rgb);
            invertedColors[i] = Color.inverseFromRGB(rgb);
        }
    }

    @Test
    public void inverseFromOneIntRGBA() {
        for (int i = 0; i < invertedColors.length; i++) {
            int rgba = rdm.nextInt(0x10ABCDEF);
            baseColors[i]     = Color.fromRGBA(rgba);
            invertedColors[i] = Color.inverseFromRGBA(rgba);
        }
    }

    @Test
    public void inverseFromOneIntABGR() {
        for (int i = 0; i < invertedColors.length; i++) {
            int abgr = rdm.nextInt(0x10ABCDEF);
            baseColors[i]     = Color.fromABGR(abgr);
            invertedColors[i] = Color.inverseFromABGR(abgr);
        }
    }

    @Test
    public void inverseFromOneStringRGB() {
        for (int i = 0; i < invertedColors.length; i++) {
            String rgb = "" + rdm.nextInt(0xFFFFFF);
            baseColors[i]     = Color.fromRGB(rgb);
            invertedColors[i] = Color.inverseFromRGB(rgb);
        }
    }

    @Test
    public void inverseFromOneStringRGBA() {
        for (int i = 0; i < invertedColors.length; i++) {
            String rgba = "" + rdm.nextInt(0x10ABCDEF);
            baseColors[i]     = Color.fromRGBA(rgba);
            invertedColors[i] = Color.inverseFromRGBA(rgba);
        }
    }
}