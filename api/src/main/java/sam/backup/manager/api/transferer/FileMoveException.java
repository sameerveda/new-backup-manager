package sam.backup.manager.api.transferer;

import java.io.IOException;
import java.nio.file.Path;

import sam.backup.manager.api.file.FileEntity;

public class FileMoveException extends FileEntityException {
	public final Path src;
	public Path target;

	public FileMoveException(FileEntity ft, Path src, Path target, IOException e) {
		super(ft, e);
		this.src = src;
		this.target = target;
	}
}
