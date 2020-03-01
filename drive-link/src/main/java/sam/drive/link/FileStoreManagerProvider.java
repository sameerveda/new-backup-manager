package sam.drive.link;

import org.codejargon.feather.Provides;

import sam.backup.manager.api.FileStoreManager;
import sam.di.InjectorProvider;

public class FileStoreManagerProvider implements InjectorProvider {
	@Provides
	FileStoreManager get(FileStoreManagerImpl impl) {
		return impl;
	}
}
