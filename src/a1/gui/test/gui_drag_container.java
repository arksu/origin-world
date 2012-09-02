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

import a1.Input;
import a1.Log;
import a1.Render2D;
import a1.gui.GUI_Control;
import a1.gui.utils.DragInfo;
import org.newdawn.slick.Color;

public class gui_drag_container extends GUI_Control {
	// имеем ли мы нечто что можно перетащить
	public boolean have_some = false;
	// что то над нами тащат...
	public boolean drag_above_me = false;
	
	public gui_drag_container(GUI_Control parent) {
		super(parent);
		drag_enabled = true;
	}

	boolean pressed = false;
	public boolean DoMouseBtn(int btn, boolean down) {
		if (down) {
			if (pressed) {
				pressed = false;
				DoClick();
			}
			pressed = true;
		} else pressed = false;
		// в контейнере начинаем драг
		if (down && btn == Input.MB_LEFT && MouseInMe() && have_some) {
			// если левой мышкой чото начинаем тащить
			//gui.BeginCheckDrag(this);
			gui.BeginDrag(this, new gui_somedrag(), gui.mouse_pos.sub(abs_pos));
			// при этом вещь не удаляется из контейнера.
			// удалить надо только когда закончится драг.
			return true;
		}
		return false;
	}
	
	void DoClick() {
		Log.info("CLICKED");
	}
	
	// запрос на возможность принять контрол
	public boolean DoRequestDrop(DragInfo info) {
		// тут по идее надо проверять что мы пытаемся дропнуть на контейнер.
		// и вернуть истину только если дроп возможен.
		return (info.drag_control instanceof gui_somedrag); 
	}
	
	public void DoEndDrag(DragInfo info) {
		// закончили перетаскивание
		// тут момент бросания некой вещи на этот контейнер
		// (не с которого начали перетаскивание)
		if (info.drag_control instanceof gui_somedrag) {
			((gui_drag_container)info.drag_control.drag_parent).have_some = false;
			have_some = true;
		}
	}

	public void DoUpdate() {
		drag_above_me = (MouseInMe() && gui.drag_info.state == DragInfo.DRAG_STATE_ACCEPT);
	}
	
	public void DoRender() {
		Render2D.Disable2D();
		if (drag_above_me)
			Render2D.FillRect(abs_pos, size, Color.yellow);
		else
		if (have_some)
			Render2D.FillRect(abs_pos, size, Color.red);
		else
			Render2D.FillRect(abs_pos, size, Color.white);
		Render2D.Enable2D();
	}
	
}
