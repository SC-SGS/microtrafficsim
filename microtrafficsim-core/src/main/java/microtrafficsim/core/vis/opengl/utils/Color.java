package microtrafficsim.core.vis.opengl.utils;

import microtrafficsim.math.Vec3f;
import microtrafficsim.math.Vec4f;
import microtrafficsim.utils.hashing.FNVHashBuilder;


/**
 * RGBA-color described by floats, ranging from 0 to 1. All assignment and construction-methods will automatically
 * clamp the color.
 *
 * @author Maximilian Luz
 */
public class Color {
    public float r, g, b, a;


    /**
     * Constructs a new {@code Color} from the specified RGB float-values. The alpha-component is set to 1.
     *
     * @param r the red component as float, 0 to 1.
     * @param g the green component as float, 0 to 1.
     * @param b the blue component as float, 0 to 1.
     */
    public Color(float r, float g, float b) {
        this(r, g, b, 1.f);
    }

    /**
     * Constructs a new {@code Color} from the specified RGBA values.
     *
     * @param r the red component as float, 0 to 1.
     * @param g the green component as float, 0 to 1.
     * @param b the blue component as float, 0 to 1.
     * @param a the alpha component as float, 0 to 1.
     */
    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;

        clamp();
    }

    /**
     * Copy-constructs a {@code Color} from the specified one.
     *
     * @param other the color to copy.
     */
    public Color(Color other) {
        this(other.r, other.g, other.b, other.a);
    }


    /**
     * Constructs a new {@code Color} from the specified RGB byte-values.
     *
     * @param r the red component as (unsigned) byte.
     * @param g the green component as (unsigned) byte.
     * @param b the blue component as (unsigned) byte.
     * @return the created {@code Color}.
     */
    public static Color from(byte r, byte g, byte b) {
        return new Color((r & 0xFF) / 255.f, (g & 0xFF) / 255.f, (b & 0xFF) / 255.f);
    }

    /**
     * Constructs a new {@code Color} from the specified RGBA byte-values.
     *
     * @param r the red component as (unsigned) byte.
     * @param g the green component as (unsigned) byte.
     * @param b the blue component as (unsigned) byte.
     * @param a the alpha component as (unsigned) byte.
     * @return the created {@code Color}.
     */
    public static Color from(byte r, byte g, byte b, byte a) {
        return new Color((r & 0xFF) / 255.f, (g & 0xFF) / 255.f, (b & 0xFF) / 255.f, (a & 0xFF) / 255.f);
    }

    /**
     * Constructs a new {@code Color} from the specified RGB int-values.
     *
     * @param r the red component as int, ranging from 0 to 255.
     * @param g the green component as int, ranging from 0 to 255.
     * @param b the blue component as int, ranging from 0 to 255.
     * @return the created {@code Color}.
     */
    public static Color from(int r, int g, int b) {
        return new Color(r / 255.f, g / 255.f, b / 255.f);
    }

    /**
     * Constructs a new {@code Color} from the specified RGBA int-values.
     *
     * @param r the red component as int, ranging from 0 to 255.
     * @param g the green component as int, ranging from 0 to 255.
     * @param b the blue component as int, ranging from 0 to 255.
     * @param a the alpha component as int, ranging from 0 to 255.
     * @return the created {@code Color}.
     */
    public static Color from(int r, int g, int b, int a) {
        return new Color(r / 255.f, g / 255.f, b / 255.f, a / 255.f);
    }

    /**
     * Constructs a new {@code Color} from the specified RGBA values.
     *
     * @param r the red component as float, 0 to 1.
     * @param g the green component as float, 0 to 1.
     * @param b the blue component as float, 0 to 1.
     * @param a the alpha component as float, 0 to 1.
     * @return the created {@code Color}.
     */
    public static Color from(float r, float g, float b, float a) {
        return new Color(r, g, b, a);
    }

    /**
     * Constructs a new {@code Color} from the specified RGB value.
     *
     * @param rgb the integer-value describing the RGB value using the lower three bytes, i.e. as described by HTML
     *            hexadecimal color-codes (in hexadecimal notation: {@code 0xRRGGBB}).
     * @return the created {@code Color}.
     */
    public static Color fromRGB(int rgb) {
        return new Color(((rgb >> 16) & 0xFF) / 255.f, ((rgb >> 8) & 0xFF) / 255.f, (rgb & 0xFF) / 255.f);
    }

    /**
     * Constructs a new {@code Color} from the specified RGBA value.
     *
     * @param rgba the integer-value describing the RGB value using the upper three bytes and the alpha-value as the
     *             lower byte (in hexadecimal notation: {@code 0xRRGGBBAA}).
     * @return the created {@code Color}.
     */
    public static Color fromRGBA(int rgba) {
        return new Color(((rgba >> 24) & 0xFF) / 255.f,
                         ((rgba >> 16) & 0xFF) / 255.f,
                         ((rgba >> 8) & 0xFF) / 255.f,
                         (rgba & 0xFF) / 255.f);
    }

    /**
     * Constructs a new {@code Color} from the specified ABGR value.
     *
     * @param abgr the integer-value describing the RGB value using the upper three bytes and the alpha-value as the
     *             lower byte (in hexadecimal notation: {@code 0xAABBGGRR}).
     * @return the created {@code Color}.
     */
    public static Color fromABGR(int abgr) {
        return new Color((abgr & 0xFF) / 255.f,
                         ((abgr >> 8) & 0xFF) / 255.f,
                         ((abgr >> 16) & 0xFF) / 255.f,
                         ((abgr >> 24) & 0xFF) / 255.f);
    }

    /**
     * Constructs a new {@code Color} by decoding the given String as RGB integer-value.
     *
     * @param s the string to decode as color.
     * @return the created {@code Color}.
     */
    public static Color fromRGB(String s) {
        return fromRGB(Integer.decode(s));
    }

    /**
     * Constructs a new {@code Color} by decoding the given String as RGBA integer-value.
     *
     * @param s the string to decode as color.
     * @return the created {@code Color}.
     */
    public static Color fromRGBA(String s) {
        return fromRGBA(Integer.decode(s));
    }

    /**
     * Constructs a new {@code Color} from the values stored in the given three-component vector.
     * {@code x}, {@code y} and {@code z} will be interpreted as {@code r}, {@code g} and {@code b} respectively, the
     * alpha-component will be set to one.
     *
     * @param v the vector to convert to a {@code Color}.
     * @return the converted {@code Color}.
     */
    public static Color from(Vec3f v) {
        return new Color(v.x, v.y, v.z);
    }

    /**
     * Constructs a new {@code Color} from the values stored in the given three-component vector.
     * {@code x}, {@code y}, {@code z} and {@code w} will be interpreted as {@code r}, {@code g}, {@code b} and
     * {@code a} respectively, the alpha-component will be set to one.
     *
     * @param v the vector to convert to a {@code Color}.
     * @return the converted {@code Color}.
     */
    public static Color from(Vec4f v) {
        return new Color(v.x, v.y, v.z, v.w);
    }


    /**
     * Copy-assign values given by the specified {@code Color} to this instance.
     *
     * @param other the {@code Color} to copy.
     * @return this {@code Color}.
     */
    public Color set(Color other) {
        this.r = other.r;
        this.g = other.g;
        this.b = other.b;
        this.a = other.a;

        return this;
    }

    /**
     * Set the color-values to the specified ones.
     *
     * @param r the new red-component value.
     * @param g the new green-component value.
     * @param b the new blue-component value.
     * @param a the new alpha-component value.
     * @return this {@code Color}
     */
    public Color set(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;

        return clamp();
    }

    /**
     * Set the color-values to the specified ones.
     *
     * @param r the new red-component value as unsigned byte (ranging from 0 to 255).
     * @param g the new green-component value as unsigned byte (ranging from 0 to 255).
     * @param b the new blue-component value as unsigned byte (ranging from 0 to 255).
     * @param a the new alpha-component value as unsigned byte (ranging from 0 to 255).
     * @return this {@code Color}.
     */
    public Color set(byte r, byte g, byte b, byte a) {
        this.r = (r & 0xFF) / 255.f;
        this.g = (g & 0xFF) / 255.f;
        this.b = (b & 0xFF) / 255.f;
        this.a = (a & 0xFF) / 255.f;

        return this;
    }

    /**
     * Set the color-values to the specified ones.
     *
     * @param r the new red-component value as int (ranging from 0 to 255).
     * @param g the new green-component value as int (ranging from 0 to 255).
     * @param b the new blue-component value as int (ranging from 0 to 255).
     * @param a the new alpha-component value as int (ranging from 0 to 255).
     * @return this {@code Color}.
     */
    public Color set(int r, int g, int b, int a) {
        this.r = r / 255.f;
        this.g = g / 255.f;
        this.b = b / 255.f;
        this.a = a / 255.f;

        return clamp();
    }

    /**
     * Set the color-values to the specified ones.
     *
     * @param rgb the integer-value describing the RGB value using the lower three bytes, i.e. as described by HTML
     *            hexadecimal color-codes (in hexadecimal notation: {@code 0xRRGGBB}).
     * @param a   the new alpha-component value.
     * @return this {@code Color}.
     */
    public Color set(int rgb, float a) {
        this.r = ((rgb >> 24) & 0xFF) / 255.f;
        this.g = ((rgb >> 16) & 0xFF) / 255.f;
        this.b = ((rgb >> 8) & 0xFF) / 255.f;
        this.a = a;

        if (this.a < 0.f)
            this.a = 0.f;
        else if (this.a > 1.f)
            this.a = 1.f;

        return this;
    }

    /**
     * Set the color-values to the specified ones.
     *
     * @param rgba the integer-value describing the RGB value using the upper three bytes and the alpha-value as the
     *             lower byte (in hexadecimal notation: {@code 0xRRGGBBAA}).
     * @return this {@code Color}.
     */
    public Color set(int rgba) {
        this.r = ((rgba >> 24) & 0xFF) / 255.f;
        this.g = ((rgba >> 16) & 0xFF) / 255.f;
        this.b = ((rgba >> 8) & 0xFF) / 255.f;
        this.a = (rgba & 0xFF) / 255.f;

        return this;
    }

    /**
     * Set the color-values by decoding the specified string as color-integer.
     *
     * @param rgb the string to decode as color.
     * @param a   the new alpha-component value.
     * @return this {@code Color}.
     */
    public Color set(String rgb, float a) {
        return set(Integer.decode(rgb), a);
    }

    /**
     * Set the color-values by decoding the specified string as color-integer.
     *
     * @param rgba the string to decode as color.
     * @return this {@code Color}.
     */
    public Color set(String rgba) {
        return set(Integer.decode(rgba));
    }

    /**
     * Set the color-values by copying the specified three-component vector.
     *
     * @param rgb the vector to copy, {@code x}, {@code y} and {@code z} representing {@code r}, {@code g} and
     *            {@code b} respectively.
     * @param a   the new alpha-component value.
     * @return this {@code Color}.
     */
    public Color set(Vec3f rgb, float a) {
        this.r = rgb.x;
        this.g = rgb.y;
        this.b = rgb.z;
        this.a = a;

        return clamp();
    }

    /**
     * Set the color-values by copying the specified three-component vector.
     *
     * @param rgba the vector to copy, {@code x}, {@code y}, {@code z} and {@code w} representing {@code r}, {@code g},
     *             {@code b} and {@code a} respectively.
     * @return this {@code Color}.
     */
    public Color set(Vec4f rgba) {
        this.r = rgba.x;
        this.g = rgba.y;
        this.b = rgba.z;
        this.a = rgba.w;

        return clamp();
    }


    /**
     * Return the color encoded as RGBA integer value.
     *
     * @return this color as RGBA integer value.
     */
    public int toIntRGBA() {
        int r = ((int) (this.r * 255)) & 0xFF;
        int g = ((int) (this.g * 255)) & 0xFF;
        int b = ((int) (this.b * 255)) & 0xFF;
        int a = ((int) (this.a * 255)) & 0xFF;

        return r << 24 | g << 16 | b << 8 | a;
    }

    /**
     * Return the color encoded as ABGR integer value.
     *
     * @return this color as ABGR integer value.
     */
    public int toIntABGR() {
        int r = ((int) (this.r * 255)) & 0xFF;
        int g = ((int) (this.g * 255)) & 0xFF;
        int b = ((int) (this.b * 255)) & 0xFF;
        int a = ((int) (this.a * 255)) & 0xFF;

        return a << 24 | b << 16 | g << 8 | r;
    }

    /**
     * Return this color converted to an HTML hex-color string.
     *
     * @return this color as hex-string.
     */
    public String toHexStringRGBA() {
        String hex = Integer.toHexString(toIntRGBA()).toUpperCase();

        while (hex.length() < 8)
            hex = '0' + hex;

        return hex;
    }

    /**
     * Return this color converted to a four-component vector.
     *
     * @return this color as four-component vector.
     */
    public Vec4f toVec4f() {
        return new Vec4f(r, g, b, a);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " { #" + toHexStringRGBA() + " }";
    }


    /**
     * Clamps this color to the normalized space (0 to 1) and returns it.
     *
     * @return this color after clamping it.
     */
    public Color clamp() {
        if (r < 0.f)
            r = 0.f;
        else if (r > 1.f)
            r = 1.f;

        if (g < 0.f)
            g = 0.f;
        else if (g > 1.f)
            g = 1.f;

        if (b < 0.f)
            b = 0.f;
        else if (b > 1.f)
            b = 1.f;

        if (a < 0.f)
            a = 0.f;
        else if (a > 1.f)
            a = 1.f;

        return this;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Color)) return false;

        Color other = (Color) obj;
        return this.r == other.r
                && this.g == other.g
                && this.b == other.b
                && this.a == other.a;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(r)
                .add(g)
                .add(b)
                .add(a)
                .getHash();
    }
}
