package sam.backup.manager.config.base.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sam.backup.manager.api.config.Filter;
import sam.backup.manager.api.config.HasFilterArrays;
import sam.string.StringUtils;

public abstract class FilterImpl implements Filter, HasFilterArrays {
	private String[] name, glob, regex, path, startsWith, endsWith, classes;
	private FilterImpl invert;
	private ConfigImpl config;

	public void setConfig(ConfigImpl config) {
		this.config = config;
		if (invert != null)
			invert.setConfig(config);
	}

	@Override
	public boolean test(Path p) {
		if (invert != null && invert.test(p)) {
			return false;
		}

		return path(p) || name(p.getFileName()) || startsWith(p) || endsWith(p) || glob(p) || regex(p) || classes(p);

	}

	@Override
	public Map<String, String[]> getArrays() {
		Map<String, String[]> map = new HashMap<>();

		if (name != null)
			map.put("name", Arrays.copyOf(name, name.length));
		if (glob != null)
			map.put("glob", Arrays.copyOf(glob, glob.length));
		if (regex != null)
			map.put("regex", Arrays.copyOf(regex, regex.length));
		if (path != null)
			map.put("path", Arrays.copyOf(path, path.length));
		if (startsWith != null)
			map.put("startsWith", Arrays.copyOf(startsWith, startsWith.length));
		if (endsWith != null)
			map.put("endsWith", Arrays.copyOf(endsWith, endsWith.length));
		if (classes != null)
			map.put("classes", Arrays.copyOf(classes, classes.length));

		if (invert != null) {
			Map<String, String[]> map2 = invert.getArrays();
			map2.forEach((s, t) -> map.put("invert-" + s, t));
		}
		return map;
	}

	private static boolean isNull(Object o) {
		return o == null;
	}

	private static boolean invalidArray(String[] array) {
		return array == null || array.length == 0;
	}

	private static Stream<String> stream(String[] array) {
		return Arrays.stream(array).filter(s -> !s.trim().isEmpty());
	}

	private static Predicate<Path> toPredicate(String[] array, Function<String, Predicate<Path>> mapper) {
		return stream(array).map(mapper).reduce((x, y) -> x.or(y)).get();
	}

	Predicate<Path> clss;

	@SuppressWarnings("unchecked")
	private boolean classes(Path p) {
		if (invalidArray(classes))
			return false;

		if (isNull(clss)) {
			String path = customFilterDir();
			ClassLoader temp = null;
			try {
				temp = path == null ? ClassLoader.getSystemClassLoader()
						: new URLClassLoader(new URL[] { new File(path).toURI().toURL() },
								ClassLoader.getSystemClassLoader());
			} catch (MalformedURLException e1) {
				throw new RuntimeException("bad value for custom.filter.dir: " + path);
			}

			ClassLoader loader = temp;

			clss = toPredicate(classes, s -> {
				try {
					return (Predicate<Path>) Class.forName(s, false, loader).newInstance();
				} catch (ClassNotFoundException | ClassCastException | InstantiationException
						| IllegalAccessException e) {
					throw new RuntimeException("filter class error: className: " + s, e);
				}
			});
		}
		return clss.test(p);
	}

	/**
	 * TODO System2.lookup("custom.filter.dir"); if(path == null) LOGGER.warn("no
	 * value set for custom.filter.dir");
	 * 
	 * @return
	 */
	protected abstract String customFilterDir();

	private Predicate<Path> endsWiths;

	private boolean endsWith(Path p) {
		if (invalidArray(endsWith))
			return false;
		if (isNull(endsWiths)) {
			endsWiths = toPredicate(endsWith, s -> {
				Path path = Paths.get(s);
				return x -> x.endsWith(path);
			});
		}
		return endsWiths.test(p);
	}

	private Predicate<Path> startsWiths;

	private boolean startsWith(Path p) {
		if (invalidArray(startsWith))
			return false;
		if (isNull(startsWiths)) {
			startsWiths = toPredicate(startsWith, s -> {
				Path path = Paths.get(s);
				return x -> x.startsWith(path);
			});
		}
		return startsWiths.test(p);
	}

	private Set<Path> paths;

	private boolean path(Path p) {
		if (invalidArray(path))
			return false;
		if (isNull(paths)) {
			paths = stream(path).map(s -> {
				if (s.charAt(0) == '\\' || s.charAt(0) == '/')
					return config.getSource().resolve(s.substring(1));
				if (s.contains("%source%"))
					s = s.replace("%source%", config.getSource().toString());
				if (s.contains("%target%") && config.getTarget() != null)
					s = s.replace("%target%", config.getTarget().toString());
				return Paths.get(s);
			}).collect(Collectors.toSet());
		}

		return paths.contains(p);
	}

	private Set<Path> names;

	private boolean name(Path p) {
		if (invalidArray(name))
			return false;
		if (isNull(names))
			names = stream(name).map(Paths::get).collect(Collectors.toSet());

		return names.contains(p);
	}

	private static final FileSystem fs = FileSystems.getDefault();

	private Predicate<Path> regexs;

	private boolean regex(Path p) {
		if (invalidArray(regex))
			return false;
		if (isNull(regexs)) {
			regexs = toPredicate(regex, s -> {
				PathMatcher m = fs.getPathMatcher("regex:".concat(s.replace("/", "\\\\")));
				return x -> m.matches(x);
			});
		}
		return regexs.test(p);
	}

	private Predicate<Path> globs;

	private boolean glob(Path p) {
		if (invalidArray(glob))
			return false;
		if (isNull(globs)) {
			globs = toPredicate(glob, s -> {
				PathMatcher rgx = fs.getPathMatcher("glob:".concat(s));
				return StringUtils.contains(s, '/') ? (x -> rgx.matches(x)) : (x -> rgx.matches(x.getFileName()));
			});
		}
		return globs.test(p);
	}

	public boolean isAlwaysFalse() {
		return Stream.of(name, glob, regex, path, startsWith, endsWith, classes).allMatch(FilterImpl::invalidArray);
	}

	@Override
	public String toString() {
		return asString();
	}
}
