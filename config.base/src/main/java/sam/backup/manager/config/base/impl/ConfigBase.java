package sam.backup.manager.config.base.impl;

import java.io.Serializable;
import java.nio.file.Path;

import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.config.Filter;

abstract class ConfigBase implements Serializable, Config {
	private static final long serialVersionUID = 1L;

	protected String name;
	protected String source;
	protected String target;
	protected FilterImpl excludes;
	protected FilterImpl targetExcludes;
	protected TargetConfigImpl targetConfig;
	protected WalkConfigImpl walkConfig;

	protected transient Filter excluder, targetExcluder, includer;
	protected transient Path sourceP, targetP;

	protected abstract RootConfig getRoot();
	
	@Override
	public String getName() {
		return name;
	}
	
	public Path getSource() {
		return sourceP != null ? sourceP :  (sourceP = getRoot().pathResolve(source));
	}
	public Path getTarget() {
		if(targetP == null) 
			targetP = target == null ? null : getRoot().pathResolve(target);
		return targetP;
	}

	@Override
	public TargetConfigImpl getTargetConfig() {
		if (this.targetConfig == null)
			targetConfig = new TargetConfigImpl();

		targetConfig.setRootConfig(getRoot().targetConfig);
		return targetConfig;
	}

	@Override
	public WalkConfigImpl getWalkConfig() {
		if (this.walkConfig == null)
			walkConfig = new WalkConfigImpl();

		walkConfig.setRootConfig(getRoot().walkConfig);
		return walkConfig;
	}

	protected void init() {
		for (FilterImpl f : new FilterImpl[]{excludes, targetExcludes}) {
			if (f != null)
				f.setConfig((ConfigImpl) this);	
		}
	}

	protected static Filter combine(Filter root, Filter self) {
		if (root == null && self == null)
			return (p -> false);
		if (root == null)
			return self;
		if (self == null)
			return root;

		return self.or(root);
	}
}
