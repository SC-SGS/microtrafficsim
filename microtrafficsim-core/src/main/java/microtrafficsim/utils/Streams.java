package microtrafficsim.utils;

import java.io.*;
import java.util.ArrayList;


/**
 * Utilities for Java Streams.
 *
 * @author Maximilian Luz
 */
public class Streams {
    private Streams() {}


    /**
     * Copies the contents of the specified stream line-wise to an array, including newline-characters and returns the
     * array.
     *
     * @param in the stream from which to read the lines.
     * @return the stream separated to lines, including newline-characters.
     * @throws IOException if an error ocurres while reading from the stream.
     */
    public static String[] toStringArrayEOL(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        ArrayList<String> lines = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line + "\n");
        }

        return lines.toArray(new String[lines.size()]);
    }


    /**
     * Write the contents of the given stream to a temporary file.
     *
     * @param prefix    the prefix of the temporary file.
     * @param suffix    the suffix of the temporary file (may be {@code null}).
     * @param resource  the stream to write to the temporary file.
     * @param delOnExit set this to {@code true} if this file should be deleted
     *                  on exit.
     * @throws IOException when an {@code IOException} occurs while reading from
     *                     the given {@code InputStream} or writing to the temporary file.
     * @return the temporary {@code File} containing everything from {@code
     * resource}.
     * @see File#createTempFile(String, String)
     */
    public static File toTemporaryFile(String prefix, String suffix, InputStream resource, boolean delOnExit)
            throws IOException {
        return toTemporaryFile(prefix, suffix, resource, null, delOnExit);
    }

    /**
     * Write the contents of the given stream to a temporary file.
     *
     * @param prefix    the prefix of the temporary file.
     * @param suffix    the suffix of the temporary file (may be {@code null}).
     * @param resource  the stream to write to the temporary file.
     * @param directory the directory in which the file should be created (may be
     *                  {@code null}).
     * @param delOnExit set this to {@code true} if this file should be deleted
     *                  on exit.
     * @throws IOException when an {@code IOException} occurs while reading from
     *                     the given {@code InputStream} or writing to the temporary file.
     * @return the temporary {@code File} containing everything from {@code
     * resource}.
     * @see File#createTempFile(String, String, File)
     */
    public static File toTemporaryFile(String prefix, String suffix, InputStream resource, File directory,
                                       boolean delOnExit) throws IOException {

        File tmp = File.createTempFile(prefix, suffix, directory);
        if (delOnExit) tmp.deleteOnExit();

        FileOutputStream out = new FileOutputStream(tmp);

        byte[] buffer = new byte[4096];
        int nRead;
        while ((nRead = resource.read(buffer)) != -1) {
            out.write(buffer, 0, nRead);
        }

        out.close();

        return tmp;
    }
}
