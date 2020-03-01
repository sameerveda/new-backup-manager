package sam.backup.manager.api.file;

import java.nio.file.FileVisitResult;

public interface FileTreeWalker {
	FileVisitResult file(FileEntity file);
	FileVisitResult dir(Dir dir);
}
