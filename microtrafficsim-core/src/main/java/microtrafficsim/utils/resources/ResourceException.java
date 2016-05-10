package microtrafficsim.utils.resources;

import java.io.IOException;


public class ResourceException extends IOException {
	
	private static final long serialVersionUID = 2537380971878927752L;
	

	public ResourceException() {
		super();
	}

	public ResourceException(String message) {
		super(message);
	}

	public ResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceException(Throwable cause) {
		super(cause);
	}
}
