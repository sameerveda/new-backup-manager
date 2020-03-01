package sam.backup.manager.walker;

import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import sam.backup.manager.api.InjectorKeys;
import sam.backup.manager.api.Utils;
import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.file.FileTree;
import sam.backup.manager.api.file.FileTreeManager;
import sam.backup.manager.api.file.TreeType;
import sam.backup.manager.api.walker.WalkListener;
import sam.backup.manager.api.walker.WalkManager;
import sam.backup.manager.api.walker.WalkMode;

@Singleton
class WalkManagerImpl implements WalkManager {
	final FileTreeManager fileTreeManager;
	final Utils utils;
	final boolean saveExcludeList;

	@Inject
	public WalkManagerImpl(FileTreeManager fileTreeManager, Utils utils, @Named(InjectorKeys.SAVE_EXCLUDE_LIST) boolean saveExcludeList) {
		this.fileTreeManager = fileTreeManager;
		this.utils = utils;
		this.saveExcludeList = saveExcludeList;
	}
	@Override
	public Callable<FileTree> newWalkTask(Config config, TreeType treeType, WalkMode mode, WalkListener listener) {
		return new WalkTask(config.getSource().path(), config.getTarget().path(), config, mode, listener, fileTreeManager, utils, treeType, saveExcludeList);
	}
}
