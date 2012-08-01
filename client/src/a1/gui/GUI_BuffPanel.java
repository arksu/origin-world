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

import a1.Player;

public class GUI_BuffPanel extends GUI_Control {
	List<GUI_Buff> buffs = new ArrayList<GUI_Buff>();
	
	
	public GUI_BuffPanel(GUI_Control parent) {
		super(parent);
	}
	
	public void Clear() {
		for (GUI_Buff b : buffs) {
			b.Unlink();
		}
		buffs.clear();
		SetSize(0, 0);
	}
	
	public void AddBuff(Player.Buff b) {
		GUI_Buff bb = new GUI_Buff(this);
		bb.buff = b;
		bb.SetPos(buffs.size()*38, 0);
		bb.SetSize(32, 32);
		bb.simple_hint = b.type;
		buffs.add(bb);
		
		SetSize(buffs.size()*38, 32);
	}

}
