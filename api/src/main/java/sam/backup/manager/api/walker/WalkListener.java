package sam.backup.manager.api.walker;

import java.nio.file.Path;

import sam.backup.manager.api.file.Dir;
import sam.backup.manager.api.file.FileEntity;

public interface WalkListener {
	void onFileFound(FileEntity ft, long size, WalkMode mode);
	void onDirFound(Dir ft, WalkMode mode);
	void stateChange(WalkState s);
	void failed(String msg, Throwable error);
	void startWalking(Path path);
	void endWalking(Path path);
}
