package sam.backup.manager.api.config;

import java.util.Map;

public interface HasFilterArrays {
	public Map<String, String[]> getArrays();
	
	default String asString() {
		StringBuilder sb = new StringBuilder();
		
		getArrays().forEach((s,t) -> {
			sb.append(s).append("  ").append('[');
			if(t != null && t.length != 0) {
				for (String z : t) {
					sb.append(z).append(", ");
				}
				sb.setLength(sb.length() - 2);
			}
			sb.append("]\n");
		});
		
		return sb.toString();
	}
}
