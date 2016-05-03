package microtrafficsim.utils.fileio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This class allows the user to work with the data files.
 * 
 * @author Dominic Parga Cacheiro
 */
public class FileManager {
	public final String PATH_TO_DATA_FILES;
//	public final String ABSOLUTE_PATH_TO_DATA_FILES;

    public FileManager() {
		this("data");
    }

    /**
     * Creates a file manager that saves files inside the folder of path equal to PATH_TO_DATA_FILES.
     *
     * @param PATH_TO_DATA_FILES Path to data folder
     */
    public FileManager(String PATH_TO_DATA_FILES) {
        this.PATH_TO_DATA_FILES = PATH_TO_DATA_FILES;
//        ABSOLUTE_PATH_TO_DATA_FILES = System
//                .getProperty("user.dir")
//                + System.getProperty("file.separator")
//                + PATH_TO_DATA_FILES;
    }

	/**
	 * Write the given String to a txt-file with name fileName.
	 * 
	 * @param fileName
	 *            Name of the data file
	 * @param data
	 *            String of data
	 * @param overwrite
	 *            YES, if the file with the given name already exists and shall
	 *            be overwritten
	 */
	public void writeDataToFile(String fileName, String data,
			boolean overwrite) {

		File path = new File(PATH_TO_DATA_FILES);

		if (!path.exists()) {
			path.mkdirs();
		}

		try {
			PrintWriter outputStream = new PrintWriter(new FileOutputStream(
					PATH_TO_DATA_FILES + System.getProperty("file.separator")
							+ fileName, !overwrite));

			outputStream.print(data);

			outputStream.close();
		} catch (IOException e) {
			System.err.println("Couldn't open/create file " + fileName);
		}
	}
}
