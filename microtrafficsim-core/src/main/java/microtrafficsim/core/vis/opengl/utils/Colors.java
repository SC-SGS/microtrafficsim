package microtrafficsim.core.vis.opengl.utils;


public class Colors {
    private Colors() {}


    public static Color black() {
        return Color.fromRGB(0x000000);
    }

    public static Color white() {
        return Color.fromRGB(0xFFFFFF);
    }

    public static Color red() {
        return Color.fromRGB(0xFF0000);
    }

    public static Color green() {
        return Color.fromRGB(0x00FF00);
    }

    public static Color blue() {
        return Color.fromRGB(0x0000FF);
    }
}
