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
//класс для отладки гуя.

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;

import a1.Coord;
import a1.Input;
import a1.Render2D;

public class GUI_Debug {
	static public boolean active=false;
	
	static public GUI_Control debug_ctrl = null;
	
	static public void Render() {
		Render2D.Text("", 10, 300, "GUI DEBUG ON!");
		// подсветим нужный контрол
		if (debug_ctrl != null && !debug_ctrl.terminated) {
			Render2D.Disable2D();
			Render2D.ChangeColor(Color.red);
			Render2D.Rectangle(debug_ctrl.abs_pos, debug_ctrl.size, 2);
			Render2D.ChangeColor();
			Render2D.Enable2D();
		}
	}
	
	static public void Update() {
		// выбираем по ЛКМ
		if (GUI.getInstance().mouse_in_control != null && Input.MouseBtns[0]) debug_ctrl= GUI.getInstance().mouse_in_control;
		
		
		// передвигаем стрелками
		if (debug_ctrl != null && !debug_ctrl.terminated) {
			if (Input.KeyHit(Keyboard.KEY_H)) {
				debug_ctrl.visible = !debug_ctrl.visible;
			}
			
			int d = Input.isCtrlPressed()?1:5;
			Coord dd = Coord.z;
			if (Input.KeyHit(Keyboard.KEY_LEFT))
				dd = new Coord(-d, 0);
			if (Input.KeyHit(Keyboard.KEY_RIGHT))
				dd = new Coord(d, 0);
			if (Input.KeyHit(Keyboard.KEY_UP))
				dd = new Coord(0, -d);
			if (Input.KeyHit(Keyboard.KEY_DOWN))
				dd = new Coord(0, d);
			
			debug_ctrl.SetPos(debug_ctrl.pos.add(dd));
		}
	}
}
