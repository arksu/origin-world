/*
 * This file is part of the Origin-World game client.
 * Copyright (C) 2012 Arkadiy Fattakhov <ark@ark.su>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package a1.dialogs;

import a1.*;
import a1.gui.GUI;
import a1.gui.GUI_Button;
import a1.gui.GUI_Panel;
import a1.gui.GUI_Panel.RenderMode;
import a1.gui.GUI_Window;
import a1.net.NetLogin;
import org.newdawn.slick.Color;

import java.util.ArrayList;
import java.util.List;

import static a1.Main.LoginConnect;

public class dlg_Chars extends Dialog {
	public static dlg_Chars dlg = null;
	public List<GUI_Button> buttons = new ArrayList<GUI_Button>();
	public int last_char_id = 0;
	GUI_Window wnd;

	static {
		Dialog.AddType("dlg_chars", new DialogFactory() {
			public Dialog create() {
				return new dlg_Chars();
			}
		});
	}

	// список персонажей получен
	static public void CharsRecv() {
		if (Config.quick_login_mode && dlg.last_char_id > 0) {
			Packet p = new Packet(NetLogin.LOGINSERVER_GETCOOKIE);
			p.write_int(dlg.last_char_id);
			LoginConnect.Send(p);
			Dialog.HideAll();	
		} else {
			for (GUI_Button b : dlg.buttons) {
				b.enabled = true;
			}
		} 
		Config.quick_login_mode = false;
	}
	
	static public void AddChar(final int char_id, String name) {
		if (dlg == null)
			return;
		GUI_Button btn = new GUI_Button(dlg.wnd) {
			public void DoClick() {
				Packet p = new Packet(NetLogin.LOGINSERVER_GETCOOKIE);
				p.write_int(char_id);
				LoginConnect.Send(p);
				Dialog.HideAll();
			}
		};
		dlg.buttons.add(btn);
		btn.caption = name;
		btn.SetPos(100, dlg.buttons.size() * 30 + 10);
		btn.SetSize(200, 20);
		btn.CenterX();
		btn.enabled = false;

		if (char_id == dlg.last_char_id) {
			GUI_Panel panel = new GUI_Panel(dlg.wnd);
			panel.bg_color = Color.orange;
			panel.render_mode = RenderMode.rmColor;
			panel.SetPos(25, dlg.buttons.size() * 30 + 10);
			panel.SetSize(20, 20);
		}
		dlg.wnd.SetSize(250, btn.pos.y + 40);
		dlg.wnd.Center();
	}

	public void DoShow() {
		dlg = this;
		wnd = new GUI_Window(GUI.getInstance().normal) {
			protected void DoClose() {
				NetLogin.error_text = "aborted";
				Main.ReleaseAll();
			}
		};
		wnd.SetSize(250, 100);
		wnd.caption = Lang.getTranslate("login", "select_char");
		wnd.resizeable = false;
	}

	public void DoHide() {
		for (GUI_Button btn : buttons) {
			btn.Unlink();
			btn = null;
		}
		buttons.clear();
		dlg = null;
		wnd.Unlink();
		wnd = null;
	}

	public static boolean Exist() {
		return dlg != null;
	}
}
