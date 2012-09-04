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
import a1.net.Packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Инвентарь объекта
 */
public class Inventory {
    /**
     * родитель
     */
    public Inventory parent = null;
    /**
     * ид объекта чей инвентарь
     */
    public int objid = 0;
    /**
     * ширина
     */
    public int w;
    /**
     * высота
     */
    public int h;

    /**
     * список вещей
     */
    public List<InvItem> items = new ArrayList<InvItem>();

    /**
     * окно с отображением инвентаря
     */
    public GUI_Inventory ctrl = null;

    /**
     * constructor
     */
    public Inventory(int id) {
        this.objid = id;
    }

    public void Read(Packet pkt) {
        w = pkt.read_byte();
        h = pkt.read_byte();
        int count = pkt.read_word();
        items.clear();
        while (count > 0) {
            count--;
            InvItem item = new InvItem(pkt);
            items.add(item);
        }
    }

    public void getCtrls(Map<Integer, GUI_Inventory> map) {
        if (ctrl != null) map.put(objid, ctrl);

        for (InvItem i : items) {
            i.getCtrls(map);
        }
    }

    /**
     * найти вещь в инвентаре по ее ид
     * @param objid
     * @return
     */
    public InvItem getItem(int objid) {
        for (InvItem i : items) {
            if (i.objid == objid) return i;

            if (i.isHaveInventory()) {
                InvItem ii = i.inv.getItem(objid);
                if (ii != null) return ii;
            }
        }
        return null;
    }

    public void CtrlClosed(int objid) {
        if (this.objid == objid) {
            ctrl = null;
            return;
        }

        InvItem i = getItem(objid);
        if (i != null && i.isHaveInventory()) {
            i.inv.ctrl = null;
        }
    }



    public static Map<Integer, Coord> inv_pos = new HashMap<Integer, Coord>();

    public static void UpdateInvPos(int objid, Coord pos) {
        inv_pos.put(objid, pos);
    }

    public static Coord getInvPos(int objid) {
        Coord p = inv_pos.get(objid);
        if (p != null) return p;
        if (objid == Player.CharID) return  new Coord(100, 200);
        return new Coord(400, 200);
    }
}
