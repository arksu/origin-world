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

package a1.gui.utils;

import a1.Coord;
import a1.gui.GUI_Control;
import a1.gui.GUI_DragControl;

public class DragInfo {
	public static final int DRAG_STATE_NONE = 0;
	// over empty space or controls that don't have drag'n'drop
	public static final int DRAG_STATE_MISS = 1;
	// over another Object that accept dropping on it
	public static final int DRAG_STATE_ACCEPT = 2;
	// over another Object that refuse dropping on it
	public static final int DRAG_STATE_REFUSE = 3;
	//--------------------------------------------------------------------	
	
	// состояние перетаскивания 
	public int state = DRAG_STATE_NONE;
	// перетаскиваемый контрол
	public GUI_DragControl drag_control = null;
	// смещение перетаскиваемого объекта относительно мыши
	public Coord hotspot = new Coord(0, 0);
	
	public void Reset() {
		state = DRAG_STATE_NONE;
		
		if (drag_control != null) {
			GUI_Control c = drag_control;
			drag_control = null;
			c.Unlink();
		}
		
		hotspot = new Coord(0, 0);
	}
}
