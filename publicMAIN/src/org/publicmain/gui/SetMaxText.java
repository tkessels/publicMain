package org.publicmain.gui;

import javax.swing.text.PlainDocument;

/**
 * Diese Klasse stellt ein PlainDocument f�r das Eingabefeld im ChatWindow bereit.
 * 
 * In dieser Klasse werden Beschr�nkungen und automatische �nderungen an das Eingabefeld
 * des ChatWindows gesetzt bzw angewand.
 * 
 * @author ATRM
 */
public class SetMaxText extends PlainDocument {

	private int limit;
	private boolean toUppercase = false;

	/**
	 * Dieser Konstruktor erstellt ein PlainDocument mit Limit
	 * 
	 * Dieser Konstruktor erstellt ein PlainDocument und setzt
	 * einen Maximalmalwert (limit) f�r die Anzahl an einzugebender
	 * Buchstaben.
	 *  
	 * @param limit
	 */
	SetMaxText( int limit ) {
		super();
		this.limit = limit;
	} //eom SetMaxText

	//	Ggf. f�r die weitere Entwicklung ben�tigt.	
	//	/**
	//	 * Dieser Konstruktor erstellt ein PlainDocument mit Limit und Upper
	//	 * 
	//	 * @param limit
	//	 * @param upper
	//	 */
	//	SetMaxText( int limit, boolean upper ) {
	//		super();
	//		this.limit = limit;
	//		toUppercase = upper;
	//	} //eom SetMaxText( int limit, boolean upper )
	// 
	//	/* (non-Javadoc)
	//	 * @see javax.swing.text.PlainDocument#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
	//	 */
	//	public void insertString( int offset, String  str, AttributeSet attr ) throws BadLocationException {
	//		if ( str == null ) return;
	//		if ( ( getLength() + str.length() ) <= limit ) {
	//			if ( toUppercase ) str = str.toUpperCase();
	//			super.insertString( offset, str, attr );
	//		}
	//	} //eom insertString()

} //eoc SetMaxText