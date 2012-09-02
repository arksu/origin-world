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
import a1.utils.InventoryClick;
import org.newdawn.slick.Color;

import java.util.ArrayList;
import java.util.List;


public class GUI_Inventory extends GUI_Control {
    /**
     * ид объекта чей инвентарь открыли
     */
    public int objid = 0;
    /**
     * размер инвентаря
     */
    public Coord inv_size = Coord.z;

	public GUI_Inventory(GUI_Control parent) {
		super(parent); 
	}

	
	public void DoRender() {
		for (int i = 0; i < inv_size.x; i++)
			for (int j = 0; j < inv_size.y; j++)
				getSkin().Draw("icon_bg", abs_pos.x + i*33, abs_pos.y + j*33);

        if (Player.hand.isExist() && MouseInChilds()) {
            int mx = gui.mouse_pos.x - abs_pos.x;
            int my = gui.mouse_pos.y - abs_pos.y;
            int xx = (mx - Player.hand.offset_x + 16) / 33;
            int yy = (my - Player.hand.offset_y + 16) / 33;

            Render2D.ChangeColor(new Color(0, 170, 0, 70));
            Render2D.Disable2D();
            Render2D.FillRect(new Coord(xx, yy).mul(33).add(abs_pos), new Coord(Player.hand.w, Player.hand.h).mul(33));
            Render2D.Enable2D();
        }

    }
	
    public void AssignInventory(Inventory inv) {
        this.objid = inv.objid;
        inv_size = new Coord(inv.w, inv.h);
        SetSize(inv_size.mul(33));
        // удаляем все что было в инвентаре
        while (child != null) child.Unlink();

        List<GUI_InvItem> items = new ArrayList<GUI_InvItem>();
        for (InvItem it : inv.items) {
            GUI_InvItem item = new GUI_InvItem(this);
            item.Assign(it);
            items.add(item);
        }
        for (int i =0; i < inv.w; i++)
            for (int j = 0; j < inv.h; j++) {
                boolean f = false;
                for (GUI_InvItem ii : items) {
                    f = (ii.Contains(i, j));
                    if (f) break;
                }
                if (!f) {
                    GUI_InvItem iii = new GUI_InvItem(this);
                    iii.SetLocal(i,j);
                    items.add(iii);
                }
            }

    }

    public void ItemClick(InventoryClick click) { }


}
