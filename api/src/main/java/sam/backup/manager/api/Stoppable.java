package sam.backup.manager.api;

@FunctionalInterface
public interface Stoppable {
	void stop() throws Exception;
	default String description() { return null; }
}
