package sam.backup.manager.api.config;

import java.nio.file.Path;

public interface Config {
	public String getName() ;
	public Path getSource();
	public Path getTarget();
	public TargetConfig getTargetConfig() ;
	public WalkConfig getWalkConfig() ;
	public boolean isDisabled();
	public Filter getSourceExcluder();
	public Filter getTargetExcluder();
	public Filter getZipSelector();
	public ConfigType getType();
}


