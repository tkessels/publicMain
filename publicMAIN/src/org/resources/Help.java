package org.resources;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;

import org.publicmain.common.LogEngine;
//import the sun.audio package

/**
 * Diese Klasse ermöglicht das Einbinden von Icon's und Files.
 * 
 * Diese Klasse ermöglicht das Einbinden von Icon's und Files mit richtigem Pfad.
 * Desweiteren wird die Möglichkeit gegeben Icon's zu formatieren.
 * 
 * @author ATRM
 */
public class Help {

	/**
	 * Diese Methode bindet ein ImageIcon ein.
	 * 
	 * Diese Methode bindet ein ImageIcon ein mit den Abmessungen 16 * 16 Pixel.
	 * 
	 * @param filename name des Bildes
	 * @return ImageIcon fertiges Bild
	 */
	public static ImageIcon getIcon(String filename) {
		return getIcon(filename, 16);
	}
	
	/**
	 * Diese Methode bindet ein ImageIcon ein.
	 * 
	 * Diese Methode bindet ein ImageIcon ein mit den Abmessungen size * size Pixel.
	 * 
	 * @param filename Name des Bildes
	 * @param size gewünschte quadratische Größe
	 * @return ImageIcon fertiges Bild
	 */
	public static ImageIcon getIcon(String filename,int size) {
		return getIcon(filename, size,size);
	}

	/**
	 * Diese Methode gibt einen InputStream auf ein Object im JAR-File.
	 * 
	 * @param filename Name der Datei
	 * @return InputStream
	 * @throws IOException
	 */
	public static InputStream getInputStream(String filename) throws IOException {
		URL resource = Help.class.getResource(filename);
		if (resource !=null) return new BufferedInputStream(resource.openStream());
		else return null;
	}

	/**
	 * Diese Methode liefert ein Fileobjekt für eine Datei im JAR-File.
	 * 
	 * Diese Methode liefert ein Fileobjekt für eine Datei im JAR-File indem es
	 * eine temporäre Kopie erstellt und Fileobjekt darauf verweist.
	 * 
	 * @param filename Name der Datei
	 * @return File Fileobjekt auf tmp Kopie
	 * @throws IOException
	 */
	public static File getFile(String filename) throws IOException {

		File tmp = null;

		InputStream inputStream = getInputStream(filename);

		try {
			tmp = File.createTempFile("publicMain", "script");

			try (BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(tmp));
					BufferedInputStream bin = new BufferedInputStream(
							inputStream);) {
				byte[] cup = new byte[512];
				int len = -1;
				while ((len = bin.read(cup)) != -1) {
					bos.write(cup, 0, len);
				}
			} catch (IOException e1) {
				LogEngine.log("Resources", e1 );
			}
		} catch (Exception e) {
			LogEngine.log("Resources", e );
		}

		return tmp;
	}

	/**
	 * Diese Methode bindet ein ImageIcon ein.
	 * 
	 * Diese Methode bindet ein ImageIcon ein mit den Abmessungen size * size Pixel.
	 * 
	 * @param filename Anzupassendes Bild
	 * @param size_x  X-Größe
	 * @param size_y  Y-Größe
	 * @return ImageIcon fertiges Icon
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

	/**
	 * Diese Methode liefert ein Clipobjekt.
	 * 
	 * Diese Methode liefert ein Clipobjekt für ein im JAR-File befindliches Soundfile.
	 * 
	 * @param filename name der Datei
	 * @return Clip Clipobjekt für das File
	 */
	public static Clip getSound(final String filename) {
		try {
			InputStream stream = getInputStream(filename);
			if (stream!=null) {
				clip = AudioSystem.getClip();
				AudioInputStream inputStream = AudioSystem.getAudioInputStream(stream);
				clip.open(inputStream);
				return clip;
			}
		} catch (Exception e) {
			LogEngine.log("Resources",e);
		}
		return null;
	}
	
	/**
	 * Diese Methode spielt einen Sound ab.
	 * 
	 * Diese Methode spielt ein .wav bzw. .au File ab.
	 * 
	 * @param filename name der Datei
	 */
	public static synchronized void playSound(final String filename) {
		stopSound();
		new Thread(new Runnable() {
			public void run() {
				clip=getSound(filename);
				if(clip!=null) {
					clip.start();
				}
			}
		}).start();
	}
	
	/**
	 * Diese Methode stoppt die Wiedergabe.
	 * 
	 * Diese Methode stoppt die Wiedergabe eines zuvor gestarteten Soundfiles.
	 */
	public static synchronized void stopSound() {
		if (clip!=null) {
			clip.stop();
		}
		clip=null;
	}
	private static Clip clip;
}

