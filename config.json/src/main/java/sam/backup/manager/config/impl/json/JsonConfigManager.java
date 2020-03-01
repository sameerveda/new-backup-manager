package sam.backup.manager.config.impl.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.gson.Gson;

import sam.backup.manager.api.AppConfigProvider;
import sam.backup.manager.api.FileStoreManager;
import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.config.ConfigManager;
import sam.backup.manager.api.config.ConfigType;
import sam.config.JsonConfig;

@Singleton
class JsonConfigManager implements ConfigManager {
	private final Logger logger;
	private RootConfigImpl config;

	@Inject
	public JsonConfigManager(FileStoreManager fsm, AppConfigProvider configProvider) throws IOException {
		logger = LogManager.getLogger(getClass());

		logger.debug("INIT {}", getClass());

		JsonConfig js = configProvider.get(getClass());
		Path jsonPath = Paths.get(js.getString("config_file"));
		if(!Files.isRegularFile(jsonPath))
			throw new IOException("file not found: \""+jsonPath+"\"");
		
		Path targetDrive = fsm.getTargetDrive();
		Map<String, String> global_vars = new HashMap<>(); 
		global_vars.put("%DETECTED_DRIVE%", targetDrive == null ? "" : targetDrive.toString());

		JSONObject json = new JSONObject(new JSONTokener(Files.newBufferedReader(jsonPath)));
		JSONObject vars = (JSONObject) json.remove("vars");
		vars.keySet().forEach(s -> global_vars.put("%"+s+"%", vars.getString(s)));

		putIfAbsent(json, "name", "root");
		putIfAbsent(json, "source", "root");
		putIfAbsent(json, "target", "root");

		String[] keys = global_vars.keySet().toArray(new String[0]);
		int n = 0;
		while(n++ < 3) {
			for (String s : keys) 
				global_vars.put(s, applyVars(global_vars.get(s), global_vars));
		}

		String jsonString = applyVars(json.toString(), global_vars);
		config = new Gson().fromJson(jsonString, RootConfigImpl.class);
		config.init();
	}

	private void putIfAbsent(JSONObject json, String key, String value) {
		if(!json.has(key)) {
			json.put(key, value);	
		}
	}

	private String applyVars(String target, Map<String, String> vars) {
		String[] result = {target};
		vars.forEach((s,t) -> result[0] = result[0].replace(s, t));
		return result[0];
	}

	@Override
	public List<Config> get(ConfigType type) {
		return Arrays.asList(Objects.requireNonNull(type) == ConfigType.LIST ? config.getLists() : config.getTargets());
	}
}
