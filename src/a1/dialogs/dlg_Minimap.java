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
import a1.gui.GUI;
import a1.gui.GUI_Minimap;
import a1.gui.GUI_Window;

public class dlg_Minimap extends Dialog {
	public static dlg_Minimap dlg = null;
	public static final int MINIMAPS_IN_ROW = 3;
	public static final int MINIMAPS_IN_COLUMN = 3;
		
	GUI_Window wnd;
	public GUI_Minimap minimap;

	static {
		Dialog.AddType("dlg_minimap", new DialogFactory() {		
			public Dialog create() {
				return new dlg_Minimap();
			}
		});
	}
	
	public void DoShow() {
		dlg = this;
		
		wnd = new GUI_Window(GUI.getInstance().normal);
		
		
		wnd.Center();
		wnd.caption = "Minimap";
		wnd.set_close_button(true);
		wnd.resizeable = false;
		
		minimap = new GUI_Minimap(wnd);
		minimap.SetPos(10, 35);
		wnd.SetSize(minimap.size.x + 10, minimap.size.y + 30);
		wnd.SetPos(Config.getScreenWidth() - wnd.size.x - 15, Config.getScreenHeight() - wnd.size.y - 15);
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
