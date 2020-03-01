package sam.backup.manager.file.base;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;

import sam.backup.manager.api.Utils;
import sam.backup.manager.api.Writable;
import sam.backup.manager.api.file.Attrs;
import sam.backup.manager.api.file.DestinationType;
import sam.backup.manager.api.file.Dir;
import sam.backup.manager.api.file.FileEntity;
import sam.backup.manager.api.file.FileTreeManager;
import sam.myutils.MyUtilsBytes;

public class FileTreeString implements Writable {
	private final Utils utils;
	
	@Inject
	public FileTreeString(Utils utils) {
		this.utils = utils;
	}
	
	private Predicate<FileEntity> filter;
	private DestinationType destinationType;
	private Dir dir;
	
	public void set(Dir dir, DestinationType destinationType, Predicate<FileEntity> filter) {
		this.dir = Objects.requireNonNull(dir);
		this.destinationType = Objects.requireNonNull(destinationType);
		this.filter = filter == FileTreeManager.ALWAYS_TRUE ? null : filter;		
	}
	
	public void write(Appendable sink) throws IOException {
		appendDetails(Objects.requireNonNull(dir), new char[0], sink);
		walk(new char[] {' ', '|'}, dir, sink);
	}
	
	private void walk(final char[] separator, Dir dir, Appendable sink) throws IOException {
		for (FileEntity f : dir) {
			if(filter == null || filter.test(f)) {
				appendDetails(f, separator, sink);
				
				if(f.isDirectory()) {
					int length = separator.length;
					char[] sp2 = Arrays.copyOf(separator, length + 6);
					Arrays.fill(sp2, length, sp2.length, ' ');
					if(length != 2) {
						sp2[length - 1] = ' ';
						sp2[length - 2] = ' ';
					}
					sp2[sp2.length - 3] = '|';
					sp2[sp2.length - 2] = '_';
					sp2[sp2.length - 1] = '_';

					walk(sp2, asDir(f), sink);
				}
			}
		}
	}
	private Dir asDir(FileEntity f) {
		return (Dir)f;
	}
	
	private final StringBuilder buffer = new StringBuilder(); 
	
	private void appendDetails(FileEntity f, char[] separator, Appendable sink) throws IOException {
		append(separator, sink)
		.append(f.getFileName());
		
		Attrs ak = (destinationType == DestinationType.SOURCE ? f.getSourcePath() : f.getTargetPath()).getAttrs();
		long size = ak.size();

		if(!f.isDirectory() ? size <= 0 : ( f.isDirectory() && asDir(f).childrenCount() == 0)) 
			sink.append('\n');
		else {
			sink.append('\t')
			.append('(');

			if(size > 0) {
				buffer.setLength(0);
				MyUtilsBytes.bytesToHumanReadableUnits(size, false, buffer);
				sink.append(buffer);
			}
			
			if(f.isDirectory() && asDir(f).childrenCount() != 0)
				sink.append(' ').append(utils.toString(asDir(f).childrenCount())).append(" files");

			sink.append(")\n");	
		}
	}
	
	private Appendable append(char[] separator, Appendable sink) throws IOException {
		for (char c : separator) 
			sink.append(c);
		return sink;
	}
}
