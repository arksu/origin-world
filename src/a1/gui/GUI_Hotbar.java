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
import org.newdawn.slick.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * контрол для хотбара
 */
public class GUI_Hotbar extends GUI_Control{
    public Hotbar hotbar;
    List<GUI_HotbarSlot> slots = new ArrayList<GUI_HotbarSlot>();

    public GUI_Hotbar(GUI_Control parent, Hotbar h) {
        super(parent);
        this.hotbar = h;
    }

    public void UpdateState() {
        // обновить позицию
        SetPos(hotbar.pos);

        // размеры
        int w = hotbar.buttons_count / hotbar.rows_count + (hotbar.buttons_count % hotbar.rows_count > 0 ? 1:0);
        int h = hotbar.buttons_count / w + (hotbar.buttons_count % w > 0 ? 1 : 0);
        SetSize(w * getSlotSize() + (w+1) * hotbar.padding, h * getSlotSize() + (h+1) * hotbar.padding);

        // удаляем старые слоты
        for (GUI_HotbarSlot hs : slots) {
            hs.Unlink();
        }
        slots.clear();
        // положение слотов
        for (HotbarSlot s : hotbar.slots) {
            slots.add(new GUI_HotbarSlot(this, s));
        }
        int ax = 0;
        int ay = 0;
        int ssize = getSlotSize();
        for (GUI_HotbarSlot s : slots) {
            s.SetPos(
                    hotbar.padding + ax*(ssize+hotbar.padding),
                    hotbar.padding + ay*(ssize+hotbar.padding)
            );
            s.SetSize(ssize, ssize);
            ax++;
            if (ax >= w) {
                ax = 0;
                ay++;
            }
        }
    }

    public void DoRender() {
        if (hotbar.alpha_bg > 0)
        getSkin().Draw("hotbar_bg", abs_pos.x, abs_pos.y, size.x, size.y, Skin.StateDefault, new Color(255,255,255,hotbar.alpha_bg));
    }

    public void DoRenderAfterChilds() {
        if (isMoveable()) {
            Color col = (MouseInMe() || MouseInChilds()) ? new Color(0, 210, 0, 100) : new Color(0, 178, 0, 100);

            Render2D.ChangeColor(col);
            Render2D.Disable2D();
            Render2D.FillRect(abs_pos, size);
            Render2D.Enable2D();

            Render2D.Text("default", abs_pos.x, abs_pos.y, size.x, size.y, Render2D.Align_Center, hotbar.GetName(), Color.white);
        }
    }

    protected int getSlotSize() {
        // в будущем будем учитывать масштабирование
        return 32;
    }

    public boolean isMoveable() {
        return dlg_Hotbars.Exist() && dlg_Hotbars.dlg.isMovingMode();
    }

    public boolean DoMouseBtn(int btn, boolean down) {
        if (!enabled) return false;

        if (btn == Input.MB_LEFT && isMoveable()) {
            if (down) {
                if (MouseInMe() || MouseInChilds()) {
                    BeginDragMove();
                    return true;
                }
            } else {
                if (SelfDragged() && isMoveable()) {
                    hotbar.pos = new Coord(pos);
                    hotbar.SaveSettings();
                    if (dlg_Hotbars.dlg.current_hotbar == hotbar) {
                        dlg_Hotbars.dlg.ed_offset_x.value = pos.x;
                        dlg_Hotbars.dlg.ed_offset_y.value = pos.y;
                    }
                }
                EndDragMove();
            }
        }

        return false;
    }

    // условие по котороу определяется мышь в контроле
    public boolean CheckMouseInControl() {
        return visible && !hotbar.click_through;
    }
}
