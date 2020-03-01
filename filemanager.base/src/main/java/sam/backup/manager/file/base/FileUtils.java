package sam.backup.manager.file.base;

import java.util.function.Consumer;
import java.util.function.Predicate;

import sam.backup.manager.api.file.Dir;
import sam.backup.manager.api.file.FileEntity;
import sam.backup.manager.api.file.WalkableFull;
import static sam.backup.manager.api.file.DestinationType.*;

public interface FileUtils {
	public static class Meta {
		public int childCount;
		public int deepCount;
		public long srcSize;
		public long trgtSize;
	}
	
	@SuppressWarnings("unchecked")
	public static Meta computeMeta(WalkableFull source, Predicate<FileEntity> filter) {
		Meta meta = new Meta();
		
		Consumer<? extends FileEntity> consumer = f -> {
			if(f.getParent() == source)
				meta.childCount++;
			meta.deepCount++;
			if(!f.isDirectory()) {
				meta.srcSize =+ f.getSize(SOURCE);
				meta.trgtSize =+ f.getSize(TARGET);
			}
		};
		
		source.walkFull((Consumer<FileEntity>)consumer, (Consumer<Dir>)consumer, filter);
		return meta;
	}
}
