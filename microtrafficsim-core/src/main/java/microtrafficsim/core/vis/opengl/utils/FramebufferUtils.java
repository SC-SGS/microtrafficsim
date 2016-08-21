package microtrafficsim.core.vis.opengl.utils;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * Utilities for frame-buffers.
 *
 * @author Maximilian Luz
 */
public class FramebufferUtils {
    private FramebufferUtils() {}

    /**
     * Copies the color-attachment of the primary frame-buffer to a {@code BufferedImage}.
     *
     * @param drawable the drawable used for rendering.
     * @return the color-attachment of the primary frame-buffer as {@code BufferedImage}.
     */
    public static BufferedImage toBufferedImage(GLAutoDrawable drawable) {
        int width  = drawable.getSurfaceWidth();
        int height = drawable.getSurfaceHeight();

        BufferedImage screenshot = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteBuffer    buffer     = ByteBuffer.allocate(width * height * 4);

        drawable.getGL().glReadPixels(0, 0, width, height, GL.GL_RGBA, GL.GL_BYTE, buffer);

        for (int y = 1; y <= height; y++) {
            for (int x = 0; x < width; x++) {
                screenshot.setRGB(x, height - y, (buffer.get() * 2) << 16 | (buffer.get() * 2) << 8 | (buffer.get() * 2));
                buffer.get();
            }
        }

        return screenshot;
    }

    /**
     * Writes the color-attachment of the primary frame-buffer to the specified image-file using the specified format.
     *
     * @param drawable the drawable used for rendering.
     * @param format   the image-format in which the file should be written.
     * @param file     the file to write the color-attachment to.
     * @throws IOException if an exception accessing the file occurs.
     */
    public static void writeFramebuffer(GLAutoDrawable drawable, String format, File file) throws IOException {
        BufferedImage screenshot = toBufferedImage(drawable);
        ImageIO.write(screenshot, format, file);
    }
}
