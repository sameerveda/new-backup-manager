package sam.backup.manager.api;

import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.List;

public interface FileStoreManager {
	public List<FileStore> getDrives();
	public Path getTargetDrive();
	public String getTargetDriveId();
}
