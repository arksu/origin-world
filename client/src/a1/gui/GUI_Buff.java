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

import a1.Lang;
import a1.Main;
import a1.Player;

public class GUI_Buff extends GUI_Control {
	Player.Buff buff;
	
	public GUI_Buff(GUI_Control parent) {
		super(parent);
		
	}
	
	public void DoUpdate() {
		buff.duration -= Main.dt;
		simple_hint = Lang.getTranslate("hint", "buff_"+buff.type)+" "+Integer.toString(buff.duration / 1000)+" sec";
	}
	
	public void DoRender() {
		getSkin().Draw("hint", abs_pos, size);
		getSkin().Draw("icon_buff_"+buff.type, abs_pos);
	}

}
