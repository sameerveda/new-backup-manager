package sam.backup.manager.view.backup;

import static sam.fx.helpers.FxClassHelper.addClass;
import static sam.fx.helpers.FxClassHelper.setClass;

import java.util.List;
import java.util.concurrent.Executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import sam.backup.manager.api.PathWrap;
import sam.backup.manager.api.file.FileEntity;
import sam.backup.manager.api.walker.State;
import sam.backup.manager.app.UtilsFx;
import sam.backup.manager.transfere.TransferEvent;
import sam.backup.manager.transfere.TransferListener;
import sam.backup.manager.transfere.TransferTask;
import sam.backup.manager.view.ButtonType;
import sam.backup.manager.view.CustomButton;
import sam.fx.helpers.FxConstants;
import sam.fx.helpers.FxGridPane;
import sam.fx.helpers.FxHBox;
import sam.fx.helpers.FxText;


@SuppressWarnings("rawtypes")
class TransferView extends BorderPane {
	private static final Logger LOGGER = LogManager.getLogger(TransferView.class);

	private GridPane center;
	private Hyperlink source;
	private Hyperlink target;
	private TextArea progressTA ;
	
	private Text currentProgressT ;
	private Text totalProgressT ;
	private Text stateText ;

	private final Text filesStats = new Text();
	private final CustomButton uploadCancelBtn, filesBtn;
	private ProgressBar currentPB;
	private ProgressBar totalPB;
	
	private final Executor executor;
	private TransferTask task;

	public TransferView(Executor executor) {
		addClass(this, "transfer-view");
		this.executor = executor;

		uploadCancelBtn = new CustomButton(ButtonType.UPLOAD, this::buttonAction);
		filesBtn = new CustomButton(ButtonType.FILES, this::buttonAction);

		HBox buttom = FxHBox.buttonBox(uploadCancelBtn, filesBtn);
		buttom.setAlignment(Pos.CENTER_LEFT);
		
		setTop(new VBox(3, FxText.ofClass("header"), filesStats));
		setBottom(buttom);
	}
	
	private Text text(String text) {
		Text t = new Text(text);
		t.getStyleClass().add("text");
		return t;
	}
	private Text text() {
		return FxText.ofClass("text");
	}
	private Hyperlink link() {
		return new Hyperlink();
	}
	
	@SuppressWarnings("unchecked")
	private class TListener implements TransferListener {
		
		private <E extends FileEntity> void save(List<E> files, String suffix) {
			//FIXME Utils.writeInTempDir(task.getConfig(), "transfer-log-", suffix, new FileTreeString(rootDir, files), LOGGER);
		}
		
		@Override
		public void stateChanged(State s) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void subProgress(FileEntity ft, long read, long total) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void totalProgress(long read, long total) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void generalEvent(TransferEvent type) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void generalEvent(TransferEvent type, TransferEvent subtype, Object attachment) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void start(TransferEvent type, FileEntity f) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void success(TransferEvent type, FileEntity f) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void completed(TransferEvent type, FileEntity f) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void failed(TransferEvent type, FileEntity f, Throwable e) {
			// TODO Auto-generated method stub
			
		}
		
		/* FIXME 
		 * 	@Override
	public void update() {
		if(state == null || isState(State.UPLOADING))
			return;

		transferer.update();

		uploadCancelBtn.setDisable(selectedCount()  - copiedCount() <= 0);
		if(copiedCount() == 0) filesStats.setText(String.format("files: %s (%s files)", bts(filesSelectedSize()), selectedCount()));
		else filesStats.setText(String.format("remaining: %d (%d files), total: %d (%d files)", bts(filesSelectedSize() - copiedSize()), selectedCount() - copiedCount(),  bts(filesSelectedSize()), selectedCount()));
		summery.set(transferer.getFilesSelectedSize(), transferer.getFilesCopiedSize());

		totalProgressFormat = new BasicFormat("Total Progress: {}/"+bts(summery.getTotalSize())+"  | {}/{}/"+selectedCount()+" ({})");
	}
		 */
	}
	
	public void setTask(TransferTask newTask) {
		UtilsFx.ensureFxThread();
		
		if(newTask == null)
			setCenter(null);
		
		if(task != null)
			clear(task);
//FIXME		
	}
	
	private void clear(TransferTask task) {
		
		// TODO remove old task associated view data
		
	}
	
	private void buttonAction(ButtonType type) {
		switch (type) {
			case UPLOAD:
				start();	
				break;
			case CANCEL:
				stop();	
				break;
			case FILES:
				//FIXME FilesView.open("select files to backup", config, fileTree, FilesViewSelector.backup()).setOnCloseRequest(e -> update());	
				break;
			default:
				throw new IllegalStateException("unknown action: "+type);
		}
	}
	public void stop() {
		/* FIXME
		 * setState(CANCELLED);
		uploadCancelBtn.setTransferEvent(ButtonType.UPLOAD);
		//FIXME summery.stop();
		 */
	}
	 
	private void start() {
		if(task == null)
			throw new IllegalStateException();
			
		if(center == null) 
			init();
		
		if(getCenter() != center)
			setCenter(center);
		
		set(source, task.getFileTree().getSourcePath());
		set(target, task.getFileTree().getTargetPath());
		
		task.execute(executor);
		//TODO 
	}
	private void set(Hyperlink link, PathWrap p) {
		link.setText(p == null ? null : p.string());
	}
	
	private GridPane init() {
		currentProgressT = text();
		totalProgressT = text();
		stateText = text();
		currentPB = new ProgressBar(0);
		totalPB = new ProgressBar(0);

		totalPB.setMaxWidth(Double.MAX_VALUE);
		currentPB.setMaxWidth(Double.MAX_VALUE);
		
		center = FxGridPane.gridPane(5);
		source = link();
		target = link();

		progressTA = new TextArea();
		progressTA.setEditable(false);
		progressTA.setPadding(FxConstants.INSETS_5);

		setClass("text", progressTA);
		
		center.addRow(0, text("  source: "), source);
		center.addRow(1, text("  target: "), target);
		center.addRow(2, progressTA);
		
		GridPane.setRowSpan(progressTA, GridPane.REMAINING);
		GridPane.setColumnSpan(progressTA, GridPane.REMAINING);
		
		ColumnConstraints c = new ColumnConstraints();
		c.setFillWidth(true);
		c.setHgrow(Priority.ALWAYS);
		c.setMaxWidth(Double.MAX_VALUE);
		FxGridPane.setColumnConstraint(center, 1, c);
		
		RowConstraints r = new RowConstraints();
		r.setFillHeight(true);
		r.setVgrow(Priority.ALWAYS);
		r.setMaxHeight(Double.MAX_VALUE);
		
		FxGridPane.setRowConstraint(center, 2, r);
		return center;
	}

	/* TODO
	 * 	private State run2() {
		fx(() -> getChildren().setAll(getHeaderText(),progressTA, currentProgressT, currentPB, totalProgressT, totalPB, uploadCancelBtn));

		summery.start();

		try {
			if(transferer.call() == CANCELLED)
				return State.CANCELLED;
		} catch (InterruptedException e) {
			return State.CANCELLED;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		summery.stop();
		return COMPLETED;
	}
	private boolean isState(State s) {
		return s == state.get();
	}
	private void setState(State state) {
		if(isState(COMPLETED))
			throw new IllegalStateException("trying to change state after COMPLETED");

		fx(() -> {
			this.state.set(state);

			if(isState(COMPLETED))
				setCompleted();

			getChildren().remove(stateText);
			if(state == QUEUED) {
				stateText.setText("QUEUED");
				getChildren().add(stateText);
			}
		});
	}
	private void setCompleted() {
		getChildren().clear();
		Pane p = new Pane();
		p.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(p, Priority.ALWAYS);
		HBox top = new HBox(getHeaderText(), p, button("close", "Delete_10px.png", e -> ((Pane)getParent()).getChildren().remove(this)));

		Text  t = new Text(
				new StringBuilder("COMPLETED")
				.append("\nFiles: ").append(copiedCount())
				.append("\nSize: ").append(bts(summery.getTotalSize()))
				.append("\nTime taken: ").append(millisToString(summery.getTimeTaken()))
				.append(summery.getTimeTaken() < 3000 ? "" : "\nAverage Speed: "+bts(summery.getAverageSpeed())+"/s")
				.toString());

		top.setPadding(new Insets(5,0,5,2));
		setClass(t, "completed-text");
		getChildren().addAll(top, t);

		MyUtilsCmd.beep(4);
		fx(() -> FxPopupShop.showHidePopup("transfer completed", 1500));
		startEndAction.onComplete(this);

		if(progressTA != null)
			Utils.writeInTempDir(config, "transfer-log", null, progressTA.getText(), LOGGER);

		progressTA = null;
		currentProgressT = null;
		totalProgressT = null;
		uploadCancelBtn = null;
		currentPB = null;
		totalPB = null;
		totalProgressFormat = null;
		transferer = null;
		state = null;
		startEndAction = null;
		stateText = null;
	}
	@Override
	public void copyStarted(Path src, Path target) {
		fx(() -> progressTA.appendText("src: "+sourceSubpather.apply(src)+"\ntarget: "+targetSubpather.apply(target)+"\n---------\n"));
	}
	@Override
	public void copyCompleted(Path src, Path target) { }
	@Override
	public void addBytesRead(long n) {
		if(isCancelled())
			return;

		summery.update(n);
		updateProgress();
	}

	private volatile BasicFormat totalProgressFormat;
	private volatile LongConsumer currentProgressFormat;

	private void updateProgress() {
		fx(() -> {
			setProgressBar(currentPB, bytesRead(), currentSize());
			setProgressBar(totalPB, summery.getBytesRead(), summery.getTotalSize());
			currentProgressFormat.accept(bytesRead());
			totalProgressT.setText(totalProgressFormat.format(bts(copiedSize()), copiedCount(), selectedCount() - copiedCount(), speed()));
		});
	}

	private void setProgressBar(ProgressBar bar, long current, long total) {
		bar.setProgress(divide(current, total));
	}
	@Override
	public void newTask() {
		String s = "/"+bts(currentSize());
		currentProgressFormat = bytes -> currentProgressT.setText(bts(bytes)+s);
		updateProgress();
	}

	private long currentSize() {
		return transferer.getCurrentFileSize();
	}
	private String bts(long size) {
		return bytesToString(size);
	}
	private int copiedCount() { return transferer.getFilesCopiedCount(); }
	private int selectedCount() { return transferer.getFilesSelectedCount(); }
	private long copiedSize() { return transferer.getFilesCopiedSize(); }
	private long filesSelectedSize() { return transferer.getFilesSelectedSize(); }
	private String speed() { return summery.getSpeedString(); }
	private long bytesRead() { return transferer.getCurrentBytesRead(); }
	 */


}

