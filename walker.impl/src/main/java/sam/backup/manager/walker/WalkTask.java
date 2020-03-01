package sam.backup.manager.walker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sam.backup.manager.api.Utils;
import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.file.FileTree;
import sam.backup.manager.api.file.FileTreeManager;
import sam.backup.manager.api.file.FileTreeMeta;
import sam.backup.manager.api.file.PathListToFileTree;
import sam.backup.manager.api.file.TreeType;
import sam.backup.manager.api.walker.WalkListener;
import sam.backup.manager.api.walker.WalkMode;
import static sam.backup.manager.api.walker.WalkMode.*;

class WalkTask implements Callable<FileTree> {
	public static final Logger logger = LogManager.getLogger(WalkTask.class); 

	private final Config config;

	private final WalkMode initialWalkMode;
	private final WalkListener listener;
	private final Path source;
	private final Path target;
	private final FileTreeManager fileTreeManager;
	private final Utils utils;
	private final TreeType treeType;
	private final boolean saveExcludeList;

	public WalkTask(Path source, Path target, Config config, WalkMode walkMode, WalkListener listener, FileTreeManager fileTreeManager, Utils utils, TreeType treeType, boolean saveExcludeList) {
		this.treeType = treeType;
		this.config = config;
		this.initialWalkMode = walkMode;
		this.listener = listener;
		this.source = source;
		this.target = target;
		this.fileTreeManager = fileTreeManager;
		this.saveExcludeList = saveExcludeList;
		this.utils = utils;
	}

	private static final Set<FileTree> sourceWalkCompleted = Collections.newSetFromMap(new IdentityHashMap<>());
	private static final Set<FileTree> targetWalkCompleted = Collections.newSetFromMap(new IdentityHashMap<>());
	private boolean targetWalked;

	@Override
	public FileTree call() throws IOException {
		if(Files.notExists(this.source)) 
			throw new FileNotFoundException("Source not found: "+this.source);
			
		boolean sourceWalkFailed = true;
		List<Path> exucludePaths = new ArrayList<>();
		FileTree fileTree = null;

		try {
			if(initialWalkMode.isSource()) 
				fileTree = walk(SOURCE, source, exucludePaths);
			
			sourceWalkFailed = false;

			if(config.getWalkConfig().walkTarget() 
					&& initialWalkMode.isTarget() 
					&& target != null 
					&& Files.exists(target)){
				
				fileTree = walk(BACKUP, target, exucludePaths);
				targetWalked = true;
			}
		} catch (IOException e) {
			String s = sourceWalkFailed ? "Source walk failed: "+source : "Target walk failed: "+target;
			logger.error(s, e);
			throw new IOException(s, e);
		}

		if(!exucludePaths.isEmpty() && saveExcludeList)
			utils.saveInTempDir(new PathListToFileTree(exucludePaths), config, "excluded", source.getFileName()+".txt");
		
		new ProcessFileTree(fileTree, config, targetWalked, fileTreeManager).run();
		return fileTree;
	}

	private FileTree walk(WalkMode mode, Path path, List<Path> exucludePaths) throws IOException {
		FileTreeMeta fileTree = fileTreeManager.getFileTreeFor(config, treeType);
		Set<FileTree> set = mode == SOURCE ? sourceWalkCompleted : targetWalkCompleted;
		if(set.contains(fileTree)) {
			logger.debug("{} walk skipped: {}", mode, path);
		} else {
			try(Walker walker = walker(mode, exucludePaths, fileTree)) {
				fileTree =  walker.call();
				fileTreeManager.updateFileTreeFor(fileTree, config, treeType, path);
				set.add(fileTree);
			}
		}
		return  fileTree;
			
	}

	private Walker walker(WalkMode w, List<Path> exucludePaths, FileTreeMeta fileTree) {
		if(w != SOURCE && w != BACKUP)
			throw new IllegalStateException("unknown walk mode: "+w);
		
		return new Walker(
				config, 
				listener, 
				w == SOURCE ? source : target,
				w == SOURCE ? config.getSourceExcluder() : config.getTargetExcluder(),
				w, 
				exucludePaths,
				fileTree
				);
	}
}