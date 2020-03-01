package sam.backup.manager.app;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codejargon.feather.Provides;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.javafx.application.LauncherImpl;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import sam.backup.manager.api.SelectionListener;
import sam.backup.manager.api.Utils;
import sam.backup.manager.impl.Impl;
import sam.config.LoadConfig;
import sam.di.FeatherInjector;
import sam.di.Injector;
import sam.di.InjectorProvider;
import sam.fx.alert.FxAlert;
import sam.fx.developer.utils.CssLiveReload;
import sam.fx.popup.FxPopupShop;
import sam.myutils.Checker;

@SuppressWarnings("restriction")
public class App extends Application {
	public static final String ROOT_STAGE_KEY = "ROOT_STAGE";

	private final Logger logger = LogManager.getLogger(App.class);

	private final HBox tabs_box = new HBox(3);
	private final BorderPane root = new BorderPane();
	private final Scene scene = new Scene(root);
	private Stage stage;
	private Tab selected_tab;
	private Impl impl; 
	
	public App() {
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void init() throws Exception {
		List providers = InjectorProvider.detect();
		providers.addAll(Arrays.asList(this, this.impl = new Impl(1) {}));
		Injector.init(new FeatherInjector(providers));

		tabs_box.getStyleClass().add("tabs_box");

		JSONObject tabsJson = Injector.getInstance().instance(Utils.class).jsonObjectFromResource("tabs.json");
		JSONArray tabs = tabsJson.getJSONArray("tabs");
		for (int i = 0; i < tabs.length(); i++) {
			JSONObject json = tabs.getJSONObject(i);
			if (!json.optBoolean("disable", false))
				tabs_box.getChildren().add(new Tab(json));
		}

		root.setTop(tabs_box);
	}

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		FxAlert.setParent(stage);
		FxPopupShop.setParent(stage);

		scene.getStylesheets().add("css/styles.css");

		tabs_box.getChildren().stream().map(Tab.class::cast).map(t -> t.config.optString("css"))
				.filter(Checker::isNotEmptyTrimmed).forEach(scene.getStylesheets()::add);

		stage.setScene(scene);
		stage.setWidth(500);
		stage.setHeight(500);
		stage.show();

		if (tabs_box.getChildren().isEmpty())
			root.setCenter(UtilsFx.bigPlaceholder("NO TABS SPECIFIED"));
		else {
			((Tab) tabs_box.getChildren().get(0)).fire();
		}

		CssLiveReload.start(stage.getScene().getStylesheets(), Paths.get(
				"D:\\importents_are_here\\eclipse_workplace\\Grouped-Projects\\backup-manager\\view\\src\\main\\resources\\css"),
				"styles.css");
	}

	private class Tab extends Button {
		final JSONObject config;
		Node node;

		public Tab(JSONObject json) {
			this.config = json;
			setText(config.getString("title"));
			getStyleClass().setAll("sam-tab");

			setOnAction(e -> select());
		}

		public void select() {
			try {
				if (node == null) {
					try {
						node = (Node) Injector.getInstance().instance(Class.forName(config.getString("class")));
					} catch (Throwable e) {
						FxAlert.showErrorDialog(config, "failed to init tab", e);
						tabs_box.getChildren().remove(this);
						return;
					}
				}

				root.setCenter(node);
				getStyleClass().add("selected");

				if (selected_tab != null)
					selected_tab.getStyleClass().remove("selected");
				selected_tab = this;

				if (node instanceof SelectionListener)
					((SelectionListener) node).selected();
			} catch (Exception e) {
				logger.error("failed to select: {}, config: {}", node, config, e);
				FxAlert.showErrorDialog(node + "\n" + config, "failed to select", e);
			}
		}
	}

	@Override
	public void stop() throws Exception {
		try {
			impl.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

	@Provides
	@Named(ROOT_STAGE_KEY)
	public Stage stage() {
		return stage;
	}
}
