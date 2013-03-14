/**
 * 
 */
package org.publicmain.gui;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * Diese Klasse stellt ein DefaultStyledDocument für das Message Text Feld bereit (JTextPane msgTextPane)
 * 
 * 
 * Diese Klasse erbt von DefaultStyleDocument und stellt Methoden bereit um den Inhalt unseres Message Text Feldes
 * (also die versendeten Chatnachrichten) zu gestalten und formatieren.
 * @author ABerthold
 *
 */
public class EditorDocument extends DefaultStyledDocument {

	private static final long serialVersionUID = 3944310942560671073L;

	/**
	 * Standardkonstruktor
	 */
	public EditorDocument() {
		super();
	}

	/**
	 * Konstruktor der Content und StyleContext erwartet
	 * @param arg0 Content
	 * @param arg1 StyleContext
	 */
	public EditorDocument(Content arg0, StyleContext arg1) {
		super(arg0, arg1);
	}

	/**
	 * Konstruktor der StyleContext erwartet
	 * @param arg0 StyleContext
	 */
	public EditorDocument(StyleContext arg0) {
		super(arg0);
	}

	/**
	 * Diese Methode ermöglicht das einfügen von Bildern
	 * @param pos Position an der eingefügt wird
	 * @param ico Welches Bild (ImageIcon) soll eingefügt werden
	 * @throws BadLocationException 
	 */
	public void setIcon(int pos, ImageIcon ico) throws BadLocationException {
		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setIcon(sas, ico);
		insertString(pos, " ", sas);
	}

	/**
	 * Diese Methode ermöglicht es die Schrift kursiv darzustellen 
	 * @param start (int) ab hier wird kursiv
	 * @param end	(int) bis hier wird kursiv
	 * @param active (boolean) kursiv true / false 
	 */
	public void setItalic(int start, int end, boolean active) {
		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setItalic(sas, active);
		setCharacterAttributes(start, end - start, sas, false);
	}
	
	/**
	 * Diese Methode prüft ob eine Stelle im Text kursiv ist
	 * @param pos (int) welche position soll abgefragt werden
	 * @return (boolean) kursiv true / false
	 */
	public boolean isItalic(int pos) {
		Element element = getCharacterElement(pos);
		return StyleConstants.isItalic(element.getAttributes());
	}

	/**
	 * Diese Methode ermöglicht es die Schrift fett darzustellen
	 * @param start (int) ab hier wird fett
	 * @param end	(int) bis hier wird fett
	 * @param active (boolean) fett true / false 
	 */
	public void setBold(int start, int end, boolean active) {
		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setBold(sas, active);
		setCharacterAttributes(start, end - start, sas, false);
	}
	
	/**
	 * Diese Methode prüft ob eine Stelle im Text fett ist
	 * @param pos (int) welche position soll abgefragt werden
	 * @return (boolean) fett true / false
	 */
	public boolean isBold(int pos) {
		Element element = getCharacterElement(pos);
		return StyleConstants.isBold(element.getAttributes());
	}

	/**
	 * Diese Methode ermöglicht es die Schrift unterstrichen darzustellen
	 * @param start (int) ab hier wird unterstrichen
	 * @param end	(int) bis hier wird unterstrichen
	 * @param active (boolean) unterstrichen true / false 
	 */
	public void setUnderline(int start, int end, boolean active) {
		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setUnderline(sas, active);
		setCharacterAttributes(start, end - start, sas, false);
	}
	
	/**
	 * Diese Methode prüft ob eine Stelle im Text unterstrichen ist
	 * @param pos (int) welche position soll abgefragt werden
	 * @return (boolean) unterstrichen true / false
	 */
	public boolean isUnderline(int pos) {
		Element element = getCharacterElement(pos);
		return StyleConstants.isUnderline(element.getAttributes());
	}

	/**
	 * Diese Methode legt fest welcher Font verwendet wird
	 * @param start (int) ab hier wird Font gesetzt
	 * @param end 	(int) bis hier wird Font gesetzt
	 * @param font (String) die gewünschte Font
	 */
	public void setFont(int start, int end, String font) {
		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setFontFamily(sas, font);
		setCharacterAttributes(start, end - start, sas, false);
	}


	/**
	 * Diese Methode ermöglicht es die Größe des Font zu ändern
	 * @param start (int) ab hier wird die Größe geändert
	 * @param end	(int) bis hier wird die Größe geändert
	 * @param size (int) die Größe die eingestellt werden soll
	 */
	public void setFontSize(int start, int end, int size) {
		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setFontSize(sas, size);
		setCharacterAttributes(start, end - start, sas, false);
	}

	
	/**
	 * Diese Methode ermöglicht es die Farbe des Font zu ändern
	 * @param start (int) ab hier wird Farbe geändert
	 * @param end	(int) bis hier wird Farbe geändert
	 * @param col	(Color) die Farbe die eingestellt werden soll
	 */
	public void setForeground(int start, int end, Color col) {
		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setForeground(sas, col);
		setCharacterAttributes(start, end - start, sas, false);
	}

	
	/**
	 * Diese Methode ermöglicht es die Hintergrundfarbe zu ändern
	 * @param start (int) ab hier wird Hintergrundfarbe geändert
	 * @param end	(int) bis hier wird Hintergrundfarbe geändert
	 * @param col	(Color) die Farbe die gesetzt werden soll
	 */
	public void setBackground(int start, int end, Color col) {
		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setBackground(sas, col);
		setCharacterAttributes(start, end - start, sas, false);
	}

}
