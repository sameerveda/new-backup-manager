package sam.backup.manager.api.transferer;

import java.io.IOException;
import java.nio.file.Path;

import sam.backup.manager.api.file.FileEntity;

public class DirCreationFailedException extends FileEntityException {
	private static final long serialVersionUID = 1L;
	
	public transient final Path path;
	
	public DirCreationFailedException(Path path, FileEntity forFile, IOException e) {
		super(forFile, e);
		this.path = path;
	}
}
