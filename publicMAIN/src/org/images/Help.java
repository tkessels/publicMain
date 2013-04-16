package org.images;

import javax.swing.ImageIcon;

public class Help {
	public static ImageIcon getIcon(String filename) {
		return new ImageIcon(Help.class.getResource(filename));
	}

}
