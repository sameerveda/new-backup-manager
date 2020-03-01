package sam.backup.manager.file.impl;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import sam.backup.manager.api.file.FileTree;

class SerializerImpl implements Serializer {
	private Path saveDir;

	@Override
	public void setSaveDir(Path saveDir) {
		this.saveDir = Objects.requireNonNull(saveDir);
	}

	@Override
	public FileTree read(String filename) throws IOException {
		try (InputStream os = Files.newInputStream(saveDir.resolve(filename.concat(".tsv")), READ); 
				GZIPInputStream gos = new GZIPInputStream(os, 1024 * 8);
				InputStreamReader w = new InputStreamReader(gos, StandardCharsets.UTF_8)) {
		}
		// TODO
		return null;
	}

	@Override
	public void save(FileTree filetree, String filename) throws IOException {
		try (OutputStream os = Files.newOutputStream(saveDir.resolve(filename.concat(".tsv")), CREATE, TRUNCATE_EXISTING); 
				GZIPOutputStream gos = new GZIPOutputStream(os, 1024 * 8);
				OutputStreamWriter w = new OutputStreamWriter(gos, StandardCharsets.UTF_8)) {
			
			StringBuilder sb = new StringBuilder();
			
			for (DirImpl d : ((FileTreeImpl)filetree).getDirs()) {
				sb.setLength(0);
				// TODO
			}
		}
	}
}
