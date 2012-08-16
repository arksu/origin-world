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


import a1.Config;

import java.io.*;

public class FileCache implements ResourceCache {
	public File base;

	public FileCache(File base) {
		this.base = base;
	}

	public File forres(String res_name) {
		File res = base;
		String[] comp = res_name.split("/");
		for(int i = 0; i < comp.length - 1; i++) 
			res = new File(res, comp[i]);
		String ext = (res_name.equals(Config.versions_file))?"":Resource.ext;
		return(new File(res, comp[comp.length - 1] + ext));
	}
	
	public static FileCache create() {
		try {
			File dir = new File(".");
			if(!dir.exists() || !dir.isDirectory() || !dir.canRead())
				return(null);
			File base = new File(dir, Config.local_cache_dir);
			if ((!base.exists() && !base.mkdirs()) || !base.canWrite())
				return(null);
			return(new FileCache(base));
		} catch(SecurityException e) {
			return(null);
		}
	}

	public OutputStream store(String name) throws IOException {
		File nm = forres(name);
		File dir = nm.getParentFile();
		dir.mkdirs();
		return new FilterOutputStream(new FileOutputStream(nm));
	}

	public InputStream fetch(String name) throws IOException {
		return(new FileInputStream(forres(name)));
	}
}
