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

import a1.Input;
import a1.Render2D;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;

import static a1.gui.Skin.*;

/**
 * поле ввода для целочисленных значений, поддержка колеса мыши
 */
public class GUI_SpinEdit extends GUI_Control {

    public int max, min, value;
    public String font = "default";
    public Color text_color = Color.white;
    public int step = 1;

    GUI_Button btn_inc, btn_dec;
    boolean pressed = false;

    public GUI_SpinEdit(GUI_Control parent) {
        super(parent);
        btn_dec = new GUI_Button(this) {
            public void DoClick() {
                DoDec();
                gui.SetFocus(parent);
            }
            public boolean DoMouseWheel(boolean isUp, int len) {
                return parent.DoMouseWheel(isUp, len);
            }
        };
        btn_inc = new GUI_Button(this) {
            public void DoClick() {
                DoInc();
                gui.SetFocus(parent);
            }
            public boolean DoMouseWheel(boolean isUp, int len) {
                return parent.DoMouseWheel(isUp, len);
            }
        };
        btn_dec.skin_element = "button_left";
        btn_dec.SetSize(getSkin().GetElementSize("button_left"));
        btn_inc.skin_element = "button_right";
        btn_inc.SetSize(getSkin().GetElementSize("button_right"));
        skin_element = "edit";
        focusable = true;

        max = 100;
        min = 0;
        value = 0;
    }

    public void DoInc() {
        if (value < max) {
            value += step;
            if (value > max) value = max;
            DoChanged();
        }
    }

    public void DoDec() {
        if (value > min) {
            value -= step;
            if (value < min) value = min;
            DoChanged();
        }
    }

    public void DoInc2() {
        if (value < max) {
            value += step*10;
            if (value > max) value = max;
            DoChanged();
        }
    }

    public void DoDec2() {
        if (value > min) {
            value -= step*10;
            if (value < min) value = min;
            DoChanged();
        }
    }

    public void DoChanged() { }

    public void DoSetSize() {
        btn_dec.SetPos(0,(size.y-btn_dec.Height()) / 2);
        btn_inc.SetPos(size.x - btn_inc.Width(),(size.y-btn_inc.Height()) / 2);
    }

    public void DoRender() {
        int state;
        if (!enabled)
            state = StateDisable;
        else {
            if (isFocused())
                state = StateNormal_Checked;
            else {
                if (MouseInMe()) {
                    if (pressed)
                        state = StatePressed;
                    else
                        state = StateHighlight;
                }
                else
                    state = StateNormal;
            }
        }
        getSkin().Draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y, state);
        Render2D.Text(font, abs_pos.x, abs_pos.y, size.x, size.y, Render2D.Align_Center, String.valueOf(value), text_color);
    }

    public boolean DoMouseWheel(boolean isUp, int len) {
        if (!MouseInMe() && !btn_dec.MouseInMe() && !btn_inc.MouseInMe())
            return false;

        if (isUp)
            DoInc();
        else
            DoDec();
        return true;
    }

    public boolean DoMouseBtn(int btn, boolean down) {
        if (!enabled) return false;

        if (btn == Input.MB_LEFT)
            if (down) {
                if (MouseInMe()){
                    gui.SetFocus(this);

                    pressed = true;
                    return true;
                }
            } else {
                if (pressed && MouseInMe()) {
                    DoClick();
                    return true;
                }
                pressed = false;
            }
        return false;
    }

    public boolean DoKey(char c, int key, boolean down) {
        if (isFocused()) {
            if (down) {
                if (key == Keyboard.KEY_UP || key == Keyboard.KEY_RIGHT)
                    DoInc();
                else if (key == Keyboard.KEY_DOWN || key == Keyboard.KEY_LEFT)
                    DoDec();
                else if (key == Keyboard.KEY_PRIOR)
                    DoInc2();
                else if (key == Keyboard.KEY_NEXT)
                    DoDec2();
            }
            return true;
        } else
            return false;
    }

    public void DoClick() {

    }
}
