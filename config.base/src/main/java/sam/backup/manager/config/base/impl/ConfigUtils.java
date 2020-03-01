package sam.backup.manager.config.base.impl;

public class ConfigUtils {

	public static <T> T either(T t1, T t2, T defaultValue) {
		if(t1 == null && t2 == null)
			return defaultValue;
		return t1 != null ? t1 : t2;
	}
}
