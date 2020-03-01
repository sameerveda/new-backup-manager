package sam.backup.manager.walker;

import org.codejargon.feather.Provides;

import sam.backup.manager.api.walker.WalkManager;
import sam.di.InjectorProvider;

public class WalkInjectProvider implements InjectorProvider {
	@Provides
	WalkManager manager(WalkManagerImpl impl) {
		return impl;
	}
}
