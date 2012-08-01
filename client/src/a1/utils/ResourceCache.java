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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ResourceCache {
	public OutputStream store(String name) throws IOException;
	public InputStream fetch(String name) throws IOException;

	public static ResourceCache global_cache = Impl.make_cache();

	public static class Impl {
		private static ResourceCache make_cache() {
			ResourceCache ret;
			if((ret = FileCache.create()) != null)
				return(ret);
			return(null);
		}
	}
}