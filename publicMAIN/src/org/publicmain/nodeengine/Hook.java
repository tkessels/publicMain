package org.publicmain.nodeengine;

import org.publicmain.common.LogEngine;
import org.publicmain.common.MSG;
import org.publicmain.common.MSGCode;
import org.publicmain.common.NachrichtenTyp;

public class Hook {
	private NachrichtenTyp typ;
	private MSGCode code;
	private Long sender;
	private Long reciever;
	private String gruppe;
	private Object payload;
	private MSG hookedMSG;
	private boolean filter;
	
	public Hook(NachrichtenTyp typ, MSGCode code, Long sender, Long reciever, String gruppe, Object payload, boolean filter) {
		super();
		this.typ = typ;
		this.code = code;
		this.sender = sender;
		this.reciever = reciever;
		this.gruppe = gruppe;
		this.payload = payload;
		this.filter=filter;
	}

	public synchronized boolean check(MSG x) {
		boolean typ_check=(typ==null)||typ==x.getTyp();
		boolean code_check=(code==null)||code==x.getCode();
		boolean sender_check=(sender==null)||sender==x.getSender();
		boolean reciever_check=(reciever==null)||reciever==x.getEmpfänger();
		boolean gruppe_check=(gruppe==null)||gruppe==x.getGroup();
		boolean payload_check=(payload==null)||payload==x.getData();
		
		
		if(typ_check&&code_check&&sender_check&&reciever_check&&gruppe_check&&payload_check) {
//			System.out.println("Hooked packed found !!!! Filter:"+filter+ " MSG:" + x);
			LogEngine.log(this.toString(),"hooked", x);
			this.notifyAll();
			hookedMSG=x;
			return filter;
		}
		return false;
	}




	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Hook [" + (typ != null ? "typ=" + typ + ", " : "") + (code != null ? "code=" + code + ", " : "") + (sender != null ? "sender=" + sender + ", " : "") + (reciever != null ? "reciever=" + reciever + ", " : "") + (gruppe != null ? "gruppe=" + gruppe + ", " : "") + (payload != null ? "payload=" + payload + ", " : "") + (hookedMSG != null ? "hookedMSG=" + hookedMSG + ", " : "") + "filter=" + filter + "]";
	}

	public MSG getHookedMSG() {
		return hookedMSG;
	}



}
