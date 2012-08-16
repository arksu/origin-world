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

public class Tile {
//	public int level = 0;
//	public boolean is_water = false;
	public int type = 0;
	public Sprite ground;
	
	public Tile(Coord tc) {
		ground = MapCache.getground(tc);
		
		int l = MapCache.getlevel(tc);
//		is_water = (l & 128) > 0;
//		level = ((l << 3) & 0xff) >> 3;
	}
	
	public static int getlevel(Coord tc) {
		int l = MapCache.getlevel(tc);
		return ((l << 3) & 0xff) >> 3;
	}
}
