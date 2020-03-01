package sam.backup.manager.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sam.backup.manager.api.Utils;
import sam.backup.manager.api.config.Config;
import sam.functions.IOExceptionConsumer;
import sam.io.serilizers.WriterImpl;
import sam.myutils.MyUtilsPath;

public abstract class UtilsImpl extends Utils {
	protected Logger LOGGER = getLogger(getClass());
	public final Path APP_DATA = Paths.get("app_data");
	protected Supplier<String> counter;

	public ExecutorService threadPool0;

	protected void init(Path tempDir) {
		counter = new Supplier<String>() {
			int n = number(tempDir);

			@Override
			public String get() {
				return (n++) + " - ";
			}
		};

		LOGGER = getLogger(getClass());
		Thread.setDefaultUncaughtExceptionHandler(
				(thread, exception) -> LOGGER.error("thread: {}", thread.getName(), exception));
	}

	private String[] numbers = new String[500];

	@Override
	public String toString(int n) {
		if (n >= numbers.length || n < 0)
			return Integer.toString(n);

		String s = numbers[n];
		if (s == null)
			numbers[n] = s = Integer.toString(n);
		return s;
	}

	@Override
	public Path tempDirFor(Config config) {
		return tempDir().resolve(config.getType().toString()).resolve(config.getName());
	}

	@Override
	public Path tempDir() {
		Path tempDir;
		String dt = MyUtilsPath.pathFormattedDateTime();
		String dir = Stream.of(MyUtilsPath.TEMP_DIR.toFile().list()).filter(s -> s.endsWith(dt)).findFirst()
				.orElse(null);

		if (dir != null) {
			tempDir = MyUtilsPath.TEMP_DIR.resolve(dir);
		} else {
			int n = number(MyUtilsPath.TEMP_DIR);
			tempDir = MyUtilsPath.TEMP_DIR.resolve((n + 1) + " - " + MyUtilsPath.pathFormattedDateTime());
			try {
				Files.createDirectories(tempDir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		return tempDir;
	}

	@Override
	public Logger getLogger(Class<?> cls) {
		return LogManager.getLogger(cls.getSimpleName());
	}
}
