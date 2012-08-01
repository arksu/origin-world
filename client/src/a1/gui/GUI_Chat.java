/*
 *  This file is part of the Origin-World game client.
 *  Copyright (C) 2012 Arkadiy Fattakhov <ark@ark.su>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 3 of the License.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package a1.gui;

import org.lwjgl.input.Keyboard;

import a1.ChatHistory;
import a1.Input;
import a1.KinInfo;
import a1.Obj;
import a1.ObjCache;
import a1.Player;
import a1.dialogs.dlg_Game;
import a1.net.NetGame;


public class GUI_Chat extends GUI_Control {
	public static final int CHAT_AREA = 0;
	public static final int CHAT_PRIVATE = 1;
	public static final int CHAT_VILLAGE = 2;
	
	GUI_Edit edit;
	GUI_Memo[] chans = new GUI_Memo[3];
	int current_channel;
	String current_nick = "";
	
	public GUI_Chat(GUI_Control parent) {
		super(parent);
		edit = new GUI_Edit(this){
			public void DoEnter() {
				DoSend();
			};
		};
		for (int i = 0; i < 3; i++) {
			chans[i] = new GUI_Memo(this);
		}
		SetChannel(CHAT_AREA);
	}
	
	public void Open() {
		gui.SetFocus(edit);
	}
	
	protected void DoSend() {
		if (edit.text.length() < 1) return;
		
		// отправляем на сервер
		NetGame.SEND_Chat(CHAT_AREA, "", edit.text);
		// только если это не админ команда 
		if (!edit.text.startsWith("/")) {
			// говорим в клиенте
			RECV_ThisSay(current_channel, Player.CharID, "", edit.text);
		}
		// но в любом случае надо добавить в хистори
		ChatHistory.Add(edit.text);
		// и очистить поле ввода в чате
		edit.SetText("");
	}
	
	public void DoUpdate() {
		super.DoUpdate();
		
		if (gui.focused_control == edit) {
			String h;
			
			if (Input.KeyHit(Keyboard.KEY_UP)) {
				h = ChatHistory.Prev();
				if (h.length()>0) {
					edit.SetText(h);
					edit.SetCursor(999);
				}
			}
			if (Input.KeyHit(Keyboard.KEY_DOWN)) {
				h = ChatHistory.Next();
				if (h.length()>0) {
					edit.SetText(h);
					edit.SetCursor(999);
				}
			}
		}
	}
	
	protected void UpdateSize() {
		edit.SetSize(size.x, 24);
		edit.SetPos(0, size.y - edit.size.y);
		
		for (GUI_Memo m : chans) {
			m.SetSize(size.x, size.y - edit.size.y - 5);
			m.SetPos(0, 0);
		}
	}
	
	protected void SetChannel(int c) {
		SetChannel(c, "");
	}
	
	protected void SetChannel(int c, String nick) {
		current_channel = c;
		current_nick = nick;
		for (GUI_Memo m : chans) {
			m.Hide();
		}
		chans[c].Show();
	}
	

	public void DoSetSize() {
		super.DoSetSize();
		UpdateSize();
	}
	
	public void RECV_ThisSay(int c, int objid, String nick, String msg) {
		ObjCache.obj_say(c, msg, objid);
		Obj o = ObjCache.get(objid);
		if (o != null) {
			KinInfo k = o.getattr(KinInfo.class);
			if (k != null)
				msg = k.name + ": " + msg;
		}
		// system channel
		if (c == 1)
			chans[0].AddLine("sys: "+msg);
		else
			chans[c].AddLine(msg);
	}
	
	public static void RECV_Say(int c, int objid, String nick, String msg) {
		if (dlg_Game.dlg != null && dlg_Game.dlg.chat != null) {
			dlg_Game.dlg.chat.RECV_ThisSay(c, objid, nick, msg);
		}
	}

}
