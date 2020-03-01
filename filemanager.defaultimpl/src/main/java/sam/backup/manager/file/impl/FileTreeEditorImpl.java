package sam.backup.manager.file.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import sam.backup.manager.api.file.Attr;
import sam.backup.manager.api.file.DestinationType;
import sam.backup.manager.api.file.FileState;
import sam.backup.manager.api.file.FileTreeEditor;
import sam.backup.manager.file.base.AttrImpl;
import sam.myutils.Checker;

class FileTreeEditorImpl implements FileTreeEditor<DirImpl, FileEntityImpl> {
	private static final AtomicBoolean OPEN = new AtomicBoolean();
	
	private Path current_path;
	private DirImpl current_dir;
	private Set<String> current_names;
	
	private boolean closed = false;
	private final IdProvider  idProvider;
	private final HashMap<Path, DirImpl> dirsMap = new HashMap<>();
	private final Map<DirImpl, Set<String>> namesMap = new IdentityHashMap<>();
	private final List<DirImpl> dirs;
	private final DestinationType destinationType;
	
	public FileTreeEditorImpl(IdProvider idProvider, DestinationType destinationType, List<DirImpl> dirs) {
		if(!OPEN.compareAndSet(false, true)) 
			throw new IllegalStateException("only one instace allowed at a time");
		Checker.requireNonNull("idProvider, destinationType, dirs", idProvider, destinationType, dirs);
		
		this.idProvider = idProvider;
		this.dirs = dirs;
		this.destinationType = destinationType;
		dirs.forEach(this::putDir);
	}

	private void putDir(DirImpl d) {
		this.dirsMap.put((destinationType == DestinationType.SOURCE ? d.getSourcePath() : d.getTargetPath()).path(), d);
	}

	@Override
	public FileEntityImpl addFile(Path path, Attr af, DestinationType destinationType) throws IOException {
		ensureOpen();
		FileEntityImpl file = addFile(findDir(path.getParent()), path.getFileName().toString());
		file.setAttr((AttrImpl)af, destinationType);
		return file;
	}

	private DirImpl findDir(Path path) {
		if(Objects.equals(this.current_path, path))
			return current_dir;
		
		DirImpl d = dirsMap.get(path);
		if(d == null)
			throw new IllegalStateException("no dir found for: "+path);
		setDir(path, d);
		return d;
	}
	
	private final Function<DirImpl, Set<String>> namesComputer = d -> {
		Set<String> set = new HashSet<>();
		d.forEach(f -> set.add(f.getFileName()));
		return set;
	};
	
	private void setDir(Path path, DirImpl d) {
		this.current_dir = d;
		this.current_path = path;
		this.current_names = d == null ? null : namesMap.computeIfAbsent(d, namesComputer); 
	}

	private FileEntityImpl find(DirImpl dir, String filename, boolean isDir) {
		if(dir.isEmpty())
			return null;
		
		Set<String> names = dir == current_dir ? current_names : namesMap.computeIfAbsent(dir, namesComputer);
		return names.contains(filename) ? dir.find(filename, isDir) : null;
	}
	
	@Override
	public boolean hasState(FileEntityImpl file, FileState state) {
		return ((FileEntityImpl)file).hasState(state);
	}

	@Override
	public DirImpl addDir(Path path, Attr attr, DestinationType destinationType) throws IOException {
		ensureOpen();
		DirImpl dir = addDir(findDir(path.getParent()), path.getFileName().toString());
		dir.setAttr((AttrImpl)attr, destinationType);
		return dir;
	}
	
	@Override
	public FileEntityImpl addFile(DirImpl parent, String filename) throws IOException {
		ensureOpen();
		if(parent == null || Checker.isEmptyTrimmed(filename))
			throw new NullPointerException((parent == null && filename == null) ? "parent == null || filename == null is not valid" : (parent == null ? "parent" : "filename") + " cannot be null");
		
		FileEntityImpl f = find(parent, filename, false);
		if(f == null) {
			f = new FileEntityImpl(idProvider.nextFileId(), parent, filename);
			parent.add(f);
			addName(filename, parent);
		}
		return f;
	}

	private void addName(String filename, DirImpl parent) {
		if(current_dir == parent)
			current_names.add(filename);
		else
			namesMap.computeIfAbsent(parent, namesComputer).add(filename);
	}

	@Override
	public DirImpl addDir(DirImpl parent, String dirname) throws IOException {
		ensureOpen();
		if(parent == null || Checker.isEmptyTrimmed(dirname))
			throw new NullPointerException((parent == null && dirname == null) ? "parent == null || dirname == null is not valid" : (parent == null ? "parent" : "dirname") + " cannot be null");
		
		DirImpl d = (DirImpl)find(parent, dirname, true);
		if(d == null) {
			d = new DirImpl(idProvider.nextDirId(), parent, dirname);
			dirs.add(d);
			putDir(d); 
			parent.add(d);
			addName(dirname, parent);
		}
		return d;
	}

	private void ensureOpen() {
		if(closed)
			throw new IllegalStateException("closed");
	}
	
	@Override
	public void close() throws IOException {
		closed = true;
		OPEN.set(false);
	}

	@Override
	public void addState(FileEntityImpl file, FileState state) {
		ensureOpen();
		file.addState(state);
	}

	@Override
	public void removeState(FileEntityImpl file, FileState state) {
		ensureOpen();
		file.removeState(state);
	}

}
