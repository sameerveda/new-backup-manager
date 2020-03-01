package sam.backup.manager.file.impl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import sam.backup.manager.api.file.DestinationType;
import sam.backup.manager.api.file.FileState;
import sam.backup.manager.file.base.AbstractFile;
import sam.backup.manager.file.base.AttrImpl;

class FileEntityImpl extends AbstractFile<DirImpl> {
	private transient Set<FileState> states = Collections.emptySet(); 
	
	public FileEntityImpl(int id, DirImpl parent, String filename) {
		super(id, parent, filename);
	}

	boolean setAttr(AttrImpl attr, DestinationType destinationType) {
		if(attr == null || destinationType == null)
			throw new NullPointerException("attr: "+attr+", destinationType: "+destinationType);
		return (destinationType == DestinationType.SOURCE ? sourceAttrs : targetAttrs).setCurrent(attr);
	}

	public boolean hasState(FileState state) {
		return this.states.contains(state);
	}

	public void addState(FileState state) {
		Objects.requireNonNull(state);
		
		if(this.states == Collections.EMPTY_SET)
			this.states = EnumSet.of(state);
		else 
			this.states.add(state);
	}
	public void removeState(FileState state) {
		this.states.remove(Objects.requireNonNull(state));
	}
}