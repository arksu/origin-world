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

public class PrioQueue <E extends Prioritized> {	
	private Elem<E> list = null;
	
	@SuppressWarnings("hiding")
	private class Elem<E> {
		public Elem<E> next = null;
		public E data;
		
		public Elem(E data) {
			this.data = data;
		}
	}
	
	public void add(E e) {
		if (e == null) return;
		
		Elem<E> c = new Elem<E>(e);
		if (list != null) {
			c.next = list;
			list = c;
		} else {
			list = c;
		}
			
		
//		Elem<E> c = list;
//		Elem<E> p = null;
//		while (c != null) {
//			if (e.Priority() > c.data.Priority()) {
//				Elem<E> n = new Elem<E>(e);
//				n.next = c;
//				if (p != null)
//					p.next = n;
//				else
//					list = n;
//				return;
//			}
//			p = c;
//			c = c.next;
//		}
//		if (p != null) {
//			Elem<E> n = new Elem<E>(e);
//			p.next = n;
//			return;
//		} else {
//			list = new Elem<E>(e);
//		}
	}
	
	public E poll() {
		Elem<E> e = peek();
		if (e != null) {
			remove(e);
			return e.data;
		}
		return null;
//		if (list == null) return null;
//		Elem<E> e = list;
//		list = e.next;
//		return e.data;
	}
	
	private Elem<E> peek() {
		if (list == null)
			return null;
		int prio = list.data.Priority();
		Elem<E> e = list;
		Elem<E> ret = list;
		while (e != null) {
			if (e.data.Priority() > prio) {
				ret = e;
				prio = e.data.Priority();
			}
			e = e.next;
		}
		return ret;
	}
	
	private void remove(Elem<E> e) {
		if (list == null)
			return;
		Elem<E> c = list;
		Elem<E> p = null;
		while (c != null) {
			if (c == e) {
				if (p != null)
					p.next = c.next;
				else
					list = c.next;
				return;
			}
			p = c;
			c = c.next;
		}
	}
	
}
