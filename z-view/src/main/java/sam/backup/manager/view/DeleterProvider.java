package sam.backup.manager.view;

import org.codejargon.feather.Provides;

import sam.di.InjectorProvider;
import sam.reference.WeakAndLazy;

public class DeleterProvider implements InjectorProvider {
	private final WeakAndLazy<Deleter> val = new WeakAndLazy<>(Deleter::new);
    
    @Provides
    public Deleter get() {
        return val.get();
    }
}
