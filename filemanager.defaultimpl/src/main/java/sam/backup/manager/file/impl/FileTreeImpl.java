package sam.backup.manager.file.impl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import sam.backup.manager.api.file.DestinationType;
import sam.backup.manager.api.file.FileEntity;
import sam.backup.manager.api.file.FileState;
import sam.backup.manager.api.file.FileTree;
import sam.backup.manager.api.file.FileTreeDeleter;
import sam.backup.manager.api.file.FileTreeEditor;

class FileTreeImpl extends DirImpl implements FileTree, IdProvider {
	private final List<DirImpl> dirs = new ArrayList<>();
	private AtomicInteger fileId;
	private AtomicInteger dirId;
	
	FileTreeImpl(int id, String dirname) {
		super(id, null, dirname);
	}
	
	void init() {
		int[] n = {1, 1};
		walkFull(f -> n[0] = Math.max(n[0], f.id), d -> n[1] = Math.max(n[1], d.id));

		fileId = new AtomicInteger(n[0]);
		dirId = new AtomicInteger(n[1]);
	}

	@Override
	public FileTreeDeleter getDeleter() {
		// TODO
		return null;
	}

	@Override
	public FileTreeEditor<DirImpl, FileEntityImpl> getEditor(Path start, DestinationType destinationType) {
		return new FileTreeEditorImpl(this, destinationType, dirs);
	}

	@Override
	public boolean hasState(FileEntity file, FileState state) {
		return ((FileEntityImpl)file).hasState(state);
	}

	@Override
	public List<DirImpl> getDirs() {
		return Collections.unmodifiableList(dirs);
	}

	@Override
	public int nextFileId() {
		return fileId.incrementAndGet();
	}

	@Override
	public int nextDirId() {
		return dirId.incrementAndGet();
	}
}
