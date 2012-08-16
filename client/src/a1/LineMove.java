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

public class LineMove extends ObjAttr {
	Coord start, end, cur;
	long start_time;
	double speed;
	double cur_len;

	static final int SMOTH_RADIUS = 20;

	public LineMove(Obj obj, int x, int y, int vx, int vy, int speed) {
		super(obj);
		start = new Coord(x,y);
		end = new Coord(vx, vy);
		this.speed = speed;
		cur_len = 0;
		cur = start;
		start_time = System.currentTimeMillis();
		
		if (obj != null && start.dist(end) > 0) {
			obj.direction = GUI_Map.m2s(start).direction(GUI_Map.m2s(end));
		}
	}

	// пришел очередной пакет мува. надо скорректировать движение для плавности
	public void update_move(int x, int y, int vx, int vy, int speed) {
		Coord new_end = new Coord(vx, vy);
		Coord new_start = new Coord(x,y); 
		start_time = System.currentTimeMillis();
		// определяем где мы. и где точка начала нового движения, проверяем радиус
		if (cur.dist(new_start) > SMOTH_RADIUS) {
			start = new Coord(x,y);
			end = new Coord(vx, vy);
			this.speed = speed;
			cur_len = 0;
			cur = start;
		} else {
			start = cur;
			end = new_end;
			cur_len = 0;
			cur = start;
			this.speed = start.dist(end) / new_start.dist(new_end) * (double)speed;
		}
		if (obj != null && start.dist(end) > 0) {
			obj.direction = GUI_Map.m2s(start).direction(GUI_Map.m2s(end));
		}
	}

	public Coord get_pos() {
		return cur;
	}
	
	public Coord get_end() {
		return end;
	}

	public void update() {
		long dt = System.currentTimeMillis() - start_time;
		cur_len = (((double)dt / 1000) * speed);
		double d = start.dist(end);
		cur = new Coord( 
				(int) Math.round((end.sub(start).x / d) * cur_len),
				(int) Math.round((end.sub(start).y / d) * cur_len) );
		//		Log.info("dt= "+dt+" cur="+cur.toString()+" cur_len="+cur_len+" speed="+speed);
		if (cur_len > start.dist(end)) 
			cur = end;
		else
			cur = cur.add(start);
	}
	
	public boolean stopped() {
		return cur.equals(end);
	}

}
