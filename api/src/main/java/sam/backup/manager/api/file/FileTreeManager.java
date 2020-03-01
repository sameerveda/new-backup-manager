package sam.backup.manager.api.file;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Predicate;

import sam.backup.manager.api.Writable;
import sam.backup.manager.api.config.Config;

public interface FileTreeManager {
	Predicate<FileEntity> ALWAYS_TRUE = s -> true;
	
	@SuppressWarnings("unchecked")
	public static <E extends FileEntity> Predicate<E> alwaysTrue() {
		return (Predicate<E>) ALWAYS_TRUE;
	}
	
	Predicate<FileEntity> containsInFilter(Collection<? extends FileEntity> containsIn);	
		default Writable toWritable(Dir dir, DestinationType destinationType, Appendable sink) throws IOException  {
		return toWritable(dir, destinationType, alwaysTrue(), sink);
	}
	Writable toWritable(Dir dir, DestinationType destinationType, Predicate<FileEntity> filter, Appendable sink) throws IOException ;
	FileTreeMeta getFileTreeFor(Config config, TreeType treeType);
	Attr newAttr(long lastModified, long size);
	Attrs newAttrs(Attr old);
}
