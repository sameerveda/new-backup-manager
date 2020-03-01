package sam.backup.manager.api;

import java.io.IOException;

public interface Writable {
	void write(Appendable sink) throws IOException;
}
