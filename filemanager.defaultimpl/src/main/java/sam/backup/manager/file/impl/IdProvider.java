package sam.backup.manager.file.impl;

public interface IdProvider {
	int nextFileId();
	int nextDirId();
}
