package microtrafficsim.examples.mapviewer.tilebased;

import microtrafficsim.core.map.TileFeatureProvider;
import microtrafficsim.core.map.layers.TileLayerDefinition;
import microtrafficsim.core.map.layers.TileLayerSource;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.context.RenderContext.UncaughtExceptionHandler;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.opengl.shader.ShaderCompileError;
import microtrafficsim.core.vis.opengl.shader.ShaderLinkError;
import microtrafficsim.core.vis.opengl.utils.FramebufferUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;


public class Utils {
	
	public static void setFeatureProvider(Set<TileLayerDefinition> layers, TileFeatureProvider provider) {
		for (TileLayerDefinition def : layers) {
			TileLayerSource src = def.getSource();

			if (src instanceof FeatureTileLayerSource)
				((FeatureTileLayerSource) src).setFeatureProvider(provider);
		}
	}
	
	public static void asyncScreenshot(RenderContext context) {
		new Thread(() -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileFilter() {

				@Override
				public String getDescription() {
					return ".png";
				}

				@Override
				public boolean accept(File f) {
					if (f.isDirectory()) return true;

					String extension = null;

					String s = f.getName();
					int i = s.lastIndexOf('.');

					if (i > 0 &&  i < s.length() - 1)
						extension = s.substring(i+1).toLowerCase();

					if (extension == null) return false;

					switch (extension) {
					case "png":		return true;
					default:		return false;
					}
				}
			});

			int action = chooser.showSaveDialog(null);
		
			if (action == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (file.exists()) file.delete();

				try {
					file.createNewFile();
				} catch (IOException e) {
					return;
				}

				context.addTask(c -> {
					try {
						FramebufferUtils.writeFramebuffer(c.getDrawable(), "png", file);
					} catch (IOException e) {
						/* ignore if we can't write to the file and clean up */
						if (file.exists())
							file.delete();
					}
				});
			}
		}).start();;
	}
	
	public static class DebugExceptionHandler implements UncaughtExceptionHandler {
	
		@Override
		public void uncaughtException(RenderContext context, Throwable exception) {
			if (exception instanceof ShaderCompileError)
				exceptionPrintf(System.err, (ShaderCompileError) exception);
			else if (exception instanceof ShaderLinkError)
				exceptionPrintf(System.err, (ShaderLinkError) exception);
			else
				exception.printStackTrace();
		
			// XXX: clean exit strategy?
			Runtime.getRuntime().halt(1);
		}
		
		private void exceptionPrintf(PrintStream out, ShaderCompileError error) {
			out.println(error.toString());
			out.println("-- LOG -------------------------------------------------------------------------");
			out.println(error.getShaderInfoLog());
			out.println("-- SOURCE ----------------------------------------------------------------------");
			out.println(error.getShaderSource());
			out.println("-- STACK TRACE -----------------------------------------------------------------");
			error.printStackTrace(out);
		}
		
		private void exceptionPrintf(PrintStream out, ShaderLinkError error) {
			out.println(error.toString());
			out.println("-- LOG -------------------------------------------------------------------------");
			out.println(error.getProgramInfoLog());
			out.println("-- STACK TRACE -----------------------------------------------------------------");
			error.printStackTrace(out);
		}
	}
}
