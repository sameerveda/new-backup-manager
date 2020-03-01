package sam.backup.manager.api;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import sam.backup.manager.api.config.Config;
import sam.functions.IOExceptionConsumer;
import sam.io.IOUtils;
import sam.io.serilizers.WriterImpl;
import sam.nopkg.Resources;

public abstract class Utils {
	public abstract Logger getLogger(Class<?> cls);

	public abstract String toString(int n);

	public abstract Path tempDir();

	public abstract Path tempDirFor(Config config);

	public String bytesToString(long bytes) {
		if (bytes == 0)
			return "0";
		if (bytes < 1048576)
			return bytesToString(bytes, 1024) + "KB";
		if (bytes < 1073741824)
			return bytesToString(bytes, 1048576) + "MB";
		else
			return bytesToString(bytes, 1073741824) + "GB";

	}

	private String bytesToString(long bytes, long divisor) {
		double d = divide(bytes, divisor);
		if (d == (int) d)
			return this.toString((int) d);
		else
			return String.valueOf(d);
	}

	public String millisToString(long millis) {
		if (millis <= 0)
			return "N/A";
		return durationToString(Duration.ofMillis(millis));
	}

	private final StringBuilder sb = new StringBuilder();

	public String durationToString(Duration d) {
		synchronized (sb) {
			sb.setLength(0);

			char[] chars = d.toString().toCharArray();
			for (int i = 2; i < chars.length; i++) {
				char c = chars[i];
				switch (c) {
					case 'H':
						sb.append("hours ");
						break;
					case 'M':
						sb.append("min ");
						break;
					case 'S':
						sb.append("sec");
						break;
					case '.':
						sb.append("sec");
						return sb.toString();
					default:
						sb.append(c);
						break;
				}
			}
			return sb.toString();
		}
	}

	public double divide(long dividend, long divisor) {
		if (divisor == 0 || dividend == 0)
			return 0;
		return (dividend * 100 / divisor) / 100D;
	}

	public String millsToTimeString(Long d) {
		return d == null || d <= 0 ? "--"
				: LocalDateTime.ofInstant(Instant.ofEpochMilli(d), ZoneOffset.of("+05:30"))
						.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
	}

	public int number(Path path) {
		if (Files.notExists(path)) {
			return 0;
		}

		Pattern p = Pattern.compile("^(\\d+) - ");

		return Stream.of(path.toFile().list()).map(p::matcher).filter(Matcher::find).map(m -> m.group(1))
				.mapToInt(Integer::parseInt).max().orElse(0);
	}

	public JSONObject jsonObjectFromResource(String locator) {
		return new JSONObject(new JSONTokener(ClassLoader.getSystemResourceAsStream(locator)));
	}

	public void withWriter(Path path, boolean append, IOExceptionConsumer<WriterImpl> action) throws IOException {
		try (Resources r = Resources.get();
				FileChannel fc = IOUtils.fileChannel(path, append);
				WriterImpl impl = new WriterImpl(fc, r.chars(), false, r.encoder())) {
			action.accept(impl);
		}
	}

	/**
	 * closed the source after use
	 * @param source
	 * @param action
	 * @throws Exception
	 */
	public <E extends AutoCloseable> void handle(E source, IOExceptionConsumer<E> action) throws Exception {
		try(E e2 = source) {
			action.accept(e2);
		}
	}

	public Path saveInTempDir(Writable w, Config config, String directory, String fileName) throws IOException {
		Path p = path(tempDir(), directory, config.getName(), fileName);
		save(p, w);
		return p;	
	}
	
	private void save(Path p, Writable w) throws IOException {
		Files.createDirectories(p.getParent());
		
		try(BufferedWriter os = Files.newBufferedWriter(p, WRITE, CREATE, TRUNCATE_EXISTING)) {
			w.write(os);
		}
	}

	private Path path(Path root, String child1, String...child) {
		return root.resolve(Paths.get(child1, child));
	}
}
