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

import a1.Lang;
import a1.Player;
import a1.Render2D;
import a1.gui.*;
import a1.gui.GUI_Panel.RenderMode;
import org.newdawn.slick.Color;

public class dlg_Stat extends Dialog {
	public static dlg_Stat dlg = null;
	
	GUI_Window wnd;
	GUI_Label combat_lbl, nature_lbl, industry_lbl, 
		str_lbl, con_lbl, dex_lbl, int_lbl, agi_lbl, wis_lbl, cha_lbl, perc_lbl,
        knw_lbl;
	GUI_Progressbar str_bar, con_bar, dex_bar, int_bar, agi_bar, wis_bar, cha_bar, perc_bar;
	GUI_Panel stats_panel, exp_panel, skills_panel;
    GUI_Button btn_combat, btn_industry, btn_nature;
    GUI_KnowledgeList knw;
    GUI_Knowledge knw_desc;
	
	static {
		Dialog.AddType("dlg_stat", new DialogFactory() {
			public Dialog create() { return new dlg_Stat(); }
		});
	}

    int row_margin;
    int posy, posx, right_offset, bar_w, bar_h, bar_x, bar_y;
    int exp_mode = 2;

	public void DoShow() {
		dlg = this;
		wnd = new GUI_Window(GUI.getInstance().normal) {
			protected void DoClose() {
				dlg_Stat.dlg.wnd = null;
				Dialog.Hide("dlg_stat");
			}
		};
		wnd.SetSize(607, 330);
		wnd.caption = Lang.getTranslate("generic", "stats");
		
		row_margin = 20;
		posy = 6; posx = 7; right_offset = 10;
		bar_w = 60; bar_h = 18; bar_x = 40; bar_y = 1;
		//---------------------------------------------------------------------------
		stats_panel = new GUI_Panel(wnd);
		stats_panel.SetPos(11, 116);
		stats_panel.SetSize(150, 172);
		stats_panel.render_mode = RenderMode.rmSkin;
		stats_panel.skin_element = "hint";

        str_lbl = new GUI_Label(stats_panel); str_bar = new GUI_Progressbar(stats_panel);
        place_stat("str", str_lbl, str_bar);

        con_lbl = new GUI_Label(stats_panel); con_bar = new GUI_Progressbar(stats_panel);
        place_stat("con", con_lbl, con_bar);

        agi_lbl = new GUI_Label(stats_panel); agi_bar = new GUI_Progressbar(stats_panel);
        place_stat("agi", agi_lbl, agi_bar);

        perc_lbl = new GUI_Label(stats_panel); perc_bar = new GUI_Progressbar(stats_panel);
        place_stat("perc", perc_lbl, perc_bar);

        int_lbl = new GUI_Label(stats_panel); int_bar = new GUI_Progressbar(stats_panel);
        place_stat("int", int_lbl, int_bar);

        cha_lbl = new GUI_Label(stats_panel); cha_bar = new GUI_Progressbar(stats_panel);
        place_stat("cha", cha_lbl, cha_bar);

        dex_lbl = new GUI_Label(stats_panel); dex_bar = new GUI_Progressbar(stats_panel);
        place_stat("dex", dex_lbl, dex_bar);

        wis_lbl = new GUI_Label(stats_panel); wis_bar = new GUI_Progressbar(stats_panel);
        place_stat("wis", wis_lbl, wis_bar);

		//---------------------------------------------------------------------------
		exp_panel = new GUI_Panel(wnd);
		exp_panel.SetPos(11, 36);
		exp_panel.SetSize(150, 72);
		exp_panel.render_mode = RenderMode.rmSkin;
		exp_panel.skin_element = "hint";

		posy= 6;
        combat_lbl = new GUI_Label(exp_panel);
        industry_lbl = new GUI_Label(exp_panel);
        nature_lbl = new GUI_Label(exp_panel);
        place_exp("exp_combat", combat_lbl);
        place_exp("exp_industry", industry_lbl);
        place_exp("exp_nature", nature_lbl);

        //---------------------------------------------------------------------------
        skills_panel = new GUI_Panel(wnd);
        skills_panel.SetPos(171, 36);
        skills_panel.SetSize(425, 252);
        skills_panel.render_mode = RenderMode.rmSkin;
        skills_panel.skin_element = "hint";
        
        btn_combat = new GUI_Button(skills_panel) {
            public void DoClick() {
                knw.SetKnowledgeBase("combat");
                knw_desc.SetKnowledge(null);
                exp_mode = 0;
            }

            protected boolean getPressed() {
                return exp_mode == 0;
            };
        };
        btn_combat.SetPos(10, 10);
        btn_combat.SetSize(100, 23);
        btn_combat.caption = Lang.getTranslate("generic","exp_combat");

        btn_industry = new GUI_Button(skills_panel) {
            public void DoClick() {
                knw.SetKnowledgeBase("industry");
                knw_desc.SetKnowledge(null);
                exp_mode = 1;
            }
            protected boolean getPressed() {
                return exp_mode == 1;
            };
        };
        btn_industry.SetPos(115, 10);
        btn_industry.SetSize(100, 23);
        btn_industry.caption = Lang.getTranslate("generic","exp_industry");

        btn_nature = new GUI_Button(skills_panel) {
            public void DoClick() {
                knw.SetKnowledgeBase("nature");
                knw_desc.SetKnowledge(null);
                exp_mode = 2;
            }
            protected boolean getPressed() {
                return exp_mode == 2;
            };
        };
        btn_nature.SetPos(220, 10);
        btn_nature.SetSize(100, 23);
        btn_nature.caption = Lang.getTranslate("generic","exp_nature");


        //--------------
        knw_lbl = new GUI_Label(skills_panel);
        knw_lbl.caption = Lang.getTranslate("generic","knowledges")+":";
        knw_lbl.SetPos(10, 35);

        knw = new GUI_KnowledgeList(skills_panel) {
            public void DoClick() {
                knw_desc.SetKnowledge( knw_list.get(SelectedItem) );
            }
        };
        knw.SetPos(10, 55);
        knw.SetSize(130, 190);
        knw.SetKnowledgeBase("nature");

        knw_desc = new GUI_Knowledge(skills_panel);
        knw_desc.SetPos(145, 35);
        knw_desc.SetSize(275, 210);
        knw_desc.UpdatePos();

		UpdateStats();
	}

    private void place_exp(String capt, GUI_Label ll) {
        GUI_Label lbl = new GUI_Label(exp_panel);
        lbl.SetPos(posx, posy);
        lbl.caption = Lang.getTranslate("generic", capt);
        ll.SetPos(posx, posy);
        ll.SetSize(exp_panel.size.x-posx-right_offset, row_margin);
        ll.align = Render2D.Align_Right + Render2D.Align_Top;
        posy+=row_margin;

    }

    private void place_stat(String name, GUI_Label ll, GUI_Progressbar bb) {
        GUI_Label lbl = new GUI_Label(stats_panel);
        lbl.SetPos(posx, posy);
        lbl.caption = name.toUpperCase();
        ll.SetPos(posx, posy);
        ll.SetSize(stats_panel.size.x-posx-right_offset, row_margin);
        ll.align = Render2D.Align_Right + Render2D.Align_Top;
        ll.simple_hint=Lang.getTranslate("generic", "stat_"+name);
        bb.SetPos(posx+bar_x, posy+bar_y);
        bb.SetSize(bar_w, bar_h);
        posy+=row_margin;
    }

    public void UpdateSkills() {
        int old = knw.GetSelected();
        String kn = "";
        if (old >= 0) 
             kn = knw.knw_list.get(old).name;
        switch (exp_mode) {
            case 0:
                knw.SetKnowledgeBase("combat");
                break;
            case 1:
                knw.SetKnowledgeBase("industry");
                break;
            case 2:
                knw.SetKnowledgeBase("nature");
                break;
        }
        knw.SetSelectedKnw(kn);
        int sel = knw.GetSelected();
        if (sel >= 0)
            knw_desc.SetKnowledge( knw.knw_list.get(sel) );
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
		bar.simple_hint = Lang.getTranslate("generic","food")+ ": "+format_int(Integer.toString(stat.fep))+"/"+format_int(Integer.toString(stat.max_fep));
	}
	
	public static boolean Exist() {
		return dlg != null;
	}
}
