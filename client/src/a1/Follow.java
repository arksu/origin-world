/*
 *  This file is part of the Origin-World game client.
 *  Copyright (C) 2012 Arkadiy Fattakhov <ark@ark.su>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 3 of the License.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package a1;

public class Follow extends ObjAttr {
	int objid;
	Coord draw_offset;
	int addz;
	
	public Follow(Obj obj, int objid, Coord droff, int addz) {
		super(obj);
		this.objid = objid;
		this.draw_offset = droff;
		this.addz = addz;
	}
	
	public Coord get_pos() {
		Obj tgt = ObjCache.get(objid);
		if (tgt == null) return obj.pos;
		else {
			Coord c = tgt.getpos();
			return c;
		}
	}

}
