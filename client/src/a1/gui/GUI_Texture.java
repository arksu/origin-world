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

import a1.Sprite;
import org.newdawn.slick.opengl.Texture;

public class GUI_Texture extends GUI_Control {
	public Sprite spr;
	public String skin_element = "";
	public String mode = "spr";
	
	public GUI_Texture(GUI_Control parent) {
		super(parent);
	}
	
	public void setTexture(Texture t) {
		spr = new Sprite(t);
	}
	
	public void setTexture(Sprite s) {
		spr = s;
	}
	
	public void DoRender() {
		if (mode.equals("spr"))
			spr.draw(abs_pos.x, abs_pos.y, size.x, size.y);
		if (mode.equals("skin_element"))
			getSkin().Draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y);
	}
}
