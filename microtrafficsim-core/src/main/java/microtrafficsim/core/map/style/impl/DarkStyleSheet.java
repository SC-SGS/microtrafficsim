package microtrafficsim.core.map.style.impl;

import microtrafficsim.core.map.style.BasicStyleSheet;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.utils.logging.EasyMarkableLogger;


/**
 * A dark style-sheet for the MapViewer. It uses the colors defined in {@link LightStyleSheet} inverted.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class DarkStyleSheet extends BasicStyleSheet {

    private final EasyMarkableLogger logger = new EasyMarkableLogger(DarkStyleSheet.class);

//    @Override
//    public Color getDefaultVehicleColor() {
//        return null;
//    }
//
//    @Override
//    public Color getColor(AbstractVehicle vehicle) {
//        Color[] redToGreen = new Color[] {
//                Color.fromRGB(0xD61E24),
//                Color.fromRGB(0xED7834),
//                Color.fromRGB(0xFFC526),
//                Color.fromRGB(0x8FA63F),
//                Color.fromRGB(0x0776E3)
//        };
//        Color[] orangeToBlue = new Color[] {
//                Color.fromRGB(0xE30707),
//                Color.fromRGB(0xE33E07),
//                Color.fromRGB(0xEB7F00),
//                Color.fromRGB(0x225378),
//                Color.fromRGB(0x225378),
//                Color.fromRGB(0x225378)
//        };
//        Color[] redToBlue = new Color[] {
//                Color.fromRGB(0xE33E07),
//                Color.fromRGB(0xFFC806),
//                Color.fromRGB(0x009489),
//                Color.fromRGB(0x167DA3),
//                Color.fromRGB(0x1D3578),
//                Color.fromRGB(0x162352)
//        };
//        Color[] darkRedToBrown = new Color[] {
//                Color.fromRGB(0x8C3730),
//                Color.fromRGB(0xBF6E3F),
//                Color.fromRGB(0xD98943),
//                Color.fromRGB(0xF2A649),
//                Color.fromRGB(0x594F3C),
//                Color.fromRGB(0x594F3C)
//        };
//        return redToBlue[vehicle];
//    }

    @Override
    public Color getBackgroundColor() {
        return Color.inverseFromRGB(0xFFFFFF);
    }

    @Override
    protected boolean isInlineActive(String streetFeatureName, int zoom) {

        switch (streetFeatureName) {
            case "motorway":
            case "trunk":
            case "primary":
            case "secondary":
            case "tertiary":
            case "unclassified":
            case "residential":
            case "living_street":
                return zoom >= 16;
            case "road":
                return zoom >= 17;
            default: // should be never reached
                logger.info("It is not defined whether " + streetFeatureName + " has an active inline.");
                return false;
        }
    }

    @Override
    protected Color getColorOutline(String streetFeatureName) {

        switch (streetFeatureName) {
            case "motorway":
                return Color.inverseFromRGB(0xFF6F69);
            case "trunk":
                return Color.inverseFromRGB(0x659118);
            case "primary":
                return Color.inverseFromRGB(0xDB9E36);
            case "secondary":
                return Color.inverseFromRGB(0x105B63);
            case "tertiary":
                return Color.inverseFrom(177, 98, 134);
            case "unclassified":
                return Color.inverseFrom(104, 157, 106);
            case "residential":
                return Color.inverseFrom(124, 111, 100);
            case "road":
                return Color.inverseFrom(146, 131, 116);
            case "living_street":
                return Color.inverseFrom(146, 131, 116);
            default: // should be never reached
                logger.info("The outline color of " + streetFeatureName + " is not defined.");
                return getBackgroundColor();
        }
    }

    @Override
    protected Color getColorInline(String streetFeatureName) {
        return Color.inverseFromRGB(0xFDFDFD);
    }

    @Override
    protected float getLineWidthOutline(String streetFeatureName, int zoom) {

        switch (streetFeatureName) {
            case "motorway":
                if (zoom >= 16)
                    return 60.f;
                if (zoom >= 14)
                    return 80.f;
                return 95.f;
            case "trunk":
                if (zoom >= 16)
                    return 60.f;
                if (zoom >= 14)
                    return 80.f;
                return 95.f;
            case "primary":
                if (zoom >= 16)
                    return 50.f;
                if (zoom >= 14)
                    return 70.f;
                return 85.f;
            case "secondary":
                if (zoom >= 16)
                    return 50.f;
                if (zoom >= 14)
                    return 70.f;
                return 85.f;
            case "tertiary":
                if (zoom >= 16)
                    return 50.f;
                if (zoom >= 14)
                    return 70.f;
                return 85.f;
            case "unclassified":
                if (zoom >= 16)
                    return 40.f;
                if (zoom >= 14)
                    return 60.f;
                return 75.f;
            case "residential":
                if (zoom >= 16)
                    return 40.f;
                if (zoom >= 14)
                    return 60.f;
                return 75.f;
            case "road":
                if (zoom >= 17)
                    return 40.f;
                if (zoom >= 14)
                    return 60.f;
                return 75.f;
            case "living_street":
                if (zoom >= 16)
                    return 32.f;
                if (zoom >= 14)
                    return 45.f;
                return 60.f;
            default: // should be never reached
                logger.info("The outline line width of " + streetFeatureName + " is not defined.");
                return 0;
        }
    }

    @Override
    protected float getLineWidthInline(String streetFeatureName, int zoom) {

        if (zoom >= 16) {
            switch (streetFeatureName) {
                case "motorway":
                    return 48.f;
                case "trunk":
                    return 48.f;
                case "primary":
                    return 40.f;
                case "secondary":
                    return 40.f;
                case "tertiary":
                    return 40.f;
                case "unclassified":
                    return 30.f;
                case "residential":
                    return 30.f;
                case "road":
                    if (zoom >= 17)
                        return 30.f;
                case "living_street":
                    return 24.f;
            }
        }
        logger.info("The inline line width of " + streetFeatureName + " is not defined.");
        return 0;
    }
}