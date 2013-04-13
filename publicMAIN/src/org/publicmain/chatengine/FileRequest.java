package org.publicmain.chatengine;

import java.io.File;

import org.publicmain.common.Node;

public class FileRequest {
	public File	datei;
	public long	size;
	public Node	user;

	public FileRequest(File datei, long size, Node user) {
		this.datei = datei;
		this.size = size;
		this.user = user;
	}
}