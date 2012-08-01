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
package a1.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import a1.Config;
import a1.DialogFactory;
import a1.Lang;
import a1.Main;
import a1.Render2D;
import a1.gui.GUI;
import a1.gui.GUI_Label;
import a1.utils.Resource;

public class dlg_Loading extends Dialog {
	public GUI_Label lbl;
	public GUI_Label lbl2;
	private int count = 1;
	private int timer = 0;
	public static List<Resource> reslist = new ArrayList<Resource>();
	public static dlg_Loading dlg = null;
	
	public static String LoadingName = "";
	public static int LoadingLen = 0;
	public static int LoadingProcessed = 0;
	public static Map<String, Integer> LoadingSize = new HashMap<String, Integer>();
	
	static {
		Dialog.AddType("dlg_loading", new DialogFactory() {		
			public Dialog create() {
				return new dlg_Loading();
			}
		});
	}
	
	static public void AddLoading(String name) {
		reslist.add(Resource.load(name));
	}
	
	static public void FillLoading() {
		Set<String> s = Resource.srv_versions.keySet();
		for (String ss : s) {
			if (!ss.equals("client") && !(!Config.SoundEnabled && ss.equals("sound")))
				AddLoading(ss);
		}
	}
	
	public void DoShow() {
		dlg = this;
		lbl = new GUI_Label(GUI.getInstance().normal);
		lbl.caption = Lang.getTranslate("generic", "loading") + ".";
		lbl.SetSize(300, 30);
		lbl.SetPos(Config.ScreenWidth / 2 - 50,Config.ScreenHeight / 2 - 50);
		lbl.align = Render2D.Align_Default;
		
		lbl2 = new GUI_Label(GUI.getInstance().normal);
		lbl2.caption = "";
		lbl2.SetSize(300, 300);
		lbl2.SetPos(lbl.pos.x-20, lbl.pos.y + 30);
		lbl2.align = Render2D.Align_Default;	
	}

	public void DoHide() {
		dlg = null;
		lbl.Unlink();
		lbl = null;
		lbl2.Unlink();
		lbl2 = null;
	}

	public void DoUpdate() {
		boolean all_loaded = true;
		for (Resource s : reslist) {
			if (s.error != null)
				Main.GlobalError("fail load resource: "+s.name);
			if (s.loading || !s.ready) {
				all_loaded = false;
				break;
			}
		}
		if (all_loaded) {
			Dialog.HideAll();
			Main.ResLoaded();
			// если в конфиге не установлен язык - покажем диалог выбора языка
			if (Config.current_lang.length() < 1)
				Dialog.Show("dlg_language");
			else 
				dlg_Login.ShowLogin();
			return;
		}
		
		
		timer += Main.dt;
		while (timer > 333) {
			timer -= 333;
			count++;
			if (count > 6) count = 1;
			String s = "";
			for (int i=1; i<count; i++) {
				s += ".";
			}
			
			lbl.caption = "Loading ("+getLoadedCount()+" / "+reslist.size()+") " + s;
		}
		lbl2.caption = make_caption();
	}
	
	private int getLoadedCount() {
		int r = 0;
		for (Resource s : reslist) {
			if (!s.loading && s.ready) {
				r++;
			}
		}
		return r;
	}
	
	private String make_caption() {
		String s1 = "";
		synchronized (LoadingName) {
			s1 = LoadingName;
		}
		
		synchronized (LoadingSize) {
			if (s1.length() > 1 && LoadingSize.containsKey(s1)) {
				int len = LoadingSize.get(s1);
				s1 = "<"+s1 + "> " + LoadingProcessed/1024 + " Kb / " + len/1024 + " Kb";
			}
			
		}
		
		return s1;
	}
	
	public static boolean Exist() {
		return dlg != null;
	}
}
