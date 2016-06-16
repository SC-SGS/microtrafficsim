package microtrafficsim.core.vis.opengl.utils;

import microtrafficsim.math.Vec3f;
import microtrafficsim.math.Vec4f;
import microtrafficsim.utils.hashing.FNVHashBuilder;


public class Color {
	public float r, g, b, a;
	
	
	public Color(float r, float g, float b) {
		this(r, g, b, 1.f);
	}
	
	public Color(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		
		clamp();
	}
	
	public Color(Color other) {
		this(other.r, other.g, other.b, other.a);
	}
		
	
	public static Color from(byte r, byte g, byte b) {
		return new Color((r & 0xFF) / 255.f, (g & 0xFF) / 255.f, (b & 0xFF) / 255.f);
	}
	
	public static Color from(byte r, byte g, byte b, byte a) {
		return new Color((r & 0xFF) / 255.f, (g & 0xFF) / 255.f, (b & 0xFF) / 255.f, (a & 0xFF) / 255.f);
	}
	
	public static Color from(int r, int g, int b) {
		return new Color(r / 255.f, g / 255.f, b / 255.f);
	}
	
	public static Color from(int r, int g, int b, int a) {
		return new Color(r / 255.f, g / 255.f, b / 255.f, a / 255.f);
	}

	public static Color from(float r, float g, float b, float a) {
		return new Color(r, g, b, a);
	}

	public static Color fromRGB(int rgb) {
		return new Color(
				((rgb >> 16) & 0xFF) / 255.f,
				((rgb >> 8) & 0xFF) / 255.f,
				(rgb & 0xFF) / 255.f
		);
	}
	
	public static Color fromRGBA(int rgba) {
		return new Color(
				((rgba >> 24) & 0xFF) / 255.f,
				((rgba >> 16) & 0xFF) / 255.f,
				((rgba >> 8) & 0xFF) / 255.f,
				(rgba & 0xFF) / 255.f
		);
	}
	
	public static Color fromABGR(int abgr) {
		return new Color(
				(abgr & 0xFF) / 255.f,
				((abgr >> 8) & 0xFF) / 255.f,
				((abgr >> 16) & 0xFF) / 255.f,
				((abgr >> 24) & 0xFF) / 255.f
		);
	}
	
	public static Color fromRGB(String s) {
		return fromRGB(Integer.decode(s) << 8 | 0xFF);
	}
	
	public static Color fromRGBA(String s) {
		return fromRGBA(Integer.decode(s));
	}
	
	public static Color from(Vec3f v) {
		return new Color(v.x, v.y, v.z);
	}
	
	public static Color from(Vec4f v) {
		return new Color(v.x, v.y, v.z, v.w);
	}
	
	
	public Color set(Color other) {
		this.r = other.r;
		this.g = other.g;
		this.b = other.b;
		this.a = other.a;
		
		return this;
	}
	
	public Color set(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		
		return clamp();
	}
	
	public Color set(byte r, byte g, byte b, byte a) {
		this.r = (r & 0xFF) / 255.f;
		this.g = (g & 0xFF) / 255.f;
		this.b = (b & 0xFF) / 255.f;
		this.a = (a & 0xFF) / 255.f;
		
		return this;
	}
	
	public Color set(int r, int g, int b, int a) {
		this.r = r / 255.f;
		this.g = g / 255.f;
		this.b = b / 255.f;
		this.a = a / 255.f;
		
		return clamp();
	}
	
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
	
	public Color set(int rgba) {
		this.r = ((rgba >> 24) & 0xFF) / 255.f;
		this.g = ((rgba >> 16) & 0xFF) / 255.f;
		this.b = ((rgba >> 8) & 0xFF) / 255.f;
		this.a = (rgba & 0xFF) / 255.f;
		
		return this;
	}
	
	public Color set(String rgb, float a) {
		return set(Integer.decode(rgb), a);
	}
	
	public Color set(String rgba) {
		return set(Integer.decode(rgba));
	}
	
	public Color set(Vec3f rgb, float a) {
		this.r = rgb.x;
		this.g = rgb.y;
		this.b = rgb.z;
		this.a = a;
		
		if (this.a < 0.f)
			this.a = 0.f;
		else if (this.a > 1.f)
			this.a = 1.f;
		
		return this;
	}
	
	public Color set(Vec4f rgba) {
		this.r = rgba.x;
		this.g = rgba.y;
		this.b = rgba.z;
		this.a = rgba.w;
		
		return clamp();
	}

	
	public int toIntRGBA() {
		int r = ((int) (this.r * 255)) & 0xFF;
		int g = ((int) (this.g * 255)) & 0xFF;
		int b = ((int) (this.b * 255)) & 0xFF;
		int a = ((int) (this.a * 255)) & 0xFF;
		
		return r << 24 | g << 16 | b << 8 | a;
	}
	
	public int toIntABGR() {
		int r = ((int) (this.r * 255)) & 0xFF;
		int g = ((int) (this.g * 255)) & 0xFF;
		int b = ((int) (this.b * 255)) & 0xFF;
		int a = ((int) (this.a * 255)) & 0xFF;
		
		return a << 24 | b << 16 | g << 8 | r;
	}
	
	public String toHexStringRGBA() {
		String hex = Integer.toHexString(toIntRGBA()).toUpperCase();
		
		while (hex.length() < 8)
			hex = '0' + hex;
		
		return hex;
	}
	
	public Vec4f toVec4f() {
		return new Vec4f(r, g, b, a);
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() + " { #" + toHexStringRGBA() + " }";
	}
	
	
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
		if (!(obj instanceof Color))
			return false;
		
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
