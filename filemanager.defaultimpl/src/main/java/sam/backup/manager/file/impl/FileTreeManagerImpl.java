package sam.backup.manager.file.impl;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.Collections.synchronizedMap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sam.backup.manager.api.InjectorKeys;
import sam.backup.manager.api.ShutDownHooks;
import sam.backup.manager.api.Stoppable;
import sam.backup.manager.api.Writable;
import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.file.Attr;
import sam.backup.manager.api.file.Attrs;
import sam.backup.manager.api.file.DestinationType;
import sam.backup.manager.api.file.Dir;
import sam.backup.manager.api.file.FileEntity;
import sam.backup.manager.api.file.FileTree;
import sam.backup.manager.api.file.FileTreeMeta;
import sam.backup.manager.api.file.TreeType;
import sam.backup.manager.file.base.AbstractFileTreeManager;
import sam.backup.manager.file.base.AttrImpl;
import sam.backup.manager.file.base.AttrsImpl;
import sam.backup.manager.file.base.FileTreeString;
import sam.myutils.Checker;
import sam.nopkg.EnsureSingleton;

@Singleton
class FileTreeManagerImpl extends AbstractFileTreeManager implements Stoppable {
	private static final EnsureSingleton SINGLETON = new EnsureSingleton();
	{ SINGLETON.init(); }

	private final Logger logger = LogManager.getLogger(getClass());
	private final Provider<FileTreeString> fileTreeString;
	private final List<FtMeta> wraps = new ArrayList<>();
	private final Map<String, Long> old_lastUpdated, new_lastUpdated;
	private final Serializer serializer;
	private final Path saveDir;
	private final Path lastUpdatedFile;

	@Inject
	public FileTreeManagerImpl(@Named(InjectorKeys.APP_DIR) Path appDir, Provider<FileTreeString> fileTreeString,
			ShutDownHooks hooks, Serializer serializer) throws ClassNotFoundException, IOException {
		this.serializer = serializer;
		this.fileTreeString = fileTreeString;
		hooks.addShutDownHooks(this);

		this.saveDir = appDir.resolve(getClass().getSimpleName());
		Files.createDirectory(saveDir);
		serializer.setSaveDir(saveDir);

		Map<String, Long> map = new HashMap<>();
		this.lastUpdatedFile = this.saveDir.resolve("last_modifies");

		if (Files.notExists(lastUpdatedFile)) {
			logger.debug("file not found: {}", lastUpdatedFile);
		} else {
			Files.lines(lastUpdatedFile).forEach(s -> {
				int n = s.indexOf('\t');
				map.put(s.substring(0, n), new Long(s.substring(n + 1)));
			});
		}
		this.old_lastUpdated = synchronizedMap(map);
		this.new_lastUpdated = synchronizedMap(new HashMap<>());
	}

	private class FtMeta implements FileTreeMeta {
		private final TreeType treeType;
		private final Config config;
		private final String filename;
		private final String key;
		private volatile FileTree tree;

		public FtMeta(TreeType treeType, Config config) {
			this.treeType = treeType;
			this.config = config;
			this.key = toKey(config, treeType);
			this.filename = treeType + "-" + config.getName() + "-" + key.hashCode();
		}

		@Override
		public Config getConfig() {
			return config;
		}

		@Override
		public TreeType getTreeType() {
			return treeType;
		}

		@Override
		public Long getLastUpdateTime() {
			Long n = new_lastUpdated.get(key);
			return n != null ? n : old_lastUpdated.get(key);
		}

		@Override
		public synchronized FileTree getFileTree() throws IOException {
			if (this.tree == null)
				this.tree = serializer.read(filename);

			return tree;
		}

		@Override
		public synchronized void save() throws IOException {
			if (tree == null)
				return;

			serializer.save(tree, filename);
			new_lastUpdated.put(key, System.currentTimeMillis());
		}
	}

	private FtMeta get(Config c, TreeType treeType) {
		for (FtMeta wrap : wraps) {
			if (wrap.config == c && wrap.treeType == treeType)
				return wrap;
		}
		FtMeta w = new FtMeta(treeType, c);
		wraps.add(w);
		return w;
	}

	private String toKey(Config c, TreeType treeType) {
		return treeType.toString() + ":" + c.getSource().toString();
	}

	@Override
	public Writable toWritable(Dir dir, DestinationType destinationType, Predicate<FileEntity> filter, Appendable sink)
			throws IOException {
		FileTreeString f = fileTreeString.get();
		f.set(dir, destinationType, filter);
		return f;
	}

	@Override
	public FileTreeMeta getFileTreeFor(Config config, TreeType treeType) {
		Checker.requireNonNull("config, treeType", config, treeType);
		synchronized (wraps) {
			return get(config, treeType);
		}
	}

	@Override
	public Attr newAttr(long lastModified, long size) {
		return lastModified <= 0 && size <= 0 ? AttrImpl.EMPTY : new AttrImpl(lastModified, size);
	}

	@Override
	public Attrs newAttrs(Attr old) {
		return old == null || old == AttrImpl.EMPTY ? AttrsImpl.EMPTY : new AttrsImpl((AttrImpl)old);
	}

	@Override
	public void stop() throws IOException {
		if (!new_lastUpdated.isEmpty()) {
			try (BufferedWriter buf = Files.newBufferedWriter(lastUpdatedFile, CREATE, APPEND)) {
				for (String s : new_lastUpdated.keySet()) {
					buf.append(s).append('\t').append(new_lastUpdated.get(s).toString()).append('\n');
				}
			}
			logger.debug("updated ({}): {}", new_lastUpdated.size(), lastUpdatedFile);
		}

		// TODO Auto-generated method stub
	}
}
