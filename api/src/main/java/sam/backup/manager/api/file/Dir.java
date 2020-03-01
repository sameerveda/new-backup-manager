package sam.backup.manager.api.file;

public interface  Dir extends FileEntity, Iterable<FileEntity> {
	/**
	 * files + dirs count for depth 1
	 * @return
	 */
	int childrenCount();
	/**
	 * files + dirs count in this tree
	 * @return
	 */
	int deepCount();
	default boolean isDirectory() {
		return true;
	}
	default boolean isEmpty() {
		return childrenCount() == 0;
	}
}

