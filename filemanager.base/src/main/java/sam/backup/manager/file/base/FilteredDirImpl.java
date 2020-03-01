package sam.backup.manager.file.base;

import static sam.backup.manager.api.file.DestinationType.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;

import sam.backup.manager.api.PathWrap;
import sam.backup.manager.api.file.DestinationType;
import sam.backup.manager.api.file.Dir;
import sam.backup.manager.api.file.FileEntity;
import sam.backup.manager.api.file.FileTreeManager;
import sam.backup.manager.api.file.FilteredDir;
import sam.backup.manager.api.file.WalkableFull;
import sam.backup.manager.file.base.FileUtils.Meta;
import sam.collection.FilteredIterator;

public class FilteredDirImpl<D extends Dir & HasMod & WalkableFull> implements FilteredDir {
	private final D source;
	private final Dir parent;
	private final Predicate<FileEntity> filter;
	private int childCount, deepCount;
	private long srcSize, trgtSize;
	private int mod = -1;

	public FilteredDirImpl(D source, D parent, Predicate<FileEntity> filter) {
		this.parent = parent;
		this.source = Objects.requireNonNull(source);
		this.filter = Objects.requireNonNull(filter);
		
		if(this.filter == FileTreeManager.ALWAYS_TRUE)
			throw new IllegalArgumentException("bad value for filter: ALWAYS_TRUE");
	}
	
	
	
    @Override 
	public int childrenCount() {
		update();
		return childCount;
	}
	
	private void update() {
		if(mod == source.modValue())
			return;
		Meta meta = FileUtils.computeMeta(source, filter);
		
		this.childCount = meta.childCount;
		this.deepCount = meta.deepCount;
		this.srcSize = meta.srcSize;
		this.trgtSize = meta.trgtSize;
		
		mod = source.modValue();
	}

	/*
	 * u will see isEmpty() check everywhere, 
	 * it is to check if dir is modified since last access 
	 */
	@Override 
	public boolean isEmpty() {
		return childrenCount() == 0;
	}
	@Override 
	public int getId() {
		throw new IllegalAccessError(); // TODO do filtered dir needs an id?, if yes should it be the id of source dir
	}
	@Override 
	public Dir getParent() {
		return parent;
	}
    
	@Override 
	public Iterator<FileEntity> iterator() {
		if(isEmpty())
			return Collections.emptyIterator();
		else 
			return new FilteredIterator<>(source.iterator(), filter);
	}
	@Override
	public Spliterator<FileEntity> spliterator() {
		if(isEmpty())
			return Spliterators.emptySpliterator();
		else 
			return Spliterators.spliteratorUnknownSize(iterator(), 0);
	}
	@Override
	public void forEach(Consumer<? super FileEntity> action) {
		if(isEmpty())
			return;
		
		for (FileEntity f : this) 
			action.accept(f);
	}

	@Override
	public String getFileName() {
		return source.getFileName();
	}

	@Override
	public PathWrap getTargetPath() {
		return source.getTargetPath();
	}

	@Override
	public PathWrap getSourcePath() {
		return source.getSourcePath();
	}

	@Override
	public int deepCount() {
		update();
		return deepCount;
	}

	@Override
	public long getSize(DestinationType destinationType) {
		update();
		return Objects.requireNonNull(destinationType) == SOURCE ? srcSize : trgtSize;
	}
	
}
