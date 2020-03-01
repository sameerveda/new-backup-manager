package sam.backup.manager.app;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;

import sam.backup.manager.api.Utils;
import sam.functions.IOExceptionConsumer;
import sam.io.serilizers.WriterImpl;

public class UtilsErrorHandled {
	private final Utils utils;
	private final ErrorHandler handler;

	@Inject
	public UtilsErrorHandled(Utils utils, ErrorHandler handler) {
		this.utils = utils;
		this.handler = handler;
	}
	
	public void withWriter(Path path, boolean append, String msgOnError, IOExceptionConsumer<WriterImpl> action)  {
		try {
			utils.withWriter(path, append, action);
		} catch (IOException e) {
			handler.handle(msgOnError, e);
		}
	}

	public <E extends AutoCloseable> void handle(E source, String msgOnError, IOExceptionConsumer<E> action) {
		try {
			utils.handle(source, action);
		} catch (Exception e) {
			handler.handle(msgOnError, e);
		} 
	}
}
