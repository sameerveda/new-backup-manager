package sam.backup.manager.file.base;

import java.util.BitSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sam.backup.manager.api.file.Dir;
import sam.backup.manager.api.file.FileEntity;
import sam.backup.manager.api.file.FileTreeManager;
import sam.collection.IntSet;
import sam.myutils.System2;

public abstract class AbstractFileTreeManager implements FileTreeManager {
	public static final String BITSET_SIZE_KEY = "FileManagerBase.BITSET_SIZE";
	protected final Logger logger = LogManager.getLogger(getClass());
	public final int bitsetSize;
	
	public AbstractFileTreeManager(int bitsetSize) {
		if(bitsetSize < 100)
			throw new IllegalArgumentException("minimum bitsetSize is 100, but was given: "+bitsetSize);
		this.bitsetSize = bitsetSize;
	}
	
	public AbstractFileTreeManager() {
		this(Optional.ofNullable(System2.lookup("FileManagerBase.BITSET_SIZE")).map(Integer::parseInt).orElse(500));
	}

	@Override public Predicate<FileEntity> containsInFilter(Collection<? extends FileEntity> containsIn) {
		if(containsIn.isEmpty())
			return (f -> false);
		if(containsIn.size() == 1) {
			int id = containsIn.iterator().next().getId();
			return (f -> f.getId() == id);
		}
		
		int max = (int) containsIn.stream().mapToInt(FileEntity::getId).max().getAsInt();
		BitSet bitset = new BitSet(Math.min(bitsetSize, max));
		IntSet set = max <= bitsetSize ? null : new IntSet();
		
		containsIn.forEach(f -> {
			int id = f.getId();
			if(id < bitsetSize)
				bitset.set(id);
			else 
				set.add(id);
		});
		
		logger.debug(() -> String.format("created contains in filter: ,max: %d, size: %d, bitset.size: %d, intset.size: %d", max, containsIn.size(), bitset.cardinality(), set.size()));
		
		return f -> {
			int id = f.getId();
			return id < bitsetSize ? bitset.get(id) : set.contains(id);
		};
	}
}
