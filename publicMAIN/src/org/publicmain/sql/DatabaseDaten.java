package org.publicmain.sql;

/**
 * 
 * Diese Klasse Stellt eine Speicherm�glichkeit f�r Daten f�r und aus der Datenbank da.
 * Sie gibt die M�glichkeit sowohl Zelleninhalte als auch Spalten�berschriften zu halten.
 * 
 * @author ATRM
 */
public class DatabaseDaten {
	private String[] spalten�berschriften;
	private String[][] zelleninhalt;

	/**
	 * Konstruktor zum anlegen einer DatabaseDaten
	 * @param spalten�berschriftens	�bergebene Spalten�berschriften
	 * @param zelleninhalts			�bergebene Zelleninhaltes
	 */
	public DatabaseDaten(String[] spalten�berschriftens,String[][] zelleninhalts) {
		this.spalten�berschriften = spalten�berschriftens;
		this.zelleninhalt = zelleninhalts;//test
	}
	
	/**
	 * HEX HEX - trifft�s wohl am besten.
	 * @param mask
	 * @return Hoehe als int
	 */
	private static int high(int mask)
	{
		mask -= ((mask >> 1) & 0x55555555);
		mask = (mask & 0x33333333) + ((mask >> 2) & 0x33333333);
		return (((mask + (mask >> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;
	}
	
	/**
	 * @param i
	 * @param mask
	 * @return true if is up, false if not
	 */
	private static boolean isUp(int i,int mask)
	{
		return (((mask>>>i)&1)==1); 
	}
	
	public int getRows() {
		return zelleninhalt.length;
	}
	
	public int getCols() {
		return spalten�berschriften.length;
	}
	
	public String[] getHeader() {
		return spalten�berschriften;
	}
	public String[][] getData(){
		return zelleninhalt;
	}

	

	public String[] getHeader(int colSelection) {
		String[] header = new String[high(colSelection)];
		int new_i=0;
		for (int i = 0; i < spalten�berschriften.length; i++) {
			if(isUp(i, colSelection))header[new_i++]=spalten�berschriften[i];
		}
		return header;
	}
	

	public String[][] getData(int colSelection) {
		String[][] stringdata = new String[zelleninhalt.length][high(colSelection)];
		for (int i = 0; i < zelleninhalt.length; i++) {
			int new_j = 0;
			for (int j = 0; j < zelleninhalt[i].length; j++) {
				if(isUp(j, colSelection))stringdata[i][new_j++]=zelleninhalt[i][j];
			}
		}
		return stringdata;
	}
}



