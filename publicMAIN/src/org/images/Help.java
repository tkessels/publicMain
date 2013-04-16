package org.images;

import javax.swing.ImageIcon;
import javax.swing.plaf.metal.MetalFileChooserUI;
import javax.swing.plaf.metal.MetalIconFactory;

public class Help {
	public static ImageIcon getIcon(String filename) {
		ImageIcon imageIcon = new ImageIcon(Help.class.getResource(filename));
		if(imageIcon==null)imageIcon=new ImageIcon(Help.class.getResource("g4174.png"));
		return imageIcon;
	}

}
