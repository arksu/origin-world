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

import a1.Config;
import a1.DialogFactory;
import a1.Lang;
import a1.gui.GUI;
import a1.gui.GUI_Button;
import a1.gui.GUI_Panel;
import a1.net.NetGame;

import java.util.HashMap;
import java.util.Map;

public class dlg_Context extends Dialog {
	public GUI_Panel panel;
	public GUI_Button[] buttons;
	public Map<GUI_Button, String> options = new HashMap<GUI_Button, String>();
	
	public static dlg_Context dlg = null;
	static {
		Dialog.AddType("dlg_context", new DialogFactory() {		
			public Dialog create() {
				return new dlg_Context();
			}
		});
	}
	
	public void AddItems(int x, int y, String[] items) {
		buttons = new GUI_Button[items.length];
		int i = 0;
		for (String s : items) {
			buttons[i] = new GUI_Button(panel) {
				public void DoClick() {
					NetGame.SEND_context_action(dlg_Context.this.options.get(this));
					Dialog.Hide("dlg_context");
					//dlg_Context.this.Hide();
				}
			};
			buttons[i].SetPos(x, y+i*30);
			buttons[i].SetSize(200,20);
			buttons[i].caption = Lang.getTranslate("context", s);
			options.put(buttons[i], s);
			i++;
		}
	}
	
	public void DoShow() {
		dlg = this;
		
		panel = new GUI_Panel(GUI.getInstance().modal) {
			public void DoClick() {
				NetGame.SEND_context_action("");
				Dialog.Hide("dlg_context");
			}
		};
		panel.SetSize(Config.getScreenWidth(), Config.getScreenHeight());
	}

	public void DoHide() {
		dlg = null;
		
		panel.Unlink();
		panel = null;
	}

	public static boolean Exist() {
		return dlg != null;
	}
}
