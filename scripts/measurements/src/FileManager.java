import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;


/**
 * This class allows the user to work with the data files.
 *
 * @author Dominic Parga Cacheiro
 */
public class FileManager {
    private HashMap<File, Scanner> activeScanners;
    private HashMap<Scanner, File> activeScannersReverse;

    public FileManager() {
        activeScanners        = new HashMap<>();
        activeScannersReverse = new HashMap<>();
    }

    /**
     * Write the given String to a txt-file with name fileName.
     *
     * @param pathname
     *            Name of the data file's path
     * @param data
     *            String of data
     * @param overwrite
     *            YES, if the file with the given name already exists and shall
     *            be overwritten
     */
    public void writeDataToFile(String path, String filename, String data, boolean overwrite) {

        File dirs = new File(path);
        if (!dirs.exists()) { dirs.mkdirs(); }

        try {
            PrintWriter outputStream = new PrintWriter(
                    new FileOutputStream(path + System.getProperty("file.separator") + filename, !overwrite));

            outputStream.print(data);

            outputStream.close();
        } catch (IOException e) {
            System.err.println("Couldn't open/create file " + path + System.getProperty("file.separator") + filename);
        }
    }

    public Scanner openScanner(File file) {
        if (!activeScanners.containsKey(file)) {
            Scanner scanner = null;

            try {
                // init scanner to read out the file
                scanner = new Scanner(file);
                scanner.useDelimiter(";");

                activeScanners.put(file, scanner);
                activeScannersReverse.put(scanner, file);
            } catch (IOException e) {
                activeScanners.remove(file);
                activeScannersReverse.remove(scanner);
                System.err.println("Couldn't open/read file " + file.getName());
            }

            return scanner;
        }
        return null;
    }

    public void closeScanner(File file) {
        Scanner scanner = activeScanners.remove(file);
        activeScannersReverse.remove(scanner);
        scanner.close();
    }

    public void closeScanner(Scanner scanner) {
        File file = activeScannersReverse.remove(scanner);
        activeScanners.remove(file);
        scanner.close();
    }
}
