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
import a1.obj.build;

public class GUI_BuildSlot extends GUI_Control {
	public String icon_name = "";
	public int num1 = 0;
	public int num2 = 0;
	public int num3 = 0;
    public int order = 0;
	public boolean pressed = false;
	
	
	public GUI_BuildSlot(GUI_Control parent) {
		super(parent);
	}

    public void Assign(build.BuildSlot s) {
        icon_name = s.icon;
        simple_hint = Lang.getTranslate("server", icon_name);
        num1 = s.n1;
        num2 = s.n2;
        num3 = s.n3;
        SetSize(140, 40);
    }

	public void DoRender() {
		// выводим фон
		getSkin().Draw("listbox",abs_pos.x, abs_pos.y, size.x, size.y);
		// вывести иконку
        String ic = "icon_"+icon_name;
        if (!getSkin().hasElement(ic)) ic ="item_"+icon_name;
        int oy = (size.y - 32) / 2;
		getSkin().Draw(ic, abs_pos.x + 10, abs_pos.y + oy, 32, 32);
		// цифры
		Render2D.Text("", abs_pos.x+50, abs_pos.y + oy+4, num1+" / "+num2+" / "+num3);
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
	
	public void DoClick() { }
}
