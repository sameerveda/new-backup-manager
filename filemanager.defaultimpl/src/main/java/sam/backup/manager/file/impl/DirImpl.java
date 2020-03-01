package sam.backup.manager.file.impl;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SIBLINGS;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.FileVisitResult.TERMINATE;
import static org.apache.commons.collections4.iterators.UnmodifiableIterator.unmodifiableIterator;
import static sam.backup.manager.api.file.FileTreeManager.*;
import static sam.backup.manager.api.file.FileTreeManager.alwaysTrue;

import java.nio.file.FileVisitResult;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;

import sam.backup.manager.api.file.DestinationType;
import sam.backup.manager.api.file.Dir;
import sam.backup.manager.api.file.FileEntity;
import sam.backup.manager.api.file.FileTreeWalker;
import sam.backup.manager.api.file.Walkable;
import sam.backup.manager.api.file.WalkableFull;
import sam.backup.manager.file.base.FileUtils;
import sam.backup.manager.file.base.FileUtils.Meta;

class DirImpl extends FileEntityImpl implements Dir, WalkableFull, Walkable {
	protected volatile int mod, lastMod = -1;
	protected int deepCount;
	protected long srcSize, trgtSize;
	protected final ArrayList<FileEntityImpl> children = new ArrayList<>();

	public DirImpl(int id, DirImpl parent, String dirname) {
		super(id, parent, dirname);
	}

	@Override
	public Iterator<FileEntity> iterator() {
		return unmodifiableIterator(children.iterator());
	}

	@Override
	public int childrenCount() {
		return children.size();
	}

	@Override
	public boolean isEmpty() {
		return children.isEmpty();
	}
	
	@Override
	public void walkFull(Consumer<FileEntity> onFile, Consumer<Dir> onDir, Predicate<FileEntity> filter) {
		if (!isEmpty()) {
			children.forEach(f -> {
				if(filter != ALWAYS_TRUE && !filter.test(f))
					return;
				
				if (f.isDirectory()) {
					onDir.accept((DirImpl) f);
					((DirImpl) f).walkFull(onFile, onDir);
				} else {
					onFile.accept(f);
				}
			});
		}
	}
	
	@Override 
	public FileVisitResult walk(FileTreeWalker walker, Predicate<FileEntity> filter) {
		if(children.isEmpty())
			return CONTINUE;
		
		for (int i = 0; i < children.size(); i++) {
			if(filter != ALWAYS_TRUE && !filter.test(children.get(i)))
				continue;

			FileVisitResult result = walkApply(children.get(i), walker, filter);

			if(result == TERMINATE)
				return TERMINATE;
			if(result == SKIP_SIBLINGS)
				break;
		}

		return CONTINUE;
	}

	protected FileVisitResult walkApply(FileEntity f, FileTreeWalker walker, Predicate<FileEntity> filter) {
		DirImpl dir = f.isDirectory() ? (DirImpl)f : null;
		
		if(!filter.test(f) || dir != null && dir.isEmpty())
			return CONTINUE;


		FileVisitResult result = dir != null ? walker.dir(dir) : walker.file(f);

		if(result == TERMINATE || result == SKIP_SIBLINGS)
			return result;

		if(result != SKIP_SUBTREE && dir != null && dir.walk(walker, filter) == TERMINATE)
			return TERMINATE;

		return CONTINUE;
	}

	@Override
	public void forEach(Consumer<? super FileEntity> action) {
		if (isEmpty())
			return;
		children.forEach(action);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Spliterator<FileEntity> spliterator() {
		if (isEmpty())
			return Spliterators.emptySpliterator();

		Spliterator s = children.spliterator();
		return s;
	}

	public void add(FileEntityImpl file) {
		this.children.add(file);
		mod++;
	}

	protected FileEntityImpl find(String filename, boolean isDir) {
		if (children.isEmpty())
			return null;

		for (int i = 0; i < children.size(); i++) {
			FileEntityImpl f = children.get(i);
			if (f.isDirectory() == isDir && f.filename.equals(filename))
				return f;
		}
		return null;
	}
	
	@Override
	public long getSize(DestinationType destinationType) {
		update();
		return Objects.requireNonNull(destinationType) == DestinationType.SOURCE ? srcSize : trgtSize;
	}

	private void update() {
		if(this.mod == this.lastMod)
			return;
		this.lastMod = this.mod;
		
		if(children.isEmpty()) {
			this.srcSize = 0;
			this.trgtSize = 0;
			this.deepCount = 0;
		} else {
			Meta meta = FileUtils.computeMeta(this, alwaysTrue());
			this.srcSize = meta.srcSize;
			this.trgtSize = meta.trgtSize;
			this.deepCount = meta.deepCount;
		}
	}

	@Override
	public int deepCount() {
		update();
		return deepCount;
	}
}