package sam.backup.manager.api.config;

import java.nio.file.Path;
import java.util.Map;

@FunctionalInterface
public interface Filter { 
	default Map<String, Object> toMap() { return null; }
	public boolean test(Path p);

	default CombinedFilter or(Filter other) {
		if(other instanceof CombinedFilter) {
			((CombinedFilter)other).add(this);
			return (CombinedFilter)other; 	
		}
		if(this instanceof CombinedFilter) {
			((CombinedFilter)this).add(other);
			return (CombinedFilter)this; 	
		}
		
		CombinedFilter c = new CombinedFilter();
		c.add(other);
		c.add(this);
		
		return c;
	}

}
