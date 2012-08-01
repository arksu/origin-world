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

import org.newdawn.slick.Color;

import a1.ActionsMenu;
import a1.Coord;
import a1.Hotkeys;
import a1.Hotkeys.Hotkey;
import a1.dialogs.dlg_Game;
import a1.gui.utils.DragInfo;
import a1.net.NetGame;
import a1.utils.AppSettings;

import a1.Render2D;

public class GUI_ToolbarSlot extends GUI_Control {
	/**
	 * Величина на которую нужно отвести иконку чтобы начался драг (защита от случайных драгов в нелокнутых барах)
	 */
	public static final int DRAG_SAFE_ZONE_SIZE = 5;
	/**
	 * Имя слота (для загрузки состояния)
	 */
	protected String slotName;
	/**
	 * Хоткей привязанный к данной кнопке
	 */
	protected Hotkey slotHotkey;
	/**
	 * Название действия, которое привязано к данному слоту
	 */
	protected String actionName;
	/**
	 * Дефолтный конструктор слота
	 * @param parent Родительский элемент
	 */
	public GUI_ToolbarSlot(GUI_Control parent) {
		super(parent);
		SetSize(GUI_Toolbar.SLOT_SIZE, GUI_Toolbar.SLOT_SIZE);
	}
	
	/**
	 * Конструктор слота поддерживающего сейв/лоад состояния
	 * @param parent Родительский элемент (тулбар с заданным уникальным именем)
	 * @param index индекс слота в массиве слотов
	 */
	public GUI_ToolbarSlot(GUI_Control parent, int index) {
		this(parent);
		if (parent instanceof GUI_Toolbar) {
			if (((GUI_Toolbar)parent).toolbarName != null) {
				slotName = String.format("%s_%d", ((GUI_Toolbar)parent).toolbarName, index);
				loadSlotState();
				
				if (slotHotkey == null) {
					slotHotkey = new Hotkey(org.lwjgl.input.Keyboard.getKeyIndex(String.valueOf(index + 1)), 0, new Hotkeys.HotkeyCallbackExtended(this) {
						public void onEvent() {
							if (this.storedData instanceof GUI_ToolbarSlot) {
								((GUI_ToolbarSlot)this.storedData).Activate();
							}
						}
					});
					Hotkeys.addHotkey(slotHotkey);
				}
			}
		}
	}

	
	protected void loadSlotState() {
		int key = (Integer)AppSettings.getCharacterValue("DEFAULT_PLAYER", slotName + "_key", Integer.class);
		int mode = (Integer)AppSettings.getCharacterValue("DEFAULT_PLAYER", slotName + "_mode", Integer.class);
		String act_name = (String)AppSettings.getCharacterValue("DEFAULT_PLAYER", slotName + "_action", String.class);
		if (key != 0) {
			slotHotkey = new Hotkey(key, mode, new Hotkeys.HotkeyCallbackExtended(this) {
				public void onEvent() {
					if (this.storedData instanceof GUI_ToolbarSlot) {
						((GUI_ToolbarSlot)this.storedData).Activate();
					}
				}
			});
			Hotkeys.addHotkey(slotHotkey);
		}
		if (act_name != "") {
			setSlot(act_name);
		}
	}
	
	protected void saveSlotState() {
		if (slotHotkey != null) AppSettings.putCharacterValue("DEFAULT_PLAYER", slotName + "_key", slotHotkey.virtualCode);
		if (slotHotkey != null) AppSettings.putCharacterValue("DEFAULT_PLAYER", slotName + "_mode", slotHotkey.virtualModes);
		if (actionName != null) AppSettings.putCharacterValue("DEFAULT_PLAYER", slotName + "_action", actionName);
	}

	
	/**
	 * Флаг показывает нажата ли в данный момент кнопка
	 */
	protected boolean pressed = false;
	/**
	 * Координаты последнего нажатия
	 */
	protected Coord press_coord = new Coord(-1, -1);
	@Override
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
				Activate();
				pressed = false;
				return true;
			}
			pressed = false;
		}
		return false;
	}
	
	@Override
	public void DoRender() {
		getSkin().Draw("window", abs_pos.x, abs_pos.y, size.x, size.y, StateNormal);
		if (actionName != null) {
			if (getSkin().hasElement("icon_" + actionName)) {
				getSkin().Draw("icon_" + actionName, abs_pos.x, abs_pos.y, size.x, size.y, StateNormal);
			} else getSkin().Draw("icon_unknown", abs_pos.x, abs_pos.y, size.x, size.y, StateNormal);
		}
		if (slotHotkey != null)
			Render2D.Text(
					"smallfont",
					abs_pos.x
							+ size.x
							- Render2D.getfontMetrics("smallfont", slotHotkey.toString()).x
							- GUI_Toolbar.BORDER_SIZE - 2,
					abs_pos.y
							+ size.y
							- Render2D.getfontMetrics("smallfont", slotHotkey.toString()).y
							- GUI_Toolbar.BORDER_SIZE, Render2D.getfontMetrics(
							"smallfont", slotHotkey.toString()).x, Render2D
							.getfontMetrics("smallfont", slotHotkey.toString()).y,
					Render2D.Align_Stretch, slotHotkey.toString(), Color.white);
	}
	
	@Override
	public void DoDestroy() {
		Hotkeys.removeHotkey(slotHotkey);
		saveSlotState();
	}
	
	@Override
	public void DoUpdate() {
		if (!(((GUI_Toolbar)parent).isLocked())) {
			if (gui.mouse_pos.dist(press_coord) > DRAG_SAFE_ZONE_SIZE && pressed && press_coord.x >= 0) {
				press_coord = new Coord(-1, -1);
				gui.BeginDrag(this, new GUI_Icon(actionName), gui.mouse_pos.sub(abs_pos));
			}
		}
	}
	
	/**
	 * Активировать данный слот
	 */
	public void Activate() {
		if (actionName != null) {
			if (ActionsMenu.haveAction(actionName) && !(ActionsMenu.withoutChilds(actionName))) {
				if (dlg_Game.Exist()) 
			        dlg_Game.dlg.actions_panel.LocalClick(actionName, ActionsMenu.getParent(actionName));
			} else NetGame.SEND_action(actionName);
		}
	}
	
	/**
	 * Устанавливает определенное действие в данный слот
	 * @param name Имя дейстия
	 */
	public void setSlot(String name) {
		actionName = name;
	}
	
	/**
	 * Сбрасывает данный слот (очищает)
	 */
	public void unsetSlot() {
		actionName = null;
	}
	
	@Override
	public boolean DoRequestDrop(DragInfo info) {
		return (info.drag_control instanceof GUI_Icon) && !(((GUI_Toolbar)parent).isLocked()); 
	}
	
	@Override
	public void DoEndDrag(DragInfo info) {
		if (info.drag_control instanceof GUI_Icon && !(((GUI_Toolbar)parent).isLocked())) {
			setSlot(((GUI_Icon)info.drag_control).iname);
			if (info.drag_control.drag_parent instanceof GUI_ToolbarSlot)
				((GUI_ToolbarSlot)info.drag_control.drag_parent).unsetSlot();
		}
	}
}
