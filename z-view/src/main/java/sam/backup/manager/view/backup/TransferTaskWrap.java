package sam.backup.manager.view.backup;

import static sam.backup.manager.api.walker.State.RUNNING;

import sam.backup.manager.api.config.Config;
import sam.backup.manager.api.file.Dir;
import sam.backup.manager.api.file.FileTree;
import sam.backup.manager.transfere.TransferTask;
import static javafx.application.Platform.*;

public class TransferTaskWrap extends TransferTask {

	public TransferTaskWrap(Config config, FileTree fileTree, Dir rootDir) {
		super(config, fileTree, rootDir);
	}
	
	protected void ensureNotRunning() {
		if(!isFxApplicationThread())
			throw new IllegalStateException("not fx thread");
		super.ensureNotRunning();
	}
	
	@Override
	protected void run() {
		if(isFxApplicationThread())
			throw new IllegalStateException("running in Fx thread");

		super.run();
	}
}
