package sam.backup.manager.api.file;

import java.io.IOException;
import java.nio.file.Path;

/* Currently serves as a general purpose Class
 * in future will modify it to serve as a transectional unit.
 * 
 * @author Sameer
 *
 */
public interface FileTreeEditor extends AutoCloseable {
	FileEntity addFile(Path file, Attr attr, DestinationType destinationType) throws IOException;
	Dir addDir(Path dir, Attr attr, DestinationType destinationType) throws IOException;
	
	FileEntity addFile(Dir parent, String filename) throws IOException;
	Dir addDir(Dir parent, String dirname) throws IOException;

	boolean hasState(FileEntity file, FileState state);
	// TODO void setAttr(Attr attr, DestinationType destinationType, Path dir);
	void close() throws IOException;
	void addState(FileEntity file, FileState state);
	void removeState(FileEntity file, FileState state);
}
