package microtrafficsim.core.mapviewer.utils;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.context.exceptions.UncaughtExceptionHandler;
import microtrafficsim.core.vis.opengl.shader.ShaderCompileException;
import microtrafficsim.core.vis.opengl.shader.ShaderLinkException;
import microtrafficsim.core.vis.opengl.utils.FramebufferUtils;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;


/**
 * Utility class for the MapViewer.
 *
 * @author Maximilian Luz
 */
public class Utils {
    private static final Logger logger = new EasyMarkableLogger(Utils.class);

    /**
     * Create a screenshot of the context, asycnronously.
     *
     * @param context the context of which the screenshot should be created.
     */
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
                    int    i = s.lastIndexOf('.');

                    if (i > 0 && i < s.length() - 1) extension = s.substring(i + 1).toLowerCase();

                    if (extension == null) return false;

                    switch (extension) {
                        case "png": return true;
                        default:    return false;
                    }
                }
            });

            int action = chooser.showSaveDialog(null);

            if (action == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file.exists()) {
                    if (!file.delete()) {
                        logger.error("could not delete file" + file.getName());
                        return;
                    }
                }

                try {
                    if (!file.createNewFile()) {
                        logger.error("could not create file" + file.getName());
                        return;
                    }
                } catch (IOException e) {
                    logger.error("could not create file" + file.getName());
                    return;
                }

                context.addTask(c -> {
                    try {
                        FramebufferUtils.writeFramebuffer(c.getDrawable(), "png", file);
                    } catch (IOException e) {
                        /* ignore if we can't write to the file and clean up */
                        if (file.exists()) {
                            if (!file.delete())
                                logger.error("could not delete file" + file.getName());
                        }
                    }
                    return null;
                });
            }
        }).start();
    }

    /**
     * The debug-exception-handler used to handle all uncaught exceptions on the render-context
     */
    public static class DebugExceptionHandler implements UncaughtExceptionHandler {

        @Override
        public void uncaughtException(RenderContext context, Throwable exception) {
            if (exception instanceof ShaderCompileException)
                exceptionPrintf(System.err, (ShaderCompileException) exception);
            else if (exception instanceof ShaderLinkException)
                exceptionPrintf(System.err, (ShaderLinkException) exception);
            else
                exception.printStackTrace();

            // XXX: clean exit strategy?
            Runtime.getRuntime().halt(1);
        }

        private void exceptionPrintf(PrintStream out, ShaderCompileException error) {
            out.println(error.toString());
            out.println("-- LOG -------------------------------------------------------------------------");
            out.println(error.getShaderInfoLog());
            out.println("-- SOURCE ----------------------------------------------------------------------");
            out.println(error.getShaderSource());
            out.println("-- STACK TRACE -----------------------------------------------------------------");
            error.printStackTrace(out);
        }

        private void exceptionPrintf(PrintStream out, ShaderLinkException error) {
            out.println(error.toString());
            out.println("-- LOG -------------------------------------------------------------------------");
            out.println(error.getProgramInfoLog());
            out.println("-- STACK TRACE -----------------------------------------------------------------");
            error.printStackTrace(out);
        }
    }
}
