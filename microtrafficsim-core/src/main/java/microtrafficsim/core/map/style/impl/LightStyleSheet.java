package microtrafficsim.core.map.style.impl;

import microtrafficsim.core.map.style.BasicStyleSheet;
import microtrafficsim.core.vis.opengl.utils.Color;


/**
 * A light style-sheet for the MapViewer.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class LightStyleSheet extends BasicStyleSheet {

    @Override
    public Color getBackgroundColor() {
        return Color.fromRGB(0xFFFFFF);
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
            case "living-street":
                return zoom >= 16;
            case "road":
                return zoom >= 17;
            default: // should be never reached
                return false;
        }
    }

    @Override
    protected Color getColorOutline(String streetFeatureName) {

        switch (streetFeatureName) {
            case "motorway":
                return Color.fromRGB(0xFF6F69);
            case "trunk":
                return Color.fromRGB(0x659118);
            case "primary":
                return Color.fromRGB(0xDB9E36);
            case "secondary":
                return Color.fromRGB(0x105B63);
            case "tertiary":
                return Color.from(177, 98, 134);
            case "unclassified":
                return Color.from(104, 157, 106);
            case "residential":
                return Color.from(124, 111, 100);
            case "road":
                return Color.from(146, 131, 116);
            case "living-street":
                return Color.from(146, 131, 116);
            default: // should be never reached
                return getBackgroundColor();
        }
    }

    @Override
    protected Color getColorInline(String streetFeatureName) {
        return Color.fromRGB(0xFDFDFD);
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
            case "living-street":
                if (zoom >= 16)
                    return 32.f;
                if (zoom >= 14)
                    return 45.f;
                return 60.f;
            default: // should be never reached
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
                case "living-street":
                    return 24.f;
            }
        }
        return 0;
    }
}