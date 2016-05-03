package microtrafficsim.utils.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import microtrafficsim.utils.Streams;


public class PackagedResource extends Resource {
	
	private final Class<?> clazz;
	private final String name;
	
	public PackagedResource(Class<?> clazz, String name) {
		this.clazz = clazz;
		this.name = name;
	}
	
	@Override
	public URL toURL() throws ResourceException {
		URL url = clazz.getResource(name);
		
		if (url == null) {
			throw new ResourceException("Resource '" + name + "' (class '"
					+ clazz.getCanonicalName() + "') not found.");
		}
		
		return clazz.getResource(name);
	}

	@Override
	public InputStream asStream() throws ResourceException {
		InputStream in = clazz.getResourceAsStream(name);
		
		if (in == null) {
			throw new ResourceException("Resource '" + name + "' (class '"
					+ clazz.getCanonicalName() + "') not found.");
		}
		
		return in;
	}
	
	public File asTemporaryFile() throws IOException {
		return asTemporaryFile(null);
	}
	
	public File asTemporaryFile(File directory) throws IOException {
		return asTemporaryFile(directory, true);
	}
	
	public File asTemporaryFile(File directory, boolean deleteAfterExit) throws IOException {
		try (InputStream in = asStream()) {
			String path = clazz.getResource(name).getPath();
			
			String fullname = path.substring(path.lastIndexOf('/') + 1);
			int dot = fullname.lastIndexOf('.');
			
			String name;
			String suffix;
			
			// Note: temp filename must be at least 3 characters long
			if (dot < fullname.length() && dot >= 3) {
				name = fullname.substring(0, dot);
				suffix = fullname.substring(dot);
				
			} else if (fullname.length() >= 3) {
				name = fullname;
				suffix = null;

			} else {
				name = fullname + ".asFile.";
				suffix = null;
			}
		
			return Streams.toTemporaryFile(name, suffix, in, directory, deleteAfterExit);
		}
	}
}
