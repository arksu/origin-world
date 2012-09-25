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

import a1.*;
import a1.net.NetGame;
import org.newdawn.slick.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static a1.gui.GUI_Knowledge.BAR_OFFSET;
import static a1.gui.GUI_Knowledge.LV_OFFSET;
import static a1.gui.Skin.*;

public class GUI_SkillList extends GUI_ListBox {
    Knowledge knw;
    public List<Skill> skill_list = new ArrayList<Skill>();
    public String font_name = "default";
    public String hint_font = "default";

    public GUI_SkillList(GUI_Control parent) {
        super(parent);
    }

    @Override
    public String getHint() {
        String str = "";

        int sel = GetMouseItemIndex();
        if (sel < 0) return null;
        Skill s = skill_list.get( sel );

        if (s != null) {
            if (s.level == 0) str += "req exp : "+s.req_exp+"%n";
            str += Lang.getTranslate("skills","hint_"+s.name);
        }

        return str;
    }


    public void SetKnowledge(Knowledge k) {
        knw = k;
        Rebuild();
    }

    private class SkillComparator implements Comparator<Skill> {
        public int compare(Skill s1, Skill s2) {
            return s1.name.compareTo(s2.name);
        }
    }


    private void Rebuild() {
        skill_list.clear();
        if (knw != null)
        for (Skill s : knw.skills) {
                skill_list.add(s);
        }
        SkillComparator comp = new SkillComparator();
        Collections.sort(skill_list, comp);
    }

    protected void DoDrawItem(int index, int x, int y, int w, int h) {
        Skill s = skill_list.get(index);
        if (s != null) {
            Color col = (s.level > 0 ? (s.level > knw.level?Color.red:Color.white):Color.gray);
            Render2D.Text(font_name, x, y, Lang.getTranslate("skills",skill_list.get(index).name), col);
            Render2D.Text(font_name, x+LV_OFFSET, y,
                    String.valueOf(s.level), col);
            if (s.level > 0)
                DrawBar(x+BAR_OFFSET,y,w-BAR_OFFSET,h, s);
            else
                DrawBuyBtn(x+BAR_OFFSET,y,w-BAR_OFFSET,h, index);
        }

    }
    
    private void DrawBar(int x, int y, int w, int h, Skill s) {
        getSkin().Draw("hint", x,y,w,h);
        Render2D.Disable2D();
        float pc = ((float)s.bar / (float)s.max_bar) *  ((float)w-2);
        Render2D.FillRect(new Coord(x+1,y+1), new Coord(Math.round(pc), h-2), new Color(73, 135, 73));
        Render2D.Enable2D();
    }

    private void DrawBuyBtn(int x, int y, int w, int h, int index) {
        int state = (MouseInMe() && gui.MouseInRect(new Coord(x,y), new Coord(w,h))) ? (pressed ? StatePressed : StateHighlight) : StateDefault;
        getSkin().Draw("button", x, y, w, h, state);
        Render2D.Text(font_name, x, y, w, h, Render2D.Align_Center, "Buy", Color.white);
    }

    protected boolean OnItemClick(int index, int ax, int ay, int btn, boolean down) {
        boolean result = false;
        if (down && btn == Input.MB_LEFT) {
            SelectedItem = index;
            result = true;
            Skill s = skill_list.get(index);
            Coord ac = new Coord(ax, ay);
            if (
                    s != null &&
                    s.level == 0 &&
                    MouseInMe() &&
                    ac.in_rect(
                            new Coord(BAR_OFFSET, 0),
                            new Coord(size.x-GetVertScrollWidth()-BAR_OFFSET, GetItemHeight(index))
                    )
            ) {
                BuySkill(s);
            }
        }
        return result;
    }

    protected void BuySkill(Skill s) {
        if (knw != null) {
            Log.debug("buy skill: " + s.name);
            NetGame.SEND_skill_buy(s.name, knw.name);
        }
    }

    public int GetCount() {
        return skill_list.size();
    }

    public int GetItemHeight(int index) {
        if (index < 0 || index >= skill_list.size()) {
            return 0;
        }
        return Render2D.GetTextHeight(font_name, skill_list.get(index).name)+2;
    }
}
