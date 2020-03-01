package sam.backup.manager.config.base.impl;

import java.io.Serializable;

import sam.backup.manager.api.config.Filter;

public abstract class ConfigImpl extends ConfigBase implements Serializable {
	private static final long serialVersionUID = 1L;
	
	protected String source;
	protected String target;
	protected boolean disable;
	protected FilterImpl zip;

	protected transient RootConfig root;

	public ConfigImpl() {}

	public void init(RootConfig rootConfig) {
		this.root = rootConfig;
		if(zip != null) 
			zip.setConfig(this);

		super.init();
	}

	@Override
	protected RootConfig getRoot() {
		return root;
	}
	@Override
	public Filter getSourceExcluder() {
		if(excluder != null) return excluder;
		return excluder = combine(root.getSourceExcluder(), excludes);
	}
	@Override
	public Filter getTargetExcluder() {
		if(targetExcluder != null) return targetExcluder;
		return targetExcluder = combine(root.getTargetExcluder(), targetExcludes);
	}
	@Override public boolean isDisabled() {
		return disable;
	}
	@Override public FilterImpl getZipSelector() {
		return zip;
	}
	@Override
	public String toString() {
		return "Config [name=" + name + ", source=" + source + ", target=" + target + ", disable=" + disable + "]";
	}
}
