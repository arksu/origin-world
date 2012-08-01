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

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import a1.Coord;
import a1.Input;
import a1.Lang;
import a1.gui.utils.DragInfo;
import a1.net.NetGame;
import a1.utils.AppSettings;

public class GUI_ActionBarSlot extends GUI_Control {
	public boolean dnd_have_some = false;
	public boolean dnd_drag_above_me = false;
	protected boolean pressed = false;
	
	public int slot_hotkey = 0;
	public String slot_bind_name;
	public String slot_action_name;

	private Coord press_coord = Coord.z;
	
	private GUI_ActionBarSlotOptions options;
	
	public GUI_ActionBarSlot(GUI_Control parent) {
		super(parent);
		drag_enabled = true;
		SetSize(32,32);
	}
	
	public GUI_ActionBarSlot(GUI_Control parent, String bind_name) {
		this(parent);
		slot_bind_name = bind_name;
		
		options = new GUI_ActionBarSlotOptions(this, true, true);
		options.SetPos(0, -options.size.y);
		options.Hide();
	}
	
	public void SetHotkey(int key) {
		slot_hotkey = key;
		options.Hide();
	}
	
	public void DeleteFromBar() {
		UnsetSlot();
		options.Hide();
	}
	
	public void SetSlot(String act_name) {
		slot_action_name = act_name;
		simple_hint = Lang.getTranslate("hint", act_name);
		dnd_have_some = true;
		SaveState();
	}
	
	public void UnsetSlot() {
		dnd_have_some = false;
		slot_action_name = null;
		simple_hint = "";
		if (((GUI_ActionBar)parent).allow_slot_save) AppSettings.putCharacterValue("DEFAULT_PLAYER", slot_bind_name, GUI.class);
	}
	
	public void DoDestroy() {
		if (((GUI_ActionBar)parent).allow_slot_save) AppSettings.putCharacterValue("DEFAULT_PLAYER", slot_bind_name, GUI.class);
	}
	
	public void LoadState() {
		String result = (String)AppSettings.getCharacterValue("DEFAULT_PLAYER", slot_bind_name, String.class);
		if (!result.equals("")) SetSlot(result);
		if (((GUI_ActionBar)parent).allow_slot_hotkeys) {
			slot_hotkey = (Integer)AppSettings.getCharacterValue("DEFAULT_PLAYER", slot_bind_name + "_key", Integer.class);
		}
	}
	
	public void SaveState() {
		if (dnd_have_some) AppSettings.putCharacterValue("DEFAULT_PLAYER", slot_bind_name, slot_action_name);
		if (((GUI_ActionBar)parent).allow_slot_hotkeys) AppSettings.putCharacterValue("DEFAULT_PLAYER", slot_bind_name + "_key", slot_hotkey);
	}
	
	public void DoRender() {
		getSkin().Draw("icon_bg", abs_pos.x, abs_pos.y);
		if (slot_action_name != null && dnd_have_some) {
			if (getSkin().hasElement("icon_" + slot_action_name)) {
				getSkin().Draw("icon_" + slot_action_name, abs_pos.x, abs_pos.y, size.x, size.y, StateNormal);
			} else getSkin().Draw("icon_unknown", abs_pos.x, abs_pos.y, size.x, size.y, StateNormal);
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
		if (((GUI_ActionBar)parent).allow_slot_drag && dnd_have_some){
			if (gui.mouse_pos.dist(press_coord) > 5 && pressed && press_coord.x >= 0) {
				press_coord = new Coord(-1, -1);
				gui.BeginDrag(this, new GUI_Icon(slot_action_name), gui.mouse_pos.sub(abs_pos));
			}
		}
		if (((GUI_ActionBar)parent).allow_slot_hotkeys && dnd_have_some) {
			if (Keyboard.isKeyDown(slot_hotkey)) {
				NetGame.SEND_action(slot_action_name);
			}
		}
	}
	
	public boolean DoRequestDrop(DragInfo info) {
		return (info.drag_control instanceof GUI_Icon) && ((GUI_ActionBar)parent).allow_slot_drop; 
	}
	
	public void DoEndDrag(DragInfo info) {
		if (info.drag_control instanceof GUI_Icon && ((GUI_ActionBar)parent).allow_slot_drop) {
			SetSlot(((GUI_Icon)info.drag_control).iname);
			if (info.drag_control.drag_parent instanceof GUI_ActionBarSlot)
				((GUI_ActionBarSlot)info.drag_control.drag_parent).UnsetSlot();
		}
	}
	
	public void DoClick(int btn) {
		if (!dnd_have_some) return;
		if (btn == Input.MB_LEFT) NetGame.SEND_action(slot_action_name);
		//else if (((GUI_ActionBar)parent).allow_slot_hotkeys) options.ToggleVisible();
	}
	
	public class GUI_ActionBarSlotOptions extends GUI_Control {
		public GUI_ActionBarSlotOptions(GUI_Control parent, boolean bind, boolean delete) {
			super(parent);	
			btns = new ArrayList<GUI_IconButton>();
			
			if (bind) {
				GUI_IconButton btn = new GUI_IconButton(this) {
					private boolean listenForKey = false;
					
					public void DoClick() {
						listenForKey = true;
					}
					
					public void DoUpdate() {
						if (!listenForKey) return;
						int key = 0;
						for (int i = 0; i < 255; i++)
						    if (Keyboard.isKeyDown(i)) key = i;
						if (key != 0) {
							listenForKey = false;
							((GUI_ActionBarSlot)parent.parent).SetHotkey(key);
						}
					}
					
					
				};
				btn.SetSize(GUI_ActionBar.slotsize, GUI_ActionBar.slotsize);
				btn.SetIcon("icon_axe_test");
				btns.add(btn);
			}
	
			for (int i = 0; i < btns.size(); i++) {
				btns.get(i).SetPos(GUI_ActionBar.borderwidth, GUI_ActionBar.borderwidth + (GUI_ActionBar.slotsize + 1) * i);
			}
			
			SetSize(GUI_ActionBar.borderwidth * 2 + GUI_ActionBar.slotsize, GUI_ActionBar.borderwidth * 2 + (GUI_ActionBar.slotsize + 1) * btns.size() - 1);
		}
		
		List<GUI_IconButton> btns;

		public void DoRender() {
			getSkin().Draw("window", abs_pos.x, abs_pos.y, size.x, size.y, StateNormal);
		}
	}
}
