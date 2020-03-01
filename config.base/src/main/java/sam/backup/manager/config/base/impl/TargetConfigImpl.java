package sam.backup.manager.config.base.impl;

import static sam.backup.manager.config.base.impl.ConfigUtils.either;

import sam.backup.manager.api.config.TargetConfig;

public class TargetConfigImpl implements TargetConfig {
	private Boolean checkModified, hardSync;
	
	private transient TargetConfigImpl rootConfig;
	
	void setRootConfig(TargetConfigImpl rootConfig) {
		this.rootConfig = rootConfig;
	}
	
	TargetConfigImpl() {}

	@Override
	public boolean checkModified() {
		return either(checkModified, rootConfig.checkModified, true);
	}
	@Override
	public boolean hardSync() {
		return either(hardSync, rootConfig.hardSync, false);
	}
}
