package sam.backup.manager.file.base;

import sam.backup.manager.api.file.Attr;
import sam.backup.manager.api.file.Attrs;

public class AttrsImpl implements Attrs {
	public static final AttrsImpl EMPTY = new AttrsImpl(AttrImpl.EMPTY) {
		@Override
		public boolean setCurrent(AttrImpl current) {
			throw new IllegalAccessError();
		}
		@Override
		public void setUpdated() {
			throw new IllegalAccessError();
		}
	};
	
	protected final AttrImpl old;
	protected AttrImpl current, nnew;
	
	public AttrsImpl(AttrImpl old) {
		this.old = old;
		this.current = old;
		this.nnew = old;
	}

	public Attr getCurrent() {
		return current;
	}
	public boolean setCurrent(AttrImpl current) {
		if(current.equals(this.current))
			return false;
		
		this.current = current;
		return true;
	}
	public Attr getOld() {
		return old;
	}
	public Attr getNew() {
		return nnew;
	}
	public boolean isModified() {
		return old != current;
	}

	public void setUpdated() {
		current = nnew;
	}
	public long size() {
		return !isPure(current) ? current.size() : isPure(old) ? -1 : old.size();
	}
	
	/**
	 * return true if attr is null or, attr is not initialized  
	 * @param attr
	 * @return
	 */
	public static boolean isPure(Attr attr) {
		return attr == null || attr == EMPTY || attr.lastModified() <= 0;
	}

	@Override
	public String toString() {
		return "Attrs [old=" + old + ", current=" + current + ", nnew=" + nnew + "]";
	}
}
