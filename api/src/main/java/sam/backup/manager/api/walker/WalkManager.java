package sam.backup.manager.api.walker;

import java.util.concurrent.Callable;

import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.file.FileTree;
import sam.backup.manager.api.file.TreeType;

public interface WalkManager {
	Callable<FileTree> newWalkTask(Config config, TreeType treeType, WalkMode mode, WalkListener listener);
}
