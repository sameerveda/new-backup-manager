package sam.backup.manager.view.list;

import java.util.Collection;

import javax.inject.Provider;
import javax.inject.Singleton;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.config.ConfigManager;
import sam.backup.manager.api.config.ConfigType;
import sam.backup.manager.view.AbstractMainView;
import sam.backup.manager.view.TextViewer;
import sam.di.Injector;
import sam.fx.helpers.FxCss;
import sam.nopkg.EnsureSingleton;

@Singleton
public class ListsViews extends AbstractMainView {
	private static final EnsureSingleton singleton = new EnsureSingleton();
	{singleton.init();}
	
	private Provider<TextViewer> textViewer;  

	@Override
	protected Collection<Config> data(ConfigManager c) {
	    return c.get(ConfigType.LIST);
	}
    @Override
    protected String header(int size) {
        return "Lists ("+size+")";
    }
	@Override
	protected String nothingFoundString() {
		return "NO LIST CONFIG(s) FOUND";
	}
	
	@Override
	protected Node initView(Injector injector, Collection<Config> configs) {
		textViewer = injector.provider(TextViewer.class);
		CheckBox cb = new CheckBox("save without asking");
		cb.setOnAction(e -> ListConfigView.saveWithoutAsking = cb.isSelected());

		HBox buttons = new HBox(10, cb);
		buttons.setPadding(new Insets(2, 5, 2, 5));
		buttons.setBorder(FxCss.border(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new BorderWidths(1, 0, 1, 0)));
		buttons.setAlignment(Pos.CENTER_LEFT);

		VBox root  = new VBox(2);
		ScrollPane rootSp = new ScrollPane(root);

		root.setFillWidth(true);
		rootSp.setFitToWidth(true);
		rootSp.setHbarPolicy(ScrollBarPolicy.NEVER);
		
		configs.forEach(c -> root.getChildren().add(injector.instance(ListConfigView.class).init(c, this::textView)));
		Node node = getTop();
		setTop(null);
		setTop(new BorderPane(node, null, null, buttons, null));
		
		return rootSp;
	}

	private void textView(String s) {
		TextViewer ta = textViewer.get();
		ta.setText(s);

		Node node = getCenter();

		ta.setOnBackAction(() -> setCenter(node));
		setCenter(ta);
	} 
}
