package org.publicmain.sql;

public class DatabaseDaten {
	public String[] spaltenüberschriften;
	public String[][] zelleninhalt;

	public DatabaseDaten(String[] spaltenüberschriftens,String[][] zelleninhalts) {
		this.spaltenüberschriften = spaltenüberschriftens;
		this.zelleninhalt = zelleninhalts;
	}
}