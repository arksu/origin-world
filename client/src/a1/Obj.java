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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import a1.gui.GUI;
import a1.gui.GUI_Map;
import a1.utils.Resource;
import a1.utils.Utils;
import static a1.MapCache.TILE_SIZE;

public class Obj {
	public static final int PLAYER_FRAMES = 8;
	
	public int id = 0;
	public Coord pos = Coord.z;
	// последнее направление двжиения, то куда должен быть направлен объект при
	// отрисовке
	public int direction = 0;
	// время в которое создан объект (нужно для анимации)
	public long start_time = System.currentTimeMillis() + (long) (Math.random() * 5000);
	private Map<Class<? extends ObjAttr>, ObjAttr> attr = new HashMap<Class<? extends ObjAttr>, ObjAttr>();
    // erlang params
	private List<ObjParam> params = new ArrayList<ObjParam>();
	
    // params
    public String obj_type = "";
    public int hp = 0;
    public int shp = 0;
    public boolean is_opened = false;
    public int follow_id = 0;
    public Coord follow_offset = Coord.z;
    public int follow_addz = 0;
    public boolean ignore_overlap = false;
    public List<Integer> links = new ArrayList<Integer>();
	
	// hardcode ))
	protected Coord step_offset = Coord.z; // отступ для отрисовки (нужно для подпрыгивающего чара в нужные кадры анимации)
	protected List<Coord> count_pos = new ArrayList<Coord>(); // позиции для отображения нужного количества объектов (да.... хардкод)
	protected Coord objc; // текущие координаты для отрисовки, запоминаем если отрисовываем несколько растений в тайле
	
	public List<Class<? extends ObjAttr>> to_delete = new ArrayList<Class<? extends ObjAttr>>();
	public List<ObjEffect> effects = new ArrayList<ObjEffect>();

	public Obj(int id, Coord c) {
		this.id = id;
		this.pos = c;
	}

	public Obj(Coord c) {
		this(0, c);
	}

	public Coord getpos() {
		// если есть параметр движения - берем координаты из него
		LineMove m = getattr(LineMove.class);
		if (m != null)
			return (m.get_pos());
		else {
            if (follow_id != 0) {
                Obj tgt = ObjCache.get(follow_id);
                if (tgt == null) {
                    return pos;
                } else {
                    return tgt.getpos();
                }
            } else return pos;
		}
	}

	public void move(Coord c) {
		LineMove m = getattr(LineMove.class);
		if (m != null)
			delattr(LineMove.class);
		pos = c;
	}
	
	public Coord draw_offset() {
		Coord res = Coord.z;
        if (follow_id != 0) res = res.add(follow_offset);
		return res;
	}

	public void prepare_draw(Coord oc) {
		ObjParam p = getparam("count");
		int count = 1;
		if (p != null) {
			if (!Config.count_objs)
				count = 1;
			else
				count = Utils.ErlangInt(p.term);
			if (count > 1) {
				while (count > 0) {
					count--;
					// если указано количество - добавляем напрямую т.к. это растение
					Drawable d = getattr(Drawable.class);
					if (d != null) {
						objc = count_pos.get(count);
						add_part(Resource.draw_items.get(d.name), GUI_Map.m2s(objc).add(oc), 0, true); // ignore overlap
					}
				}
			} else {
				Drawable d = getattr(Drawable.class);
				if (d != null) {
					objc = getpos();
					add_part(Resource.draw_items.get(d.name), GUI_Map.m2s(objc).add(oc), 0, true); // ignore overlap
				}
			}
			// рисуем тень
			add_part(Resource.draw_items.get("plant_shadow"), GUI_Map.m2s(getpos()).add(oc), 0, true);
	
		} else {
			objc = getpos();
			prepare_draw_coord( GUI_Map.m2s(objc).add(oc));			
		}

	}
	
	private void prepare_draw_coord(Coord c) {
		Drawable d = getattr(Drawable.class);
		if (d == null)
			return;

		int addz = 0;
        if (follow_id != 0)
			addz = follow_addz;

		String draw_name = d.name;
		
		// дефолт по умолчанию
		step_offset = Coord.z;
		add_draw_part(draw_name, c, addz);
		
		// пропарсим эквип и добавим части эквипа
		// TODO
		// add_draw_part(draw_name, c.add(step_offset), addz);
	}
	
	private void add_draw_part(String draw_name, Coord c, int addz) {
		String dr_name = draw_name;
		// если движемся
		if (is_moving()) dr_name += "_move";
		
		// учтем направление
		if (direction != 0) 
			dr_name += "_"+direction;
		
		// check params
		if (is_opened)
			dr_name += "_opened";

		if (obj_type.equals("player")) {
			// ищем нужную часть игрока для отрисовки
			Resource.ResDraw dr = Resource.draw_items.get(dr_name);
			if (dr != null) {
				// ищем анимации
				for (Resource.ResLayer l : dr.layers) {
					// если это точно анимация игрока
					if (l instanceof Resource.ResAnim && ((Resource.ResAnim)l).frames_count == PLAYER_FRAMES) {
						// хардкодом заставим подпрыгивать чара на 1 пиксель 
						int frame = ((Resource.ResAnim)l).get_current_frame(System.currentTimeMillis() + start_time);
						if (frame == 0 || frame == 1 || frame == 4 || frame == 5)
							step_offset = new Coord(0, 1);
						break;
					}
				}
				add_part(dr, c, addz);
			}
			// тень выводим отдельно чтобы не прыгала вместе с игроком
			add_part(Resource.draw_items.get("player_shadow"), c, addz, true);
		} else {
			add_part(Resource.draw_items.get(dr_name), c, addz, ignore_overlap);
		}
	}
	
	private void add_part(Resource.ResDraw dr, Coord c, int addz, boolean ignore_overlap) {
		if (dr == null) {
			return;
		}

		// real screen coords
		Coord cn = new Coord(c.x - dr.offx, c.y - dr.offy);
		cn = cn.add(draw_offset());
        cn = cn.add(step_offset);

		// check visibility
		if (
				(cn.x + dr.width < 0) || (cn.y + dr.height < 0) || 
				(cn.x > Config.ScreenWidth) || (cn.y > Config.ScreenHeight)
			)
			return;

		// add part layers to render list
		for (Resource.ResLayer l : dr.layers) {
			if (l instanceof Resource.ResWeightList) {
				// если это весовой список
				for (Resource.ResLayer ll : ((Resource.ResWeightList) l).items.pick(MapCache.randoom(objc)).items) {
					GUI.map.render_parts.add(new RenderPart(c, cn, ll, this, ll.addz + addz, ignore_overlap));
				}
			} else {
				GUI.map.render_parts.add(new RenderPart(c, cn, l, this, l.addz + addz, ignore_overlap));
			}
		}
	}
	
	private void add_part(Resource.ResDraw dr, Coord c, int addz) {
		add_part(dr, c, addz, false);
	}
	
	private boolean is_moving() {
		
		LineMove m = getattr(LineMove.class);
		if (m != null) {
			return !m.stopped();
		}
		return false;
	}

	public void update() {
		for (ObjAttr a : attr.values())
			a.update();
		for (Class<? extends ObjAttr> c : to_delete) {
			delattr(c);
		}
		to_delete.clear();

		// апдейтим эффекты и удаляем уже отжившие свое
		for (Iterator<ObjEffect> effIter = effects.iterator(); effIter.hasNext();) {
			ObjEffect effect = effIter.next();
			effect.update();
			if (!effect.alive) {
				effIter.remove();
			}
		}
	}

	public void render_effects(Coord dc) {
		for (ObjEffect e : effects) {
			e.render(dc);
		}
	}

	public void add_effect(ObjEffect e) {
		effects.add(e);
	}

	private Class<? extends ObjAttr> attrcl(Class<? extends ObjAttr> cl) {
		while (true) {
			Class<?> p = cl.getSuperclass();
			if (p == ObjAttr.class)
				return (cl);
			cl = p.asSubclass(ObjAttr.class);
		}
	}

	public void setattr(ObjAttr a) {
		if (a instanceof ObjParam) {
			ObjParam ap = (ObjParam) a;
			ObjParam old_param = null;
			// если у объекта уже есть параметр такого же типа - удалим его
			for (ObjParam p : params) {
				if (p.type.equals(ap.type)) {
					old_param = p;
					params.remove(p);
					break;
				}
			}
			ObjParam.HandleSet(this, old_param, ap);
			params.add((ObjParam) a);
		} else {
			Class<? extends ObjAttr> ac = attrcl(a.getClass());
			attr.put(ac, a);
		}
	}

	public <C extends ObjAttr> C getattr(Class<C> c) {
		ObjAttr attr = this.attr.get(attrcl(c));
		if (!c.isInstance(attr))
			return (null);
		return (c.cast(attr));
	}

	public ObjParam getparam(String type) {
		for (ObjParam p : params) {
			if (p.type.equals(type))
				return p;
		}
		return null;
	}

	public void delattr(Class<? extends ObjAttr> c) {
//		if (c.getCanonicalName().equals("a1.LineMove"))
//			Log.info("delete attr "+c.getCanonicalName());
		attr.remove(attrcl(c));
	}
	
	public void generate_count_pos(int c) {
		count_pos.clear();
		Coord p = getpos();
		Random gen = MapCache.mkrandoom(p);
		while (c>0) {
			c--;
			count_pos.add(new Coord(
					p.x + (gen.nextInt() % (TILE_SIZE /2)) , 
					p.y + (gen.nextInt() % (TILE_SIZE /2)) 
					));
		}
	}

    // FOLLOW -----------------------------------
    // установить параметры привязки объекту
    public void fill_follow_params(int index, Obj f) {
        // я - главный объект, f - привязан ко мне
        if (obj_type.equals("player")) {
            f.follow_addz = 20;
            f.follow_offset = new Coord(0, -40);
        }
    }
    // end FOLLOW -------------------------------

	public String toString() {
		String s = super.toString();
		Drawable d = getattr(Drawable.class);
		if (d != null)
			s += " draw=" + d.name;
		Set<Class<? extends ObjAttr>> a = attr.keySet();
		if (!a.isEmpty()) {
			s += " attrs:[";
			for (@SuppressWarnings("rawtypes")
			Class ac : a) {
				s += ac.getName() + ", ";
			}
			s += "]";
		}
		return s;
	}

}
