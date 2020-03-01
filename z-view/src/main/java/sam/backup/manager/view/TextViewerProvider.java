package sam.backup.manager.view;

import org.codejargon.feather.Provides;

import sam.di.InjectorProvider;
import sam.reference.WeakAndLazy;

public class TextViewerProvider implements InjectorProvider {
	private final WeakAndLazy<TextViewer> val = new WeakAndLazy<>(TextViewer::new);
    
    @Provides
    public TextViewer get() {
        return val.get();
    }
}
