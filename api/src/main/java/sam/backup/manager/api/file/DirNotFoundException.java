package sam.backup.manager.api.file;

import java.io.IOException;

public class DirNotFoundException extends IOException {
	private static final long serialVersionUID = -1033383970407604300L;

	public DirNotFoundException() {
		super();
	}
	public DirNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	public DirNotFoundException(String message) {
		super(message);
	}
	public DirNotFoundException(Throwable cause) {
		super(cause);
	}
}
