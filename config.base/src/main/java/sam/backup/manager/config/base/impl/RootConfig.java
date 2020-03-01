package sam.backup.manager.config.base.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sam.myutils.Checker;

public abstract class RootConfig extends ConfigBase {
	private static final long serialVersionUID = 1L;

	private transient ConfigImpl[] _backups;
	private transient ConfigImpl[] _lists;
	
	@Override
	public Path getSource() {
		return sourceP != null ? sourceP :  (sourceP = pathResolve(source));
	}
	@Override
	public Path getTarget() {
		if(targetP == null) 
			targetP = target == null ? null : pathResolve(target);
		return targetP;
	}
	
	@Override
	protected void init() {
		Map<String, List<ConfigImpl>> string =  Stream.concat(backups() == null ? Stream.empty() : Arrays.stream(backups()), lists()  == null ? Stream.empty() : Arrays.stream(lists()))
				.peek(c -> {
					if(Checker.isEmptyTrimmed(c.getName()))
						throw new NullPointerException("name not specified: "+c);
				})
				.collect(Collectors.groupingBy(ConfigImpl::getName));
		
		string.values().removeIf(l -> l.size() < 2);
		
		if(!string.isEmpty()) 
			new IllegalStateException("config.name conflict: "+string);
		
	}

	protected abstract ConfigImpl[] backups();
	protected abstract ConfigImpl[] lists();

	public ConfigImpl findConfig(String name) {
		return find(name, getTargets());
	}
	public ConfigImpl findList(String name) {
		return find(name, getLists());
	}
	private ConfigImpl find(String name, ConfigImpl[] cnf) {
		Objects.requireNonNull(name, "name cannot be null");
		return Arrays.stream(cnf).filter(c -> name.equals(c.getName())).findFirst().orElseThrow(() -> new NoSuchElementException("no config found for name: "+name)); 
	}
	public boolean hasLists() {
		return getLists() != null && getLists().length != 0;
	}
	public boolean hasTargets() {
		return  getTargets() != null && getTargets().length != 0;
	}
	public ConfigImpl[] getLists() {
		if(_lists != null)
			return _lists;

		return _lists = filterAndPrepare(lists()); 
	}
	private ConfigImpl[] filterAndPrepare(ConfigImpl[] configs) {
		if(configs == null || configs.length == 0)
			return null;

		ConfigImpl[] cnf = Arrays.stream(configs)
				.filter(c -> c != null && !c.isDisabled())
				.peek(c -> c.init(this))
				.toArray(ConfigImpl[]::new);

		Map<String, List<ConfigImpl>> map = Arrays.stream(cnf).filter(c -> c.getName() != null).collect(Collectors.groupingBy(ConfigImpl::getName));

		map.values().removeIf(l -> l.size() < 2);
		if(!map.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("-------- conflicting config.name --------\n");
			map.forEach((name, cnfs) -> {
				sb.append(name).append('\n');
				cnfs.forEach(c -> sb.append("    ").append(c.getSource()).append(", ").append(c.getTarget()).append('\n'));
			});
			throw new RuntimeException(sb.toString());
		}
		return cnf;
	}
	public ConfigImpl[] getTargets() {
		if(_backups != null)
			return _backups;

		return _backups = filterAndPrepare(backups());
	}
	@Override
	public FilterImpl getSourceExcluder() {
		return excludes;
	}
	@Override
	public FilterImpl getTargetExcluder() {
		return targetExcludes;
	}
	@Override
	protected RootConfig getRoot() {
		return this;
	}
	protected Path pathResolve(String s) {
		return s == null ? null : Paths.get(s);
	}
}

