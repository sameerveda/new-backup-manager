package sam.backup.manager.api.file;

import java.nio.file.Path;
import java.util.List;

public interface FileTree extends Dir {
	FileTreeDeleter getDeleter();
	FileTreeEditor getEditor(Path start, DestinationType destinationType);
	boolean hasState(FileEntity file, FileState state);
	List<? extends Dir> getDirs();
}
