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
import a1.Lang;
import a1.gui.GUI;
import a1.gui.GUI_Button;
import a1.gui.GUI_Window;

public class dlg_Language extends Dialog {
	public static dlg_Language dlg = null;

	GUI_Window wnd;

    int lang_btn_pos = 40;
    int btn_margin = 30;
	
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
        wnd.SetSize(300, 100);
		wnd.resizeable = false;
		wnd.set_close_button(false);

        for (Lang.Struct s : Lang.langs) {
            AddLangButton(s.full_name, s.name);
        }
        wnd.SetSize(300, lang_btn_pos);
        wnd.Center();
	}

    private void AddLangButton(String lang_name, String lang) {
        GUI_Button btn = new GUI_Button(wnd) {

            public void DoClick() {
                Config.current_lang = tag;
                Config.save_options();
                Lang.LoadTranslate();
                Dialog.HideAll();
                dlg_Login.ShowLogin();
            }
        };
        btn.tag = lang;
        btn.SetPos(40, lang_btn_pos);
        lang_btn_pos += btn_margin;
        btn.SetSize(200,20);
        btn.caption = lang_name;
        btn.CenterX();
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
