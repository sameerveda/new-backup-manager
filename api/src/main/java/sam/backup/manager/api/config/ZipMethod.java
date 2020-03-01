package sam.backup.manager.api.config;

import java.util.zip.ZipEntry;

enum ZipMethod {
	STORED(ZipEntry.STORED), DEFLATED(ZipEntry.DEFLATED);
	
	private ZipMethod(int value) {
		this.value = value;
	}

	private final int value;
	public int getValue() {
		return value;
	}
}
