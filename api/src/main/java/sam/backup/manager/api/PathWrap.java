package sam.backup.manager.api;

import java.nio.file.Path;

import sam.backup.manager.api.file.Attrs;

public interface PathWrap {
	Path path();
	String string();
	boolean exists();
	Attrs getAttrs();
}