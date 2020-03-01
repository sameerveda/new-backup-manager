package sam.backup.manager.view.backup;

import static javafx.application.Platform.runLater;
import static sam.backup.manager.app.UtilsFx.hyperlink;
import static sam.backup.manager.view.ButtonType.DELETE;
import static sam.backup.manager.view.ButtonType.FILES;
import static sam.backup.manager.view.ButtonType.WALK;
import static sam.fx.helpers.FxClassHelper.addClass;
import static sam.fx.helpers.FxClassHelper.removeClass;
import static sam.fx.helpers.FxMenu.menuitem;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import sam.backup.manager.api.PathWrap;
import sam.backup.manager.api.Utils;
import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.file.DestinationType;
import sam.backup.manager.api.file.Dir;
import sam.backup.manager.api.file.FileEntity;
import sam.backup.manager.api.file.FileTree;
import sam.backup.manager.api.file.FileTreeManager;
import sam.backup.manager.api.file.FilteredDir;
import sam.backup.manager.api.file.ForcedMarkable;
import sam.backup.manager.api.file.TreeType;
import sam.backup.manager.api.walker.State;
import sam.backup.manager.api.walker.WalkListener;
import sam.backup.manager.api.walker.WalkManager;
import sam.backup.manager.api.walker.WalkMode;
import sam.backup.manager.app.UtilsErrorHandled;
import sam.backup.manager.view.ButtonAction;
import sam.backup.manager.view.ButtonType;
import sam.backup.manager.view.CustomButton;
import sam.backup.manager.view.Deleter;
import sam.backup.manager.view.FilesView;
import sam.backup.manager.view.FilesViewSelector;
import sam.backup.manager.view.ViewBase;
import sam.backup.manager.view.ViewBaseConfig;
import sam.fx.helpers.FxConstants;
import sam.fx.helpers.FxGridPane;
import sam.fx.helpers.FxLabel;
import sam.fx.helpers.FxText;
import sam.nopkg.Junk;

@ViewBaseConfig(styleClass="config-view", setCenter=false)
class TargetView extends ViewBase {
	private final Logger LOGGER = LogManager.getLogger(TargetView.class);

	private final SimpleObjectProperty<FileTree> currentFileTree = new SimpleObjectProperty<>();
	private Provider<Deleter> deleter;
	private final WalkManager walkManager;
	private Path _tempDir;
	private final Utils utils;
	private final UtilsErrorHandled utilsErrorHandled;

	@Inject
	public TargetView(Utils utils, FileTreeManager factory, WalkManager walkManager, ExecutorService executor, UtilsErrorHandled utilsErrorHandled, Provider<Deleter> deleter) {
		super(factory, walkManager, executor);
		this.walkManager = walkManager;
		this.utils = utils;
		this.utilsErrorHandled = utilsErrorHandled;
		this.deleter = deleter;
	}
	
	protected ViewBase init(Config config) {
		Label l = FxLabel.label(config.getName(),"title");
		l.setMaxWidth(Double.MAX_VALUE);
		setTop(l);
		
		l.setOnMouseClicked(e -> {
			if(getCenter() == null)
				setCenter(root);
			else 
				getChildren().remove(root);
		});
		
		return super.init(config);
	}
	
	private Path tempDir() {
		if(_tempDir == null)
			_tempDir = utils.tempDirFor(config);
		
		return _tempDir;
	}
	
	@Override
	protected Node createRoot(Config config) {
		return new MetaTabContent(config);
	}
	
	private class MetaTab extends Tab {
		public MetaTab(Config ft) {
			getStyleClass().add("meta-tab");
			setText(title(ft));
			setContent(new MetaTabContent(ft));
		}

		private String title(Config ft) {
			return ft.toString(); //FIXME
		}
	}
	
	private class MetaTabContent extends BorderPane implements ButtonAction, WalkListener  {
		final Config config;
		final Text bottomText;
		
		private final CustomButton files = new CustomButton(FILES, this);
		private final CustomButton delete = new CustomButton(DELETE, this);
		private final CustomButton walk = new CustomButton(WALK, this); 
		
		private final Text sourceSizeT, targetSizeT, sourceFileCountT; 
		private final Text sourceDirCountT, targetFileCountT, targetDirCountT;
		private final Text targetSizeT, targetFileCountT;
		
		private final SimpleObjectProperty<FilteredDir>  targetFFT = new SimpleObjectProperty<>();
		private final SimpleObjectProperty<FilteredDir>  deleteFFT = new SimpleObjectProperty<>();

		public MetaTabContent(Config meta) {
			setPadding(FxConstants.INSETS_10);
			this.config = meta;
			
			addClass(this, "meta-content");
			// TODO setContextMenu();
			
			PathWrap source = meta.getSource();
			PathWrap target = meta.getTarget();
			long lastUpdated = manager.lastUpdateTime(meta, TreeType.BACKUP);
			
			VBox top = new VBox(5,
					hbox("Source: ",source),
					hbox("Target: ",target) 
					);
			
			setTop(top);

			Label summeyLabel = FxLabel.label("SUMMERY", "summery");
			summeyLabel.setMaxWidth(Double.MAX_VALUE);
			summeyLabel.setAlignment(Pos.CENTER);
			
			sourceSizeT = text("---");
			sourceFileCountT = text("---");
			sourceDirCountT = text("---"); 

			String st = config.getWalkConfig().walkTarget() ? "--" : "N/A";
			targetSizeT = text(st); 
			targetFileCountT = text(st); 
			targetDirCountT = text(st);

			targetSizeT = text("---");
			targetFileCountT = text("---");
			
			GridPane tiles = FxGridPane.gridPane(15, 5);
			int row = 0;
			tiles.addRow(row++, text("Last updated: "), colSpan(text(lastUpdated <= 0 ? "N/A" : utils.millsToTimeString(lastUpdated)), GridPane.REMAINING));
			tiles.add(summeyLabel, 0, row++, GridPane.REMAINING, 1);
			tiles.addRow(row++, colHeaderText(""), header("Source"), header("Target"), header("New/Modified"));
			tiles.addRow(row++, colHeaderText(" size |"), sourceSizeT, targetSizeT, targetSizeT);
			tiles.addRow(row++, colHeaderText("files |"), sourceFileCountT, targetFileCountT, targetFileCountT);
			tiles.addRow(row++, colHeaderText(" dirs |"), sourceDirCountT, targetDirCountT);
			
			setCenter(tiles);
			bottomText = new Text();

			if(!exists(meta, DestinationType.SOURCE)) {
				setBottom(bottomText);
				finish("Source not found", true);
			} else {
				HBox buttons = new HBox(5, walk, files, delete, bottomText);
				buttons.setDisable(true); //TODO remove, when app is working
				setBottom(buttons);
				files.setVisible(false);
				delete.setVisible(false);
			}
			
			BorderPane.setMargin(getBottom(), new Insets(15, 5, 0, 5));
			
		}
		
		private Node colHeaderText(String string) {
			Text text = new Text(string);
			GridPane.setHalignment(text, HPos.RIGHT);
			return text;
		}

		private Node hbox(String title, PathWrap p) {
			Text text = new Text(title);
			Node link = hyperlink(p);
			
			HBox hbox = new HBox(5, text, link);
			hbox.setAlignment(Pos.CENTER_LEFT);
			HBox.setHgrow(link, Priority.ALWAYS);
			
			return hbox;
		}

		public void finish(String msg, boolean failed) {
			removeClass(this, "disable", "completed");
			removeClass(bottomText, "disable-text", "completed-text");
			String s = failed ? "disable" : "completed";
			addClass(this, s);
			addClass(bottomText, s+"-text");
			bottomText.setText(msg);
		}
		
		private Node colSpan(Node node, int colSpan) {
			GridPane.setColumnSpan(node, colSpan); 
			return node;
		}

		@Override
		public void handle(ButtonType type) {
			FilesView view;
			
			switch (type) {
				case FILES:
					view = openFilesView("select files to backup", targetFFT.get(), FilesViewSelector.backup());
					break;
				case DELETE:
					view = openFilesView("select files to delete", deleteFFT.get(), FilesViewSelector.delete());
					view.setButtons(new CustomButton(ButtonType.DELETE, e -> deleteAction()));
					break;
				case WALK:
					startWalk();
					break;
				case CANCEL:
					cancelWalk();
					break;
				case SET_MODIFIED:
					throw new IllegalStateException("not yet implemented");
				default:
					throw new IllegalArgumentException("unknown action: "+type);
			}
		}
		
		@SuppressWarnings("unchecked")
		private void cancelWalk() {
			Future<FileTree> task = (Future<FileTree>) walk.getUserData() ;
			if(task == null)
				return;
			
			walk.setUserData(null);
			task.cancel(true);
		}
		
		private void startWalk() {
			if(walk.getUserData() != null)
				return;
			
			walk.setType(ButtonType.LOADING);
			walk.setDisable(true);
			
			Future<FileTree> future = executor.submit(walkManager.newWalkTask(config, TreeType.BACKUP, WalkMode.BOTH, this));
			walk.setUserData(future);
			
			walk.setType(ButtonType.CANCEL);
			walk.setDisable(false);
		}

		public boolean hashTargets() {
			return targetFFT.get() != null && !targetFFT.get().isEmpty();
		}
		public FilteredDir getTargetFileTree() {
			return targetFFT.get();
		}
		public FilteredDir getDeleteFileTree() {
			return deleteFFT.get();
		}
		public boolean hashDeleteTargets() {
			return deleteFFT.get() != null && !deleteFFT.get().isEmpty();
		}
		public FilteredDir getDeleteTargets() {
			return deleteFFT.get();
		}
		
		private volatile long sourceSize, targetSize;
		private volatile int sourceFileCount, sourceDirCount, targetFileCount, targetDirCount;

		@Override
		public void onFileFound(FileEntity ft, long size, WalkMode mode) {
			runLater(() -> {
				if(mode == WalkMode.SOURCE) {
					sourceSizeT.setText(utils.bytesToString(sourceSize += size));
					sourceFileCountT.setText(utils.toString(++sourceFileCount));
				} else if(mode == WalkMode.BACKUP){
					targetSizeT.setText(utils.bytesToString(targetSize += size));
					targetFileCountT.setText(utils.toString(++targetFileCount));
				} else {
					throw new IllegalStateException("invalid walkMode: "+mode);
				}
			});
		}
		@Override
		public void onDirFound(Dir ft, WalkMode mode) {
			runLater(() -> {
				if(mode == WalkMode.SOURCE) 
					sourceDirCountT.setText(utils.toString(++sourceDirCount));
				else if(mode == WalkMode.BACKUP)
					targetDirCountT.setText(utils.toString(++targetDirCount));
				else 
					throw new IllegalStateException("invalid walkMode: "+mode);
			});
		}

		private void updateDeleteCounts(FilteredDir deleteFT) {
			runLater(() -> delete.setVisible(true));
		}
		private void updateTargetCounts(FilteredDir target) {
			runLater(() -> files.setVisible(true));

			long[] l = {0,0};
			walk(target, l);
			runLater(() -> {
				targetSizeT.setText(utils.bytesToString(l[1]));
				targetFileCountT.setText(utils.toString((int)l[0]));
			});
		}
		private void walk(Dir backup, long[] l) {
			for (FileEntity f : backup) {
				if(f.isDirectory())
					walk((Dir)f, l);
				else {
					l[0]++;
					l[1] += f.getSourceAttrs().size();	
				}
			}
		}
		
		/* TODO private void allfilesAction(ActionEvent e) {
			if(targetFFT.get() == null)
				if(!loadFileTree())
					return;
			openFilesView("all files", null, FilesViewSelector.all());
		}
		private void setAsLatestAction(ActionEvent e) {
			((ForcedMarkable)fileTree()).forcedMarkUpdated();
		};
		
		private void setContextMenu() {
			setOnContextMenuRequested(e -> {
				ContextMenu menu = new ContextMenu( 
						menuitem("Set as latest", this::setAsLatestAction, targetFFT.isNull().or(Bindings.createBooleanBinding(() -> fileTree() == null || !(fileTree() instanceof ForcedMarkable), currentFileTree))),
						menuitem("All files", this::allfilesAction)
						) ;
				menu.show(this, e.getScreenX(), e.getScreenY());
			});
		}*/
		
		private void deleteAction() {
			utilsErrorHandled.withWriter(tempDir().resolve("delete.txt"), true, "failed to write deletes", w -> {
				w.append(LocalDateTime.now().toString()).append("\n\n");
				manager.writeFileTreeAsString(deleteFFT.get(), w);
				w.append("\n-------------------------------------------\n");
			});
			
			deleter.get().start(fileTree(), deleteFFT.get());
		}

		private FileTree fileTree() {
			return manager.getFileTreeFor(config, TreeType.BACKUP);
		}

		@Override
		public void stateChange(State s) {
			// TODO Auto-generated method stub
			
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
	
	private FilesView openFilesView(String title, Dir dir, FilesViewSelector selector) {
		
		// FIXME Auto-generated method stub
		return Junk.notYetImplemented();
	}
		private Node header(String string) {
		return addClass(new Label(string), "text", "header");
	}
	private Text text(String str) {
		return FxText.text(str, "text");
	}
	
	/* FIXME
	 * 	@Override
	public boolean isCancelled() {
		return cancel.get();
	}
	@Override
	public void stop() {
		cancel.set(true);
		walk.setType(ButtonType.WALK);
	}
	@Override
	public void start() {
		if(isCompleted())
			return;

		cancel.set(false);
		walk.setType(ButtonType.CANCEL);
		startEndAction.start(this);
	}
	public boolean isCompleted() {
		return walk != null && walk.isDisable();
	}
	public Config getConfig() {
		return config;
	}
	public void setError(String msg, Exception e) {
		if(e != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);

			finish(msg+"\n\n"+sw, true);
		}
		else 
			finish(msg, true);
	}
	@Override
	public void walkFailed(String reason, Throwable e) {
		finish(reason, true);
		if(e != null)
			e.printStackTrace();
	}
	
		@Override
	public void walkCompleted() {
		FilteredDir backup =  fileTree().filtered(f -> f.getStatus().isTargetable());
		FilteredDir delete = !config.getTargetConfig().hardSync() ? null :  fileTree().filtered(f -> f.getStatus().isTargetDeletable());

		fx(() -> {
			targetFFT.set(backup);
			deleteFFT.set(delete);
			walk.setVisible(false);
		});

		if(!backup.isEmpty())
			updateTargetCounts(backup);
		else
			finish("Nothing to backup/delete", false);

		if(delete != null && !delete.isEmpty())
			updateDeleteCounts(delete);

		fx(() -> startEndAction.onComplete(this));
		
		// task is WalkTask which is completed
		List<Path> exucludePaths = task.getExucludePaths(); 
		
		if(!exucludePaths.isEmpty() && saveExcludeList)
		  utils.saveInTempDirHideError(new PathListToFileTree(exucludePaths), config, "excluded", src.getFileName()+".txt");

	}
	 */
}
