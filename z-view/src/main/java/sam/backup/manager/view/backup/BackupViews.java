package sam.backup.manager.view.backup;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import javax.inject.Provider;
import javax.inject.Singleton;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import sam.backup.manager.api.AppConfigProvider;
import sam.backup.manager.api.Utils;
import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.config.ConfigManager;
import sam.backup.manager.api.config.ConfigType;
import sam.backup.manager.api.file.FileTreeManager;
import sam.backup.manager.api.walker.WalkManager;
import sam.backup.manager.view.AbstractMainView;
import sam.backup.manager.view.Deleter;
import sam.config.JsonConfig;
import sam.di.Injector;
import sam.nopkg.EnsureSingleton;
import sam.reference.WeakAndLazy;

@Singleton
public class TargetViews extends AbstractMainView {
	private static final EnsureSingleton singleton = new EnsureSingleton();
	{ singleton.init(); }
	
	@Override
	protected Node initView(Injector injector, Collection<Config> configs) {
		VBox root = new VBox();
		configs.forEach(c -> root.getChildren().add(injector.instance(TargetView.class).init(c)));
		
		ScrollPane scroll = new ScrollPane(root);
		scroll.setFitToHeight(true);
		scroll.setFitToWidth(true);
		return scroll;
	}
	
	@Override
	protected Collection<Config> data(ConfigManager c) {
	    return c.get(ConfigType.BACKUP);
	}
	@Override
	protected String header(int size) {
		return "Targets ("+size+")";
	}
	@Override
	protected String nothingFoundString() {
		return "NO BACKUP CONFIG(s) FOUND";
	}
	/* FIXME 
	 * private final IStartOnComplete<TransferView>  transferAction = new IStartOnComplete<TransferView>() {
		@Override
		public void start(TransferView view) {
			fx.runAsync(view);
			statusView.addSummery(view.getSummery());
		}
		@Override
		public void onComplete(TransferView view) {
			statusView.removeSummery(view.getSummery());
			utils.putTargetLastPerformed("backup:"+view.getConfig().getSource(), System.currentTimeMillis());
			try {
				view.getConfig().getFileTree().save();
			} catch (Exception e) {
				FxAlert.showErrorDialog(view.getConfig()+"\n"+view.getConfig().getFileTree(), "Failed to save filetree", e);
			}
		}
	};
	public void start(TargetView view) {
		if(!view.getConfig().isDisabled()) {
			Config c = view.getConfig();
			if(view.loadFileTree()) // FIXME
				fx.runAsync(new WalkTask(c, WalkMode.BOTH, view, view));
		}
	}
	@Override
	public void onComplete(TargetView view) {
		if(view.hashTargets())
			fx(() -> {
				TransferView v = new TransferView(view.getConfig(), view.getTargetFileTree(), statusView, transferAction);
				centerView.add(v);
				v.stateProperty().addListener((p, o, n) -> {
					view.setDisable(n == State.QUEUED || n == State.UPLOADING);
					if(n == State.COMPLETED)
						view.finish("ALL COPIED", false);
				});
			});
		else {
			putTargetLastPerformed("backup:"+view.getConfig().getSource(), System.currentTimeMillis());
		}
	}
	 */
}
