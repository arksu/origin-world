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

import org.newdawn.slick.Color;

import a1.DialogFactory;
import a1.Lang;
import a1.Player;
import a1.Render2D;
import a1.gui.GUI;
import a1.gui.GUI_Label;
import a1.gui.GUI_Panel;
import a1.gui.GUI_Panel.RenderMode;
import a1.gui.GUI_Progressbar;
import a1.gui.GUI_Window;

public class dlg_Stat extends Dialog {
	public static dlg_Stat dlg = null;
	
	GUI_Window wnd;
	GUI_Label combat_lbl, nature_lbl, industry_lbl, 
		str_lbl, con_lbl, dex_lbl, int_lbl, agi_lbl, wis_lbl, cha_lbl, perc_lbl;
	GUI_Progressbar str_bar, con_bar, dex_bar, int_bar, agi_bar, wis_bar, cha_bar, perc_bar;
	GUI_Panel stats_panel, exp_panel;
	
	static {
		Dialog.AddType("dlg_stat", new DialogFactory() {
			public Dialog create() { return new dlg_Stat(); }
		});
	}
	
	
	public void DoShow() {
		dlg = this;
		wnd = new GUI_Window(GUI.getInstance().normal) {
			protected void DoClose() {
				dlg_Stat.dlg.wnd = null;
				Dialog.Hide("dlg_stat");
			};
		};
		wnd.SetSize(300, 330);
		wnd.caption = Lang.getTranslate("generic", "stats");
		
		int row_margin = 20;
		int posy = 6; int posx = 7; int right_offset = 10; 
		int bar_w = 60; int bar_h = 18; int bar_x = 40; int bar_y = 1;
		//---------------------------------------------------------------------------
		stats_panel = new GUI_Panel(wnd);
		stats_panel.SetPos(11, 116);
		stats_panel.SetSize(150, 170);
		stats_panel.render_mode = RenderMode.rmSkin;
		stats_panel.skin_element = "hint";
		
		GUI_Label lbl = new GUI_Label(stats_panel);
		lbl.SetPos(posx, posy); 
		lbl.caption = "STR";
		str_lbl = new GUI_Label(stats_panel);
		str_lbl.SetPos(posx, posy);
		str_lbl.SetSize(stats_panel.size.x-posx-right_offset, row_margin);
		str_lbl.align = Render2D.Align_Right + Render2D.Align_Top;
		str_lbl.simple_hint=Lang.getTranslate("generic", "stat_str");
		str_bar = new GUI_Progressbar(stats_panel);
		str_bar.SetPos(posx+bar_x, posy+bar_y);
		str_bar.SetSize(bar_w, bar_h);
		posy+=row_margin;
		
		lbl = new GUI_Label(stats_panel);
		lbl.SetPos(posx, posy); 
		lbl.caption = "CON";
		con_lbl = new GUI_Label(stats_panel);
		con_lbl.SetPos(posx, posy);
		con_lbl.SetSize(stats_panel.size.x-posx-right_offset, row_margin);
		con_lbl.align = Render2D.Align_Right + Render2D.Align_Top;
		con_lbl.simple_hint=Lang.getTranslate("generic", "stat_con");
		con_bar = new GUI_Progressbar(stats_panel);
		con_bar.SetPos(posx+bar_x, posy+bar_y);
		con_bar.SetSize(bar_w, bar_h);
		posy+=row_margin;

		lbl = new GUI_Label(stats_panel);
		lbl.SetPos(posx, posy); 
		lbl.caption = "AGI";
		agi_lbl = new GUI_Label(stats_panel);
		agi_lbl.SetPos(posx, posy);
		agi_lbl.SetSize(stats_panel.size.x-posx-right_offset, row_margin);
		agi_lbl.align = Render2D.Align_Right + Render2D.Align_Top;
		agi_lbl.simple_hint=Lang.getTranslate("generic", "stat_agi");
		agi_bar = new GUI_Progressbar(stats_panel);
		agi_bar.SetPos(posx+bar_x, posy+bar_y);
		agi_bar.SetSize(bar_w, bar_h);
		posy+=row_margin;

		lbl = new GUI_Label(stats_panel);
		lbl.SetPos(posx, posy); 
		lbl.caption = "PERC";
		perc_lbl = new GUI_Label(stats_panel);
		perc_lbl.SetPos(posx, posy);
		perc_lbl.SetSize(stats_panel.size.x-posx-right_offset, row_margin);
		perc_lbl.align = Render2D.Align_Right + Render2D.Align_Top;
		perc_lbl.simple_hint=Lang.getTranslate("generic", "stat_perc");
		perc_bar = new GUI_Progressbar(stats_panel);
		perc_bar.SetPos(posx+bar_x, posy+bar_y);
		perc_bar.SetSize(bar_w, bar_h);
		posy+=row_margin;

		lbl = new GUI_Label(stats_panel);
		lbl.SetPos(posx, posy); 
		lbl.caption = "INT";
		int_lbl = new GUI_Label(stats_panel);
		int_lbl.SetPos(posx, posy);
		int_lbl.SetSize(stats_panel.size.x-posx-right_offset, row_margin);
		int_lbl.align = Render2D.Align_Right + Render2D.Align_Top;
		int_lbl.simple_hint=Lang.getTranslate("generic", "stat_int");
		int_bar = new GUI_Progressbar(stats_panel);
		int_bar.SetPos(posx+bar_x, posy+bar_y);
		int_bar.SetSize(bar_w, bar_h);
		posy+=row_margin;

		lbl = new GUI_Label(stats_panel);
		lbl.SetPos(posx, posy); 
		lbl.caption = "CHA";
		cha_lbl = new GUI_Label(stats_panel);
		cha_lbl.SetPos(posx, posy);
		cha_lbl.SetSize(stats_panel.size.x-posx-right_offset, row_margin);
		cha_lbl.align = Render2D.Align_Right + Render2D.Align_Top;
		cha_lbl.simple_hint=Lang.getTranslate("generic", "stat_cha");
		cha_bar = new GUI_Progressbar(stats_panel);
		cha_bar.SetPos(posx+bar_x, posy+bar_y);
		cha_bar.SetSize(bar_w, bar_h);
		posy+=row_margin;

		lbl = new GUI_Label(stats_panel);
		lbl.SetPos(posx, posy);
		lbl.caption = "DEX";
		dex_lbl = new GUI_Label(stats_panel);
		dex_lbl.SetPos(posx, posy);
		dex_lbl.SetSize(stats_panel.size.x-posx-right_offset, row_margin);
		dex_lbl.align = Render2D.Align_Right + Render2D.Align_Top;
		dex_lbl.simple_hint=Lang.getTranslate("generic", "stat_dex");
		dex_bar = new GUI_Progressbar(stats_panel);
		dex_bar.SetPos(posx+bar_x, posy+bar_y);
		dex_bar.SetSize(bar_w, bar_h);
		posy+=row_margin;

		lbl = new GUI_Label(stats_panel);
		lbl.SetPos(posx, posy); 
		lbl.caption = "WIS";
		wis_lbl = new GUI_Label(stats_panel);
		wis_lbl.SetPos(posx, posy);
		wis_lbl.SetSize(stats_panel.size.x-posx-right_offset, row_margin);
		wis_lbl.align = Render2D.Align_Right + Render2D.Align_Top;
		wis_lbl.simple_hint=Lang.getTranslate("generic", "stat_wis");
		wis_bar = new GUI_Progressbar(stats_panel);
		wis_bar.SetPos(posx+bar_x, posy+bar_y);
		wis_bar.SetSize(bar_w, bar_h);
		posy+=row_margin;
		
		
		//---------------------------------------------------------------------------
		exp_panel = new GUI_Panel(wnd);
		exp_panel.SetPos(11, 36);
		exp_panel.SetSize(150, 72);
		exp_panel.render_mode = RenderMode.rmSkin;
		exp_panel.skin_element = "hint";
		
		posy= 6;
		lbl = new GUI_Label(exp_panel);
		lbl.SetPos(posx, posy); 
		lbl.caption = "Combat";
		combat_lbl = new GUI_Label(exp_panel);
		combat_lbl.SetPos(posx, posy);
		combat_lbl.SetSize(exp_panel.size.x-posx-right_offset, row_margin);
		combat_lbl.align = Render2D.Align_Right + Render2D.Align_Top;
		posy+=row_margin;
		
		lbl = new GUI_Label(exp_panel);
		lbl.SetPos(posx, posy); 
		lbl.caption = "Industry";
		industry_lbl = new GUI_Label(exp_panel);
		industry_lbl.SetPos(posx, posy);
		industry_lbl.SetSize(exp_panel.size.x-posx-right_offset, row_margin);
		industry_lbl.align = Render2D.Align_Right + Render2D.Align_Top;
		posy+=row_margin;

		lbl = new GUI_Label(exp_panel);
		lbl.SetPos(posx, posy); 
		lbl.caption = "Nature";
		nature_lbl = new GUI_Label(exp_panel);
		nature_lbl.SetPos(posx, posy);
		nature_lbl.SetSize(exp_panel.size.x-posx-right_offset, row_margin);
		nature_lbl.align = Render2D.Align_Right + Render2D.Align_Top;

		UpdateStats();
	}

	public void DoHide() {
		dlg = null;
		if (wnd != null)
			wnd.Unlink();
		wnd = null;

	}
	
	public void DoResolutionChanged() {
		wnd.Center();
	}
	
	public void UpdateStats() {
		SetStatVals(Player.stats.get("str"),  str_lbl, str_bar);
		SetStatVals(Player.stats.get("dex"),  dex_lbl, dex_bar);
		SetStatVals(Player.stats.get("int"),  int_lbl, int_bar);
		SetStatVals(Player.stats.get("agi"),  agi_lbl, agi_bar);
		SetStatVals(Player.stats.get("perc"), perc_lbl, perc_bar);
		SetStatVals(Player.stats.get("wis"),  wis_lbl, wis_bar);
		SetStatVals(Player.stats.get("con"),  con_lbl, con_bar);
		SetStatVals(Player.stats.get("cha"),  cha_lbl, cha_bar);
		
		combat_lbl.caption = format_int(Integer.toString(Player.exp.combat));
		industry_lbl.caption = format_int(Integer.toString(Player.exp.industry));
		nature_lbl.caption = format_int(Integer.toString(Player.exp.nature));
	}
	
	private String format_int(String val) {
		String res = "";
		int c = 0;
		for (int i=val.length()-1; i>=0; i--) {
			c++;
			if (c>3) {
				res = " "+res;
				c = 1;
			}
			res = val.charAt(i) + res;
		}
		return res;
	}

	private void SetStatVals(Player.Stat stat, GUI_Label lbl, GUI_Progressbar bar) {
		lbl.caption = format_int(Integer.toString(stat.level));
		bar.SetMax(stat.max_fep);
		bar.SetValue(stat.fep);
		bar.SetColor(Color.green);
		bar.simple_hint = "food: "+format_int(Integer.toString(stat.fep))+"/"+format_int(Integer.toString(stat.max_fep));
	}
	
	public static boolean Exist() {
		return dlg != null;
	}
}
