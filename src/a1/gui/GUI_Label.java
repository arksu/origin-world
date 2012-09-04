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
import a1.net.Packet;
import org.newdawn.slick.Color;



public class GUI_Label extends GUI_Control {
	public String caption = "";
	public String font = "default";
	public int align = Render2D.Align_Default;
	public Color color = Color.white;
    public boolean pressed = false;
    public boolean multi_row = false;
	
	public GUI_Label(GUI_Control parent) {
		super(parent);
	}
	
	// прочитать параметры контрола из сети
	public void DoNetRead(Packet pkt) {
		int x, y;
		x = pkt.read_int();
		y = pkt.read_int();
		SetPos(x, y);
		caption = Lang.getTranslate("server", pkt.read_string_ascii());
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
    }

	public void DoRender() {
        if (multi_row) {
            int ay = 0;
            String[] sl = caption.split("%n");
            for (String cc : sl) {
                Render2D.Text(font, abs_pos.x, abs_pos.y+ay, size.x, size.y, align, cc, color);
                ay += Render2D.GetTextHeight(font);
            }
        }
		else Render2D.Text(font, abs_pos.x, abs_pos.y, size.x, size.y, align, caption, color);
	}

}
