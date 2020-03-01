package sam.backup.manager.view;

import static sam.backup.manager.api.file.DestinationType.BACKUP;
import static sam.backup.manager.api.file.DestinationType.SOURCE;
import static sam.fx.helpers.FxClassHelper.addClass;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.file.DestinationType;
import sam.backup.manager.api.file.FileTreeManager;
import sam.backup.manager.api.walker.WalkManager;

public abstract class ViewBase extends BorderPane {
	public static final String TEXT_VIEWER = "TEXT_VIEWER";
	
	protected Config config;
	protected Node root;
	protected final FileTreeManager manager;
	protected final ExecutorService executor;
	protected final WalkManager walkManager;

	public ViewBase(FileTreeManager manager, WalkManager walkManager, ExecutorService executor) {
		this.manager = manager;
		this.executor = executor;
		this.walkManager = walkManager;
	}
	
	protected ViewBase init(Config config) {
		this.config = Objects.requireNonNull(config);
		ViewBaseConfig annot = getClass().getAnnotation(ViewBaseConfig.class);
		String s = annot.styleClass();
		if(s != null)
			addClass(this, s);
		
		root = createRoot(config);
		if(annot.setCenter()) 
			setCenter(root);
		
		return this;
	}
	protected abstract Node createRoot(Config metas);

	public static boolean exists(Config f, DestinationType type) {
		if(f == null)
			return false;
		if(type == SOURCE)
			return f.getSource() != null && f.getSource().exists();
		if(type == BACKUP) 
			return f.getTarget() != null && f.getTarget().exists();
		
		throw new IllegalArgumentException("invalid type: "+String.valueOf(type));
	}

}
