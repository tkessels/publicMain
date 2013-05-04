package org.publicmain.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import org.publicmain.common.LogEngine;

/**
 * HyperLinkController-Klasse um die Verweise in den JTextPanes/JContentPanes
 * auswähl- und anklickbar zu machen. Der HyperLinkController ist eine eigenständige
 * Klasse weil er ggf. für andere JTextPanes/JContentPanes genutzt werden soll.
 * 
 * @author ATRM
 *
 */
public class HyperLinkController implements HyperlinkListener {
	public void hyperlinkUpdate(HyperlinkEvent arg0) {
		if (arg0.getEventType()==EventType.ACTIVATED) {
			try {
				openWebpage(new URL(arg0.getURL().toString().split("“")[1]).toURI());
			} catch (Exception e) {
				LogEngine.log(this, "Could not open Webbrowser!", LogEngine.ERROR);
			}
		}
	}

	/**
	 * Diese Klasse erhält die zu öffnende URI und öffnet diese mit dem Systembrowser.
	 * 
	 * @param uri
	 * @throws IOException
	 */
	public static void openWebpage(URI uri) throws IOException {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if ((desktop != null) && desktop.isSupported(Desktop.Action.BROWSE)) {
			desktop.browse(uri);
		}
	}
}
