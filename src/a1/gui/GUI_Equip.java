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

import a1.Equip;
import a1.utils.InventoryClick;

public class GUI_Equip extends GUI_Control {
	GUI_InvItem head, body, lhand, rhand, legs, foots;
	
	public GUI_Equip(GUI_Control parent) {
		super(parent);
	}

    public void Init(Equip e) {
        while (child != null) child.Unlink();

        head = new GUI_InvItem(this) {
            protected int getEquipID() { return 201; }
        };
        head.Assign(e.head);
        if (head.objid == 0) {head.is_local = true; head.SetSize(65, 32);}
        head.SetPos(9, 10);

        body = new GUI_InvItem(this) {
            protected int getEquipID() { return 202; }
        };
        body.Assign(e.body);
        if (body.objid == 0) {body.is_local = true; body.SetSize(65, 65);}
        body.SetPos(9, 59);

        lhand = new GUI_InvItem(this) {
            protected int getEquipID() { return 203; }
        };
        lhand.Assign(e.lhand);
        if (lhand.objid == 0) {lhand.is_local = true; lhand.SetSize(65, 65);}
        lhand.SetPos(9, 130);

        rhand = new GUI_InvItem(this) {
            protected int getEquipID() { return 204; }
        };
        rhand.Assign(e.rhand);
        if (rhand.objid == 0) {rhand.is_local = true; rhand.SetSize(65, 65);}
        rhand.SetPos(229, 130);

        legs = new GUI_InvItem(this) {
            protected int getEquipID() { return 205; }
        };
        legs.Assign(e.legs);
        if (legs.objid == 0) {legs.is_local = true; legs.SetSize(65, 65);}
        legs.SetPos(9, 202);

        foots = new GUI_InvItem(this) {
            protected int getEquipID() { return 206; }
        };
        foots.Assign(e.foots);
        if (foots.objid == 0) {foots.is_local = true; foots.SetSize(65, 32);}
        foots.SetPos(9, 275);

        SetSize(280, 250);
    }

    public void ItemClick(InventoryClick click) { }
	
	public void DoRender() {
		getSkin().Draw("equip_man", abs_pos.x+75, abs_pos.y+20);
		
		DrawItemGrid(9,10,2,1);
		DrawItemGrid(9,59,2,2);
		DrawItemGrid(9,130,2,2);
		DrawItemGrid(229,130,2,2);
		DrawItemGrid(9,202,2,2);
		DrawItemGrid(9,275,2,1);
	}
	
	protected void DrawItemGrid(int x, int y, int w, int h) {
		for (int ax=0; ax<w; ax++) 
			for (int ay=0; ay<h; ay++)
				getSkin().Draw("icon_bg", abs_pos.x+x+ax*33, abs_pos.y+y+ay*33);
	}
}
