package org.publicmain.sql;

/**
 * 
 * @author LeeGewiese
 * Diese Klasse Stellt eine Speicherm�glichkeit f�r Daten f�r und aus der Datenbank da.
 * Sie gibt die M�glichkeit sowohl Zelleninhalte als auch Spalten�berschriften zu halten.
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

	
	
	/**
	 * @param colSelection
	 * @return
	 */
	public String[] getSpalten�berschriften(int colSelection) {
		String[] header = new String[high(colSelection)];
		int new_i=0;
		for (int i = 0; i < spalten�berschriften.length; i++) {
			if(isUp(i, colSelection))header[new_i++]=spalten�berschriften[i];
		}
		return header;
	}
	
	/**
	 * @param colSelection
	 * @return
	 */
	public String[][] getZelleninhalt(int colSelection) {
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


/*		int columnCount = metaData.getColumnCount();
	String[] spa�b= new String[columnCount];
	for (int column = 1; column <= columnCount; column++) {
		spa�b[column-1]= metaData.getColumnName(column);
	}

	// data of the table
	rs.last();
	int rows = rs.getRow();
	rs.beforeFirst();

	String[][] stringdata = new String[rows][columnCount];
	while (rs.next()) {
		for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
			Object zelle = rs.getObject(columnIndex);
			stringdata[rs.getRow()-1][columnIndex-1]=(zelle!=null)?zelle.toString():"";
		}
	}
}

*/

