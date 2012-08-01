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

public class IntCoord {
	public int x;
	public int y;
	public int w;
	public int h;
	
	public IntCoord(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	public IntCoord(IntCoord other) {
		this.x = other.x;
		this.y = other.y;
		this.w = other.w;
		this.h = other.h;		
	}
	
	public IntCoord(Coord ul, Coord size) {
		this.x = ul.x;
		this.y = ul.y;
		this.w = size.x;
		this.h = size.y;		
	}
	
	public IntCoord(int x, int y, Coord size) {
		this.x = x;
		this.y = y;
		this.w = size.x;
		this.h = size.y;
	}
	
	public boolean PointInRect(Coord p) {
		return (p.x >= x && p.x < x+w && p.y >= y && p.y < y+h);
	}
	
	public int Right() {
		return x+w;
	}
	
	public int Bottom() {
		return y+h;
	}
	
	public void SetRight(int a) {
		w = a - x;
	}
	
	public void SetBottom(int a) {
		h = a - y;
	}
	
	public String toString() {
		return "x="+x+" y="+y+" w="+w+" h="+h;
	}
}
