package sam.backup.manager.impl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.codejargon.feather.Provides;

import sam.backup.manager.api.StopTasksQueue;
import sam.backup.manager.api.Stoppable;
import sam.backup.manager.api.Utils;
import sam.backup.manager.utils.UtilsImpl;
import sam.nopkg.EnsureSingleton;

public abstract class Impl extends UtilsImpl implements AutoCloseable, StopTasksQueue {
	private static final EnsureSingleton singleton = new EnsureSingleton();
	{
		singleton.init();
	}

	private final Path temp_dir;
	private ExecutorService _pool;
	private ExecutorService2 pool;
	private ArrayList<RunWrap> stops = new ArrayList<>();

	public Impl(int threadCount) {
		this(null, null, threadCount);
	}

	public Impl(Path temp_dir, int threadCount) {
		this(null, Objects.requireNonNull(temp_dir), threadCount);
	}

	private Impl(Object marker, Path temp_dir, int threadCount) {
		this.temp_dir = temp_dir == null ? super.tempDir() : temp_dir;
		this._pool = threadCount == 1 ? Executors.newSingleThreadExecutor() : Executors.newFixedThreadPool(threadCount);
		this.pool = new ExecutorService2(this._pool);
		init(this.temp_dir);
	}
	
	@Override
	public Path tempDir() {
		return temp_dir;
	}

	private static class RunWrap {
		private final String location;
		private final Stoppable task;

		public RunWrap(String location, Stoppable task) {
			this.location = location;
			this.task = task;
		}
	}

	@Override
	public synchronized void addStopable(Stoppable runnable) {
		stops.add(new RunWrap(LOGGER.isDebugEnabled() ? Thread.currentThread().getStackTrace()[2].toString() : null,
				Objects.requireNonNull(runnable)));
	}

	@Override
	public void close() throws Exception {
		Exception error = null;
		try {
			_pool.shutdownNow();
			LOGGER.debug("waiting thread to die");
			_pool.awaitTermination(2, TimeUnit.SECONDS);
		} catch (InterruptedException e2) {
			error = e2;
		}

		stops.forEach(r -> {
			try {
				r.task.stop();
			} catch (Throwable e) {
				LOGGER.error("failed to run: {}, description: {}", r.location, r.task.description(), e);
			}
		});

		if (error != null)
			throw error;
	}

	@Provides public Utils me() { return this; }
	@Provides public Executor p2() { return pool; }
	@Provides public ExecutorService p3() { return pool; }
	@Provides public StopTasksQueue p1() { return this; }
}
