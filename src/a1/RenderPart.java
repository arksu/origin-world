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


import a1.gui.GUI;
import a1.gui.GUI_Map;
import a1.utils.Resource;

import java.util.Comparator;

public class RenderPart {
	public Obj owner = null; // parent object
	public Coord screen_coord; // координаты для вывода на экран с учетом всех отступов
	public Coord dc; // проекция координат объекта на экран
	public Coord coord; // мировые координаты объекта
	public Coord size; 
	public int z, subz, addz;
	public boolean is_my_player; // это часть моего персонажа на экране?
	public boolean ignore_overlap = false; // игнорировать перекрытие. показыватся будет всегда
	public boolean is_overlapped = false;
	Resource.ResLayer layer;

	public static final Comparator<RenderPart> part_cmp = new Comparator<RenderPart>() {

		public int compare(RenderPart a, RenderPart b) {
			if (a.z != b.z)
				return(a.z - b.z);
			if (a.dc.y != b.dc.y)
				return(a.dc.y - b.dc.y);
			if ((a.subz + a.addz) != (b.subz + b.addz))
				return ((a.subz + a.addz) - (b.subz + b.addz));
			if (a.dc.x != b.dc.x)
				return(a.dc.x - b.dc.x);

			if (a.owner.id != b.owner.id)
				return a.owner.id - b.owner.id;
			
			return a.hashCode() - b.hashCode();
		}
	};	
	public RenderPart(Coord dc, Coord c, Resource.ResLayer layer, Obj obj, int addz, boolean ignore_overlap) {
		this.owner = obj;
		this.screen_coord = c;
		this.dc = dc;
		this.layer = layer;
		this.subz = 0;
		this.addz = addz;
		this.size = new Coord(layer.w, layer.h);
		this.coord = obj.getpos(); 
		this.is_my_player = obj.id == Player.CharID;
		this.ignore_overlap = ignore_overlap || (obj.follow_id != 0);
		z = layer.z;
	}
	
//	public RenderPart(Coord dc, Coord c, Resource.ResLayer layer, Obj obj, int subz, int addz, boolean ignore_overlap) {
//		this.owner = obj;
//		this.screen_coord = c;
//		this.dc = dc;
//		this.layer = layer;
//		this.subz = subz;
//		this.addz = addz;
//		this.size = new Coord(layer.w, layer.h);
//		this.coord = obj.getpos();
//		this.is_my_player = obj.objid == Player.CharID;
//		this.ignore_overlap = ignore_overlap;
//		this.tc = this.coord.div(MapCache.TILE_SIZE);
//		z = layer.z;
//	}
		
	public void render() {
        GUI.map.RenderedObjects++;
        if (owner != null)
            layer.render(screen_coord.x, screen_coord.y,
                    (System.currentTimeMillis() - owner.start_time));
        else
            layer.render(screen_coord.x, screen_coord.y, (System.currentTimeMillis()));
	}
		
	public boolean check_hit(Coord c) {
		if (is_overlapped && !GUI_Map.ignore_overlapped) return false;
		
		if (c.in_rect(this.screen_coord.add(layer.offx, layer.offy), size)) {
			Coord b = c.sub(this.screen_coord);
			if (owner != null)
				return layer.check_hit(b.x, b.y, 
						(System.currentTimeMillis() - owner.start_time));
			else
				return layer.check_hit(b.x, b.y, (System.currentTimeMillis()));
		} else
			return false;
	}
}
