package sam.backup.manager.api.file;

import java.io.IOException;

import sam.backup.manager.api.config.Config;

public interface FileTreeMeta {
	public Config getConfig();
	public TreeType getTreeType();
	public Long getLastUpdateTime();
	public FileTree getFileTree() throws IOException;
	public void save() throws IOException;
}

