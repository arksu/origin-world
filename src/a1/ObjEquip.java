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

import a1.utils.Utils;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;

import static a1.utils.Resource.draw_items;

public class ObjEquip {
    equip_slot head, body, lhand, rhand, legs, foots;

    public ObjEquip(OtpErlangObject term) {
        OtpErlangList l = Utils.ErlngList(term);
        head  = new equip_slot(l.elementAt(0));
        body  = new equip_slot(l.elementAt(1));
        lhand = new equip_slot(l.elementAt(2));
        rhand = new equip_slot(l.elementAt(3));
        legs  = new equip_slot(l.elementAt(4));
        foots = new equip_slot(l.elementAt(5));
    }

    public void AddParts(Obj obj, Coord c, int addz) {
        head.add_part(obj, c, addz);
        body.add_part(obj, c, addz);
        lhand.add_part(obj, c, addz);
        rhand.add_part(obj, c, addz);
        legs.add_part(obj, c, addz);
        foots.add_part(obj, c, addz);
    }

    private class equip_slot {
        String type;

        public equip_slot(OtpErlangObject term) {
            this.type = Utils.ErlangAtom(term);
        }

        public void add_part(Obj obj, Coord c, int addz) {
            if (!type.equals("none"))
                obj.add_part( draw_items.get("equip_"+obj.get_draw_name(type)), c, addz, true );
        }
    }
}
