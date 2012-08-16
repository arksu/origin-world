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
import a1.Packet;
import a1.Render2D;
import a1.net.NetGame;

public class GUI_BuildSlot extends GUI_Control {
	public String icon_name = "";
	public int num1 = 0;
	public int num2 = 0;
	public int num3 = 0;
	public boolean pressed = false;
	
	
	public GUI_BuildSlot(GUI_Control parent) {
		super(parent);
	}

	static {
		GUI.AddType("gui_build_slot", new ControlFactory() {		
			public GUI_Control create(GUI_Control parent) {
				return new GUI_BuildSlot(parent);
			}
		});
	}
	
	// прочитать параметры контрола из сети
	public void DoNetRead(Packet pkt) {
		icon_name = pkt.read_string_ascii();
		simple_hint = Lang.getTranslate("server", pkt.read_string_ascii());
		num1 = pkt.read_int();
		num2 = pkt.read_int();
		num3 = pkt.read_int();
		SetSize(200, 40);
	}

	public void DoRender() {
		// выводим фон
		getSkin().Draw("build_slot",abs_pos.x, abs_pos.y, size.x, size.y);
		// вывести иконку
		getSkin().Draw("icon_"+icon_name, abs_pos.x, abs_pos.y);
		// цифры
		Render2D.Text("", abs_pos.x+40, abs_pos.y, num1+"/"+num2+"/"+num3);
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
	
	public void DoClick() {
		int mx = gui.mouse_pos.x - abs_pos.x;
		int my = gui.mouse_pos.y - abs_pos.y;
		NetGame.SEND_gui_click(0,0,mx, my, id, Input.MB_LEFT);		
	}
}
