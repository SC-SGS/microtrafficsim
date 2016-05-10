package microtrafficsim.core.vis.opengl.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import microtrafficsim.core.vis.errors.ResourceError;


/**
 * Utility class to load and store Texture Data.
 * 
 * @author Maximilian Luz
 */
public class TextureData2D {

	/**
	 * The width of the stored texture.
	 */
	public final int width;

	/**
	 * The width of the stored texture.
	 */
	public final int height;

	/**
	 * Buffer containing the pixel-data. Each integer corresponds to a Pixel,
	 * stored in the {@code ARGB} format, this corresponds to OpenGL's
	 * {@code GL_BGRA} format (since Java is big-endian).
	 * 
	 * <p>
	 * The pixel-data order equals the order required by OpenGL: "The first
	 * element corresponds to the lower left corner of the texture image.
	 * Subsequent elements progress left-to-right through the remaining texels
	 * in the lowest row of the texture image, and then in successively higher
	 * rows of the texture image. The final element corresponds to the upper
	 * right corner of the texture image." (OpenGL documentation,
	 * {@code glTexImage2D}).
	 * </p>
	 */
	public final IntBuffer data;

	/**
	 * Creates a new {@code TextureData2D} with the given parameters.
	 * 
	 * @param width
	 *            the textures width.
	 * @param height
	 *            the textures height.
	 * @param data
	 *            the textures pixel-data, see {@link TextureData2D#data}.
	 */
	public TextureData2D(int width, int height, IntBuffer data) {
		this.width = width;
		this.height = height;
		this.data = data;
	}
	

	/**
	 * Loads the Texture Data from the given input stream.
	 * 
	 * @param in
	 *            the {@code InputStream} from which the Texture Data should be
	 *            loaded.
	 * 
	 * @return the loaded Texture Data.
	 * 
	 * @throws IOException
	 */
	public static TextureData2D load(InputStream in) throws IOException {
		BufferedImage image = ImageIO.read(in);
		IntBuffer b = IntBuffer.allocate(image.getWidth() * image.getHeight());

		for (int y = image.getHeight() - 1; y >= 0; y--) {
			for (int x = 0; x < image.getWidth(); x++) {
				b.put(image.getRGB(x, y));
			}
		}

		b.rewind();

		return new TextureData2D(image.getWidth(), image.getHeight(), b);
	}

	/**
	 * Loads the Texture Data from the specified resource.
	 * 
	 * @param clazz
	 *            the class from which the resource should be loaded.
	 * @param resource
	 *            the path of the resource to load.
	 * 
	 * @return the loaded Texture Data.
	 * 
	 * @throws ResourceError
	 *             if an {@code IOException} occurred while accessing the
	 *             resource.
	 * 
	 * @see TextureData2D#load(InputStream)
	 */
	public static TextureData2D loadFromResource(Class<?> clazz, String resource) {
		try {
			return load(clazz.getResourceAsStream(resource));
		} catch (IOException e) {
			throw new ResourceError(resource, e);
		}
	}
}
