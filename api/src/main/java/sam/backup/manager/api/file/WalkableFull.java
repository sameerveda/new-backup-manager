package sam.backup.manager.api.file;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface WalkableFull {
	 default void walkFull(Consumer<FileEntity> onFile, Consumer<Dir> onDir) {
		 walkFull(onFile, onDir, FileTreeManager.ALWAYS_TRUE);
	 }
	 void walkFull(Consumer<FileEntity> onFile, Consumer<Dir> onDir, Predicate<FileEntity> filter);
}
