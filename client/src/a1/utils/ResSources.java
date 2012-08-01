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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import a1.Config;
import a1.dialogs.dlg_Loading;

public class ResSources {

	static public void Init() {
		// file cache
		Resource.AddLoader(new ResourceLoader(new CacheSource()));
		// http
		Resource.AddLoader(new ResourceLoader(new HttpSource(), true));
		// jar
		try {
			Resource.AddLoader(new ResourceLoader(new JarSource()));
		} catch (Exception e) {	}
	}

	//---------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	public static class HttpSource implements StreamSource, Serializable {

		public HttpSource() {
		}

		public InputStream GetStream(String name) throws IOException {
			URL resurl;
			try {
				resurl = new URL(new URI("http", Config.resource_remote_host, Config.resource_remote_path + name + Resource.ext,"").toASCIIString());
			} catch (URISyntaxException e) {
				throw(new IOException(e));
			}
			URLConnection c;
			c = resurl.openConnection();
			c.addRequestProperty("User-Agent", Config.user_agent);
			synchronized (dlg_Loading.LoadingSize) {
				dlg_Loading.LoadingSize.put(name, c.getContentLength());
			}
			return(c.getInputStream());
		}

		public String toString() {
			return("http src=" + Config.resource_remote_host+Config.resource_remote_path);
		}

		public String GetName() {
			return "http";
		}
	}

	//---------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	public static class JarSource implements StreamSource, Serializable {
		public InputStream GetStream(String name) {
			InputStream s = Resource.class.getResourceAsStream("/res/" + name + Resource.ext);
			if(s == null)
				throw(new LoadException("no local res: " + name));
			return(s);
		}

		public String GetName() {
			return "jar";
		}

		public String toString() {
			return("jar src");
		}
	}

	//---------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	public static class CacheSource implements StreamSource, Serializable {

		public String toString() {
			return("cache src");
		}

		public InputStream GetStream(String name) throws IOException {
			if (ResourceCache.global_cache == null) 
				return null;
			try {
				return(ResourceCache.global_cache.fetch(name));
			} catch(FileNotFoundException e) {
				throw((LoadException)(new LoadException("cant find file: " + name).initCause(e)));
			}
		}

		public String GetName() {
			return "cache";
		}
	}
}
