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

import a1.*;
import a1.dialogs.dlg_Hotbars;
import a1.gui.utils.DragInfo;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;

import static org.lwjgl.input.Keyboard.KEY_ESCAPE;

/**
 * контрол слота для хотбара
 */
public class GUI_HotbarSlot  extends GUI_Control{
    HotbarSlot slot;
    /**
     * время проигрывания анимации срабатывания слота
     */
    private static int ANIM_LEN = 200;
    /**
     * длина одного кадра
     */
    private static int ANIM_FRAME = ANIM_LEN / 5;
    private static int DRAG_DIST = 3;

    Coord pressed_coord = Coord.z;
    boolean pressed = false;

    public GUI_HotbarSlot(GUI_Control parent, HotbarSlot slot) {
        super(parent);
        this.slot = slot;
        drag_enabled = true;
    }

    public void DoRender() {
        if (slot.parent.hide_empty_slots && slot.isEmpty()) return;

        int a = MouseInMe() ? 255 : slot.parent.alpha;

        // выведем подложку слота
        if (a > 0)
        getSkin().Draw("hotbar_slot", abs_pos.x-1, abs_pos.y-1, 34, 34, Skin.StateDefault, new Color(255,255,255, a) );

        // рисуем иконку действия
        if (!slot.isEmpty()) {
            if (getSkin().hasElement("icon_" + slot.Action)) {
                getSkin().Draw("icon_" + slot.Action, abs_pos.x, abs_pos.y, size.x, size.y,
                        Skin.StateNormal, new Color(255,255,255, a));
            } else getSkin().Draw("icon_unknown", abs_pos.x, abs_pos.y, size.x, size.y,
                        Skin.StateNormal, new Color(255,255,255, a));
        }

        // если надо выведем имя кнопки на которую забинден слот
        if (!slot.parent.hide_hotkey && slot.Key != 0)
            Render2D.Text(
                    "smallfont",
                    abs_pos.x
                            + size.x
                            - Render2D.getfontMetrics("smallfont", getHotKeyName()).x
                            - 1 - 2,
                    abs_pos.y
                            + size.y
                            - Render2D.getfontMetrics("smallfont", getHotKeyName()).y
                            - 1, Render2D.getfontMetrics(
                    "smallfont", getHotKeyName()).x, Render2D
                    .getfontMetrics("smallfont", getHotKeyName()).y,
                    Render2D.Align_Stretch, getHotKeyName(), new Color(255,255,255, a));

        // отрисуем анимацию запуска
        long dt = System.currentTimeMillis() - slot.time_exec;
        if (dt < ANIM_LEN) {
            int fr = ((int)dt / ANIM_FRAME) + 1;
            getSkin().Draw("hotbar_slot_anim_"+fr, abs_pos.x+(size.x-64)/2, abs_pos.y+(size.y-64)/2, 64, 64,
                    Skin.StateDefault, Color.white );
        }

    }

    public void DoUpdate() {
        if (!slot.isEmpty() && MouseInMe()) simple_hint = Lang.getTranslate("hint", slot.Action);

        // когда бинд режим. и мышка во мне
        if (dlg_Hotbars.isBindMode() && MouseInMe()) {
            if (Input.KeyHit(KEY_ESCAPE)) {
                slot.ClearHotkey();
                return;
            }
            // проверим нажатые клавиши
            for (int i = 0; i < 256; i++) {
                if (Input.KeyHit(i) && Hotkeys.isKeyValid(i)) {
                    slot.BindHotkey(
                            Input.isCtrlPressed(),
                            Input.isAltPressed(),
                            Input.isShiftPressed(),
                            i
                    );
                    break;
                }
            }
        }

        if (!slot.parent.locked && !slot.isEmpty() && gui.mouse_pos.dist(pressed_coord) > DRAG_DIST && pressed && pressed_coord.x >= 0) {
            pressed_coord.x = -1;
            gui.BeginDrag(this, new GUI_Icon(slot.Action), gui.mouse_pos.sub(abs_pos));
        }
    }

    public boolean DoMouseBtn(int btn, boolean down) {
        if (!enabled) return false;

        if (((GUI_Hotbar)parent).isMoveable()) {
            //((GUI_Hotbar)parent).DoMouseBtn(btn, down);
            return false;
        }

        if (dlg_Hotbars.isBindMode()) {
            return false;
        }

        if (slot.parent.click_through && MouseInMe())
            return false;

        if (down) {
            if (MouseInMe()){
                // если до этого не было нажато
                if (!pressed) pressed_coord = new Coord(gui.mouse_pos);
                pressed = true;
                return true;
            }
        } else {
            if (pressed && MouseInMe()) {
                Execute();
                pressed = false;
                return true;
            }
            pressed = false;
        }
        return false;
    }

    // условие по котороу определяется мышь в контроле
    public boolean CheckMouseInControl() {
        return visible && !slot.parent.click_through;
    }

    public boolean DoRequestDrop(DragInfo info) {
        return (info.drag_control instanceof GUI_Icon) && !slot.parent.locked;
    }

    public void DoEndDrag(DragInfo info) {
        // если кинули на самого себя - ничего не делаем
        if (info.drag_control.drag_parent == this) return;

        if (info.drag_control instanceof GUI_Icon && !slot.parent.locked) {
            slot.BindAction(((GUI_Icon)info.drag_control).iname);

            if (info.drag_control.drag_parent instanceof GUI_HotbarSlot)
                ((GUI_HotbarSlot)info.drag_control.drag_parent).ClearSlot();
        }
    }

    public void ClearSlot() {
        slot.BindAction("");
    }

    protected void Execute() {
        slot.Execute();
    }

    /**
     * тащат ли над нами слот?
     */
    protected boolean isDragSlotOver() {
        return gui.drag_info != null && gui.drag_info.state == DragInfo.DRAG_STATE_ACCEPT && MouseInMe();
    }

    public void DoRenderAfterChilds() {
        // если в режиме бинда хоткеев и мышь под нами - выделим цветом
        if (isDragSlotOver() || (dlg_Hotbars.isBindMode() && MouseInMe())) {
            Color col =  new Color(0, 210, 0, 100);

            Render2D.ChangeColor(col);
            Render2D.Disable2D();
            Render2D.FillRect(abs_pos, size);
            Render2D.Enable2D();
        }
    }

    protected String getHotKeyName() {
        switch (slot.Key) {
            case Keyboard.KEY_COMMA : return ",";
            case Keyboard.KEY_NUMPAD0 : return "K0";
            case Keyboard.KEY_NUMPAD1 : return "K1";
            case Keyboard.KEY_NUMPAD2 : return "K2";
            case Keyboard.KEY_NUMPAD3 : return "K3";
            case Keyboard.KEY_NUMPAD4 : return "K4";
            case Keyboard.KEY_NUMPAD5 : return "K5";
            case Keyboard.KEY_NUMPAD6 : return "K6";
            case Keyboard.KEY_NUMPAD7 : return "K7";
            case Keyboard.KEY_NUMPAD8 : return "K8";
            case Keyboard.KEY_NUMPAD9 : return "K9";

            case Keyboard.KEY_MINUS : return "-";
            case Keyboard.KEY_EQUALS : return "=";
            case Keyboard.KEY_SEMICOLON : return ";";
            case Keyboard.KEY_APOSTROPHE : return "'";
            case Keyboard.KEY_GRAVE : return "`";
            case Keyboard.KEY_PERIOD : return ".";
            case Keyboard.KEY_SLASH : return "\\";
            case Keyboard.KEY_SUBTRACT : return "K-";
            case Keyboard.KEY_ADD : return "K+";
            case Keyboard.KEY_DECIMAL : return "K.";
            case Keyboard.KEY_MULTIPLY : return "K*";
            case Keyboard.KEY_DIVIDE : return "K/";

            case Keyboard.KEY_LBRACKET : return "[";
            case Keyboard.KEY_RBRACKET : return "]";

            default: return Keyboard.getKeyName(slot.Key);
        }
    }
}
