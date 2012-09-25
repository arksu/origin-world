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

import a1.Render2D;
import org.newdawn.slick.Color;


public class GUI_Panel extends GUI_Control {
	public boolean pressed = false;
	public enum RenderMode {rmSkin, rmColor, rmNone}
	public Color bg_color = Color.white;
	public RenderMode render_mode = RenderMode.rmNone;
	
	public GUI_Panel(GUI_Control parent) {
		super(parent);
	}
	
	public void DoClick() {
	}

	public boolean DoMouseBtn(int btn, boolean down) {
		if (!enabled) return false;
		
		if (down && MouseInMe()) {
			DoClick();
			return true;
		}
		
		return false;
	}
	
	public void DoRender() {
		switch (render_mode) {
		case rmColor:
			Render2D.Disable2D();
			Render2D.FillRect(abs_pos, size, bg_color);
			Render2D.Enable2D();
			break;
		case rmSkin :
			getSkin().Draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y);
			break;
		case rmNone :
			break;
		}
	}
}
