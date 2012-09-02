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

import a1.dialogs.dlg_Game;
import a1.utils.Utils;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

import java.util.Iterator;

// униеверсальный параметр объекта 
// определяется уникальной связкой типа и ерланг терма
public class ObjParam extends ObjAttr {
	public String type;
	public OtpErlangObject term;

	public ObjParam(Obj obj, String type, OtpErlangObject term) {
		super(obj);
		this.type = type;
		this.term = term;
        Log.debug("param created type="+type+" data="+term.toString());
	}
	
	// обработать установку параметра объекту
	// old - старый параметр если уже был такого же типа
	public static void HandleSet(Obj o, ObjParam old, ObjParam newp) {
		if (newp.type.equals("hp")) {
			int new_hp = Utils.ErlangInt(newp.term);
			// если это мой чар - обновим полоску хп
			if (o.id == Player.CharID) {
				if (dlg_Game.Exist()) {
					dlg_Game.dlg.SetHP(new_hp);
				}
			}
			// если старого параметра нет - выходим. это первичная установка параметра хп
			if (old == null) return;
			int old_hp = Utils.ErlangInt(old.term);

            Log.debug("set hp old="+old_hp+" new="+new_hp);
			// вычисляем урон
			int damage = new_hp - old_hp;
			
			// только если урон есть
			if (damage != 0) {
				// красным выводим отрицательный урон. зеленым - положительный
				Coord c = o.getpos();
				FlyText.Add(c.x, c.y, (damage < 0 ? "-" : "+") + Integer.toString(damage), damage < 0?1:2);
			}

		}
		
		else if (newp.type.equals("count")) {
			o.generate_count_pos(Utils.ErlangInt(newp.term));
		}
		
		else if (newp.type.equals("obj_hp")) {
			if (newp.term instanceof OtpErlangTuple) {
				o.hp = Utils.ErlangInt( ((OtpErlangTuple) newp.term).elementAt(0) );
				o.shp = Utils.ErlangInt( ((OtpErlangTuple) newp.term).elementAt(1) );
				Log.debug("set obj hp="+o.hp+"/"+o.shp);
			}
		}

        else if (newp.type.equals("links")) {
            if (newp.term instanceof OtpErlangList) {
                o.links.clear();
                Iterator<OtpErlangObject> itr = ((OtpErlangList) newp.term).iterator();
                int index = 0;
                while (itr.hasNext()) {
                    index++;
                    int lid = Utils.ErlangInt(itr.next());
                    o.links.add(lid);
                    Obj lo = ObjCache.get(lid);
                    if (lo != null) {
                        o.fill_follow_params(index, lo);
                    }
                }
                Log.info("object links:"+o.links.toString());
            }
            if (newp.term instanceof OtpErlangString) {
                OtpErlangList l = new OtpErlangList( ((OtpErlangString)newp.term).stringValue() );
                Iterator<OtpErlangObject> itr = l.iterator();
                int index = 0;
                while (itr.hasNext()) {
                    index++;
                    int lid = Utils.ErlangInt(itr.next());
                    o.links.add(lid);
                    Obj lo = ObjCache.get(lid);
                    if (lo != null) {
                        o.fill_follow_params(index, lo);
                    }
                }
                Log.info("object links:"+o.links.toString());
            }
        }

        else if (newp.type.equals("follow")) {
            o.follow_id = Utils.ErlangInt(newp.term);
        }

        else if (newp.type.equals("ashow")) {
            o.ignore_overlap = true;
        }

        else if (newp.type.equals("direction")) {
            o.direction = Utils.ErlangInt(newp.term);
            int index = 0;
            for (int lid : o.links) {
                index++;
                Obj lo = ObjCache.get(lid);
                if (lo != null) {
                    o.fill_follow_params(index, lo);
                }
            }
        }
	}

    @Override
    public String toString() {
        return type + ':' + term.toString();
    }
}
