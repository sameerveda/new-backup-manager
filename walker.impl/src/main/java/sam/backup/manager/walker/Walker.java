package sam.backup.manager.walker;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.config.Filter;
import sam.backup.manager.api.config.WalkConfig;
import sam.backup.manager.api.file.Attr;
import sam.backup.manager.api.file.Attrs;
import sam.backup.manager.api.file.Dir;
import sam.backup.manager.api.file.FileEntity;
import sam.backup.manager.api.file.FileTree;
import sam.backup.manager.api.file.FileTreeEditor;
import sam.backup.manager.api.file.FileTreeMeta;
import sam.backup.manager.api.walker.WalkListener;
import sam.backup.manager.api.walker.WalkMode;

class Walker implements FileVisitor<Path>, Callable<FileTree>, AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger(Walker.class);
	
	private final boolean skipDirNotModified;
	private final boolean skipFiles;
	private final FileTree rootTree;
	private final Config config;
	private final WalkListener listener;
	private final Path start;
	private boolean isRoot = true;
	
	private WalkMode walkMode;
	private Filter excluder;
	private FileTreeEditor collector;
	
	private final List<Path> excludeFilesList;

	public Walker(Config config, WalkListener listener, Path start, Filter excluder, WalkMode mode, List<Path> exucludePaths, FileTreeMeta rootTree) {
		WalkConfig c = config.getWalkConfig();
		this.skipDirNotModified = c.skipDirNotModified();
		this.skipFiles = c.skipFiles();
		this.start = start;
		this.excludeFilesList = exucludePaths;

		this.rootTree = rootTree;
		this.config = config;
		this.listener = listener;
		this.excluder = excluder;
		this.walkMode = mode;
	}
	public FileTree call() throws IOException {
		collector = rootTree.getEditor(start);
		Files.walkFileTree(start, EnumSet.noneOf(FileVisitOption.class), config.getWalkConfig().getDepth(), this);
		
		return rootTree;
	}


	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		if(isRoot) { 
			collector.setAttr(new Attr(attrs.lastModifiedTime().toMillis(), 0), walkMode, dir);
			isRoot = false;
		} else if(include(dir)) {
			Dir ft = collector.addDir(dir, new Attr(attrs.lastModifiedTime().toMillis(), 0), walkMode);
			listener.onDirFound(ft, walkMode);

			/**
			 * if(walkMode == WalkMode.BACKUP && !ft.isWalked()) {
				LOGGER.info("target walk skipped: {}", ft.getTargetPath());
				return SKIP_SUBTREE;
			}
			 */
			
			if(skipDirNotModified && !atrs(ft).isModified()) {
				LOGGER.debug("source walk skipped: {}", ft.getSourcePath());
				return SKIP_SUBTREE;
			}
			collector.setWalked(ft, true);
		} else {
			excludeFilesList.add(dir);
			return SKIP_SUBTREE;
		}
		return CONTINUE;
	}
	private Attrs atrs(FileEntity ft) {
		return walkMode == WalkMode.BACKUP ? ft.getTargetAttrs() : ft.getSourceAttrs();
	}
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		return CONTINUE;
	}
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if(skipFiles)
			return CONTINUE;

		if(include(file)) {
			Attr af = new Attr(attrs.lastModifiedTime().toMillis(), attrs.size());
			FileEntity ft  = collector.addFile(file, af, walkMode);
			listener.onFileFound(ft, af.size(), walkMode);
		} else 
			excludeFilesList.add(file);

		return CONTINUE;
	}
	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		return CONTINUE;
	}
	private boolean include(Path file) { 
		return !excluder.test(file);
	}
	
	@Override
	public void close() throws IOException {
		if(collector != null)
			collector.close();
	}
}
