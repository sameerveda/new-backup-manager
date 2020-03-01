package sam.backup.manager.file.impl;

import java.io.IOException;
import java.nio.file.Path;

import sam.backup.manager.api.file.FileTree;

interface Serializer {
	void setSaveDir(Path saveDir);
	FileTree read(String filename)  throws IOException;
	void save(FileTree filetree, String filename)  throws IOException;
}
