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

import static a1.gui.Skin.StateNormal;

import org.newdawn.slick.Color;

public class GUI_IconButton extends GUI_Button {
	String icon_name = "";
	
	public GUI_IconButton(GUI_Control parent) {
		super(parent);
		SetSize(32, 32);
	}
	
	// ставим иконку по названию элемента скина
	public void SetIcon(String icon_name) {
		this.icon_name = icon_name;
	}

	public void DoRender() {
		Color col;
		if (!enabled)
			col = new Color(255, 255, 255, 120);
		else
			if (MouseInMe())
				if (pressed) 
					col = new Color(255, 255, 255, 180);
				else
					col = new Color(255, 255, 255, 255);
			else
				col = new Color(255, 255, 255, 220);
		
		getSkin().Draw(icon_name, abs_pos.x, abs_pos.y, size.x, size.y, StateNormal, col);
	}
}
