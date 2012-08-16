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

import java.util.ArrayList;
import java.util.List;

public class ChatHistory {
	static List<String> list = new ArrayList<String>();
	static int pos = 0;
	
	// сбросить позицию в хистори
	static public void Reset() {
		pos = list.size();
	}
	
	static public void Add(String msg) {
		list.add(msg);
		pos = list.size();
	}
	
	static public void Clear() {
		list.clear();
		pos = 0;
	}
	
	
	static public String Next() {
		pos++;
		if (pos >= list.size()) pos = list.size()-1;
		if (pos < list.size() && pos >=0) {
			return list.get(pos);
		}
		return "";
	}
	
	static public String Prev() {
		pos--;
		if (pos < 0) pos = 0;
		if (pos < list.size() && pos >=0) {
			return list.get(pos);
		}
		return "";
	}
	
}
