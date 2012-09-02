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


import a1.*;
import a1.dialogs.dlg_Loading;
import a1.gui.GUI_Map;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class Resource implements Prioritized { 
	// максимальное количество типов тайлов
	public static final int TILES_MAX = 256;
	
	public static ResourceLoader loaders = null;
	public static final String ext = ".res";
	private static Map<String, Resource> cache = new TreeMap<String, Resource>();
	public static Map<String, Integer> srv_versions = new TreeMap<String, Integer>();
	public static Map<String, Texture> textures = new TreeMap<String, Texture>();
	public static Map<Texture, TextureData> texture_data = new HashMap<Texture, TextureData>();
	public static Map<String, ResDraw> draw_items = new TreeMap<String, ResDraw>();
	public static List<Resource> need_load = new LinkedList<Resource>();
	public static Map<String, ResCursor> cursors = new TreeMap<String, ResCursor>();
	public static Map<String, ResBinary> binary = new TreeMap<String, ResBinary>();
	public static Map<String, ResSound> sound = new TreeMap<String, ResSound>();
	public static ResTile[] tile_sets = new ResTile[TILES_MAX];
    public static ResTextureArray texture_array = null;
	public LoadException error;
	public StreamSource source;
	public boolean loading;
	public boolean ready;
	public String name;
	private boolean need_cache;

	private int prio = 0;
	private int ver = -1;
	private List<ResBase> items = new LinkedList<ResBase>(); 

	private Resource(String name, int ver) {
		this.name = name;
		this.ver = ver;
		error = null;
		loading = true;
		ready = false;
	}

	public int Priority() {
		return prio;
	}

	public void boostprio(int newprio) {
		if (prio < newprio)
			prio = newprio;
	}	
	
	public void SetCached() {
		need_cache = true;
	}

	static public Texture getTexture(String name) {
		return textures.get(name);
	}
	
	static public TextureData getTextureData(Texture tex) {
		TextureData t = texture_data.get(tex);
		if (t == null) {
			t = new TextureData();
			t.pixel_data = tex.getTextureData();
            t.texture = tex;
			texture_data.put(tex, t);
		}
		return t;
	}
	
	public <L extends ResBase> Collection<L> get_items(Class<L> cl) {
		checkerr();
		Collection<L> ret = new LinkedList<L>();
		for(ResBase l : items) {
			if(cl.isInstance(l))
				ret.add(cl.cast(l));
		}
		return(ret);
	}
	
	public String basename() {
		int p = name.lastIndexOf('/');
		if(p < 0)
			return(name);
		return(name.substring(p + 1));
	}

	public static Resource load(String name, int ver, int prio) {
		Resource res;
		synchronized(cache) {
			res = cache.get(name);
			if(res != null) {
				if((res.ver != -1) && (ver != -1)) {
					if(res.ver < ver) {
						res = null;
						cache.remove(name);
					} else if(res.ver > ver) {
						throw(new RuntimeException(String.format("wrong ver in <%s> %d > %d src= %s", res.name, res.ver, ver, res.source)));
					}
				} else if(ver == -1) {
					if(res.error != null) {
						res = null;
						cache.remove(name);
					}
				}
			}
			if(res != null) {
				res.boostprio(prio);
				return(res);
			}
			res = new Resource(name, ver);
			res.prio = prio;
			cache.put(name, res);
		}
		loaders.load(res);
		return(res);
	}
	
	public static Resource load(String name, int ver) {
		return(load(name, ver, 0));
	}
	
	public static Resource load(String name) {
		return(load(name, -1));
	}
	
	
	private void read_buf(InputStream in, byte[] buf, OutputStream out) throws IOException {
		int ret, off = 0;
		while(off < buf.length) {
			ret = in.read(buf, off, buf.length - off);
			dlg_Loading.LoadingProcessed += ret;
			if(ret < 0)
				throw(new LoadException("error read res <" + name+">", this));

			off += ret;
		}
		if (out != null) {
			out.write(buf);
		}
	}
	
	private static abstract class ResBase {
		protected byte[] data;
		
		public void add_data(byte[] buf) {
			data = buf;
		}
		public abstract void load() throws IOException;
		public abstract void init();
	}
	
	public class ResTexture extends ResBase {
		String name;
		
		public void load() throws IOException {
			int off = 0;
			name = read_string(data, off);
			off += 2 + name.length();
			int len = (int) Utils.uint32d(data, off);
			off += 4;
			ByteArrayInputStream in = new ByteArrayInputStream(data, off, len);
			if (Config.debug) 
				Log.info("load texture <"+name+">");
			Texture tex = TextureLoader.getTexture("PNG", in, GL11.GL_NEAREST);
			textures.put(name, tex);			
		}
		
		public void init() {
			
		}
	}
	
	public class ResBinary extends ResBase {
		String name;
		public byte[] bin_data;
		
		public void load() throws IOException {
			int off = 0;
			name = read_string(data, off);
			off += 2 + name.length();
			int len = (int) Utils.uint32d(data, off);
			off += 4;
			bin_data = new byte[len];
			System.arraycopy(data, off, bin_data, 0, len);		
			binary.put(name, this);
		}
		
		public InputStream get_stream() {
			return new ByteArrayInputStream(bin_data);
		}
		
		public void init() {
			
		}
	}	
	
	public class ResSound extends ResBase {
		String name;
		public Audio oggEffect;
		
		public void load() throws IOException {
			int off = 0;
			name = read_string(data, off);
			off += 2 + name.length();
			int len = (int) Utils.uint32d(data, off);
			off += 4;
			byte[] bin_data = new byte[len];
			System.arraycopy(data, off, bin_data, 0, len);
			InputStream in = new ByteArrayInputStream(bin_data);
			oggEffect = AudioLoader.getAudio("OGG", in);
			sound.put(name, this);
		}
		
		public void init() {
			
		}
	}	
	
	public class ResCursor extends ResBase {
		String name;
		public int offx;
		public int offy;
		public byte[] png_data;
	
		public void load() throws IOException {
			int off = 0;
			name = read_string(data, off);
			off += 2 + name.length();
			
			offx = Utils.int16d(data, off); off += 2;
			offy = Utils.int16d(data, off); off += 2;
			
			int len = (int) Utils.uint32d(data, off);
			off += 4;
			png_data = new byte[len];
			System.arraycopy(data, off, png_data, 0, len);
			cursors.put(name, this);
		}
		
		public void init() {
			
		}
	}
	
	public class ResDraw extends ResBase {
		public String name;
		public int width;
		public int height;
		public int offx;
		public int offy;
		public List<ResLayer> layers = new ArrayList<ResLayer>();
		
		public void load() throws IOException {
			int off = 0;
			name = read_string(data, off);
			off += 2 + name.length();
			width = Utils.int16d(data, off); off += 2;
			height = Utils.int16d(data, off); off += 2;
			offx = Utils.int16d(data, off); off += 2;
			offy = Utils.int16d(data, off); off += 2;
			int count = Utils.uint16d(data, off); off += 2;
			byte [] buf;
			boolean have_error = false;
			while (count > 0 ) {
				count--;
				// read res name
				String type = read_string(data, off);
				off += 2 + type.length();
				// data size
				int size = (int) Utils.uint32d(data, off); off += 4;
				// data
				buf = new byte[size];
				System.arraycopy(data, off, buf, 0, size);
				off += size;
				ResLayer l = (ResLayer) create_res(type, buf);
				
				if (l != null) {
					l.owner = this;
					l.load();
					if (!l.is_error)
						layers.add(l);
					else
						have_error = true;
				} else {
					Log.info("res draw unkown type="+type);
				}
			}
			if (!have_error) {
				draw_items.put(name, this);
			}
			buf = null;
		}

		public void init() {} 
		
	}

    public abstract class ResLayer extends ResBase {
		// offset coord in drawable
		public int offx;
		public int offy;
		// depth layer
		public int z = 0;
		// additional depth
		public int addz = 0;
		// size
		public int w;
		public int h;
        // custom tag
        public int tag = 0;

		public ResDraw owner;
		public boolean is_error = false;
		
		public void update() {}
		public abstract void render(int x, int y, long timer);
		public boolean check_hit(int x, int y, long timer) {return false;}
	}
	
	public class ResWeightItem extends ResBase {
		int weight;
		public boolean is_error = false;
		public List<ResLayer> items = new LinkedList<ResLayer>();
	
		public void render(int x, int y, long timer, int seed) {
		}
		

		public void load() throws IOException {
			int off = 0;
			weight = Utils.int16d(data, off); off += 2;

			int count = Utils.int16d(data, off); off += 2;
			byte [] buf;
			while (count > 0 ) {
				count--;
				// read res name
				String type = read_string(data, off);
				off += 2 + type.length();
				// data size
				int size = (int) Utils.uint32d(data, off); off += 4;
				// data
				buf = new byte[size];
				System.arraycopy(data, off, buf, 0, size);
				off += size;
				ResLayer l = null;
				ResBase r = create_res(type, buf);
				if (r instanceof ResLayer) {
					l = (ResLayer)r; 
				} else {
					Log.info("unknown weight list item type="+type);
					is_error = true;
				}
				
				if (l != null) {
					l.load();
					if (!l.is_error)
						items.add(l);
					else
						is_error = true;
				}
			}

		}

		public void init() {
		}
		
	}
	
	public class ResWeightList extends ResLayer {
		public WeightList<ResWeightItem> items = new WeightList<ResWeightItem>();

		public void render(int x, int y, long timer) {
		}

		public void load() throws IOException {
			int off = 0;
			
			int count = Utils.int16d(data, off); off += 2;
			byte [] buf;
			while (count > 0 ) {
				count--;
				// read res name
				String type = read_string(data, off);
				off += 2 + type.length();
				// data size
				int size = (int) Utils.uint32d(data, off); off += 4;
				// data
				buf = new byte[size];
				System.arraycopy(data, off, buf, 0, size);
				off += size;
				ResWeightItem l = null;
				if (type.equals("item")) {
					l = new ResWeightItem();
					l.add_data(buf);
					l.load();
					if (!l.is_error)
						items.add(l, l.weight);
					else
						is_error = true;
				} else {
					Log.info("unknown weight list item type="+type);
					is_error = true;
				}
			}
		}

		public void init() {
		}
		
	}
	
	public class ResAnim extends ResLayer {
		public int time;
		public long start_time = System.currentTimeMillis();
		public int frames_count;
		public List<ResFrame> frames = new ArrayList<ResFrame>();
		
		public int get_current_frame() {
			return get_current_frame(-1);
		}
		
		public int get_current_frame(long atimer) {
			long cur_time;
			
			if (atimer < 0) {
				long timer = System.currentTimeMillis() - start_time;
				cur_time = timer - (timer / time) * time;
			}
			else cur_time = atimer - (atimer / time) * time;
			float frame_time = time/frames_count;
			int r = (int)(cur_time/frame_time);
			if (r > frames_count-1) r = frames_count-1;
			return r;
		}
		
		public void load() throws IOException {
			int off = 0;
			z = Utils.int16d(data, off); off += 2;
            tag = Utils.int16d(data, off); off += 2;
			addz = Utils.int16d(data, off); off += 2;


			offx = Utils.int16d(data, off); off += 2;
			offy = Utils.int16d(data, off); off += 2;
			
			time = Utils.int16d(data, off); off += 2;
			frames_count = Utils.int16d(data, off); off += 2;		
			
			int count = frames_count;
			byte [] buf;
			while (count > 0 ) {
				count--;
				// read res name
				String type = read_string(data, off);
				off += 2 + type.length();
				// data size
				int size = (int) Utils.uint32d(data, off); off += 4;
				// data
				buf = new byte[size];
				System.arraycopy(data, off, buf, 0, size);
				off += size;
				ResFrame f = null;
				ResBase r = create_res(type, buf);
				if (r instanceof ResFrame) {
					f = (ResFrame)r; 
				} else {
					is_error = true;
				}
				
				if (f != null) {
					f.load();
					w = f.tw;
					h = f.th;
					if (!f.is_error)
						frames.add(f);
					else
						is_error = true;
				}
			}
		}
		
		public void init() {	
		}
		
		public void update() {
			
		}
		public boolean check_hit(int x, int y, long timer) {
			ResFrame frame = frames.get(get_current_frame(timer));
			int a = frame.spr.getPixelAlpha(x-offx, y-offy);
			return a > 0;
		}
		
		public void render(int x, int y, long timer) {
			ResFrame frame = frames.get(get_current_frame(timer));
			frame.spr.draw(x+offx, y+offy);
		}
	}
	
	public class ResFrame extends ResBase {
		public String tex_name;
		// texture rect
		public int tw;
		public int th;
		public int tx;
		public int ty;
		public boolean is_error = false;
		public Sprite spr;
		
		public void load() throws IOException {
			int off = 0;
			tex_name = read_string(data, off);
			is_error = !textures.containsKey(tex_name);
			off += 2 + tex_name.length();
						
			tx = Utils.int16d(data, off); off += 2;
			ty = Utils.int16d(data, off); off += 2;
			tw = Utils.int16d(data, off); off += 2;
			th = Utils.int16d(data, off); off += 2;
			
			spr = new Sprite(tex_name, 0, 0, tx, ty, tw, th);
			is_error = spr.isError();
		}
		
		public void init() {
		}
	}
	
	public class ResSprite extends ResLayer {
		// texture rect
		public int tw;
		public int th;
		public int tx;
		public int ty;
		// texture
		public String tex_name;
		public Sprite spr;
		
		public void render(int x, int y, long timer) {
			spr.draw(x+offx, y+offy);
		}
		
		public void load() throws IOException {
			int off = 0;
			tex_name = read_string(data, off);
			off += 2 + tex_name.length();
			
			z = Utils.int16d(data, off); off += 2;
            tag = Utils.int16d(data, off); off += 2;
			addz = Utils.int16d(data, off); off += 2;

			offx = Utils.int16d(data, off); off += 2;
			offy = Utils.int16d(data, off); off += 2;
			
			tx = Utils.int16d(data, off); off += 2;
			ty = Utils.int16d(data, off); off += 2;
			tw = Utils.int16d(data, off); off += 2;
			th = Utils.int16d(data, off); off += 2;
			w = tw;
			h = th;
			
			spr = new Sprite(tex_name, 0, 0, tx, ty, tw, th);
			is_error = spr.isError();
		}
		public boolean check_hit(int x, int y, long timer) {
			int a = spr.getPixelAlpha(x-offx, y-offy);
			return a > 0;
		}
		
		public void init() {			
		}
	}
	
	public static class ResTile extends ResBase {
		public WeightList<Sprite> ground = new WeightList<Sprite>();
		public WeightList<Sprite> wall = new WeightList<Sprite>();
		public WeightList<Sprite>[] border_trans; 
		public WeightList<Sprite>[] corner_trans; 
		public String tex_name;
		int id;
		
		@SuppressWarnings("unchecked")
		public ResTile(int id, String tex_name, Element node) {
			this.id = id;
			this.tex_name = tex_name;
			
			int tx, ty, idx, w;
			String s;
			NodeList nList;
			//-----------------------------------
			nList = node.getElementsByTagName("ground");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					s = eElement.getAttributeNode("pos").getNodeValue();
					String[] params = s.split(",");
					tx = Integer.parseInt(params[0].trim());
			        ty = Integer.parseInt(params[1].trim());
			        
			        if (eElement.hasAttribute("w")) {
			        	w = Integer.parseInt(eElement.getAttributeNode("w").getNodeValue());
			        } else w = 10;
			        
			        Sprite spr = new Sprite(tex_name, tx, ty,GUI_Map.tile_tex_size.x, GUI_Map.tile_tex_size.y);
					ground.add(spr, w);
				}
			}
			//----------------------------------
			nList = node.getElementsByTagName("border");
			if (nList.getLength() > 0) {
				border_trans = new WeightList[15];
				for(int i = 0; i < 15; i++) {
					border_trans[i] = new WeightList<Sprite>();
				}
			}
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					s = eElement.getAttributeNode("pos").getNodeValue();
					String[] params = s.split(",");
					tx = Integer.parseInt(params[0].trim());
			        ty = Integer.parseInt(params[1].trim());
			        idx = Integer.parseInt(eElement.getAttributeNode("idx").getNodeValue());
			        
			        if (eElement.hasAttribute("w")) {
			        	w = Integer.parseInt(eElement.getAttributeNode("w").getNodeValue());
			        } else w = 10;
			        
			        Sprite spr = new Sprite(tex_name, tx, ty,GUI_Map.tile_tex_size.x, GUI_Map.tile_tex_size.y);
			        border_trans[idx-1].add(spr, w);
				}
			}
			//------------------------------------------
			nList = node.getElementsByTagName("corner");
			if (nList.getLength() > 0) {
				corner_trans = new WeightList[15];
				for(int i = 0; i < 15; i++) {
					corner_trans[i] = new WeightList<Sprite>();
				}
			}
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					s = eElement.getAttributeNode("pos").getNodeValue();
					String[] params = s.split(",");
					tx = Integer.parseInt(params[0].trim());
			        ty = Integer.parseInt(params[1].trim());
			        idx = Integer.parseInt(eElement.getAttributeNode("idx").getNodeValue());
			        
			        if (eElement.hasAttribute("w")) {
			        	w = Integer.parseInt(eElement.getAttributeNode("w").getNodeValue());
			        } else w = 10;
			        
			        Sprite spr = new Sprite(tex_name, tx, ty,GUI_Map.tile_tex_size.x, GUI_Map.tile_tex_size.y);
			        corner_trans[idx-1].add(spr, w);
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		public void load() throws IOException {
			int off = 0;
			id = Utils.int16d(data, off); off += 2;
			tex_name = read_string(data, off);
			off += 2 + tex_name.length();
			
			int w, tx, ty, idx;
			int count = Utils.int16d(data, off); off += 2;
			while (count > 0) {
				count--;
				w = Utils.int16d(data, off); off += 2;
				tx = Utils.int16d(data, off); off += 2;
				ty = Utils.int16d(data, off); off += 2;
				Sprite s = new Sprite(tex_name, tx, ty,GUI_Map.tile_tex_size.x, GUI_Map.tile_tex_size.y);
				ground.add(s, w);
			}
			count = Utils.int16d(data, off); off += 2;
			while (count > 0) {
				count--;
				w = Utils.int16d(data, off); off += 2;
				tx = Utils.int16d(data, off); off += 2;
				ty = Utils.int16d(data, off); off += 2;
				Sprite s = new Sprite(tex_name, tx, ty,GUI_Map.tilewall_tex_size.x, GUI_Map.tilewall_tex_size.y);
				wall.add(s, w);
			}
			count = Utils.int16d(data, off); off += 2;
			if (count > 0) {
				border_trans = new WeightList[15];
				for(int i = 0; i < 15; i++) {
					border_trans[i] = new WeightList<Sprite>();
				}
			}
			while (count > 0) {
				count--;
				idx = Utils.int16d(data, off); off += 2;
				w = Utils.int16d(data, off); off += 2;
				tx = Utils.int16d(data, off); off += 2;
				ty = Utils.int16d(data, off); off += 2;
				Sprite s = new Sprite(tex_name, tx, ty,GUI_Map.tile_tex_size.x, GUI_Map.tile_tex_size.y);
				border_trans[idx-1].add(s, w);
			}
			count = Utils.int16d(data, off); off += 2;
			if (count > 0) {
				corner_trans = new WeightList[15];
				for(int i = 0; i < 15; i++) {
					corner_trans[i] = new WeightList<Sprite>();
				}
			}
			while (count > 0) {
				count--;
				idx = Utils.int16d(data, off); off += 2;
				w = Utils.int16d(data, off); off += 2;
				tx = Utils.int16d(data, off); off += 2;
				ty = Utils.int16d(data, off); off += 2;
				Sprite s = new Sprite(tex_name, tx, ty,GUI_Map.tile_tex_size.x, GUI_Map.tile_tex_size.y);
				corner_trans[idx-1].add(s, w);
			}
			
			tile_sets[id] = this;
		}
		public void init() {
		}
	}

    public class ResTextureArray extends ResBase {
        int width, height, count, alpha_count;
        Texture tex, alpha_tex; // <--- временные текстуры. их заменить на нужные объекты в которые грузить из потока PNG

        public void load() throws IOException {
            int off = 0;
            width = Utils.int16d(data, off); off += 2;
            height = Utils.int16d(data, off); off += 2;
            count = Utils.int16d(data, off); off += 2;
            alpha_count = Utils.int16d(data, off); off += 2;

            int len = (int) Utils.uint32d(data, off);
            off += 4;
            ByteArrayInputStream in = new ByteArrayInputStream(data, off, len);
            tex = TextureLoader.getTexture("PNG", in);
            off += len;

            len = (int) Utils.uint32d(data, off);
            off += 4;
            in = new ByteArrayInputStream(data, off, len);
            alpha_tex = TextureLoader.getTexture("PNG", in);

            texture_array = this;
        }

        public void init() {

        }
    }
	
	public void load(InputStream in) throws IOException {
		synchronized (dlg_Loading.LoadingName) {
			dlg_Loading.LoadingName = name;
		}
		dlg_Loading.LoadingProcessed = 0;
		
		OutputStream out = null;
		Log.info("begin load res <"+name+"> from <"+source+">");
		if (need_cache && ResourceCache.global_cache != null) {
			// need to save in cache
			out = ResourceCache.global_cache.store(name);
		}
		// signature
		byte[] buf = new byte[2];
		read_buf(in, buf, out);
		if (buf[0] != 37 || buf[1] != 75) {
			throw(new LoadException("wrong resource", this));
		}
		// ver
		buf = new byte[2];
		read_buf(in, buf, out);
		int v = Utils.uint16d(buf, 0);
		if(ver == -1) {
			ver = v;
		} else {
			if(v != ver && !Config.local_start) {
				int vv = ver;
				ver = v;
				throw(new LoadException("invalid <"+name+"> ver=" + v + "!=" + vv, this));
			}
		}
		if (srv_versions != null && srv_versions.size() > 0 && !Config.local_start) {
			v = srv_versions.get(name);
			if (v != ver) {
				int vv = ver;
				ver = v;
				throw(new LoadException("invalid <"+name+"> (srv_ver=" + v + " <> ver=" + vv+")", this));
			}			
		}
		// count
		buf = new byte[2];
		read_buf(in, buf, out);
		int count = Utils.uint16d(buf, 0);

		while (count > 0) {
			count--;
			// read res name
			String type = read_string(in, out);
			// data size
			buf = new byte[4];
			read_buf(in, buf, out);
			int size = (int) Utils.uint32d(buf, 0);
			// data
			buf = new byte[size];
			read_buf(in, buf, out);
			
			ResBase r = create_res(type, buf);

			if (r != null) {
				items.add(r);
			}
		}
		
		// after loading
		if (out != null) {
			out.flush();
			out.close();
		}
		synchronized (need_load) {
			need_load.add(this);
		}
		Log.info("end load res <"+name+"> from <"+source+">");
	}
	
	public ResBase create_res(String type, byte[] data) {
		ResBase r;
		if (type.equals("texture")) 		r = new ResTexture(); else 
		if (type.equals("sprite")) 			r = new ResSprite(); else
		if (type.equals("draw")) 			r = new ResDraw(); else	
		if (type.equals("frame")) 			r = new ResFrame(); else
		if (type.equals("anim")) 			r = new ResAnim(); else	
		if (type.equals("cursor")) 			r = new ResCursor(); else
		if (type.equals("bin")) 			r = new ResBinary(); else
		if (type.equals("sound")) 			r = new ResSound(); else
		if (type.equals("weightlist")) 		r = new ResWeightList(); else
        if (type.equals("texture_array"))	r = new ResTextureArray(); else
		r = null;
		
		if (r != null) {
			r.add_data(data);
		} else {
			Log.info("unknown res type="+type);
		}
		return r;
	}
	
	static public void AddLoader(ResourceLoader new_loader) {
		synchronized(Resource.class) {
			if(loaders == null) {
				loaders = new_loader;
			} else {
				ResourceLoader l = loaders;
				while (l.next != null)
					l = l.next;
				l.next = new_loader;
			}
		}
	}

	private void checkerr() {
		if (error != null)
			throw(new RuntimeException("error in res <" + name + "> ver=" + ver + " src=" + source, error));
	}

	public String toString() {
		return ("<" +name + "> ver= "+ver+" prio="+prio);
	}
	
	private String read_string(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[2];
		read_buf(in, buf, out);
		int count = Utils.uint16d(buf, 0);
		
		buf = new byte[count];
		read_buf(in, buf, out);
		
		return new String(buf,"US-ASCII");	
	}
	
	private static String read_string(byte[] buf, int off) throws IOException {
		int count = Utils.uint16d(buf, off);
		return new String(buf, off+2, count, "US-ASCII");
	} 
		
	static public void load_versions() {
		try {
			Log.info("try load res versions...");
			InputStream in;
			// если специальный локальный старт (нужно для дебага без подключения к сети)
			if (Config.local_start) { 
				// возьмем файл ресурсов из кеша. куда его надо предварительно положить ручками
				in = ResourceCache.global_cache.fetch(Config.versions_file);
			} else {
				// грузим версии с сервера
				URL xmlurl = new URL(new URI("http", Config.resource_remote_host, Config.resource_remote_path + Config.versions_file,"").toASCIIString());
				URLConnection c;
				c = xmlurl.openConnection();
				c.addRequestProperty("User-Agent", Config.user_agent);
				in = c.getInputStream();
				
				
				// сохраним файл версий в кэш
				FileCache  fc = FileCache.create();
				if (fc == null) Main.GlobalError("ERROR: access to cache dir");
				File versions_file = fc.forres(Config.versions_file);
				
				OutputStream out = new FileOutputStream(versions_file);
				byte buf[] = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0)
					out.write(buf, 0, len);
				out.close();
				in.close();
				
				// возьмем файл ресурсов из кеша. куда его надо предварительно положить ручками
				in = ResourceCache.global_cache.fetch(Config.versions_file);
			}
		    Log.info("versions finded. parse...");
		    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		    Document doc = dBuilder.parse(in);
		    doc.getDocumentElement().normalize();
		 
		    NodeList nList = doc.getElementsByTagName("file");
		    String name, ver;
		    for (int temp = 0; temp < nList.getLength(); temp++) {
		    	 
		        Node nNode = nList.item(temp);	    
		        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		  
		           Element eElement = (Element) nNode;
		           
		           name = eElement.getAttributeNode("name").getNodeValue();
		           ver = eElement.getAttributeNode("ver").getNodeValue();
		           srv_versions.put(name, new Integer(ver));
		         }
		     }
		    Log.info("res versions success loaded");

		} catch (Exception e) {
			e.printStackTrace();
			Main.GlobalError("ERROR: failed load versions list");
		}
	}
	
	static public void update_client() {
		try {
		Log.info("try load new client...");
		// грузим жар файл с сервера
		URL xmlurl = new URL(new URI("http", Config.resource_remote_host, Config.update_client,"").toASCIIString());
		URLConnection c;
		c = xmlurl.openConnection();
		c.addRequestProperty("User-Agent", Config.user_agent);
		InputStream in = c.getInputStream();
		
		File f=new File("client.jar");
	    OutputStream out=new FileOutputStream(f);
		
		byte buf[]=new byte[4096];
	    int len;
	    while((len=in.read(buf))>0)
	    	out.write(buf,0,len);
	    out.close(); 
	    in.close();
	    
	    Log.info("client updated!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.info("ERROR: failed update client");
			System.exit(-1);
		}
	}
	
	static public void update_load() {
		synchronized (need_load) {
			if (need_load.size() == 0) return;
			
			for (Resource r : need_load) {
				try {
					Log.info("begin post load <"+r.name+">");
					for (ResBase rb : r.items) {
							rb.load();
					}
					r.ready = true;
					Log.info("end post load <"+r.name+">");
				} catch (Exception e) {
					r.error = new LoadException("failed post load <"+r.name+">");
					r.ready = true;
				}
			}
			need_load.clear();
		}
	}
	
}