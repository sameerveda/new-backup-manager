package sam.backup.manager.file.base;

import java.util.Objects;

import sam.backup.manager.api.file.Attrs;
import sam.backup.manager.api.file.DestinationType;
import sam.backup.manager.api.file.Dir;
import sam.backup.manager.api.file.FileEntity;

public abstract class AbstractFile<D extends Dir> implements FileEntity {
	public final int id;
	public final String filename;
	protected transient AbstractPathWrap source, target;
	protected transient final D parent;
	protected AttrsImpl sourceAttrs = AttrsImpl.EMPTY, targetAttrs = AttrsImpl.EMPTY;

	public AbstractFile(int id, D parent, String filename) {
		this.parent = parent;
		this.id = id;
		this.filename = filename;
	}

	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public D getParent() {
		return parent;
	}

	@Override
	public String getFileName() {
		return this.filename;
	}

	public AttrsImpl getSourceAttrs() {
		return sourceAttrs;
	}

	public AttrsImpl getTargetAttrs() {
		return targetAttrs;
	}

	@Override
	public AbstractPathWrap getTargetPath() {
		if(this.target == null) {
			this.target = new AbstractPathWrap((AbstractPathWrap)getParent().getTargetPath(), filename) {
				
				@Override
				public Attrs getAttrs() {
					return targetAttrs;
				}
			}; 
		}
		return target;
	}

	@Override
	public AbstractPathWrap getSourcePath() {
		if(this.source == null) {
			this.source = new AbstractPathWrap((AbstractPathWrap)getParent().getSourcePath(), filename) {
				
				@Override
				public Attrs getAttrs() {
					return sourceAttrs;
				}
			}; 
		}
		return source;
	}
	
	@Override
	public long getSize(DestinationType destinationType) {
		return (Objects.requireNonNull(destinationType) == DestinationType.SOURCE ? sourceAttrs : targetAttrs).size();
	}
	
}
