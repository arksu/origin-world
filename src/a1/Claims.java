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


import a1.gui.GUI_Map;
import a1.net.Packet;
import a1.obj.ObjectVisual;
import a1.obj.claim;

import java.util.HashMap;
import java.util.Map;

public class Claims {
    public static Map<Integer, ClaimPersonal> claims = new HashMap<Integer, ClaimPersonal>();
    public static ClaimPersonal expand_claim = null;

    public static void RecvClaimChange(Packet pkt) {
        int id = pkt.read_int();
        ClaimPersonal c = new ClaimPersonal(pkt, id);
        claims.put(id, c);
        Refresh();

        for (ObjectVisual ov : ObjectVisual.obj_list.values()) {
            if (ov instanceof claim) {
                ((claim)ov).RefreshClaims();
            }
        }
    }

    public static void RecvClaimRemove(Packet pkt) {
        int id = pkt.read_int();
        claims.remove(id);
        Refresh();
    }

    public static void Refresh() {
        GUI_Map.needUpdateView = true;
    }
}
