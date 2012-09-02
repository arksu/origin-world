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

package a1.utils;

import a1.Coord;
import a1.Packet;
import a1.gui.GUI;
import a1.gui.GUI_Image;

/**
 * то что держим в руке
 */
public class Hand {
    int objid = 0;
    public int w = 0;
    public int h = 0;
    public int offset_x = 0;
    public int offset_y = 0;
    String image;

    public GUI_Image ctrl;

    public void read(Packet pkt) {
        objid = pkt.read_int();
        if (objid != 0) {
            w = pkt.read_byte();
            h = pkt.read_byte();
            offset_x = pkt.read_byte();
            offset_y = pkt.read_byte();
            image = "item_"+pkt.read_string_ascii();

            if (ctrl != null) ctrl.Unlink();
            ctrl = new GUI_Image(GUI.getInstance().popup);
            ctrl.skin_element = image;
            ctrl.SetSize(ctrl.getSkin().GetElementSize(image));
            ctrl.drag = true;
            ctrl.drag_offset = new Coord(offset_x, offset_y);

        } else {
            w = 0;
            h = 0;
            offset_x = 0;
            offset_y = 0;

            if (ctrl != null) ctrl.Unlink();
        }
    }

    public boolean isExist() {
        return objid != 0;
    }

    public void Clear() {
        if (ctrl != null) {
            ctrl.Unlink();
            ctrl = null;
        }
        objid = 0;
    }
}
