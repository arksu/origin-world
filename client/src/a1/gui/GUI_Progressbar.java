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


import org.newdawn.slick.Color;

import a1.Coord;
import a1.Render2D;

public class GUI_Progressbar extends GUI_Control {
	int max = 100;
	int min = 0;
	int value = 50;
	Color color = Color.gray;
	public String bg_element_skin_name = "hint";

	public GUI_Progressbar(GUI_Control parent) {
		super(parent);
		
	}
	
	public void DoRender() {
		float s = ((float)(value-min) / (float)(max-min)) * size.x;
		int w = Math.round(s);
		getSkin().Draw(bg_element_skin_name, abs_pos.sub(2, 2), size.add(4, 4));
		Render2D.FillRect(abs_pos, new Coord(w, size.y), color);
	}
	
	public void SetValue(int val) {
		value = val;
		value = value>max?max:value<min?min:value;
	}
	
	public void SetMax(int val) {
		max = val;
	}

	public void SetMin(int val) {
		min = val;
	}
	
	public void SetColor(Color val) {
		color = val;
	}
}
