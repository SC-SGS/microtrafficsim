package microtrafficsim.utils.resources;

import java.io.InputStream;
import java.net.URL;


public abstract class Resource {
	
	public abstract InputStream asStream() throws ResourceException;
	public abstract URL toURL() throws ResourceException;
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Resource))
			return false;
			
		try {
			return this.toURL().equals(((Resource) obj).toURL());
		} catch (ResourceException e) {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		try {
			return this.toURL().hashCode();
		} catch (ResourceException e) {
			return 8161;	// random prime
		}
	}
	
	public String getUniqueName() {
		try {
			return toURL().toExternalForm();
		} catch (ResourceException e) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return getUniqueName();
	}
}
