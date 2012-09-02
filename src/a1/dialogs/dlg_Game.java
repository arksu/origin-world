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
import a1.gui.*;
import a1.net.NetLogin;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;

import java.util.ArrayList;
import java.util.List;

public class dlg_Game extends Dialog {
	public GUI_Map map;
	public GUI_Button btn_logout, btn_options, btn_stats, btn_equip, btn_inventory, btn_bug_report, btn_bars;
	public GUI_Chat chat;
	public GUI_ActionPanel actions_panel;
	public GUI_Time time;
	public GUI_BuffPanel buff_panel;
	public GUI_Target target = null;
	public List<GUI_Window> craft_wnds = new ArrayList<GUI_Window>();
	public GUI_Progressbar hp_bar, stamina_bar;
	public GUI_Label hp_lbl, stamina_lbl;
	public GUI_RunModesPanel runs_panel;

    private final int BTN_WIDTH = 120;
    private final int BTN_HEIGHT = 23;


    public static dlg_Game dlg;
	
	static {
		Dialog.AddType("dlg_game", new DialogFactory() {		
			public Dialog create() {
				return new dlg_Game();
			}
		});
	}
	
	public void DoShow() {
		Cursor.setCursor("");
		dlg = this;
		map = new GUI_Map(GUI.getInstance().custom);
		btn_logout = new GUI_Button(GUI.getInstance().normal) {
			public void DoClick() {
				NetLogin.error_text = "logged_out";
				Main.ReleaseAll();
			}
		};
		btn_logout.caption = Lang.getTranslate("login", "logout");
		btn_logout.SetSize(BTN_WIDTH, BTN_HEIGHT);
		//----------------------------------
		btn_options = new GUI_Button(GUI.getInstance().normal) {
			public void DoClick() {
				Dialog.Show("dlg_settings");
			}
		};
		btn_options.caption = Lang.getTranslate("options", "wnd_caption");
		btn_options.SetSize(BTN_WIDTH, BTN_HEIGHT);
		//------------------------------------
		btn_stats = new GUI_Button(GUI.getInstance().normal) {
			public void DoClick() {
				if (dlg_Stat.Exist()) 
					Dialog.Hide("dlg_stat");
				else
					Dialog.Show("dlg_stat");
			}
		};
		btn_stats.caption = Lang.getTranslate("generic", "stats");
		btn_stats.SetSize(BTN_WIDTH, BTN_HEIGHT);
		//---------------------------------------------------------------------------
		btn_equip = new GUI_Button(GUI.getInstance().normal) {
			public void DoClick() {
                Player.equip.Open();
			}
		};
		btn_equip.caption = Lang.getTranslate("generic", "equip");
		btn_equip.SetSize(BTN_WIDTH, BTN_HEIGHT);
        //---------------------------------------------------------------------------
        btn_inventory = new GUI_Button(GUI.getInstance().normal) {
            public void DoClick() {
                Hotkeys.OpenInventory();
            }
        };
        btn_inventory.caption = Lang.getTranslate("generic", "inventory");
        btn_inventory.SetSize(BTN_WIDTH, BTN_HEIGHT);
        //---------------------------------------------------------------------------
        btn_bug_report = new GUI_Button(GUI.getInstance().normal) {
            public void DoClick() {
                Dialog.Show("dlg_bug_report");
            }
        };
        btn_bug_report.caption = Lang.getTranslate("generic", "bug_report");
        btn_bug_report.SetSize(BTN_WIDTH, BTN_HEIGHT);

        //---------------------------------------------------------------------------
        btn_bars = new GUI_Button(GUI.getInstance().normal) {
            public void DoClick() {
                Dialog.Show("dlg_hotbars");
            }
        };
        btn_bars.caption = Lang.getTranslate("generic", "hotbars");
        btn_bars.SetSize(BTN_WIDTH, BTN_HEIGHT);

		chat = new GUI_Chat(GUI.getInstance().normal);
		chat.SetSize(300, 200);
		//Dialog.Show("dlg_minimap");

		actions_panel = new GUI_ActionPanel(GUI.getInstance().normal, "root", 5, 5);
		actions_panel.SetPos(Config.getScreenWidth() - actions_panel.Width(),Config.getScreenHeight() - actions_panel.Height());
		actions_panel.LoadState();
		
		
		time = new GUI_Time(GUI.getInstance().normal);
		time.SetSize(200, 70);
		time.SetY(10);
		time.CenterX();
		
		buff_panel = new GUI_BuffPanel(GUI.getInstance().normal);
		buff_panel.SetPos(160, 5);
		//-----------------------------
		hp_bar = new GUI_Progressbar(GUI.getInstance().normal);
		hp_bar.SetPos(5, 5);
		hp_bar.SetSize(150, 20);
		hp_bar.SetMax(100);
		hp_bar.SetMin(0);
		hp_bar.SetColor(new Color(189,49,26, 200));
		
		hp_lbl = new GUI_Label(hp_bar);
		hp_lbl.SetPos(5, 0);
		hp_lbl.SetSize(50, 20);
		//-----------------------------
		stamina_bar = new GUI_Progressbar(GUI.getInstance().normal);
		stamina_bar.SetPos(5, 31);
		stamina_bar.SetSize(150, 20);
		stamina_bar.SetMax(100);
		stamina_bar.SetMin(0);
		stamina_bar.SetColor(new Color(72,115,207, 200));
		
		stamina_lbl = new GUI_Label(stamina_bar);
		stamina_lbl.SetPos(5, 0);
		stamina_lbl.SetSize(50, 20);
		//------------------------
		runs_panel = new GUI_RunModesPanel(GUI.getInstance().normal);
		runs_panel.SetPos(10, 70);

		dlg_SysMsg.ShowSysMessage(Lang.getTranslate("sysmsg", "welcome"));
	}
	
	public void DoResolutionChanged() {
		btn_logout.SetPos(Config.getScreenWidth()-BTN_WIDTH, 0);
		btn_options.SetPos(Config.getScreenWidth()-BTN_WIDTH, btn_logout.size.y);
		btn_stats.SetPos(Config.getScreenWidth()-BTN_WIDTH, btn_options.pos.y+btn_options.size.y);
		btn_equip.SetPos(Config.getScreenWidth()-BTN_WIDTH, btn_stats.pos.y+btn_stats.size.y);
        btn_inventory.SetPos(Config.getScreenWidth()-BTN_WIDTH, btn_equip.pos.y+btn_equip.size.y);
        btn_bug_report.SetPos(Config.getScreenWidth()-BTN_WIDTH, btn_inventory.pos.y+btn_inventory.size.y);
        btn_bars.SetPos(Config.getScreenWidth()-BTN_WIDTH, btn_bug_report.pos.y+btn_bug_report.size.y);
		chat.SetPos(20,Config.getScreenHeight() - 220);
        map.DoResolutionChanged();
	}

	public void DoHide() {
		dlg = null;
		map.Unlink();
		map = null;
		
		btn_logout.Unlink();
		btn_logout = null;
		
		btn_options.Unlink();
		btn_options = null;
		
		btn_stats.Unlink();
		btn_stats = null;
		
		btn_equip.Unlink();
		btn_equip = null;

        btn_inventory.Unlink();
        btn_inventory = null;

        btn_bug_report.Unlink();
        btn_bug_report = null;

        btn_bars.Unlink();
        btn_bars = null;
		
		chat.Unlink();
		chat = null;
		
		actions_panel.Unlink();
		actions_panel = null;
		
		time.Unlink();
		time = null;
		
		buff_panel.Unlink();
		buff_panel = null;
		
		hp_bar.Unlink();
		hp_bar = null;
		hp_lbl.Unlink();
		hp_lbl = null;
		
		stamina_bar.Unlink();
		stamina_bar = null;
		stamina_lbl.Unlink();
		stamina_lbl = null;
		
		if (target != null) {
			target.Unlink();
			target = null;
		}
		// убрать все окна крафта
		for (GUI_Window w : craft_wnds) {
			w.Unlink();
		}
		craft_wnds.clear();
		
		runs_panel.Unlink();
		runs_panel = null;

        Hotbar.ClearAll();
	}
	
	public void DoUpdate() {
		// по энтеру входим в чат. только если чисто энтер без модификаторов.
		if (Input.KeyHit(Keyboard.KEY_RETURN) 
				&& !Input.isCtrlPressed() 
				&& !Input.isAltPressed() 
				&& !Input.isShiftPressed()) 
		{ 
			if (dlg != null && dlg.chat != null)
				dlg.chat.Open();
		}
	}
	
	public void MakeTarget(boolean val) {
		if (val) {
			if (target == null) {
				target = new GUI_Target(GUI.getInstance().normal);
				target.SetPos(10, 100);
				target.SetSize(200, 50);
			}
		} else {
			if (target != null) {
				target.Unlink();
				target = null;
			}
		}
	}
	
	public void SetHP(int hp) {
		hp_bar.SetValue(hp);
		hp_lbl.caption = Integer.toString(hp);
	}

	public void SetStamina(int val) {
		stamina_bar.SetValue(val);
		stamina_lbl.caption = Integer.toString(val);
	}

	public static boolean Exist() {
		return dlg != null;
	}
}
