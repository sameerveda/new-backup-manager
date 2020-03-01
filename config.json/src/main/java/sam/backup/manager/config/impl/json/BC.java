package sam.backup.manager.config.impl.json;

import sam.backup.manager.api.config.ConfigType;
import sam.backup.manager.config.base.impl.ConfigImpl;

public class BC extends ConfigImpl {
	private static final long serialVersionUID = 2403164331476074701L;

	@Override
	public ConfigType getType() {
		return ConfigType.BACKUP;
	}

}
