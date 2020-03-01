package sam.backup.manager.view;

import sam.backup.manager.api.file.FileEntity;

public interface FilesViewSelector {
	public static FilesViewSelector backup() {
		return new FilesViewSelector() {
			@Override public void set(FileEntity ft, boolean value) { ft.getStatus().setTargetable(value); }
			@Override public boolean isSelectable() { return true; }
			@Override public boolean get(FileEntity file) {return file.getStatus().isTargetable();}
		};
	};
	public static FilesViewSelector delete() {
		return new FilesViewSelector() {
			@Override public void set(FileEntity ft, boolean value) { ft.getStatus().setTargetDeletable(value); }
			@Override public boolean isSelectable() { return true; }
			@Override public boolean get(FileEntity file) {return file.getStatus().isTargetDeletable(); }
		};
	}
	public static FilesViewSelector all() {
		return  new FilesViewSelector() {
			@Override public void set(FileEntity ft, boolean value) { }
			@Override public boolean isSelectable() { return false; }
			@Override public boolean get(FileEntity file) {return false;}
		};
	};
	
	public void set(FileEntity entity, boolean value);
	public boolean isSelectable();
	public boolean get(FileEntity entity);
}