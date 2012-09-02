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

import a1.dialogs.dlg_Game;
import a1.dialogs.dlg_Hotbars;
import a1.gui.GUI;
import a1.net.NetGame;

/**
 * слот для хотбара
 */
public class HotbarSlot {
    /**
     * индекс слота в хотбаре
     */
    public int idx;
    /**
     * модификаторы хоткея
     */
    public boolean modCtrl, modShift, modAlt;
    /**
     * хоткей
     */
    public int Key;

    /**
     * действие закрепленное за слотом
     */
    public String Action;

    public Hotbar parent;

    public  long time_exec = 0;

    public HotbarSlot(Hotbar parent, int idx) {
        this.parent = parent;
        // загружаем настройки хотбара
        String sec = parent.GetSection();
        this.idx = idx;
        String prefix = "slot_"+idx+"_";
        modCtrl = CharSettings.ini.getProperty(sec, prefix+"ctrl", false);
        modAlt = CharSettings.ini.getProperty(sec, prefix+"alt", false);
        modShift = CharSettings.ini.getProperty(sec, prefix+"shift", false);
        Key = CharSettings.ini.getProperty(sec, prefix+"key", 0);
        Action = CharSettings.ini.getProperty(sec, prefix+"action", "");
    }

    /**
     * повесить хоткей на слот
     * @param ctrl
     * @param alt
     * @param shift
     * @param key
     */
    public void BindHotkey(boolean ctrl, boolean alt, boolean shift, int key) {
        this.modAlt = alt;
        this.modCtrl = ctrl;
        this.modShift = shift;
        this.Key = key;

        // сохраняем в настройках
        String sec = parent.GetSection();
        String prefix = "slot_"+idx+"_";
        CharSettings.ini.putProperty(sec, prefix+"ctrl", modCtrl);
        CharSettings.ini.putProperty(sec, prefix+"alt", modAlt);
        CharSettings.ini.putProperty(sec, prefix+"shift", modShift);
        CharSettings.ini.putProperty(sec, prefix+"key", Key);
        CharSettings.save();
    }

    public void ClearHotkey() {
        Key = 0;
        modAlt = false;
        modCtrl = false;
        modShift = false;

        // сохраняем в настройках
        String sec = parent.GetSection();
        String prefix = "slot_"+idx+"_";
        CharSettings.ini.putProperty(sec, prefix+"ctrl", modCtrl);
        CharSettings.ini.putProperty(sec, prefix+"alt", modAlt);
        CharSettings.ini.putProperty(sec, prefix+"shift", modShift);
        CharSettings.ini.putProperty(sec, prefix+"key", Key);
        CharSettings.save();
    }

    /**
     * повесить действие на слот
     * @param act
     */
    public void BindAction(String act) {
        Action = act;
        String sec = parent.GetSection();
        String prefix = "slot_"+idx+"_";
        CharSettings.ini.putProperty(sec, prefix+"action", Action);
        CharSettings.save();
    }

    /**
     * обновить. проверить нажат ли хоткей
     */
    public void Update() {
        if (
                (!dlg_Hotbars.isBindMode()) &&
                (!isEmpty()) &&
                (GUI.getInstance().focused_control == null) &&
                (Key != 0) &&
                ((modAlt && Input.isAltPressed()) || !modAlt) &&
                ((modCtrl && Input.isCtrlPressed()) || !modCtrl) &&
                ((modShift && Input.isShiftPressed()) || !modShift) &&
                (Input.KeyHit(Key)) )
        {
            Execute();
        }
    }

    /**
     * запустить хотбар
     */
    public void Execute() {
        if (isEmpty()) return;
        time_exec = System.currentTimeMillis();

            if (ActionsMenu.haveAction(Action) && !(ActionsMenu.withoutChilds(Action))) {
                if (dlg_Game.Exist())
                    dlg_Game.dlg.actions_panel.LocalClick(Action, ActionsMenu.getParent(Action));
            } else
                NetGame.SEND_action(Action);
    }

    /**
     * пуст ли слот
     */
    public boolean isEmpty() {
        return Action.isEmpty();
    }
}
