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

import static a1.gui.Skin.*;

import org.newdawn.slick.Color;

import a1.Input;
import a1.Render2D;

public class GUI_Checkbox extends GUI_Control {
	public boolean checked = false;
	boolean pressed = false;
	public String caption = "";
	public String font = "default";
	public Color caption_color = Color.white;
	
	public GUI_Checkbox(GUI_Control parent) {
		super(parent);
		skin_element = "checkbox";
	}

	public void DoClick() {
	}
	
	public boolean DoMouseBtn(int btn, boolean down) {
		if (!enabled) return false;
		
		if (btn == Input.MB_LEFT)
			if (down) {
				if (MouseInMe()){
					pressed = true;
					return true;
				}
			} else {
				if (pressed && MouseInMe()) {
					checked = !checked;
					DoClick();
					pressed = false;
					return true;
				}
				pressed = false;
			}
		return false;
	}
	
	public void DoRender() {
		int state;
		if (checked) {
			if (!enabled)
				state = StateDisable_Checked;
			else
				if (MouseInMe())
					if (pressed) 
						state = StatePressed_Checked;
					else
						state = StateHighlight_Checked;
				else
					state = StateNormal_Checked;
		} else {
			if (!enabled)
				state = StateDisable;
			else
				if (MouseInMe())
					if (pressed) 
						state = StatePressed;
					else
						state = StateHighlight;
				else
					state = StateNormal;
		}
		getSkin().Draw(skin_element, abs_pos.x, abs_pos.y, 
				getSkin().GetElementSize(skin_element).x, 
				getSkin().GetElementSize(skin_element).y, state);
		if (caption.length() > 0)
			Render2D.Text(font, 
					abs_pos.x+getSkin().GetElementSize(skin_element).x+3, abs_pos.y, 
					size.x-getSkin().GetElementSize(skin_element).x, size.y, 
					Render2D.Align_Left + Render2D.Align_VStretch, caption, caption_color);
	}
}
