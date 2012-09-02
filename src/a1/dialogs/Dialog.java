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
import a1.net.NetGame;

import java.util.Map;
import java.util.TreeMap;

public abstract class Dialog {
	public boolean active = false;

	public static Map<String, Dialog> dialogs = new TreeMap<String, Dialog>();
	public static Map<String, DialogFactory> dialog_types = new TreeMap<String, DialogFactory>();	
	public static Class<?>[] types = {
            dlg_Login.class,
            dlg_Chars.class,
            dlg_Loading.class,
            dlg_Game.class,
            dlg_Minimap.class,
            dlg_Language.class,
            dlg_Settings.class,
            dlg_Context.class,
            dlg_Version.class,
            dlg_Stat.class,
            dlg_Bug_Report.class,
            dlg_Hotbars.class
	};

	static public void Init() {
		try {
			for(Class<?> c : types)
				Class.forName(c.getName(), true, c.getClassLoader());
		} catch(ClassNotFoundException e) {
			throw(new Error(e));
		}
	}

	static public void AddType(String name, DialogFactory impl) {
		dialog_types.put(name, impl);
	}

	static public DialogFactory GetType(String name) {
		return dialog_types.get(name);
	}

	static public boolean Show(String name) {
		if (IsActive(name)) return false;
		DialogFactory f = GetType(name);
		if (f != null) {
            NetGame.SEND_dialog_open(name);
			Dialog d = f.create();
			d.Show();
			dialogs.put(name, d);
			return true;
		}
		return false;
	}

	static public void Hide(String name) {
		Dialog d = dialogs.get(name);
		if (d != null) {
            NetGame.SEND_dialog_close(name);
			d.Hide();
			dialogs.remove(name);
		}
	}

	static public void HideAll() {
		for (Dialog d : dialogs.values()) {
			d.Hide();
		}
		dialogs.clear();
	}

	static public boolean IsActive(String name) {
		Dialog d = dialogs.get(name);
		return d != null;
	}

	static public void Update() {
		for (Dialog d : dialogs.values()) {
			d.DoUpdate();
		}
	}
	
	static public void ResolutionChanged() {
		for (Dialog d : dialogs.values()) {
			d.DoResolutionChanged();
		}
	}

	public void Show() {
		if (active) return;

		active = true;
		DoShow();
		DoResolutionChanged();
	}

	public void Hide() {
		if (!active) return;

		active = false;
		DoHide();
	}

	public abstract void DoShow();

	public abstract void DoHide();

	public void DoResolutionChanged() { }
	
	public void DoUpdate() { }
	
	public static boolean Exist() {
		return false;
	}

}
