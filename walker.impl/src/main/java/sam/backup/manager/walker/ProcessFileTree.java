package sam.backup.manager.walker;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.util.ArrayList;
import java.util.List;

import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.file.Attr;
import sam.backup.manager.api.file.Attrs;
import sam.backup.manager.api.file.DestinationType;
import sam.backup.manager.api.file.Dir;
import sam.backup.manager.api.file.FileEntity;
import sam.backup.manager.api.file.FileTree;
import sam.backup.manager.api.file.FileTreeDeleter;
import sam.backup.manager.api.file.FileTreeManager;
import sam.backup.manager.api.file.FileTreeWalker;

class ProcessFileTree implements FileTreeWalker, Runnable {
	private final boolean checkModified;
	private final boolean hardSync;
	private final boolean targetWalked;
	private final List<FileEntity> willRemoved = new ArrayList<>();
	private final FileTree filetree;
	private final FileTreeManager treeManager;

	public ProcessFileTree(FileTree filetree, Config config, boolean targetWalked, FileTreeManager treeManager) {
		this.filetree = filetree;
		this.targetWalked = targetWalked;
		this.checkModified = config.getTargetConfig().checkModified();
		this.hardSync = config.getTargetConfig().hardSync();
		this.treeManager = treeManager;
	}
	
	@Override
	public void run() {
		treeManager.walk(filetree, this);
		if(!willRemoved.isEmpty()) {
			FileTreeDeleter deleter = filetree.getDeleter();
			willRemoved.forEach(f -> {
				try {
					deleter.delete(f, DestinationType.BACKUP);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});	
		}
		
	}

	@Override
	public FileVisitResult file(FileEntity ft) {
		Attrs sourceK = ft.getSourceAttrs();
		Attr source = sourceK.current();

		if(source == null) {
			if(hardSync)
				ft.getStatus().setTargetDeletable(true);
			else
				willRemoved.add(ft);

			return CONTINUE;
		}

		isBackable(ft, true);

		return CONTINUE;
	}
	private void isBackable(FileEntity ft, boolean isfile) {
		if(ft.getSourceAttrs().current() == null) 
			return;
		boolean target = check(ft, isNew(ft), isfile ? "(1) new File" : "(3) new Directory");
		if(isfile)
			target = target || check(ft, checkModified && ft.getSourceAttrs().isModified(), "(2) File Modified");
	}

	private boolean check(FileEntity f, boolean condition, String reason) {
		if(condition)
			f.getStatus().setTargetable(true, reason);
		return condition;
	}

	@Override
	public FileVisitResult dir(Dir ft) {
		if(hardSyncCheck(ft))
			return SKIP_SUBTREE;

		if(!filetree.isWalked(ft))
			return SKIP_SUBTREE;

		isBackable(ft, false);
		return CONTINUE;
	}
	private boolean hardSyncCheck(FileEntity ft) {
		boolean delete = hardSync && ft.getSourceAttrs().current() == null;
		ft.getStatus().setTargetDeletable(delete);
		return delete;
	}
	private boolean isNew(FileEntity ft) {
		if(ft.getSourceAttrs().current() == null) 
			return false;
		return targetWalked ? ft.getTargetAttrs().current() == null : ft.getSourceAttrs().old() == null;
	}
}
