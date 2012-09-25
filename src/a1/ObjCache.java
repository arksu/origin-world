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

import a1.net.Packet;
import com.ericsson.otp.erlang.OtpErlangObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ObjCache {
	// objid, obj
	public static final Map<Integer, Obj> objs = new TreeMap<Integer, Obj>();
    public static final List<ObjLocal> local = new LinkedList<ObjLocal>();
	
	static public Obj get(int id) {
		if (objs.containsKey(id)) {
			return objs.get(id);
		} else {
			Obj o = new Obj(id, Coord.z);
			objs.put(id, o);
			return o;
		}
	}
	
	static public void remove(Obj o) {
        synchronized (objs) {
		    objs.remove(o.id);
        }
	}
	
	static public void remove(int id) {
        synchronized (objs) {
		    objs.remove(id);
        }
	}
	
	static public void remove_param(int id, int p) {
		Obj o = get(id);
		if (o == null)
			return;
		switch (p) {
		case 1: o.delattr(KinInfo.class); break;
		case 2:	o.follow_id = 0; break;
		case 3: o.delattr(Light.class); break;
		case 4: o.is_opened = false; break;
        case 5: o.links.clear(); break;
		default: break;
		}
	}
	
	static public void clear_params(int id) {
		//Log.info("clear params "+objid);
		Obj o = get(id);
		if (o == null)
			return;
		o.delattr(KinInfo.class); 
		o.delattr(Follow.class); 
		o.delattr(Light.class); 
		o.is_opened = false;
	}
	
	static public void SetParam(Packet pkt) {
		int id = pkt.read_int(); 
		String type = pkt.read_string_ascii(); 
		OtpErlangObject term = pkt.read_erlang_term();
		Obj o = get(id);
		if (o == null)
			return;
		o.setattr(new ObjParam(o, type, term));
	}
	
	static public void KinInfo(int id, String name) {
		Obj o = get(id);
		if (o == null)
			return;
		o.setattr(new KinInfo(o, name));
	}
	
	static public void Follow(int id, int tgt, int offx, int offy, int addz) {
		Obj o = get(id);
		if (o == null)
			return;
		else {
			Follow f = o.getattr(Follow.class);
			if (f == null) {
				f = new Follow(o, tgt, new Coord(offx,offy), addz);
				o.setattr(f);
			} else {
				f.draw_offset = new Coord(offx,offy);
				f.addz = addz;
				f.objid = tgt;
			}
		}
	}
	
	static public void line_move_attr(int id, int x, int y, int vx, int vy, int speed) {
		Obj o = get(id);
		if (o == null)
			return;
		LineMove m = o.getattr(LineMove.class);
		if (m != null) 
			m.update_move(x,y,vx,vy,speed);
		else
			o.setattr(new LineMove(o,x,y,vx,vy,speed));
	}
	
	static public void draw_attr(int id, String name) {
		Obj o = get(id);
		if (o == null)
			return;
		
		clear_params(id);
		
		if (name.length() < 1) {
			o.delattr(Drawable.class);
		} else {
            Log.debug("drawable="+name);
			o.setattr(new Drawable(o, name));
		}
	}
	
	static public void obj_say(int channel, String msg, int id) {
		Obj o = get(id);
		if (o == null)
			return;
		if (o.getattr(ObjSay.class) != null) o.delattr(ObjSay.class);
		o.setattr(new ObjSay(o, msg, channel));
	}
	
	static public void gain_exp(int id, int combat, int industry, int nature) {
		Obj o = get(id);
		if (o == null)
			return;
		o.add_effect(new ObjExp(combat,industry,nature));
	}
	
	static public void light_attr(int id, int radius, byte strong) {
		Obj o = get(id);
		if (o == null)
			return;
		o.setattr(new Light(o, radius, strong));
	}
	
	static public void opened_attr(int id) {
		Obj o = get(id);
		if (o == null)
			return;
		o.is_opened = true;
	}
	
	static public void move(int id, int x, int y) {
		Obj o = get(id);
		if (o == null)
			return;
		o.move(new Coord(x,y));
	}
    
    static public void obj_type(int id, String t) {
        Obj o = get(id);
        if (o == null)
            return;
        Log.debug("obj type="+t);
        o.obj_type = t;
    }
	
	static public void ClearAll() {
		objs.clear();
        local.clear();
	}
	
	static public void update() {
		for (Obj o : objs.values())
			o.update();
	}

}
