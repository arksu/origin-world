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
package a1.gui.test;

import a1.Coord;
import a1.Render2D;
import a1.gui.GUI_DragControl;
import org.newdawn.slick.Color;

public class gui_somedrag extends GUI_DragControl{
	public void DoRender() {
		Render2D.Disable2D();
		Render2D.FillRect(abs_pos, new Coord(50, 50), Color.darkGray);
//		Render2D.FillRect(new Coord(100,100), new Coord(10,10), Color.green);
		Render2D.Enable2D();
	}
}
