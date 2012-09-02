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

import a1.gui.GUI_Inventory;

import java.util.Map;

/**
 * вещь в инвентаре
 */
public class InvItem {
    /**
     * ид вещи
     */
    public int objid;
    /**
     * тип вещи
     */
    public int type;
    /**
     * качество
     */
    public int quality;
    /**
     * количество
     */
    public int amount;
    /**
     * координаты
     */
    public int x;
    public int y;
    /**
     * размеры вещи
     */
    public int w;
    public int h;
    /**
     * имя иконки
     */
    public String icon_name;
    /**
     * подсказка
     */
    public String hint;
    /**
     * прогресс
     */
    public int progress, max;

    public int num;

    /**
     * инвентарь вещи (вложенный)
     */
    public Inventory inv = null;

    public InvItem(Packet pkt) {
        Read(pkt);
    }

    public void Read(Packet pkt) {
        objid = pkt.read_int();
        if (objid == 0) return;
        type = pkt.read_int();
        quality = pkt.read_word();
        x = pkt.read_byte();
        y = pkt.read_byte();
        w = pkt.read_byte();
        h = pkt.read_byte();
        icon_name = pkt.read_string_ascii();
        hint = pkt.read_string_ascii();
        amount = pkt.read_int();
        num = pkt.read_int();
        max = pkt.read_int();
        progress = pkt.read_int();
        // have inventory
        if (pkt.read_byte() != 0) {
            inv = new Inventory(objid);
            inv.Read(pkt);
        }
    }

    /**
     * имеет ли вещь инвентарь?
     * @return
     */
    public boolean isHaveInventory() {
        return inv != null;
    }

    public void getCtrls(Map<Integer, GUI_Inventory> map) {
        if (inv != null) inv.getCtrls(map);
    }
}
