package org.publicmain.nodeengine;

import java.util.ArrayList;
import java.util.List;

import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;

public class Hook {
	
	private List<Haken> allHooks=new ArrayList<Haken>();

	public MSG fishfor(NachrichtenTyp system, MSGCode nodeUpdate, long nid,boolean filter, long timeout) {
		Haken x = new Haken(NachrichtenTyp.SYSTEM, MSGCode.NODE_UPDATE, nid,filter);
		allHooks.add(x);
		synchronized (x) {
			try {
				x.wait(timeout);
			}
			catch (InterruptedException e) {
			}
		}
		allHooks.remove(x);
		return x.getHookedMSG();
	}
	
	private class Haken{
		private NachrichtenTyp typ;
		private MSGCode code;
		private Long sender;
		private Long reciever;
		private String gruppe;
		private Object payload;
		private MSG hookedMSG;
		private boolean filter;
		public Haken(NachrichtenTyp typ, MSGCode code, Long sender, boolean filter) {
			super();
			this.typ = typ;
			this.code = code;
			this.sender = sender;
//			this.reciever = reciever;
//			this.gruppe = gruppe;
//			this.payload = payload;
			this.filter=filter;
		}
		
		private synchronized boolean check(MSG x) {
			boolean typ_check=(typ==null)||typ==x.getTyp();
			boolean code_check=(code==null)||code==x.getCode();
			boolean sender_check=(sender==null)||sender==x.getSender();
//			boolean reciever_check=(reciever==null)||reciever==x.getEmpfänger();
//			boolean gruppe_check=(gruppe==null)||gruppe==x.getGroup();
//			boolean payload_check=(payload==null)||payload==x.getData();
			
			
			if(typ_check&&code_check&&sender_check) {//&&reciever_check&&gruppe_check&&payload_check) {
//				System.out.println("Hooked packed found !!!! Filter:"+filter+ " MSG:" + x);
				LogEngine.log(this.toString(),"hooked", x);
				this.notifyAll();
				hookedMSG=x;
				return filter;
			}
			return false;
		}
		public String toString() {
			return "Hook [" + (typ != null ? "typ=" + typ + ", " : "") + (code != null ? "code=" + code + ", " : "") + (sender != null ? "sender=" + sender + ", " : "") + (reciever != null ? "reciever=" + reciever + ", " : "") + (gruppe != null ? "gruppe=" + gruppe + ", " : "") + (payload != null ? "payload=" + payload + ", " : "") + (hookedMSG != null ? "hookedMSG=" + hookedMSG + ", " : "") + "filter=" + filter + "]";
		}
		private MSG getHookedMSG() {
			return hookedMSG;
		}
	}
}
