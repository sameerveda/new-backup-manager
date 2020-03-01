import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import sam.di.InjectorProvider;

public class Main {
	public static void main(String[] args) throws URISyntaxException, IOException, SQLException {
		InjectorProvider.detect().forEach(s -> System.out.println(s));
//		LoadConfig.load();
//		LauncherImpl.launchApplication(App.class, PreloaderImpl.class, args);
	}
}
