package sam.backup.manager.file.base;


import sam.backup.manager.api.file.Attr;
import sam.myutils.MyUtilsBytes;

public class AttrImpl implements Attr {
	public static final AttrImpl EMPTY = new AttrImpl(0, 0);
	
	public final long lastModified;
	public final long size;
	
	public AttrImpl(long lastModified, long size){
		this.lastModified = lastModified;
		this.size = size;
	}
	public AttrImpl(Attr from){
		this.lastModified = from.lastModified();
		this.size = from.size();
	}
	
	@Override
	public String toString() {
		return "Attr [lastModified=" + lastModified + ", size=" + size+"("+MyUtilsBytes.bytesToHumanReadableUnits(size, false) +")]";
	}
	public long size() {
		return size;
	}
	public long lastModified() {
		return lastModified;
	}
	
	public boolean equals(AttrImpl other) {
		return other == this || (other.lastModified == lastModified && other.size == size); 
	}
}
 
