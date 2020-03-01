package sam.backup.manager.api.walker;

public enum WalkMode {
	SOURCE, BACKUP, BOTH;

	public boolean isSource() {
		return this == BOTH || this == SOURCE;
	}
	public boolean isTarget() {
		return this == BOTH || this == BACKUP;
	}

}
