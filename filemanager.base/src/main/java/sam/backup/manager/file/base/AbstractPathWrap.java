package sam.backup.manager.file.base;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import sam.backup.manager.api.PathWrap;
import sam.myutils.Checker;

public abstract class AbstractPathWrap implements PathWrap {
	protected String string;
	protected Path path;

	public AbstractPathWrap(Path path) {
		Checker.requireNonNull("path", path);
		this.path = path;
	}
	public AbstractPathWrap(String string) {
		Checker.requireNonNull("string", string);
		this.string = string;
	}
	public AbstractPathWrap(AbstractPathWrap parent, String string) {
		this(string);
		
		if(parent.string != null)
			this.string = concat(parent.string, string);
		else
			this.path = parent.path.resolve(string);
	}
	
	public AbstractPathWrap(AbstractPathWrap parent, Path path) {
		this(path);
		this.path = parent.path().resolve(path);
	}

	@Override
	public Path path() {
		if(path == null)
			path = Paths.get(string);
		return path;
	} 
	@Override
	public String string() {
		if(string == null)
			string = path.toString();
		return string;
	}
	@Override
	public String toString() {
		return string();
	}

	protected static final StringBuilder sb = new StringBuilder();
	protected static String concat(String parent, String child) {
		if(Checker.isEmptyTrimmed(child))
			throw new IllegalArgumentException("invalid child value");
		
		Checker.requireNonNull("parent", parent);
		
		synchronized (sb) {
			sb.setLength(0);
			sb.append(parent);
			if(sb.length() != 0) {
				if(!isSlash(sb, sb.length() - 1))
					sb.append('\\');
			}
			
			if(isSlash(child, 0))
				sb.append(child, 1, child.length());
			else
				sb.append(child);
			
			return sb.toString();
		}
	}
	private static boolean isSlash(CharSequence cs, int i) {
		char c = cs.charAt(i);
		return c == '\\' || c == '/';
	}
	/* (non-Javadoc)
	 * @see sam.backup.manager.api.PathWrap#exists()
	 */
	@Override
	public boolean exists() {
		return path == null ? new File(string).exists() : Files.exists(path);
	}
}
