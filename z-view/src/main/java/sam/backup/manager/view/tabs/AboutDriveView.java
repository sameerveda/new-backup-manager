package sam.backup.manager.view.tabs;

import static sam.fx.helpers.FxClassHelper.addClass;
import static sam.fx.helpers.FxClassHelper.setClass;
import static sam.myutils.MyUtilsBytes.bytesToHumanReadableUnits;

import java.io.IOException;
import java.nio.file.FileStore;

import javax.inject.Singleton;

import javafx.event.EventHandler;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Text;
import sam.backup.manager.api.FileStoreManager;
import sam.backup.manager.api.SelectionListener;
import sam.di.Injector;
import sam.fx.helpers.FxGridPane;
import sam.fx.helpers.FxUtils;
import sam.myutils.MyUtilsException;

@Singleton
public class AboutDriveView extends TilePane implements EventHandler<MouseEvent>, SelectionListener {
	@Override
	public void handle(MouseEvent event) {
		if(event.getClickCount() > 1) {
			FileStoreView fs = FxUtils.find(event.getTarget(), FileStoreView.class);
			if(fs != null)
				fs.update();
		}
	}
	
	private boolean init = false;
	private FileStoreManager fsm;
	
	@Override
	public void selected() {
		if(init)
			return;
		
		init = true;
		setClass(this, "about-drive-view");
		setOnMouseClicked(this);
		
		if(fsm == null)
		    fsm = Injector.getInstance().instance(FileStoreManager.class);
		
		for(FileStore fs: this.fsm.getDrives()) 
			getChildren().add(new FileStoreView(fs));
	}
	
	private class FileStoreView extends GridPane {
		final FileStore fs;
		final Text letter = new Text();
		final ProgressBar free = new ProgressBar();
		final ProgressBar used = new ProgressBar();
		final Text totalT = new Text();
		final Text freeT = new Text();
		final Text usedT = new Text();
		
		public FileStoreView(FileStore fs) {
			this.fs = fs;
			addClass(this, "file-store-view");
			
			setClass(letter, "letter");
			GridPane.setColumnSpan(letter, GridPane.REMAINING);
			addRow(0, letter);
			addRow(2, new Text("Total Space: "), new ProgressBar(1), totalT);
			addRow(3, new Text("Free Space : "), free, freeT);
			addRow(4, new Text("Used Space : "), used, usedT);
			
			ColumnConstraints c = new ColumnConstraints();
			c.setFillWidth(true);
			c.setHgrow(Priority.ALWAYS);
			c.setMaxWidth(Double.MAX_VALUE);
			FxGridPane.setColumnConstraint(this, 1, c);
			
			update();
		}
		private void update() {
			try {
				letter.setText(fs.toString());
				long t = fs.getTotalSpace();
				long f = fs.getUnallocatedSpace();
				long u = t - f;
				
				totalT.setText(bytesToHumanReadableUnits(t, false));
				freeT.setText(bytesToHumanReadableUnits(f, false));
				usedT.setText(bytesToHumanReadableUnits(u, false));
				
				free.setProgress(((double)f)/t);
				used.setProgress(((double)u)/t);
			} catch (IOException e) {
				getChildren().clear();
				add(new TextArea(MyUtilsException.toString(e)), 0, 0, GridPane.REMAINING, GridPane.REMAINING);
			}
		}
	}
}
