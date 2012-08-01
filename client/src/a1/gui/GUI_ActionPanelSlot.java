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

import static a1.gui.Skin.StateNormal;

import a1.Coord;
import a1.Input;
import a1.Lang;
import a1.net.NetGame;

public class GUI_ActionPanelSlot extends GUI_Control{
	public boolean have_some = false;
	public boolean drag_above_me = false;
	protected boolean pressed = false;

	protected boolean allow_drag = true;

	protected boolean is_local = false;
	protected String local_parent_name;
	public String action_name;
	private Coord press_coord = Coord.z;
	
	public GUI_ActionPanelSlot(GUI_Control parent) {
		super(parent);
		drag_enabled = true;
		SetSize(32,32);
	}

	public GUI_ActionPanelSlot SetSlot(String act_name) {
		action_name = act_name;
		simple_hint = Lang.getTranslate("hint", act_name);
		have_some = true;
		return this;
	}
	
	public void DisableDrag() {
		allow_drag = false;
	}
	
	public void UnsetSlot() {
		have_some = false;
		action_name = null;
		simple_hint = "";
		is_local = false;
		allow_drag = true;
	}
	
	public GUI_ActionPanelSlot SetLocal(String parent) {
		is_local = true;
		local_parent_name = parent;
		return this;
	}
	
	public void DoRender() {
		getSkin().Draw("icon_bg", abs_pos.x, abs_pos.y);
		if (action_name != null && have_some) {
			if (getSkin().hasElement("icon_" + action_name)) {
				getSkin().Draw("icon_" + action_name, abs_pos.x+1, abs_pos.y+1, size.x, size.y, StateNormal);
			} else getSkin().Draw("icon_unknown", abs_pos.x, abs_pos.y+1, size.x+1, size.y, StateNormal);
		}
	}

	public boolean DoMouseBtn(int btn, boolean down) {
		if (!enabled) return false;

		if (down) {
			if (MouseInMe()){
				pressed = true;
				press_coord = new Coord(gui.mouse_pos);
				return true;
			}
		} else {
			if (pressed && MouseInMe()) {
				DoClick(btn);
				pressed = false;
				return true;
			}
			pressed = false;
		}
		return false;
	}
	
	public void DoUpdate() {
		if (allow_drag && have_some){
			if (gui.mouse_pos.dist(press_coord) > 5 && pressed && press_coord.x >= 0) {
				press_coord = new Coord(-1, -1);
				gui.BeginDrag(this, new GUI_Icon(action_name), gui.mouse_pos.sub(abs_pos));
			}
		}
	}
	
	public void DoClick(int btn) {
		if (!have_some) return;
		
		if (btn == Input.MB_LEFT) {
			if (is_local) {
				((GUI_ActionPanel)parent).LocalClick(action_name, local_parent_name);
			} else {
				NetGame.SEND_action(action_name);
				((GUI_ActionPanel)parent).SetToRoot();
			}
		}
	}
}
