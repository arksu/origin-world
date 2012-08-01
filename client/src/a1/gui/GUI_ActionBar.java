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

import a1.Config;
import a1.Coord;
import a1.Input;
import a1.utils.AppSettings;

public class GUI_ActionBar extends GUI_Control {
	// Родитель и имя данной бара
	public GUI_ActionBar(GUI_Control parent, String barname) {
		super(parent);
		_actionbar_slots = new ArrayList<GUI_ActionBarSlot>();
		_actionbar_options = new GUI_ActionBar_Options(this);
		_actionbar_options.Hide();
		_actionbar_name = barname;
		ActionBar_Resize();
	}

	public static final int slotsize = 32;
	public static final int borderwidth = 3;
	
	protected int _actionbar_length = 1;
	protected String _actionbar_name = "default_bar";
	protected List<GUI_ActionBarSlot> _actionbar_slots;
	protected boolean _actionbar_direction = false;
	protected GUI_ActionBar_Options _actionbar_options;
	boolean abar_resized = false; int abar_start = 0;
	boolean abar_dragged = false;
	boolean pressed = false;
	
	public boolean allow_self_move = true;
	public boolean allow_self_rotate = true;
	public boolean allow_self_close = true;
	public boolean allow_self_lock = true;
	public boolean allow_self_resize = true;
	public boolean allow_self_save = true;
	
	public boolean allow_slot_drop = true;
	public boolean allow_slot_drag = true;
	public boolean allow_slot_save = true;
	public boolean allow_slot_hotkeys = true;

	// Load position and slots
	public void LoadState() {
		if (allow_self_save) {
			int bx = (Integer)AppSettings.getCharacterValue("DEFAULT_PLAYER", _actionbar_name + "_position_x", Integer.class);
			int by = (Integer)AppSettings.getCharacterValue("DEFAULT_PLAYER", _actionbar_name + "_position_y", Integer.class);
			int len = (Integer)AppSettings.getCharacterValue("DEFAULT_PLAYER", _actionbar_name + "_length", Integer.class);
			_actionbar_direction = (Boolean)AppSettings.getCharacterValue("DEFAULT_PLAYER", _actionbar_name + "_direction", Boolean.class);
			if (bx != 0 && by != 0)	 SetPos(bx, by);
			if (len != 0) _actionbar_length = len;
			ActionBar_RebuildSlots();
		}
		if (allow_slot_save) {
			for (GUI_ActionBarSlot slot : _actionbar_slots)
				slot.LoadState();
		}
		ActionBar_Resize();
	}
	
	// Save position and slots
	public void SaveState() {
		if (allow_self_save) {
			AppSettings.putCharacterValue("DEFAULT_PLAYER", _actionbar_name + "_position_x", pos.x);
			AppSettings.putCharacterValue("DEFAULT_PLAYER", _actionbar_name + "_position_y", pos.y);
			AppSettings.putCharacterValue("DEFAULT_PLAYER", _actionbar_name + "_length", _actionbar_length);
			AppSettings.putCharacterValue("DEFAULT_PLAYER", _actionbar_name + "_direction", _actionbar_direction);
		}
		if (allow_slot_save) {
			for (GUI_ActionBarSlot slot : _actionbar_slots)
				slot.SaveState();
		}
		if (allow_self_save || allow_slot_save) Config.save_options();
	}
	
	// Setup options buttons
	public void SetOptions() {
		
	}
	
	// Rotation
	public void Rotate() {
		if (!allow_self_rotate) return;
		_actionbar_direction = !_actionbar_direction;
		ActionBar_RebuildSlots();
		ActionBar_Resize();
	}
	
	// Lock slots
	public void Lock() {
		if (!allow_self_lock) return;
		allow_slot_drag = !allow_slot_drag;
		allow_slot_drop = !allow_slot_drop;
		allow_self_move = !allow_self_move;
		allow_self_rotate = !allow_self_rotate;
		allow_self_close = !allow_self_close;
		SetOptions();
	}
	
	// Close bar
	public void Close() {
		if (allow_self_close) Unlink();
	}
	
	// Set
	// Устанавливает количество слотов
	public void SetSlots(int cols) {
		_actionbar_length = cols;
		ActionBar_RebuildSlots();
		ActionBar_Resize();
	}
	
	// Rebuild
	// true - горизонтальное, false - вертикальное
	private void ActionBar_RebuildSlots() {
		if (_actionbar_slots.size() == 0) {
			for (int i = 0; i < _actionbar_length; i++)
					_actionbar_slots.add(new GUI_ActionBarSlot(this, String.format("%s_%d", _actionbar_name, i)));
		}
		int dl = _actionbar_length - _actionbar_slots.size();
		if (dl != 0) {
			if (dl < 0) {
				for (int i = _actionbar_length; i < _actionbar_slots.size(); i++) {
					_actionbar_slots.get(i).Unlink();
					_actionbar_slots.remove(i);
				}
			} else {
				for (int i = _actionbar_slots.size(); i < _actionbar_length; i++)
					_actionbar_slots.add(new GUI_ActionBarSlot(this, String.format("%s_%d", _actionbar_name, i)));
			}
		}
		int dx = 10; // Drag zone
		for (int i = 0; i < _actionbar_length; i++)
				_actionbar_slots.get(i).SetPos(_actionbar_direction ? dx + i * (1 + slotsize) + borderwidth : borderwidth, _actionbar_direction ? borderwidth : dx + i * (1 + slotsize) + borderwidth);
	}
	
	// Resize
	// Обновление размера окна
	private void ActionBar_Resize() {
		int dx = 10 + 10; // Drag zone + Buttons zone
		int dy = borderwidth * 2 + slotsize+2;
		SetSize(_actionbar_direction ? dx + _actionbar_length * (slotsize + 1) + borderwidth * 2 - 1 : dy, 
				_actionbar_direction ? dy : dx + _actionbar_length * (slotsize + 1) + borderwidth * 2 - 1);
	}
	
	// Save state on destroy
	public void DoDestroy() {
		SaveState();
	}
	
	protected boolean mouseInDragZone() {
		return gui.mouse_pos.in_rect(abs_pos, new Coord(_actionbar_direction ? 10 : size.x, _actionbar_direction ? size.y : 10));
	}
	
	protected boolean mouseInResizeZone() {
		Coord sz = new Coord(_actionbar_direction ? 10 : size.x, _actionbar_direction ? size.y : 10);
		return gui.mouse_pos.in_rect(abs_pos.add(size).sub(sz), sz);
	}
	
	public boolean DoMouseBtn(int btn, boolean down) {
		if (!enabled) return false;

		if (down) {
			if (MouseInMe()){
				pressed = true;
				if (btn == Input.MB_LEFT) {
					if (mouseInDragZone() && allow_self_move) { 
						abar_dragged = true; 
						BeginDragMove(); 
					}
					else if (mouseInResizeZone() && allow_self_resize) {
						abar_resized = true; 
						abar_start = (_actionbar_direction ? abs_pos.x + size.x : abs_pos.y + size.y); 
					}
				}
				return true;
			}
		} else {
			if (pressed) {
				if (btn == Input.MB_LEFT) {
					if (abar_dragged) {
						abar_dragged = false;
						EndDragMove();
						SaveState();
					}
					if (abar_resized) {
						abar_resized = false;
						ActionBar_Resize();
					}
				}
				pressed = false;
				return true;
			}
			pressed = false;
		}
		return false;
	}
	
	// Update resize
	public void DoUpdate() {
		if (abar_resized && allow_self_resize) {
			int dx = (_actionbar_direction ? gui.mouse_pos.x - abar_start : gui.mouse_pos.y - abar_start);
			abar_start = _actionbar_direction ? gui.mouse_pos.x : gui.mouse_pos.y;
			int cs = (int)(((_actionbar_direction ? size.x : size.y) - borderwidth * 2 - 20) / (slotsize + 1));
			if (cs != _actionbar_length) {
				_actionbar_length = cs;
				ActionBar_RebuildSlots();
			}
			SetSize(size.add(_actionbar_direction ? dx : 0, _actionbar_direction ? 0 : dx));
		}
	}
		
	// Just render window
	public void DoRender() {
		getSkin().Draw("window", abs_pos.x, abs_pos.y, size.x, size.y, StateNormal);
	}
	
	// смена размера
	public void DoSetSize() {
		SetOptions();
	}
	
	public void DoSetPos() {
		_actionbar_options.SetPos(0, 0);
	}
	
	public class GUI_ActionBar_Options extends GUI_Control {
		public GUI_ActionBar_Options(GUI_Control parent) {
			super(parent);
			_opt_btns = new ArrayList<GUI_IconButton>();
			GUI_IconButton btn;
			
			btn = new GUI_IconButton(this) {
				public void DoClick() {
					((GUI_ActionBar)parent.parent).Rotate();
				}
			};
			btn.SetIcon("icon_gui_close");
			_opt_btns.add(btn);
			
			btn = new GUI_IconButton(this) {
				public void DoClick() {
					((GUI_ActionBar)parent.parent).Close();
				}
			};
			btn.SetIcon("icon_gui_rotate");
			_opt_btns.add(btn);
			
			btn = new GUI_IconButton(this) {
				public boolean DoMouseBtn(int btn, boolean down) {
					if (!enabled) return false;
					if (down) {
						if (MouseInMe()) {
							pressed = true;
							if (btn == Input.MB_LEFT) {
								parent.parent.BeginDragMove();
							}
							return true;
						}
					} else {
						if (pressed) {
							if (btn == Input.MB_LEFT) {
								parent.parent.EndDragMove();
							}
							pressed = false;
							return true;
						}
						pressed = false;
					}
					return false;
				}
			};
			btn.SetIcon("icon_gui_move");
			_opt_btns.add(btn);
			
			UpdateIt();
		}
		
		List<GUI_IconButton> _opt_btns;
		
		public void UpdateIt() {
			for (int i = 0; i < _opt_btns.size(); i++) {
				_opt_btns.get(i).SetPos(i * (1 + slotsize) + borderwidth, borderwidth);
			}
			SetSize(_opt_btns.size() * (1 + slotsize) + borderwidth * 2 - 1, borderwidth * 2 + slotsize);
		}
	}
}
