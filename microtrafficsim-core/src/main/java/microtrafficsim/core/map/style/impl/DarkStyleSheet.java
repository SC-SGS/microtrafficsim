package microtrafficsim.core.map.style.impl;

import microtrafficsim.core.vis.opengl.utils.Color;


/**
 * A dark style-sheet for the MapViewer. It uses the colors defined in {@link LightStyleSheet} inverted.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class DarkStyleSheet extends LightStyleSheet {

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
        return super.getBackgroundColor().invert();
    }

    @Override
    protected Color getColorOutline(String streetFeatureName) {
        return super.getColorOutline(streetFeatureName).invert();
    }

    @Override
    protected Color getColorInline(String streetFeatureName) {
        return super.getColorInline(streetFeatureName).invert();
    }
}