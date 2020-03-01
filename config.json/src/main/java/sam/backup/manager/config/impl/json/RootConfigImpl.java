package sam.backup.manager.config.impl.json;

import sam.backup.manager.api.config.ConfigType;
import sam.backup.manager.api.config.Filter;
import sam.backup.manager.config.base.impl.ConfigImpl;
import sam.backup.manager.config.base.impl.RootConfig;

class RootConfigImpl extends RootConfig {
	private static final long serialVersionUID = 2596515821517696028L;
	
	private BC[] backups;
	private LC[]  lists;

	@Override
	public ConfigType getType() {
		throw new IllegalAccessError();
	}

	@Override
	public Filter getZipSelector() {
		throw new IllegalAccessError();
	}

	@Override
	public boolean isDisabled() {
		throw new IllegalAccessError();
	}

	@Override
	protected ConfigImpl[] backups() {
		return backups;
	}

	@Override
	protected ConfigImpl[] lists() {
		return lists;
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		super.init();
	} 
}
