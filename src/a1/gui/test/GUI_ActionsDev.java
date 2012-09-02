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

import a1.ActionsMenu;
import a1.ActionsMenu.ActionsMenuItem;
import a1.Lang;
import a1.gui.GUI;
import a1.gui.GUI_Button;
import a1.gui.GUI_Control;
import a1.net.NetGame;

import java.util.ArrayList;
import java.util.List;

public class GUI_ActionsDev extends GUI_Control {
	List<ActionsMenuItem> list;
	List<GUI_Button> btns;
	String current_parent;
	
	public GUI_ActionsDev(GUI_Control parent) {
		super(parent);
		setRoot();
	}
	
	public void setRoot() {
		current_parent = "root";
		UpdateList();
	}
	
	public void DoDestroy() {
		if (btns != null && btns.size() > 0) {
			for (GUI_Button b : btns) {
				b.Unlink();
			}
		}
	}
	
	public void UpdateList() {
		if (btns != null && btns.size() > 0) {
			for (GUI_Button b : btns) {
				b.Unlink();
			}
		}
		
		list = ActionsMenu.getList(current_parent);
		btns = new ArrayList<GUI_Button>();
		int i = 0;
		for (ActionsMenuItem a : list) {
			GUI_Button b = new GUI_Button(GUI.getInstance().normal) {
				public void DoClick() {
					// если это подменю
					if (caption.contains("root_")) {
						current_parent = tag;
						UpdateList();
					} else {
					// это обычная кнопка	
						NetGame.SEND_action(caption);
						setRoot();
					}
				}
			};
			b.SetSize(200, 20);
			b.SetPos(abs_pos.x, abs_pos.y + i*30);
			b.caption = Lang.getTranslate("actions", a.name);
			b.tag = a.name;
			btns.add(b);
			i++;
		}
	}

}
