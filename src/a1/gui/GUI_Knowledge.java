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

package a1.gui;

import a1.Knowledge;
import a1.Lang;
import a1.Log;
import a1.Render2D;
import a1.net.NetGame;

public class GUI_Knowledge extends GUI_Control{
    GUI_SkillList skills;
    GUI_Label name_lbl, level_lbl, skills_lbl;
    GUI_Button btn_inc, btn_dec;
    public Knowledge knw;
    public static final int BTN_SIZE = 23;
    public static final int LV_OFFSET = 120;
    public static final int BAR_OFFSET = 150;

    public GUI_Knowledge(GUI_Control parent) {
        super(parent);
        skin_element = "listbox";

        name_lbl = new GUI_Label(this);
        level_lbl = new GUI_Label(this);
        skills_lbl = new GUI_Label(this);
        btn_dec = new GUI_Button(this) {
            public void DoClick() {
                if (knw != null) {
                    Log.debug("dec knw: "+knw.name);
                    DoDec();
                }
            }
        };
        btn_inc = new GUI_Button(this) {
            public void DoClick() {
                if (knw != null) {
                    Log.debug("inc knw: "+knw.name);
                    DoInc();
                }
            }
        };
        skills = new GUI_SkillList(this);

        name_lbl.Hide();
        level_lbl.Hide();
        btn_dec.Hide();
        btn_inc.Hide();
        skills.Hide();
        skills_lbl.Hide();

        UpdatePos();
    }

    public void UpdatePos() {
        name_lbl.SetPos(5,5);
        btn_inc.SetSize(BTN_SIZE,BTN_SIZE);
        btn_inc.SetPos( size.x - BTN_SIZE-5, 5 );
        level_lbl.SetPos(btn_inc.pos.x-27,5);
        level_lbl.align = Render2D.Align_HCenter + Render2D.Align_Top;
        level_lbl.SetSize(20,20);
        btn_dec.SetSize(BTN_SIZE,BTN_SIZE);
        btn_dec.SetPos(level_lbl.pos.x-BTN_SIZE-5, 5);

        btn_dec.caption = "-";
        btn_inc.caption = "+";

        skills_lbl.SetPos(4,32);
        skills_lbl.caption = Lang.getTranslate("generic","skills")+":";
        skills.SetPos(4,52);
        skills.SetSize(267, 155);
    }
    
    public void SetKnowledge(Knowledge k) {
        knw = k;
        skills.SetKnowledge(k);

        if (k != null) {
            name_lbl.Show();
            level_lbl.Show();
            btn_dec.Show();
            btn_inc.Show();
            skills.Show();
            skills_lbl.Show();

            name_lbl.caption = k.name;
            level_lbl.caption = String.valueOf(k.level);

        } else {
            name_lbl.Hide();
            level_lbl.Hide();
            btn_dec.Hide();
            btn_inc.Hide();
            skills.Hide();
            skills_lbl.Hide();
        }
    }

    public void DoRender() {
        getSkin().Draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y);
    }

    protected void DoDec() {
        NetGame.SEND_knowledge_dec(knw.name);
    }

    protected void DoInc() {
        NetGame.SEND_knowledge_inc(knw.name);
    }
}
