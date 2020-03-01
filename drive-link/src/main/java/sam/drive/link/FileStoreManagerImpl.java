package sam.drive.link;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import sam.backup.manager.api.FileStoreManager;

public class FileStoreManagerImpl implements FileStoreManager {
	private final List<FileStore> drives;
	private final Path targetDrive;
	private final String id;
	
	public FileStoreManagerImpl() {
		Logger logger = LogManager.getLogger(getClass());
		logger.debug("INIT {}", getClass());

		Path drive = null;
		for (Path p : FileSystems.getDefault().getRootDirectories()) {
			if(Files.exists(p.resolve(".iambackup"))) {
				drive = p;
				break;
			}
		}

		Properties p = new Properties();
		String id = null;

		if(drive != null) {
			try {
				p.load(Files.newInputStream(drive.resolve(".iambackup")));
				id = p.getProperty("id");
				if(id == null)
					id = Files.getFileStore(drive).getAttribute("volume:vsn").toString(); 
			} catch (IOException e) {
				logger.error("failed to read: "+drive.resolve(".iambackup"), e);
			}
		}
		this.targetDrive = drive;
		this.id = id;

		// TODO remove
		logger.info("{}", new JSONObject().put("DRIVE", targetDrive).put("id", this.id).toString());
		
		this.drives = StreamSupport.stream(FileSystems.getDefault().getFileStores().spliterator(), false).collect(Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), (ArrayList<FileStore> list) -> {
			list.trimToSize();
			return Collections.unmodifiableList(list);
		}));
	}
	
	public List<FileStore> getDrives() {
		return drives;
	}
	public Path getTargetDrive() {
		return targetDrive;
	}
	public String getTargetDriveId() {
		return id;
	}
}
