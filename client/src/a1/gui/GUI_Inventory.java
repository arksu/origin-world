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
package a1.gui;

import java.util.ArrayList;
import java.util.List;

import a1.Coord;
import a1.Packet;


public class GUI_Inventory extends GUI_Control {
	public Coord inv_size = Coord.z;
	
	static {
		GUI.AddType("gui_inventory", new ControlFactory() {		
			public GUI_Control create(GUI_Control parent) {
				return new GUI_Inventory(parent);
			}
		});
	}

	public GUI_Inventory(GUI_Control parent) {
		super(parent); 
	}
	
	// прочитать параметры контрола из сети
	public void DoNetRead(Packet pkt) {
		//Log.info("inventory DoNetRead");
		int w = pkt.read_int();
		int h = pkt.read_int();
		inv_size = new Coord(w,h);
		
		while (child != null) child.Unlink();
		
		int count = pkt.read_int();
		List<GUI_InvItem> items = new ArrayList<GUI_InvItem>();
		while (count > 0) {
			count--;
			GUI_InvItem item = new GUI_InvItem(this);
			item.NetRead(pkt);
			items.add(item);
		}
		for (int i =0; i < w; i++)
			for (int j = 0; j < h; j++) {
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
	
	public void DoRender() {
		for (int i = 0; i < inv_size.x; i++)
			for (int j = 0; j < inv_size.y; j++)
				getSkin().Draw("icon_bg", abs_pos.x + i*33, abs_pos.y + j*33);
	}
	


}
