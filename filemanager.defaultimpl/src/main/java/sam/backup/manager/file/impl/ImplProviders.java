package sam.backup.manager.file.impl;

import org.codejargon.feather.Provides;

import sam.backup.manager.api.file.FileTreeManager;
import sam.di.InjectorProvider;

public class ImplProviders implements InjectorProvider {
	
	@Provides
	public FileTreeManager m1(FileTreeManagerImpl impl) {
		return impl;
	}
	
	@Provides
	public Serializer m2(SerializerImpl impl) {
		return impl;
	}
}
