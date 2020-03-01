package sam.backup.manager.api.file;

import sam.backup.manager.api.PathWrap;

public interface FileEntity {
	int getId();
	Dir getParent();
	default boolean isDirectory() {
		return false;
	}
	String getFileName();
	PathWrap getTargetPath();
	PathWrap getSourcePath();
	long getSize(DestinationType destinationType);
}
