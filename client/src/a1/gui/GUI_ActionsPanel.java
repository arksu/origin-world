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

import a1.ActionsMenu;
import a1.Config;

import java.util.List;
// нужно дописывать..... должна быть по идее панелька с кнопками действий
// хинты и названия для кнопок брать из перевода на основе серверного названия действия

public class GUI_ActionsPanel extends GUI_Control {
	int rows = 0;
	int cols = 0;
	List<ActionsMenu.ActionsMenuItem> items;

	public GUI_ActionsPanel(GUI_Control parent, String parent_name) {
		super(parent);
		items = ActionsMenu.getList(parent_name);
	}

	public void SetRowCol(int x, int y) {
		rows = y;
		cols = x;
		SetSize(x * (Config.ICON_SIZE + 1) + 1, y * (Config.ICON_SIZE + 1) + 1);
	}

	public void DoRender() {
		for (int i = 0; i < cols; i++)
			for (int j = 0; j < rows; j++) {
				getSkin().Draw("icon_bg", abs_pos.x + i * 33,
						abs_pos.y + j * 33);
			}
	}
	
	protected void PlaceNavigateBtns() {
		
	}
	
	protected void PlaceButtons() {
		
	}

}
