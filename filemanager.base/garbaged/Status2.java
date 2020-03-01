package sam.backup.manager.file.base;

import sam.backup.manager.api.file.Status;

interface Status2 extends Status {
	public static final int BACKUPABLE = 0;
	public static final int COPIED = 1;
	public static final int BACKUP_DELETABLE = 2;
	
	public static final int SIZE = 3;
	
	void set(int type, boolean value);
	boolean get(int type);
	void setTargetReason(String reason);

	@Override
	default void setTargetDeletable(boolean b) {
		set(BACKUP_DELETABLE, b);
	}
	@Override
	default boolean isTargetDeletable() {
		return get(BACKUP_DELETABLE);
	}
	@Override
	default boolean isCopied() {
		return get(COPIED);
	}
	@Override
	default boolean isTargetable() {
		return get(BACKUPABLE);
	}

	@Override
	default void setCopied(boolean b) {
		set(COPIED, b);
	}

	@Override
	default void setTargetable(boolean b) {
		set(BACKUPABLE, b);
	}
	@Override
	default void setTargetable(boolean b, String reason) {
		set(BACKUPABLE, b);
		setTargetReason(reason);
	}
}
