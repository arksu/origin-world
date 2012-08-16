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

public class ActionsMenu {
	static List<ActionsMenuItem> items = new ArrayList<ActionsMenuItem>();

	// получить список по родителю
	static public List<ActionsMenu.ActionsMenuItem> getList(String parent) {
		ArrayList<ActionsMenuItem> res = new ArrayList<ActionsMenu.ActionsMenuItem>();
		for (ActionsMenuItem i : items) {
			if (i.parent.equals(parent))
				res.add(new ActionsMenuItem(i.parent, i.name));
		}
		return res;
	}
	
	static public void Clear() {
		items.clear();
	}
	
	public static String getParent(String name) {
		for (ActionsMenuItem i : items) {
			if (i.name.equals(name)) return i.parent;
		}
		return "root";
	}
		
	static public void RECV_Action(String parent, String name) {
		items.add(new ActionsMenuItem(parent,name));
	}
	
	public static boolean haveAction(String name) {
		for (ActionsMenuItem i : items) {
			if (i.name.equals(name)) return true;
		}
		return false;
	}
	
	public static boolean withoutChilds(String name) {
		return getList(name).size() == 0;
	}
	
	static public class ActionsMenuItem {
		public String parent;
		public String name;
		
		public ActionsMenuItem(String parent, String name) {
			this.name = name;
			this.parent = parent;
		}
	}
}
