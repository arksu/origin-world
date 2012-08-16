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
package a1.utils;
// реализация отлетающего текста
public class FlyParam {
	static final double fly_time = 0.65;
	static final double alpha_time = 0.85;
	static final int h = 40;
	
	static public int GetY(double X) {
		if (X <= fly_time) return (int) Math.round(h * Math.sin((X / fly_time) * Math.PI / 2));
		else return h;
	}
	
	static public int GetAlpha(double X) {
		if (X <= alpha_time) 
			return 255;
		else 
			return Math.round((float) (255 * Math.sin(((1-alpha_time)-(X-alpha_time)) / (1-alpha_time))));
	}
}
