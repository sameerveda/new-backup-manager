package sam.backup.manager.view.list;

import static javafx.application.Platform.runLater;
import static sam.backup.manager.app.UtilsFx.hyperlink;
import static sam.fx.helpers.FxClassHelper.addClass;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import sam.backup.manager.api.PathWrap;
import sam.backup.manager.api.Utils;
import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.file.DestinationType;
import sam.backup.manager.api.file.Dir;
import sam.backup.manager.api.file.FileEntity;
import sam.backup.manager.api.file.FileTreeManager;
import sam.backup.manager.api.file.TreeType;
import sam.backup.manager.api.walker.WalkListener;
import sam.backup.manager.api.walker.WalkManager;
import sam.backup.manager.api.walker.WalkMode;
import sam.backup.manager.app.UtilsFx;
import sam.backup.manager.view.ButtonAction;
import sam.backup.manager.view.ButtonType;
import sam.backup.manager.view.CustomButton;
import sam.backup.manager.view.ViewBase;
import sam.backup.manager.view.ViewBaseConfig;
import sam.console.ANSI;
import sam.fx.alert.FxAlert;
import sam.fx.helpers.FxConstants;
import sam.fx.helpers.FxText;
import sam.myutils.Checker;
import sam.reference.WeakAndLazy;

@ViewBaseConfig(styleClass="listing-view")
public class ListConfigView extends ViewBase {
	private static final Logger LOGGER =  LogManager.getLogger(ListConfigView.class);
	private static final WeakAndLazy<StringBuilder> wsb = new WeakAndLazy<>(StringBuilder::new); 

	public static boolean saveWithoutAsking;
	private Consumer<String> textViewer;
	private final Utils utils;

	@Inject
	public ListConfigView(Utils utils, FileTreeManager ftManager, WalkManager walkManager, ExecutorService executor) {
		super(ftManager, walkManager, executor);
		this.utils = utils;
	}
	
	public ViewBase init(Config config, Consumer<String> textViewer) {
		this.textViewer = textViewer;
		return super.init(config);
	}
	
	@Override
	protected Node createRoot(Config config) {
		return new PerView(config);
	}

	private class PerView extends VBox implements ButtonAction, WalkListener  {
		final Config config;

		private String treeText;
		private CustomButton button;
		private Text fileCountT, dirCountT;
		private int fileCount, dirCount;
		private FutureTask<?> task;

		public PerView(Config config) {
			super(5);
			this.config = config;
			setPadding(FxConstants.INSETS_5);

			Node src = hyperlink(config.getSource());
			addClass(src, "header");

			if(!exists(config, DestinationType.SOURCE)) {
				getChildren().addAll(src, FxText.ofString("Last updated: "+utils.millsToTimeString(manager.lastUpdateTime(config, TreeType.BACKUP))));
				setDisable(true);
			} else {
				button = new CustomButton(ButtonType.WALK, this);
				fileCountT = FxText.text("Files: --", "count-text");
				dirCountT = FxText.text("Dirs: --", "count-text");

				getChildren().addAll(src, fileCountT, dirCountT, FxText.ofString("Last updated: "+utils.millsToTimeString(manager.lastUpdateTime(config, TreeType.LIST))), button);
			}
		}

		@Override
		public void handle(ButtonType type) {
			switch (type) {
				case WALK:
					start();
					break;
				case CANCEL:
					task.cancel(true);
					Platform.runLater(() -> clearTask());
					break;
				case OPEN:
					textViewer.accept(treeText);
					break;
				default:
					throw new IllegalArgumentException(String.valueOf(type));
			}
		}
		
		@Override
		public void onFileFound(FileEntity ft, long size, WalkMode mode) {
			runLater(() -> fileCountT.setText("  Files: "+(++fileCount)));
		}
		@Override
		public void onDirFound(Dir ft, WalkMode mode) {
			runLater(() -> dirCountT.setText("  Dirs: "+(++dirCount)));
		}
		//FIXME @Override
		public void walkCompleted() {
			if(treeText == null) 
				//FIXME treeText = new FileTreeString(config.getFileTree());
			runLater(() -> button.setType(ButtonType.OPEN));
		}
		
		@Override
		public void stateChange(sam.backup.manager.api.walker.State s) {
			// TODO Auto-generated method stub
		}
		
		private void start() {
			UtilsFx.ensureFxThread();
			
			treeText = null;
			 
			int depth = config.getWalkConfig().getDepth(); 
			if(depth <= 0) {
				UtilsFx.showErrorDialog(config.getSource(), "Walk failed: \nbad value for depth: "+depth, null);
				return;
			} else if(depth == 1) {
				StringBuilder sb = wsb.get();
				sb.setLength(0);
				
				try {
					start1Depth(config, sb);
					treeText = sb.toString();
					sb.setLength(0);
				} catch (IOException e) {
					FxAlert.showErrorDialog(config, "failed to walk", e);
				}
			} else {
				if(isRunning())
					throw new IllegalStateException();
				executor.submit(walkManager.newWalkTask(config, TreeType.LIST, WalkMode.SOURCE, this));
			}
		}
		private boolean isRunning() {
			UtilsFx.ensureFxThread();
 			return task != null && !task.isCancelled() && !task.isDone() ;
		}
		private void clearTask() {
			UtilsFx.ensureFxThread();
			
			if(task == null)
				return;
			if(task.isCancelled() || task.isDone())
				task = null;
			else
				throw new IllegalStateException("running");
		}
		
		//FIXME @Override
		public void walkFailed(String reason, Throwable e) {
			LOGGER.info(ANSI.red(reason));
			e.printStackTrace();
		}

		@Override
		public void failed(String msg, Throwable error) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void startWalking(Path path) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void endWalking(Path path) {
			// TODO Auto-generated method stub
			
		}
		
	}

	private static int start1Depth(Config config, StringBuilder sink) throws IOException {
		PathWrap pw = config.getSource();

		if(pw == null || pw.path() == null) 
			throw new IOException("unresolved: "+pw);

		Path root = pw.path(); 
		LOGGER.debug("1-depth walk: "+root);

		if(!Files.isDirectory(root)) 
			throw new FileNotFoundException("dir not found: "+root);
		
		String[] names = root.toFile().list();
		sink.append(pw.string()) .append("\n |");
		
		if(Checker.isEmpty(names))
			return 0; 

		for (int i = 0; i < names.length - 1; i++)
			sink.append(names[i]).append("\n |");

		sink.append(names[names.length - 1]).append('\n');
		sink.append('\n');
		return names.length;
	}

	public Config getConfig() {
		return config;
	}
}
