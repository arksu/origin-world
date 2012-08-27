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
import a1.utils.FlyParam;
import org.newdawn.slick.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlyText {
	private static final long LIFE_TIME = 1400;
	
	public static class FlyTextItem {
		int x;
		int y;
		String msg;
		long start_time;
		int color_code = 0;
		public FlyTextItem(int x, int y, String msg, int code) {
			this.x = x;
			this.y = y;
			this.msg = msg;
			this.color_code = code;
			start_time = System.currentTimeMillis();
		}
		
		public boolean isAlive() {
			return (System.currentTimeMillis() - start_time) <= LIFE_TIME;
		}
		
		public Coord getpos() {
			return new Coord(x, y);
		}
		
		public Color getColor() {
			switch (color_code) {
			case 0: return Color.white; // Обычный инфо текст
			case 1: return Color.red; // Damage
			case 2: return Color.green; // Хинт для действия
			
			default:
				return Color.white;
			}
		}
		
		public void Render(Coord dc) {
			double t = (float)(System.currentTimeMillis() - start_time)/(float)LIFE_TIME;
			int a = FlyParam.GetAlpha(t);
			int dy = FlyParam.GetY(t);
			Color c = getColor();

			Render2D.Text("default", 
					dc.x - Render2D.GetTextWidth("default", msg)/2, 
					(int)(dc.y-dy - 35 - 40 * GUI_Map.scale),
					msg, new Color(c.getRed(), c.getGreen(), c.getBlue(), a));
		}
	}
	
	public static List<FlyTextItem> items = new ArrayList<FlyTextItem>();
	
	static public void Add(int x, int y, String msg) {
		items.add(new FlyTextItem(x, y, msg, 0));
	}
	
	static public void Add(int x, int y, String msg, int code) {
		items.add(new FlyTextItem(x, y, msg, code));
	}
	
	static public void Update() {
		for(Iterator<FlyTextItem> item_iter = items.iterator(); item_iter.hasNext();){
			FlyTextItem cur_item = item_iter.next();
			//cur_item.update();
			if (!cur_item.isAlive()) {
				item_iter.remove();
			}
		}
	}
	
	static public void Clear() {
		items.clear();
	}
}
