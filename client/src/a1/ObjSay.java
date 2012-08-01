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

public class ObjSay extends ObjAttr {
	public String msg;
	public int channel;
	int time_left;
	
	public ObjSay(Obj obj, String msg, int channel) {
		super(obj);
		this.msg = msg;
		int l = msg.length();
		if (l > 32) l = 32;
		time_left = l * 600;
		if (time_left < 4000) time_left = 4000;
		this.channel = channel;
	}
	
	public void update() {
		time_left -= Main.dt;
		if (time_left <= 0)
			// если время вышло - надо удалить параметр
			obj.to_delete.add(ObjSay.class);
	}

}
