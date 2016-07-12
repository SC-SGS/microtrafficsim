package microtrafficsim.utils.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


public class FileResource extends Resource {

    private final File file;

    public FileResource(File file) {
        this.file = file;
    }

    @Override
    public URL toURL() throws ResourceException {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new ResourceException("Resource '" + file.getPath() + "' not found.", e);
        }
    }

    @Override
    public InputStream asStream() throws ResourceException {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new ResourceException("Resource '" + file.getPath() + "' not found.", e);
        }
    }

    public File getFile() {
        return file;
    }
}
