package sam.backup.manager.config.impl.json;

import sam.backup.manager.api.config.ConfigType;
import sam.backup.manager.config.base.impl.ConfigImpl;

public class LC extends ConfigImpl {
	private static final long serialVersionUID = 2585162545462291870L;

	@Override
	public ConfigType getType() {
		return ConfigType.LIST;
	}

}
