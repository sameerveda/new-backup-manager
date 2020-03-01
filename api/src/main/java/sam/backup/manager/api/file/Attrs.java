package sam.backup.manager.api.file;

public interface Attrs {
	public Attr getCurrent();
	public Attr getOld();
	public Attr getNew();
	public boolean isModified();
	public long size();
}
