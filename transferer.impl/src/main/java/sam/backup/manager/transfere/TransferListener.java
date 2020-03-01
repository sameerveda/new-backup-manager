package sam.backup.manager.transfere;

import sam.backup.manager.api.file.FileEntity;
import sam.backup.manager.api.walker.State;

public interface TransferListener {
	void subProgress(FileEntity ft, long read, long total);
	void totalProgress(long read, long total);
	void stateChanged(State s);
	void generalEvent(TransferEvent type);
	void generalEvent(TransferEvent type, TransferEvent subtype, Object attachment);
	void start(TransferEvent type, FileEntity f);
	void success(TransferEvent type, FileEntity f);
	void completed(TransferEvent type, FileEntity f);
	void failed(TransferEvent type, FileEntity f, Throwable e);
}
