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
import a1.gui.GUI_Window;

public class dlg_Language extends Dialog {
	public static dlg_Language dlg = null;

	GUI_Window wnd;
	GUI_Button btn_en;
	GUI_Button btn_ru;
	
	static {
		Dialog.AddType("dlg_language", new DialogFactory() {		
			public Dialog create() {
				return new dlg_Language();
			}
		});
	}

	public void DoShow() {
		dlg = this;
		wnd = new GUI_Window(GUI.getInstance().normal);
		wnd.caption = "Choose language";
		wnd.SetSize(300, 150);
		wnd.Center();
		wnd.resizeable = false;
		wnd.set_close_button(false);
		
		btn_en = new GUI_Button(wnd) {
			public void DoClick() {
				Config.current_lang = "en";
				Config.save_options();
				Lang.LoadTranslate();
				Dialog.HideAll();
				dlg_Login.ShowLogin();
			}
		};
		btn_en.SetPos(40, 60);
		btn_en.SetSize(200,20);
		btn_en.caption = "English";
		btn_en.CenterX();
		
		btn_ru = new GUI_Button(wnd) {
			public void DoClick() {
				Config.current_lang = "ru";
				Config.save_options();
				Lang.LoadTranslate();
				Dialog.HideAll();
				dlg_Login.ShowLogin();
			}
		};
		btn_ru.SetPos(40, 90);
		btn_ru.SetSize(200,20);
		btn_ru.caption = "Russian";
		btn_ru.CenterX();
	}

	public void DoHide() {
		dlg = null;
		wnd.Unlink();
		wnd = null;
	}

	public static boolean Exist() {
		return dlg != null;
	}
}
