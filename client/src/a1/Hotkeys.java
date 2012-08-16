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
package a1;


import a1.dialogs.Dialog;
import a1.gui.GUI;
import a1.gui.GUI_Debug;
import a1.net.NetGame;
import a1.utils.TilesDebug;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class Hotkeys {
	
	public static class Hotkey {
		public int virtualModes;
		public int virtualCode;
		protected String hotkeyName;
		protected HotkeyCallback hotkeyCallback;
		
		/**
		 * Конструктор хоткея
		 * @param code Виртуальный код
		 * @param ctrl Нажат ли ctrl
		 * @param alt Нажат ли alt
		 * @param shft Нажат ли shift
		 * @param cback Обратный вызов
		 */
		public Hotkey(int code, boolean ctrl, boolean alt, boolean shft, HotkeyCallback cback) {
			this(code, (ctrl ? 1 : 0) + (shft ? 2 : 0) + (alt ? 4 : 0), cback);
		}
		
		/**
		 * Конструктор хоткея
		 * @param code Виртуальный код
		 * @param mode Модификаторы
		 * @param cback Обратный вызов
		 */
		public Hotkey(int code, int mode, HotkeyCallback cback) {
			virtualModes = mode;
			virtualCode = code;
			hotkeyCallback = cback;
		}
		
		/**
		 * Проверка прошло ли нажатие по данному хоткею
		 * @return true если хоткей сработал
		 */
		public boolean checkPressed() {
			if (Input.GetKeyState() == virtualModes && Input.KeyHit(virtualCode)) {
				hotkeyCallback.onEvent();
				return true;
			} else
				return false;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Hotkey) {
				Hotkey htk = (Hotkey) obj;
				if (htk.virtualModes == virtualModes && htk.virtualCode == virtualCode)
					return true;
			}
			return false;
		}
		
		@Override
		public String toString() {
			if (hotkeyName != null) return hotkeyName;
			hotkeyName = "";
			if (virtualCode == org.lwjgl.input.Keyboard.KEY_NONE) return hotkeyName;
			if ((virtualModes & 0x4) > 0) hotkeyName += "ALT+";
			if ((virtualModes & 0x1) > 0) hotkeyName += "CTRL+";
			if ((virtualModes & 0x2) > 0) hotkeyName += "SHFT+";
			hotkeyName += org.lwjgl.input.Keyboard.getKeyName(virtualCode);
			return hotkeyName;
		}
	}
	
	public interface HotkeyCallback {
		public void onEvent();
	}
	
	public static abstract class HotkeyCallbackExtended implements HotkeyCallback {
		protected Object storedData;
		
		public HotkeyCallbackExtended(Object data) {
			storedData = data;
		}
	}
	
	protected static ArrayList<Hotkey> hotkeysList = new ArrayList<Hotkeys.Hotkey>();
	
	/**
	 * Добавить хоткей в обработку
	 * @param hot Хоткей
	 * @return true если хоткей нне занят и добавлен
	 */
	public static boolean addHotkey(Hotkey hot) {
		for (Hotkey htk : hotkeysList) {
			if (htk.equals(hot)) return false;
		}
		hotkeysList.add(hot);
		return true;
	}
	
	/**
	 * Удалить хоткей из списка
	 * @param hot Хоткей
	 */
	public static void removeHotkey(Hotkey hot) {
		if (hotkeysList.contains(hot)) hotkeysList.remove(hot);
	}
	
	/**
	 * Подписать сочетание клавиш как хоткей
	 * @param key клавиша
	 * @param modes модификаторы
	 * @param callback колбэк
	 * @return true если хоткей не занят и подписан
	 */
	public static boolean SignKey(int key, int modes, HotkeyCallback callback) {
		Hotkey buf = new Hotkey(key, modes, callback);
		return addHotkey(buf);
	}
	
	
	
	
	static public void ProcessKey() {
		for (Hotkey htk : hotkeysList) {
			htk.checkPressed();
		}
		
		// тильда показывает дебаг инфу
		if (Input.KeyHit(Keyboard.KEY_GRAVE) || (Input.KeyHit(Keyboard.KEY_D) && Input.isCtrlPressed())) {
			Config.debug = !Config.debug;
		}
		
		// ScreenShot Bind
		if (Input.KeyHit(Keyboard.KEY_F12)) {
			a1.utils.Utils.MakeScreenshot();
		}
		
		if (Input.KeyHit(Keyboard.KEY_F11)) {
			GUI_Debug.active = !GUI_Debug.active;
		}
		
		if (Input.KeyHit(Keyboard.KEY_F10)) {
			GUI.getInstance().setActive(!GUI.getInstance().getActive());
		}

        if (Config.debug && Input.KeyHit(Keyboard.KEY_F9)) {
            GUI.game_gui_render = !GUI.game_gui_render;
        }

        if (Input.KeyHit(Keyboard.KEY_F6) && Config.dev_tile_mode) {
            TilesDebug.ParseTilesXML();
        }

		if (Input.KeyHit(Keyboard.KEY_F4)) {
			Config.count_objs = !Config.count_objs;
			Config.save_options();
		}


		
		// minimap
//		if (Input.KeyHit(Keyboard.KEY_M)) { 
//			Log.info("show minimap!");
//			if (Dialog.IsActive("dlg_minimap"))
//				Dialog.Hide("dlg_minimap");
//			else
//				Dialog.Show("dlg_minimap");
//		}
		
		// inventory
		if (Input.KeyHit(Keyboard.KEY_TAB)) { 
			OpenInventory();
		}
		
		if (Input.KeyHit(Keyboard.KEY_E) && Input.isCtrlPressed()) { 
			NetGame.SEND_action("open_equip");
		}
		
		if (Input.KeyHit(Keyboard.KEY_O) && Input.isCtrlPressed()) { 
			if (Dialog.IsActive("dlg_options")) 
				Dialog.Hide("dlg_options");
			else
				Dialog.Show("dlg_options");
		}
		
		if (Input.KeyHit(Keyboard.KEY_ESCAPE)) {
			if (Player.TargetID > 0) {
				NetGame.SEND_target_reset();
			}
		}
	}
	
	static public void OpenInventory() {
		NetGame.SEND_action("open_inventory");
	}
	
	static public boolean SignKey(int key, int modes) {
		return true;
	}

    /**
     * функция валидирует кнопку которая может быть забиндена
     * @param key код клавиши
     * @return можно ли навешать клавишу
     */
    static public boolean isKeyValid(int key) {
        return  (key >= Keyboard.KEY_1 && key <= Keyboard.KEY_EQUALS) ||
                (key >= Keyboard.KEY_Q && key <= Keyboard.KEY_RBRACKET) ||
                (key >= Keyboard.KEY_A && key <= Keyboard.KEY_GRAVE) ||
                (key >= Keyboard.KEY_Z && key <= Keyboard.KEY_SLASH) ||
                (key >= Keyboard.KEY_F1 && key <= Keyboard.KEY_F10) ||
                (key >= Keyboard.KEY_NUMPAD7 && key <= Keyboard.KEY_F12) ||
                (key >= Keyboard.KEY_PAUSE && key <= Keyboard.KEY_DELETE) ||
                (key == Keyboard.KEY_MULTIPLY) || (key == Keyboard.KEY_DIVIDE);
    }
}
