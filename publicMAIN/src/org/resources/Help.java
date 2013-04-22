package org.resources;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

public class Help {
	
	public static ImageIcon getIcon(String filename) {
		return getIcon(filename, 16);
	}
	public static ImageIcon getIcon(String filename,int size) {
		return getIcon(filename, size,size);
	}
	
	
	
	/**
	 * Gibt des Bild in der angeforderten Größe zurück.
	 * 
	 * @param filename, Anzupassendes Bild
	 * @param size, geforderte Größe
	 * @return, fertiges Icon
	 */
	public static ImageIcon getIcon(String filename, int size_x, int size_y) {
		URL resource = Help.class.getResource(filename);
		if (resource == null) {
			resource = Help.class.getResource("g4174.png");
		}
		ImageIcon imageIcon = new ImageIcon(resource);
		Image img = imageIcon.getImage();
		Image newimg = img.getScaledInstance(size_x, size_y,java.awt.Image.SCALE_SMOOTH);
		ImageIcon newIcon = new ImageIcon(newimg);
		return newIcon;
	}
	
}
