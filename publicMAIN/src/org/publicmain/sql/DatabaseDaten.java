package org.publicmain.sql;

/**
 * 
 * @author LeeGewiese
 * Diese Klasse Stellt eine Speichermöglichkeit für Daten für und aus der Datenbank da.
 * Sie gibt die Möglichkeit sowohl Zelleninhalte als auch Spaltenüberschriften zu halten.
 */
public class DatabaseDaten {
	private String[] spaltenüberschriften;
	private String[][] zelleninhalt;

	/**
	 * Konstruktor zum anlegen einer DatabaseDaten
	 * @param spaltenüberschriftens	übergebene Spaltenüberschriften
	 * @param zelleninhalts			übergebene Zelleninhaltes
	 */
	public DatabaseDaten(String[] spaltenüberschriftens,String[][] zelleninhalts) {
		this.spaltenüberschriften = spaltenüberschriftens;
		this.zelleninhalt = zelleninhalts;//test
	}
	
	/**
	 * HEX HEX - trifft´s wohl am besten.
	 * @param mask
	 * @return
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
	 * @return
	 */
	private static boolean isUp(int i,int mask)
	{
		return (((mask>>>i)&1)==1); 
	}
	
	public int getRows() {
		return zelleninhalt.length;
	}
	
	public int getCols() {
		return spaltenüberschriften.length;
	}
	
	public String[] getHeader() {
		return spaltenüberschriften;
	}
	public String[][] getData(){
		return zelleninhalt;
	}

	

	public String[] getHeader(int colSelection) {
		String[] header = new String[high(colSelection)];
		int new_i=0;
		for (int i = 0; i < spaltenüberschriften.length; i++) {
			if(isUp(i, colSelection))header[new_i++]=spaltenüberschriften[i];
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



