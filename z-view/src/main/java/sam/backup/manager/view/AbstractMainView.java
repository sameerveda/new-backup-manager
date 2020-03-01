package sam.backup.manager.view;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import sam.backup.manager.api.SelectionListener;
import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.config.ConfigManager;
import sam.backup.manager.app.UtilsFx;
import sam.di.Injector;
import sam.myutils.Checker;

public abstract class AbstractMainView extends BorderPane implements SelectionListener {
	protected final Logger logger = LogManager.getLogger(getClass());

	public AbstractMainView() {
		logger.debug("INIT {}", getClass());
	}

	private boolean init = false;

	@Override
	public void selected() {
		logger.debug("SELECTED {}", getClass());
		
		if(init)
			return;

		init = true;
		Injector injector = Injector.getInstance();
		ConfigManager c = injector.instance(ConfigManager.class);
		Collection<Config> configs = data(c);
		
		setTop(UtilsFx.headerBanner(header(configs.size())));
		
		if(Checker.isEmpty(configs)) 
			setCenter(UtilsFx.bigPlaceholder(nothingFoundString()));
		 else 
			setCenter(initView(injector, configs));
	}

	protected abstract Collection<Config> data(ConfigManager c);
    protected abstract Node initView(Injector injector, Collection<Config> configs);
	protected abstract String nothingFoundString();
	protected abstract String header(int size);
}
