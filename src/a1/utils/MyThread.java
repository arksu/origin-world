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

public class MyThread extends Thread {
	public MyThread(ThreadGroup g, Runnable r, String n) {
		super((g == null)?Thread.currentThread().getThreadGroup():g, r, n);
	}

	public MyThread(Runnable r, String n) {
		this(null, r, n);
	}

	public MyThread(String n) {
		this(null, n);
	}
}
