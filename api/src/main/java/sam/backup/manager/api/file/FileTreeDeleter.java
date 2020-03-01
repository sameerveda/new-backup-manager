package sam.backup.manager.api.file;

import java.io.IOException;

public interface FileTreeDeleter extends AutoCloseable {
	@Override void close() throws IOException;
	void delete(FileEntity fte, DestinationType type)  throws IOException;
}
