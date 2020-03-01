package sam.backup.manager.app;

public interface ErrorHandler {
	default void handle(Throwable error) {
		handle(null, error);
	}
	void handle(String msg, Throwable error);
}
