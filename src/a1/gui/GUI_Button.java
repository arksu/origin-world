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

import a1.Input;
import a1.Lang;
import a1.Render2D;
import a1.net.NetGame;
import a1.net.Packet;
import org.newdawn.slick.Color;

import static a1.gui.Skin.*;

public class GUI_Button extends GUI_Control {
	public boolean pressed = false;
	public String caption = "";
	public int caption_align = Render2D.Align_Center;
	public String font = "default";
	public Color caption_color = Color.white;
	public boolean render_bg = true;
	public String icon_name = "";
	
	public GUI_Button(GUI_Control parent) {
		super(parent);		
		skin_element = "button";
	}

	public void DoClick() {
		if (id > 0) {
			int mx = gui.mouse_pos.x - abs_pos.x;
			int my = gui.mouse_pos.y - abs_pos.y;
			NetGame.SEND_gui_click(0,0,mx, my, id, Input.MB_LEFT);
		}
	}
	
	// удерживать ли кнопку в нажатом состоянии
	protected boolean getPressed() {
		return false;
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
		if (!enabled)
			state = StateDisable;
		else
			if (MouseInMe())
				if (pressed) 
					state = StatePressed;
				else
					state = StateHighlight;
			else
				if (getPressed())
					state = StateHighlight;
				else
					state = StateNormal;
		if (render_bg)
			getSkin().Draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y, state);
		if (icon_name.length() > 0)
			getSkin().Draw(icon_name, abs_pos.x, abs_pos.y, size.x, size.y, state);
		if (caption.length() > 0)
			Render2D.Text(font, abs_pos.x, abs_pos.y, size.x, size.y, caption_align, caption, caption_color);
	}
	
	// прочитать параметры контрола из сети
	public void DoNetRead(Packet pkt) {
		caption = Lang.getTranslate("server", pkt.read_string_ascii());
		int w = pkt.read_int();
		int h = pkt.read_int();
		SetSize(w, h);
	}
}
