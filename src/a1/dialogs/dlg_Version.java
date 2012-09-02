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

import a1.DialogFactory;
import a1.Lang;
import a1.Render2D;
import a1.gui.GUI;
import a1.gui.GUI_Label;
import a1.gui.GUI_Window;
import a1.utils.Resource;

public class dlg_Version extends Dialog {
	public static dlg_Version dlg = null;
	
	GUI_Window wnd;
	GUI_Label status;
	
	static {
		Dialog.AddType("dlg_version", new DialogFactory() {		
			public Dialog create() {
				return new dlg_Version();
			}
		});
	}
	
	public void DoShow() {
		dlg = this;
		
		wnd = new GUI_Window(GUI.getInstance().modal);
		wnd.SetSize(450, 200);
		wnd.Center();
		wnd.caption = Lang.getTranslate("generic", "error");
		wnd.set_close_button(false);
		wnd.resizeable = false;
		
		status = new GUI_Label(wnd);
		status.SetPos(20, 90);
		status.SetSize(wnd.Width()-40, 50);
		status.align = Render2D.Align_HCenter + Render2D.Align_Top;
		status.caption = Lang.getTranslate("generic", "update_version") + " 0." + Integer.toString(Resource.srv_versions.get("client"));
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
