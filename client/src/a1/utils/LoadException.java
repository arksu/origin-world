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
package a1.utils;

import a1.Log;

@SuppressWarnings("serial")
public class LoadException extends RuntimeException {
	public Resource res;

	public LoadException(Throwable cause, Resource res) {
		super("Loading error " + res.toString() + " >> " + res.source, cause);
		Log.info("Loading error " + res.toString() + " >> " + res.source);
		this.res = res;
	}

	public LoadException(String msg, Throwable cause, Resource res) {
		super(msg, cause);
		Log.info(msg);
		this.res = res;
	}


	public LoadException(String msg, Resource res) {
		super(msg);
		Log.info(msg);
		this.res = res;
	}

	public LoadException(String msg) {
		super(msg);
		Log.info(msg);
	}
}