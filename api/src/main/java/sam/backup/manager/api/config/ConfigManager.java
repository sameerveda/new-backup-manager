package sam.backup.manager.api.config;

import java.util.Collection;

import sam.nopkg.Junk;

public interface ConfigManager {
	Collection<Config> get(ConfigType type);
	
	public static <T> T either(T t1, T t2, T defaultValue) {
		if(t1 == null && t2 == null)
			return defaultValue;
		return t1 != null ? t1 : t2;
	}
	default String key(ConfigType type, Config config) {
		// TODO Auto-generated method stub
		return Junk.notYetImplemented();
	}
} 