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

import a1.utils.MyThread;

public class ResourceLoader implements Runnable {	
	public ResourceLoader next = null;
	public boolean need_cache = false;
	private Thread th = null;
	private StreamSource src;
	private PrioQueue<Resource> queue = new PrioQueue<Resource>();
	String type = "unk";


	public ResourceLoader(StreamSource src, boolean need_cache) {
		this.src = src;
		this.type = src.GetName();
		this.need_cache = need_cache;
	}

	public ResourceLoader(StreamSource src) {
		this.src = src;
		this.type = src.GetName();
		this.need_cache = false;
	}
	
	public void run() {
		try {
			while(true) {
				Resource cur;
				synchronized(queue) {
					while((cur = queue.poll()) == null)
						queue.wait();
				}
				synchronized(cur) {
					handle(cur);
				}
				cur = null;
			}
		} catch(InterruptedException e) {
		} finally {
			synchronized(ResourceLoader.this) {
				th = null;
			}
		}
	}

	public void load(Resource res) {
		synchronized(queue) {
			queue.add(res);
			queue.notifyAll();
		}
		synchronized(ResourceLoader.this) {
			if(th == null) {
				th = new MyThread(ResourceLoader.this, "loader: "+type);
				th.setDaemon(true);
				th.start();
			}
		}
	}

	private void handle(Resource res) {
		InputStream in = null;
		try {
			res.error = null;
			res.source = src;
			try {
				try {
					if (res.name.length() > 0) {
						in = src.GetStream(res.name);
						if (need_cache) 
							res.SetCached();
						res.load(in);
						res.loading = false;
						res.notifyAll();
					}
					return;
				} catch(IOException e) {
					throw(new LoadException(e, res));
				}
			} catch(LoadException e) {
				if(next == null) {
					res.loading = false;
					res.error = e;
					res.notifyAll();
				} else {
					next.load(res);
				}
			} catch(RuntimeException e) {
				throw(new LoadException(e, res));
			}
		} finally {
			try {
				if(in != null)
					in.close();
			} catch(IOException e) {}
		}
	}
}    
