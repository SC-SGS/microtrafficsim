package microtrafficsim.core.vis.errors;


public class AsyncOperationError extends Error {

	private static final long serialVersionUID = -8775154162931991897L;
	

	public AsyncOperationError(String msg) {
		super(msg);
	}
	
	public AsyncOperationError(Throwable cause) {
		super(cause);
	}
	
	public AsyncOperationError(String msg, Throwable cause) {
		super(msg, cause);
	}
}
