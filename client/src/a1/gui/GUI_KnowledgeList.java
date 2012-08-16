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
import a1.Player;
import a1.Render2D;
import org.newdawn.slick.Color;

import java.util.ArrayList;
import java.util.List;

public class GUI_KnowledgeList extends GUI_StringList{
    String knw_base = "";
    public List<Knowledge> knw_list = new ArrayList<Knowledge>();

    public GUI_KnowledgeList(GUI_Control parent) {
        super(parent);
    }

    public void SetKnowledgeBase(String base) {
        knw_base = base;
        Rebuild();
    }

    private void Rebuild() {
        Clear();
        knw_list.clear();
        for (Knowledge k : Player.knowledges.values()) {
            if (k.base.equals(knw_base)) {
                knw_list.add(k);
                Add(k.name);
            }
        }
    }
    
    public void SetSelectedKnw(String name) {
        SetSelected(-1, false);
        for (int i =0; i < knw_list.size(); i++) {
            if (knw_list.get(i).name.equals(name)) {
                SetSelected(i, true);
                break;
            }
        }
    }

    protected void DoDrawItem(int index, int x, int y, int w, int h) {
        Render2D.Text(font_name, x, y, knw_list.get(index).name);
        Render2D.Text(font_name, x,y,w-7,h,
                Render2D.Align_Right,
                String.valueOf(knw_list.get(index).level), Color.white);
    }

}
