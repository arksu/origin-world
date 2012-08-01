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

import a1.Player;
import a1.Render2D;

public class GUI_Time extends GUI_Control {

	public GUI_Time(GUI_Control parent) {
		super(parent);
		
	}
	
	public void DoRender() {
		getSkin().Draw("hint", abs_pos.x, abs_pos.y, size.x, size.y);
		
		int real_day = Player.GlobalTime / (3600*24);
		int ingame_time = Player.GlobalTime % (3600*6);
		int ingame_day = Player.GlobalTime / (3600*6);
		int h = ingame_time / 900;
		int m = (ingame_time % 900) / 15;
		
		Render2D.Text("", abs_pos.x+5, abs_pos.y, "day from world beginning: "+Integer.toString(real_day));
		Render2D.Text("", abs_pos.x+5, abs_pos.y+20, "day "+Integer.toString(ingame_day % 30)+" of month "+Integer.toString((ingame_day / 30) % 12));
		Render2D.Text("", abs_pos.x+5, abs_pos.y+40, Integer.toString(h)+":"+(m<10?"0":"")+Integer.toString(m));
	}
	
	
	public void DoUpdate() {
		super.DoUpdate();
	}

}
