package sam.backup.manager.api;

/**
 * each runnable will be called when application stops
 * @author Sameer
 *
 */

public interface ShutDownHooks {
	public void addShutDownHooks(Stoppable runnable);
	
}
