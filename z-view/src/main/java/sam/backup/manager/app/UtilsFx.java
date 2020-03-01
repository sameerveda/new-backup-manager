package sam.backup.manager.app;

import static javafx.application.Platform.runLater;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import sam.backup.manager.api.PathWrap;
import sam.di.Injector;
import sam.fx.alert.FxAlert;
import sam.fx.helpers.FxHyperlink;
import sam.fx.helpers.FxUtils;
import sam.myutils.Checker;
import sam.reference.WeakAndLazy;

public final class UtilsFx {
	private static final Logger logger = LogManager.getLogger(UtilsFx.class);
	
	public static Stage showStage(Parent content) {
		Window window = rootWindow();
		Stage stg = new Stage();
		stg.initModality(Modality.WINDOW_MODAL);
		stg.initStyle(StageStyle.UTILITY);
		stg.initOwner(window);
		stg.setScene(new Scene(content));
		stg.getScene().getStylesheets().setAll(window.getScene().getStylesheets());
		stg.setWidth(300);
		stg.setHeight(400);
		runLater(stg::show);

		return stg;
	}

	public static Window rootWindow() {
		return Injector.getInstance().instance(Stage.class, App.ROOT_STAGE_KEY);
	}

	public static void showErrorDialog(Object text, String header, Exception error) {
		runLater(() -> FxAlert.showErrorDialog(text, header, error));
	}
	public static FileChooser selectFile(File expectedDir, String expectedName, String title) {
		return FxUtils.fileChooser(expectedDir, expectedName, title, null);
	}
	
	private static final WeakAndLazy<FXMLLoader> fxkeep = new WeakAndLazy<>(FXMLLoader::new);
	

	public static void fxml(String filename, Object root, Object controller) {
		try {
			FXMLLoader fx = fxkeep.get();
			fx.setLocation(ClassLoader.getSystemResource(filename));
			fx.setController(controller);
			fx.setRoot(root);
			fx.load();
		} catch (IOException e) {
			showErrorAndWait(null, "fxml failed: " + filename, e);
			System.exit(0);
		}
	}

	public static void showErrorAndWait(Object text, Object header, Exception e) {
		AtomicBoolean b = new AtomicBoolean();
		runLater(() -> {
			FxAlert.showErrorDialog(text, header, e);
			b.set(true);
		});
		waitUntil(b);
	}
	private static void waitUntil(AtomicBoolean stopper) {
		while (!stopper.get()) { }
	}
	public static void fxml(Object parentclass, Object root, Object controller) {
		fxml("fxml/" + parentclass.getClass().getSimpleName() + ".fxml", root, controller);
	}

	public static void fxml(Object obj) {
		fxml(obj, obj, obj);
	}

	public static void stylesheet(Parent node) {
		String name = "stylesheet/" + node.getClass().getSimpleName() + ".css";
		URL url = ClassLoader.getSystemResource(name);
		if (url == null) {
			logger.error("stylesheet not found: " + name);
			return;
		}
		node.getStylesheets().add(url.toExternalForm());
	}

	public static Node hyperlink(PathWrap wrap) {
		if(wrap != null && wrap.path() != null) {
			Hyperlink hyperlink = FxHyperlink.of(wrap.path());
			hyperlink.setMinWidth(400);
			return hyperlink;
		} else {
			Text t = new Text(wrap == null ? "--" : wrap.string());
			t.setDisable(true);
			return t;
		}
	}
	public static Node hyperlink(List<PathWrap> wraps) {
		if(Checker.isEmpty(wraps))
			return hyperlink((PathWrap)null);
		if(wraps.size() == 1)
			return hyperlink(wraps.get(0));
		VBox box = new VBox(2);
		wraps.forEach(p -> box.getChildren().add(hyperlink(p)));
		
		return box;
	}
	public static Node button(String tooltip, String icon, EventHandler<ActionEvent> action) {
		Button button = new Button();
		button.getStyleClass().clear();
		button.setTooltip(new Tooltip(tooltip));
		button.setGraphic(new ImageView(icon));
		button.setOnAction(action);
		return button;
	}

	public static Node bigPlaceholder(String text) {
		Label l = new Label(text);
		l.getStyleClass().add("big-placeholder");
		return l;
	}

	public static Node headerBanner(String header) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void ensureFxThread() {
		// TODO Auto-generated method stub
		
	}
}
