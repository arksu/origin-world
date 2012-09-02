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

public class Coord implements Comparable<Coord> {

	public int x, y;
	public static final Coord z = new Coord();

	public Coord(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Coord(Coord c) {
		this(c.x, c.y);
	}

	public Coord() {
		this(0, 0);
	}
	
	public Coord clone() {
		return new Coord(this);
	}

	public static Coord sc(double a, double r) {
		return (new Coord((int) (Math.cos(a) * r), -(int) (Math.sin(a) * r)));
	}

	public boolean equals(Object o) {
		if (!(o instanceof Coord))
			return (false);
		Coord c = (Coord) o;
		return ((c.x == x) && (c.y == y));
	}

	public int compareTo(Coord c) {
		if (c.y != y)
			return (c.y - y);
		if (c.x != x)
			return (c.x - x);
		return (0);
	}

	public Coord add(int ax, int ay) {
		return (new Coord(x + ax, y + ay));
	}
	
	public Coord add(int a) {
		return (new Coord(x + a, y + a));
	}

	public Coord add(Coord b) {
		return (add(b.x, b.y));
	}

	public Coord sub(int ax, int ay) {
		return (new Coord(x - ax, y - ay));
	}
	
	public Coord sub(int v) {
		return (new Coord(x - v, y - v));
	}

	public Coord sub(Coord b) {
		return (sub(b.x, b.y));
	}

	public Coord mul(int f) {
		return (new Coord(x * f, y * f));
	}

	public Coord mul(double f) {
		return (new Coord((int) (x * f), (int) (y * f)));
	}

    public Coord mul(double fx, double fy) {
        return (new Coord((int) (x * fx), (int) (y * fy)));
    }

	public Coord inverse() {
		return (new Coord(-x, -y));
	}

	public Coord mul(Coord f) {
		return (new Coord(x * f.x, y * f.y));
	}

	public Coord div(Coord d) {
		int v, w;

		v = ((x < 0) ? (x + 1) : x) / d.x;
		w = ((y < 0) ? (y + 1) : y) / d.y;
		if (x < 0)
			v--;
		if (y < 0)
			w--;
		return (new Coord(v, w));
	}

	public Coord div(int d) {
		return (div(new Coord(d, d)));
	}

	public Coord mod(Coord d) {
		int v, w;

		v = x % d.x;
		w = y % d.y;
		if (v < 0)
			v += d.x;
		if (w < 0)
			w += d.y;
		return (new Coord(v, w));
	}
	
	public static Coord abs(Coord c) {
		return new Coord(Math.abs(c.x), Math.abs(c.y));
	}
	
	public Coord mod(int d) {
		int v, w;

		v = x % d;
		w = y % d;
		if (v < 0)
			v += d;
		if (w < 0)
			w += d;
		return (new Coord(v, w));
	}
	// находится ли внутри прямоугольника. c - начальные координаты. s - размер
	public boolean in_rect(Coord c, Coord s) {
		return ((x >= c.x) && (y >= c.y) && (x < c.x + s.x) && (y < c.y + s.y));
	}

	public String toString() {
		return ("(" + x + ", " + y + ")");
	}

	public int direction(Coord to_point) {
		Coord vector = to_point.sub(this);
		if (vector.x == 0 && vector.y == 0) return 0;
		double a = Math.atan2(vector.y, vector.x);
		if (a < 0) a = Math.PI + Math.PI + a;
		return (int) Math.round(a / (Math.PI / 4)+6) % 8;
	}
	
	public double dist(Coord o) {
		long dx = o.x - x;
		long dy = o.y - y;
		return (Math.sqrt((dx * dx) + (dy * dy)));
	}

    public int area() {
        return x * y;
    }
	

}