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

import a1.KinInfo;
import a1.Obj;
import a1.ObjCache;
import a1.Player;
import a1.Render2D;

public class GUI_Target extends GUI_Control {

	public GUI_Target(GUI_Control parent) {
		super(parent);
	}
	
	public void DoRender() {
		getSkin().Draw("listbox", abs_pos.x, abs_pos.y, size.x, size.y);
		// если таргет ид больше 0 - выводим инфу о нем
		if (Player.TargetID > 0) {
			Obj o = ObjCache.get(Player.TargetID);
			KinInfo kin = o.getattr(KinInfo.class);
			if (kin != null) {
				Render2D.Text("", abs_pos.x, abs_pos.y, kin.name);
			}
		}
	}
	
	public void DoUpdate() {
		
	}

}
