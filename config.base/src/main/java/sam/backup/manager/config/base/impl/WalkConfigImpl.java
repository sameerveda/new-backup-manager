package sam.backup.manager.config.base.impl;

import static sam.backup.manager.config.base.impl.ConfigUtils.either;

import sam.backup.manager.api.config.WalkConfig;

public class WalkConfigImpl implements WalkConfig {
	private Integer depth;
	private Boolean walkTarget;
	private Boolean skipDirNotModified;
	private Boolean skipFiles;
	private Boolean saveExcludeList;

	private transient WalkConfigImpl rootConfig;

	void setRootConfig(WalkConfigImpl rootConfig) {
		this.rootConfig = rootConfig;
	}
	public boolean walkTarget() {
		return either(walkTarget, rootConfig.walkTarget, false);
	}
	public boolean skipDirNotModified() {
		return either(skipDirNotModified, rootConfig.skipDirNotModified, false);
	}
	public boolean skipFiles() {
		return either(skipFiles, rootConfig.skipFiles, false);
	}
	public int getDepth() {
		return depth == null ? Integer.MAX_VALUE : depth;
	}
	public boolean saveExcludeList() {
		return either(saveExcludeList, rootConfig.saveExcludeList, false);
	}

}
