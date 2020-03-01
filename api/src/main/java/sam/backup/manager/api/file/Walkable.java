package sam.backup.manager.api.file;

import java.nio.file.FileVisitResult;
import java.util.function.Predicate;

public interface Walkable {
	default FileVisitResult walk(FileTreeWalker walker) {
		return walk(walker, FileTreeManager.alwaysTrue());
	}
	FileVisitResult walk(FileTreeWalker walker, Predicate<FileEntity> filter);
}
