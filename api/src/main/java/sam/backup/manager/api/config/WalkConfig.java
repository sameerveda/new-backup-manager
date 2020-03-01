package sam.backup.manager.api.config;

public interface WalkConfig {
	public boolean walkTarget();
	public boolean skipDirNotModified() ;
	public boolean skipFiles() ;
	public int getDepth() ;
}
