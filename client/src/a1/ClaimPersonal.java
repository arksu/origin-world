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

public class ClaimPersonal {
    public int owner_id;
    public int object_id;
    public Coord lt, rb;

    public ClaimPersonal(Packet pkt, int owner_id) {
        this.owner_id = owner_id;
        object_id = pkt.read_int();
        lt = new Coord(pkt.read_int(), pkt.read_int());
        rb = new Coord(pkt.read_int(), pkt.read_int());
        Log.debug( "new claim: "+this.toString() );
    }

    @Override
    public String toString() {
        return "owner="+owner_id+" obj="+object_id+" lt="+lt.toString()+" rb="+rb.toString();
    }

    public ClaimPersonal (ClaimPersonal c) {
        this.owner_id = c.owner_id;
        this.object_id = c.object_id;
        this.lt = c.lt.clone();
        this.rb = c.rb.clone();

    }

    public void ExpandLeft() {
        lt = lt.sub(1,0);
    }

    public void ExpandRight() {
        rb = rb.add(1, 0);
    }

    public void ExpandUp() {
        lt = lt.sub(0,1);
    }

    public void ExpandDown() {
        rb = rb.add(0, 1);
    }
}
