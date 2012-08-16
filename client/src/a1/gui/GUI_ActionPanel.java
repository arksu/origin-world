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
import a1.ActionsMenu.ActionsMenuItem;
import a1.Config;
import a1.Input;

import java.util.List;

import static a1.gui.Skin.StateNormal;

public class GUI_ActionPanel extends GUI_Control{
	public GUI_ActionPanel(GUI_Control parent) {
		super(parent);
	}
	
	public GUI_ActionPanel(GUI_Control parent, String barname, int szx, int szy) {
		this(parent);
		_apanel_x = szx;
		_apanel_y = szy;
		_apanel_name = barname;
		ActionBar_RebuildSlots();
		_apanel_dragger = new GUI_Control(this) {
			public boolean DoMouseBtn(int btn, boolean down) {
				if (btn == Input.MB_LEFT) {
					if (down) {
						if (MouseInMe()) {
							parent.BeginDragMove();
							return true;
						}				
					} else {
						parent.EndDragMove();
                        SaveState();
                    }
				}
				return false;
			}
			
			public void DoRender() {
				getSkin().Draw("window", abs_pos.x, abs_pos.y, size.x, size.y, StateNormal);
			}
		};
		_apanel_dragger.SetSize(12, 32);
		_apanel_dragger.SetPos(-_apanel_dragger.size.x, 0);
		readonly_mode_rootLevel = barname;
	}
	
	public int slotsize = 32;
	public int borderwidth = 2;
	
	protected int _apanel_x = 1;
	protected int _apanel_y = 1;
	protected GUI_ActionPanelSlot[][] _apanel_slots;
	protected GUI_Control _apanel_dragger;
	protected int readonly_mode_current_index = 0;
	protected int readonly_mode_current_eindex = 0;
	protected String readonly_mode_previousLevel;
	protected String readonly_mode_rootLevel;
	protected String _apanel_name = "root";
	
	public void LoadFromActionsMenu() {
		readonly_mode_previousLevel = _apanel_name;
		readonly_mode_rootLevel = _apanel_name;
		SetToRoot();
	}
	
	private void ReloadFromActionsMenu() {
		Clear();
		
		List<ActionsMenu.ActionsMenuItem> currentLevelItems = ActionsMenu.getList(_apanel_name);
		if (readonly_mode_current_index > currentLevelItems.size()) readonly_mode_current_index = 0;
		readonly_mode_current_eindex = (currentLevelItems.size() - readonly_mode_current_index) < _apanel_x * _apanel_y - 3 ? currentLevelItems.size() : _apanel_x * _apanel_y - 3;
		
		for (ActionsMenuItem i : currentLevelItems.subList(readonly_mode_current_index, readonly_mode_current_eindex)) {
			if (i.name.contains("root_")){
				ActionBar_GetFreeSlot(true).SetSlot(i.name).SetLocal(i.parent);
			} else ActionBar_GetFreeSlot(true).SetSlot(i.name);
		}
		
		if (!_apanel_name.equals(readonly_mode_rootLevel)) ActionBar_GetFreeSlot(false).SetSlot("abar_nav_top").SetLocal(readonly_mode_previousLevel).DisableDrag();
		if (readonly_mode_current_eindex != currentLevelItems.size()) ActionBar_GetFreeSlot(false).SetSlot("abar_nav_right").SetLocal(readonly_mode_rootLevel).DisableDrag();
		if (readonly_mode_current_index > 0) ActionBar_GetFreeSlot(false).SetSlot("abar_nav_left").SetLocal(readonly_mode_rootLevel).DisableDrag();
	}
	
	public void SetToRoot() {
		_apanel_name = readonly_mode_rootLevel;
		readonly_mode_current_index = 0;
		ReloadFromActionsMenu();
	}
	
	public void LocalClick(String name, String parent) {
		if (name.equals("abar_nav_left")) {
			readonly_mode_current_index -= readonly_mode_current_eindex;
			if (readonly_mode_current_index < 0) readonly_mode_current_index = 0;
			ReloadFromActionsMenu();
		} else if (name.equals("abar_nav_right")) {
			readonly_mode_current_index += readonly_mode_current_eindex;
			ReloadFromActionsMenu();
		} else if (name.equals("abar_nav_top")) {
			_apanel_name = parent;
			readonly_mode_previousLevel = ActionsMenu.getParent(parent);
			readonly_mode_current_index = 0;
			ReloadFromActionsMenu();
		} else {
			readonly_mode_previousLevel = _apanel_name;
			_apanel_name = name;
			readonly_mode_current_index = 0;
			ReloadFromActionsMenu();
		}
	}
	
	private void Clear() {
		for (int i = 0; i < _apanel_x; i++)
			for (int j = 0; j < _apanel_y; j++) {
					_apanel_slots[i][j].UnsetSlot();
				}
	}
	
	// true - слева направо, false - наоборот
	private GUI_ActionPanelSlot ActionBar_GetFreeSlot(boolean direction) {
		for (int j = 0; j < _apanel_y; j++) {
			for (int i = 0; i < _apanel_x; i++) {
				int bx = (direction ? i : _apanel_x - i -1);
				int by = (direction ? j : _apanel_y - j - 1);
				if (!_apanel_slots[bx][by].have_some) {
					return _apanel_slots[bx][by];
				}
			}
		}
		return null;
	}
	
	private void ActionBar_RebuildSlots() {
		if (_apanel_slots == null) {
			_apanel_slots = new GUI_ActionPanelSlot[_apanel_x][_apanel_y];
			for (int i = 0; i < _apanel_x; i++)
				for (int j = 0; j < _apanel_y; j++) {
					_apanel_slots[i][j] = new GUI_ActionPanelSlot(this);
				}
		}
		for (int i = 0; i < _apanel_x; i++)
			for (int j = 0; j < _apanel_y; j++) {
				_apanel_slots[i][j].SetPos(1 + i * (1 + slotsize) + borderwidth, j * (1 + slotsize) + borderwidth);
			}
		ActionBar_Resize();
	}
	
	// Обновление размера окна
	private void ActionBar_Resize() {
		SetSize(_apanel_x * (slotsize + 1) + borderwidth * 2, _apanel_y * (slotsize + 1) + borderwidth * 2);
	}
	
	// Load position and slots
	public void LoadState() {
//		int bx = (Integer)AppSettings.getCharacterValue("DEFAULT_PLAYER", readonly_mode_rootLevel + "_position_x", Integer.class);
//		int by = (Integer)AppSettings.getCharacterValue("DEFAULT_PLAYER", readonly_mode_rootLevel + "_position_y", Integer.class);
//		if (bx != 0 && by != 0)	SetPos(bx, by);
	}
	
	// Save position and slots
	public void SaveState() {
//		AppSettings.putCharacterValue("DEFAULT_PLAYER", readonly_mode_rootLevel + "_position_x", pos.x);
//		AppSettings.putCharacterValue("DEFAULT_PLAYER", readonly_mode_rootLevel + "_position_y", pos.y);
        Config.save_options();
	}
	
	// Just render window
	public void DoRender() {
		getSkin().Draw("window", abs_pos.x, abs_pos.y, size.x, size.y, StateNormal);
	}
	
	public void DoDestroy() {
		SaveState();
	}
}
