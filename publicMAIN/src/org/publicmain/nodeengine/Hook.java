package org.publicmain.nodeengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;

/**
 * Dies Klasse erlaubt es eine Reihe von Vergleichskriterien (Haken) abzuspeichern und eine blockende Abfrage zu starten. 
 * Dies Klasse erlaubt es einer Methode einen Haken zu registrieren und zu blocken bis eine MSG entsprechender Zusammensetzung angekommen ist. 
 * Dabei wird ein Timeout ben�tigt um tote Hooks zu verhindern.
 *  
 * @author ATRM
 * 
 */

public class Hook {
	private boolean onlyFirstMatch=true;
	private volatile List<Haken> allHooks=new CopyOnWriteArrayList<Haken>();  //Liste aller registrierten Filter
	
	private void add(Haken toAdd) {
			allHooks.add(toAdd);
	}
	
	private void remove(Haken toRemove) {
			allHooks.remove(toRemove);
	}

	/**
	 * Richtet einen Hook ein der blockt bis eine MSG mit den Entsprechenden Daten von der NodeEngine verarbeitet wird. Dabei sind die Parameter UND verkn�pft und m�ssen alle erf�llt werden.
	 * 
	 * @param typ	Nachrichtentyp der zu erwartenden Nachricht. Also ob <code>SYSTEM, GROUP, PRIVATE</code> oder <code>DATA</code>
	 * @param code Der MSGCode der Nachricht. Also um welchen Typ von SystemNachricht es sich handelt (z.B.:<code>NODE_UPDATE </code> oder <code>ECHO_REQUEST</code>)
	 * @param nid NodeID des Absenders
	 * @param filter gibt an ob die Nachricht von der weiteren Verarbeitung in der NodeEngine ausgeschlossen werden soll. <ul><li><code>true</code>-Nachricht wird von handle nicht weiter betrachtet<li><code>false</code>-Nachricht wird normal weiterverarbeitet.
	 * @param timeout gibt die Dauer in Millisekunden an f�r die der Hook aktiv bleiben soll bevor er sich selbst enfernt.
	 * @return gibt die Nachricht zur�ck die den Hook ausgel�st hat oder <code>null</code> wenn das <code>timeout</code> abgelaufen ist ohne eine Nachricht zu matchen.
	 */
	public MSG fishfor(NachrichtenTyp typ, MSGCode code, Long nid,Object payload,boolean filter, long timeout) {
		Haken x = new Haken(typ,code, nid,payload,filter);
		add(x);
		synchronized (x) {
			try {
				x.wait(timeout);
			}
			catch (InterruptedException e) {
			}
		}
		remove(x);
		return x.getHookedMSG();
	}
	
	public MSG fishfor(NachrichtenTyp typ, MSGCode code, Long nid,Object payload,boolean filter, long timeout,MSG paket) {
		Haken x = new Haken(typ,code, nid,payload,filter);
		add(x);
		synchronized (x) {
			try {
				NodeEngine.getNE().routesend(paket);
				x.wait(timeout);
			}
			catch (InterruptedException e) {
			}
		}
		remove(x);
		return x.getHookedMSG();
	}
	
	
	public MSG fishfor(NachrichtenTyp typ, MSGCode code, Long nid,Object payload,boolean filter, long timeout,Runnable dothat) {
		Haken x = new Haken(typ,code, nid,payload,filter);
		add(x);
		synchronized (x) {
			try {
				new Thread(dothat).start();
				x.wait(timeout);
			}
			catch (InterruptedException e) {
			}
		}
		remove(x);
		return x.getHookedMSG();
	}
	
	
	/**Nicht blockende Methode die f�r eine gewisse Zeit <code>timeout</code> alle Nachrichten verwirft die den angegebennen Krieterien entspricht.
	 * @param typ Typ der zu verwerfenden Nachricht
	 * @param typ	Nachrichtentyp der zu filternden Nachricht. (z.b:<code>SYSTEM, GROUP, PRIVATE</code> oder <code>DATA</code>)
	 * @param code Der MSGCode der Nachricht. Also um welchen Typ von SystemNachricht es sich handelt (z.B.:<code>NODE_UPDATE </code> oder <code>ECHO_REQUEST</code>)
	 * @param nid NodeID des Absenders
	 * @param timeout gibt die Dauer in Millisekunden an f�r die der Hook aktiv bleiben soll bevor er sich selbst enfernt.
	 * 	 */
	public void filter(NachrichtenTyp typ, MSGCode code, Long nid,Object payload, final long timeout) {
	final Haken x = new Haken(typ, code, nid,payload,true);
	new Thread(new Runnable() {
		public void run() {
			add(x);
			synchronized (x) {
				try {
					Thread.sleep(timeout);
				} catch (InterruptedException e) {
				}
			}
			remove(x);
		}
	}).start();
}

	
	public boolean check(MSG paket) {
		boolean tmp = false;
		for (Haken cur : allHooks) {
			if (cur.check(paket)) {
				if (onlyFirstMatch) return cur.filter;
				else tmp |= cur.filter;
			}
		}
		return tmp;
	}
	@Override
	public String toString() {
		return allHooks.toString();
	}
	
	
	/**interne Klasse f�r Parameter Kapsel und Semaphore 
	 * @author tkessels
	 *
	 */
	private class Haken{
		//Filterinformationen
		private NachrichtenTyp typ;
		private MSGCode code;
		private Long sender;
		private Object payload;
		//private Long reciever;
		//private String gruppe;
		
		private boolean filter;
		

		private MSG hookedMSG;

		public Haken(NachrichtenTyp typ, MSGCode code, Long sender, Object payload, boolean filter) {
			super();
			this.typ = typ;
			this.code = code;
			this.sender = sender;
			this.payload = payload;
//			this.reciever = reciever;
//			this.gruppe = gruppe;
			this.filter=filter;
		}
		
		private synchronized boolean check(MSG x) {
//			System.out.println(allHooks);
			boolean typ_check=(typ==null)||typ==x.getTyp();
			boolean code_check=(code==null)||code.equals(x.getCode());
			boolean sender_check=(sender==null)||sender.equals(x.getSender());
//			System.out.println(sender);
//			System.out.println(x.getSender());
			boolean payload_check=(payload==null)||payload.equals(x.getData());
//			boolean reciever_check=(reciever==null)||reciever==x.getEmpf�nger();
//			boolean gruppe_check=(gruppe==null)||gruppe==x.getGroup();
//			System.out.println("typ:"+typ_check);
//			System.out.println("code:"+code_check);
//			System.out.println("sender:"+sender_check);
//			System.out.println("payload:"+payload_check);
			
			
			if(typ_check&&code_check&&sender_check&&payload_check) {//&&reciever_check&&gruppe_check) {
				LogEngine.log(this.toString(),"hooked", x);
				hookedMSG=x;
				this.notifyAll();
				return true;
			}
			return false;
		}
		
		public String toString() {
			return "Hook [" + (typ != null ? "typ=" + typ + ", " : "") + (code != null ? "code=" + code + ", " : "") + (sender != null ? "sender=" + sender + ", " : "") + "filter=" + filter +"]";
		}

		private MSG getHookedMSG() {
			return hookedMSG;
		}

		private Hook getOuterType() {
			return Hook.this;
		}
	}

	
}
