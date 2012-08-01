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
package a1.gui;

import java.nio.ByteBuffer;
import a1.Log;

import java.nio.IntBuffer;
import java.util.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.newdawn.slick.Color;

import a1.Config;
import a1.Coord;
//import a1.Drawable;
import a1.Drawable;
import a1.FlyText;
import a1.Input;
import a1.KinInfo; 
import a1.LineMove;
import a1.MapCache;
import a1.Obj;
import a1.ObjCache;
import a1.ObjSay;
import a1.Player;
import a1.Rect;
import a1.Render2D;
import a1.RenderPart;
import a1.Sprite;
import a1.Tile;
import static a1.MapCache.TILE_SIZE;
//import a1.utils.Resource;
//import a1.utils.ResourceCache;
import static a1.net.NetGame.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2i;


public class GUI_Map extends GUI_Control {
	///////////////
	public static boolean render_rects = false;
	public static boolean render_lightmap = false;
	/////////////////
	public static final Coord tile_tex_size = new Coord(50, 25);
	public static final Coord tilewall_tex_size = new Coord(64, 20);
	public int RenderedTiles = 0;
	public int RenderedObjects = 0;
	public static Map<String, Class<? extends Camera>> cameras = new TreeMap<String, Class<? extends Camera>>();
	public List<RenderPart> render_parts = new ArrayList<RenderPart>();
	public RenderPart[] clickable;
	public Obj mouse_in_object = null;
	public Coord mouse_tile_coord = Coord.z;
	public Coord mouse_map_pos = Coord.z;
	public Obj place_obj = null;
	
	public Camera camera = null;
	public Coord mc = Coord.z;
	public Coord player_coord = Coord.z;
    
	
	// отрисован ли уже игрок
	private boolean player_rendered;
	// рект игрока на экране
	private Rect player_rect;
	public static final float OVERLAY_K = 0.33f;
	public static boolean ignore_overlapped = false;
	
	private long last_time_mouse_send = 0;
    private boolean mouse_left_pressed = false;

	static {
		cameras.put("fixed", FixedCam.class);
	}

	public GUI_Map(GUI_Control parent) {
		super(parent);
		GUI.map = this;
		SetSize(Config.ScreenWidth, Config.ScreenHeight);
		set_camera("fixed");
	}

	// обработчик апдейта
	public void DoUpdate() {
		mouse_map_pos = s2m(gui.mouse_pos.add(viewoffset(size, this.mc).inverse()));
		mouse_tile_coord = tilify(mouse_map_pos);
		
		FlyText.Update();
		
		Coord my = my_coord();
		if (!player_coord.equals(my)) {
			player_coord = my;
//			if (camera != null) {
//				camera.reset();
//			}
		}

        // move by mouse
        if (
                Config.move_inst_left_mouse &&
                MouseInMe() &&
                mouse_left_pressed &&
                (System.currentTimeMillis() - last_time_mouse_send > 500)
                ) {
            SendMapClick(Input.MB_LEFT);
        }
		
		if (Input.KeyHit(Keyboard.KEY_HOME) && camera != null) {
			camera.reset();
		}
		if (Input.KeyHit(Keyboard.KEY_G) && Input.isCtrlPressed()) {
			Config.tile_grid = !Config.tile_grid;
		}
		
		if (camera != null) {
			if (get_player() != null)
				camera.setpos(this, get_player(), size);
			camera.update(this);
		}
		
		if (Input.KeyHit(org.newdawn.slick.Input.KEY_F2)) 
			render_rects = !render_rects;
				
//		if (Input.KeyHit(org.newdawn.slick.Input.KEY_F4)) 
//			render_lightmap = !render_lightmap;
		
		if (place_obj != null)
			place_obj.pos = new Coord(mouse_tile_coord);
		
		PrepareRenderParts();
		//PrepareTiles();
		// need sort parts in render list
		Collections.sort(render_parts, RenderPart.part_cmp);
	}	

	// обработчик рендера
	public void DoRender() {
		if (render_lightmap) CreateLightMap();
		
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		RenderedObjects = 0;
		DrawTiles();
		DrawObjects();
		DrawFlyText();
		
		if (render_lightmap) RenderLightMap();
		
		if (Config.debug) {
			Render2D.Text("", 10, 200, "mc="+mc.toString());
			if (mouse_in_object != null)
				Render2D.Text("", 10, 220, "obj="+mouse_in_object.toString());
			if (mouse_map_pos != null)
				Render2D.Text("", 10, 240, "mouse_map_pos="+mouse_map_pos.toString());
			if (mouse_tile_coord != null) {
				Render2D.Text("", 10, 260, "mouse_tile_coord="+mouse_tile_coord.toString());
				Coord tc = mouse_tile_coord.div(TILE_SIZE);
				Render2D.Text("", 10, 280, "mouse_tile="+tc.toString());
				Render2D.Text("", 10, 300, "mouse_tile_level="+Integer.toString(Tile.getlevel(tc)));
			}
			
			if (player_rect != null)
				Render2D.Text("", 10, 320, "player_rect="+player_rect.toString());
		}
	}

	// обработчик нажатия кнопок мыши
	public boolean DoMouseBtn(int btn, boolean down) {
        if (btn == Input.MB_LEFT) mouse_left_pressed = MouseInMe() && down;
		if (!MouseInMe() &&  down) return false;
		
		// обработаем камеру
		if (camera != null) {
			if (camera.DoMouseBtn(this, gui.mouse_pos, mc, btn, down)) return true;
		}

		// если и в камеру не попали - шлем клик по карте на сервер
		if (down) {
			mouse_map_pos = s2m(gui.mouse_pos.add(viewoffset(size, this.mc).inverse()));
			if (mouse_map_pos != null) SendMapClick(btn);
			return true;
		}
		
		return false;
	}
    
    private void SendMapClick(int btn) {
        last_time_mouse_send = System.currentTimeMillis();
        SEND_map_click(
                place_obj!=null?0:(mouse_in_object!=null?mouse_in_object.id:0),
                place_obj!=null?mouse_tile_coord.x:mouse_map_pos.x,
                place_obj!=null?mouse_tile_coord.y:mouse_map_pos.y,
                gui.mouse_pos.x, gui.mouse_pos.y,
                btn, Input.GetKeyState()
        );
    }

	// CAMERAS ==============================================================

	public static class Camera {
		Coord last_mouse_pos = Coord.z;

		public void setpos(GUI_Map mv, Obj player, Coord sz) {}

		public boolean DoMouseBtn(GUI_Map mv, Coord sc, Coord mc, int button, boolean down) {
			return(false);
		}

		public void update(GUI_Map mv) {
			if (!last_mouse_pos.equals(mv.gui.mouse_pos)) {
				last_mouse_pos = new Coord(mv.gui.mouse_pos);
				mouse_move(mv, last_mouse_pos);
			}
		}

		public void mouse_move(GUI_Map mv, Coord sc) {}

		public void moved(GUI_Map mv) {}

		//		public static void borderize(GUI_Map mv, Obj player, Coord sz, Coord border) {}

		public void reset() {}
	}

	private static abstract class DragCam extends Camera {
		Coord begin_drag, mo;
		boolean dragging = false;

		public boolean DoMouseBtn(GUI_Map mv, Coord sc, Coord mc, int button, boolean down) {
			if (button == Input.MB_MIDDLE) 
				if (down) {
					mv.gui.SetMouseGrab(mv);
					begin_drag = sc;
					mo = null;
					dragging = true;
					return (true);
				} else {
					if (dragging) {
						mv.gui.SetMouseGrab(null);
						dragging = false;
						if(mo == null) {
							mv.mc = mc;
							moved(mv);
						}
						return (true);
					}
				}
			return(false);
		}

		public void mouse_move(GUI_Map mv, Coord sc) {
			if(dragging) {
				Coord off = sc.add(begin_drag.inverse());
				if((mo == null) && (off.dist(Coord.z) > 5))
					mo = mv.mc;
				if(mo != null) {
					mv.mc = mo.add(s2m(off).inverse());
					moved(mv);
				}
			}
		}

	}

	static class FixedCam extends DragCam {
		private Coord off = Coord.z;
		private boolean setoff = false;

		public void setpos(GUI_Map mv, Obj player, Coord sz) {
			if(setoff) {
				off = mv.mc.add(player.getpos().inverse());
				setoff = false;
			}
			mv.mc = player.getpos().add(off);

		}

		public void moved(GUI_Map mv) {
			setoff = true;
		}
		public void reset() {
			off = Coord.z;
		}
	}

	// CAMERAS END ==============================================================

	public void set_place(String name) {
		if (name.equals("none")) 
			place_obj = null;
		else {
			place_obj = new Obj(mouse_tile_coord);
			place_obj.setattr(new Drawable(place_obj, name));
		}
	}
	
	public static Coord m2s(Coord c) {
		return(new Coord((c.x * 2) - (c.y * 2), c.x + c.y));
	}

	public static Coord s2m(Coord c) {
		return(new Coord((c.x / 4) + (c.y / 2), (c.y / 2) - (c.x / 4)));
	}
    public static Coord tilify(Coord c) {
    	c = c.div(TILE_SIZE);
    	c = c.mul(TILE_SIZE);
    	c = c.add(TILE_SIZE /2);
    	return(c);
    }
    
	static Coord viewoffset(Coord sz, Coord vc) {
		return(m2s(vc).inverse().add(sz.div(2)));
	}	

	public void set_camera(String name) {
		Class<? extends Camera> ct = cameras.get(name);
		try {
			try {
				camera =ct.newInstance();
			} catch(IllegalAccessException e) {
			}
		} catch(InstantiationException e) {
			throw(new Error(e));
		}
		camera = new FixedCam();
	}
	
	private Obj get_player() {
		return ObjCache.get(Player.CharID);
	}
	
	private Coord my_coord() {
		Obj p = get_player();
		if (p != null) 
			return p.getpos();
		return Coord.z;
	}

//	public void PrepareTiles() {
//		int x, y, i;
//		int stw, sth;
//		Coord oc, tc, ctc, sc;
//
//		RenderedTiles = 0;
//		Render2D.ChangeColor();
//		Sprite.setStaticColor();
//		
//		// размеры тайла для расчетов
//		stw = (TILE_SIZE * 4)-2;
//		sth = TILE_SIZE * 2;
//		// оффсет центра экрана
//		oc = viewoffset(size, mc);
//		// начальный тайл для отрисовки
//		tc = mc.div(TILE_SIZE);
//		tc.x += -(size.x / (2 * stw)) - (size.y / (2 * sth)) - 2;
//		tc.y += (size.x / (2 * stw)) - (size.y / (2 * sth));
//		
//		// определяем область видимости тайлов
//		// создаем массив объектов которые находятся в области видимости
//		
//		// проходим построчно по тайлам
//		// для каждого тайла:
//		for(y = 0; y < (size.y / sth) + 13; y++) {
//			for(x = 0; x < (size.x / stw) + 3; x++) {
//				for(i = 0; i < 2; i++) {
//					// координаты текущего тайла
//					ctc = tc.add(new Coord(x + y, -x + y + i));
//					// экранные координаты тайла
//					sc = m2s(ctc.mul(TILE_SIZE)).add(oc);
//					sc.x -= TILE_SIZE * 2;
//					
//					render_parts.add(new RenderPart(ctc, oc));
//				}
//			}
//		}
//	}
	
	public void DrawTiles() {
		int x, y, i;
		int stw, sth;
		Coord oc, tc, ctc, sc;

		RenderedTiles = 0;
		Render2D.ChangeColor();
		Sprite.setStaticColor();
		
		// размеры тайла для расчетов
		stw = (TILE_SIZE * 4)-2;
		sth = TILE_SIZE * 2;
		// оффсет центра экрана
		oc = viewoffset(size, mc);
		// начальный тайл для отрисовки
		tc = mc.div(TILE_SIZE);
		tc.x += -(size.x / (2 * stw)) - (size.y / (2 * sth)) - 2;
		tc.y += (size.x / (2 * stw)) - (size.y / (2 * sth));
		
		// определяем область видимости тайлов
		// создаем массив объектов которые находятся в области видимости
		
		// проходим построчно по тайлам
		// для каждого тайла:
		for(y = 0; y < (size.y / sth) + 13; y++) {
			for(x = 0; x < (size.x / stw) + 3; x++) {
				for(i = 0; i < 2; i++) {
					// координаты текущего тайла
					ctc = tc.add(new Coord(x + y, -x + y + i));
					// экранные координаты тайла
					sc = m2s(ctc.mul(TILE_SIZE)).add(oc);
					sc.x -= TILE_SIZE * 2;
					// выводим тайл
					// выводим объекты на этом тайле
					drawtile(ctc, sc);
				}
			}
		}
		
		// если надо выводим сетку тайлов
		if (Config.tile_grid) {
			Render2D.Disable2D();
			for (y = 0; y < (size.y / sth) + 2; y++) {
				for (x = 0; x < (size.x / stw) + 3; x++) {
					for (i = 0; i < 2; i++) {
						ctc = tc.add(new Coord(x + y, -x + y + i));
						sc = m2s(ctc.mul(TILE_SIZE)).add(oc);
						drawtile_grid(ctc, sc);
					}
				}
			}
			Render2D.Enable2D();
		}
	}
	
	public void PrepareRenderParts() {
		Coord oc = viewoffset(size, mc);
		render_parts.clear();
        try {
            synchronized (ObjCache.objs) {
                Collection<Obj> os = ObjCache.objs.values();
                for (Obj o : os) {
                    o.prepare_draw(oc);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		if (place_obj != null) {
			//Coord dc = m2s(place_obj.getpos()).add(oc);
			place_obj.prepare_draw(oc);
		}
		
	}
	
	// влезает ли объект на экран.
	public boolean CheckDrawCoord(Coord dc) {
		return !(dc.x < -100 || dc.y < -100 || dc.x > Config.ScreenWidth+100 || dc.y > Config.ScreenHeight + 100);
	}
	
	public Color get_render_part_color(RenderPart p) {
		// если ставим объект - делаем его слегка прозрачным
		if (p.owner!=null) {
			if (p.owner == place_obj)
				return new Color(1.0f,1.0f,1.0f,0.8f);
			else
				// подсвечиваем зеленым объект под мышкой
				if (p.owner == mouse_in_object) 
					return new Color(0,1.0f,0,1.0f); 
				else
					// для всех остальных выводим как есть
					if (p.owner.shp > 0) {
						float k = (float)p.owner.hp / (float)p.owner.shp; 
						return new Color(1.0f,k,k,1.0f);
					} else {
						return new Color(1.0f,1.0f,1.0f,1.0f);
					}
		} else 
			// для всех остальных выводим как есть
			return new Color(1.0f,1.0f,1.0f,1.0f);
	}
	
	public void DrawObjects() {

		// render all parts
		if (!render_rects) {
			player_rendered = false;
			player_rect = null;
			for (RenderPart p : render_parts) {
				Color col = get_render_part_color(p);
				
				if (Config.hide_overlapped && !p.ignore_overlap) {
					if (player_rendered && !p.is_my_player) {
						if (player_rect.is_intersect(p.screen_coord.x, p.screen_coord.y, p.size.x, p.size.y)) {
							p.is_overlapped = true;
							if (col == null) 
								col = new Color(1.0f,1.0f,1.0f,OVERLAY_K);
							else
								col.a = col.a * OVERLAY_K;
						}
					}
				}
				
				Sprite.setStaticColor(col);
				
				p.render();
				
				if (Config.hide_overlapped && !p.ignore_overlap) {
					if (p.is_my_player && p.z > 1) {
						if (!player_rendered)
							player_rect = new Rect(p.screen_coord, p.size);
						else
							player_rect = player_rect.Union(p.screen_coord, p.size);
						player_rendered = true;
					}
				}
			}
			
	        mouse_in_object = null;
	        ignore_overlapped = false;
	        if (MouseInMe()) {
		        // переворачиваем список наоборот
		        for(int u = render_parts.size() - 1; u>=0; u--)
		        {
		            RenderPart p = render_parts.get(u);
		            if (p.owner != place_obj && p.check_hit(gui.mouse_pos)) {
		        		mouse_in_object = p.owner;
		        		break;
		        	}
		        }
		        if (mouse_in_object == null && Config.hide_overlapped) {
		        	ignore_overlapped = true;
			        for(int u = 0; u < render_parts.size(); u++)
			        {
			            RenderPart p = render_parts.get(u);
			            if (p.is_overlapped && p.owner != place_obj && p.check_hit(gui.mouse_pos)) {
			        		mouse_in_object = p.owner;
			        		break;
			        	}
			        }
		        }
	        }

		}
		Sprite.setStaticColor();
		 
		Coord oc = viewoffset(size, mc);
		// выводим атрибуты объектов
		for (Obj o : ObjCache.objs.values()) {
			Coord dc = m2s(o.getpos()).add(oc);
			if (!CheckDrawCoord(dc)) continue;
			///////////////
			if (render_rects) {
				Render2D.ChangeColor(Color.red);
				Render2D.Disable2D();
				Render2D.FillRect(dc.add(-10, -40), new Coord(20,40));
				Render2D.Enable2D();
			}
			///////////////////-----------------------------------------------
			// ник
			String nick = "";
			KinInfo kin = o.getattr(KinInfo.class);
			if (kin != null)
				nick =  kin.name;
		
			///////////
			if (Config.debug) {
				nick +=" dir="+o.direction;
				// направление движения
				LineMove l = o.getattr(LineMove.class);
				if (l != null) {
					Render2D.Disable2D();
					Render2D.ChangeColor(Color.red);
					Render2D.Line(m2s(o.getpos()).add(oc), m2s(l.get_end()).add(oc), 1f);
					Render2D.Enable2D();
					nick += " move";
				}
			}
			if (kin != null)
				Render2D.Text("default", dc.x, dc.y-68,0,0, Render2D.Align_Center, nick, Color.white);
			//----------------------------------------------------------------
			
			///////////////
			// что сказал чар
			ObjSay say = o.getattr(ObjSay.class);
			if (say != null) {
				String m = say.msg;
				if (m.length() > 32) m = m.substring(0, 31)+"...";
				int w = Render2D.GetTextWidth("default", m) + 15;
				int w1, w2, w3;
				int left_w = 35;
				int bh = getSkin().GetElementSize("baloon_center").y;
				w2 = getSkin().GetElementSize("baloon_center").x;
				if (w < 38) w = 38;
				if (w < left_w+getSkin().GetElementSize("baloon_right").x) { 
					w3 = getSkin().GetElementSize("baloon_right").x;
					w1 = w - w2 - w3;
				} else {
					w3 = w - left_w;
					w1 = left_w - w2;	
				}
				int bx = dc.x - w1 - (w2/2);
				int by = dc.y - bh - 69;
				getSkin().Draw("baloon_left", bx, by, w1, bh);
				getSkin().Draw("baloon_center", bx+w1, by, w2, bh);
				getSkin().Draw("baloon_right", bx+w1+w2, by, w3, bh);
				
				Render2D.Text("default", bx, by,w,27, Render2D.Align_Center, m, Color.white);
			}
			
			// выводим эффекты объекта
			o.render_effects(dc);
		}
	}
	
	public void DrawFlyText() {
		Sprite.setStaticColor();		
		Coord oc = viewoffset(size, mc);

		for (FlyText.FlyTextItem i : FlyText.items) {
			Coord dc = m2s(i.getpos()).add(oc);
			if (CheckDrawCoord(dc)) {
				i.Render(dc);
			}
		}
	}

	public void drawtile(Coord tc, Coord sc) {	
		Sprite s = MapCache.getground(tc);
		if (s != null) { 
			s.draw(sc); 
			RenderedTiles++;
		}
		
		Sprite[] ss = MapCache.gettrans(tc);
		if (ss != null)
		for (Sprite s1 : ss) {
			if (s1 != null) {
				s1.draw(sc);
				RenderedTiles++;
			}
		}
	}
	
	/*
	// отрисовать указанный тайл в экранных координатах
	// а также вывести объекты находящиеся на нем
	public void drawtile_levels(Coord tc, Coord sc) {
		Sprite s, wall;
		int lv, my_lv=0;
		Tile t = new Tile(tc);
		boolean is_water = t.is_water;
		s = t.ground;
		wall = t.wall;
		if (render_tile_levels) {			
			lv = t.level;
			my_lv = lv;
			if (is_water && lv < WATER_LEVEL)
				lv = WATER_LEVEL;
			if (my_lv >= WATER_LEVEL) is_water = false;
		} else {
			if (is_water) s = water;
			lv = 0;
		}
		// посмотрим какого уровня вокруг тайлы
		// если есть перепад больше уровня боковой стенки - надо вывести под собой столбец из тайлов
		int lv1 = (lv - Tile.getlevel(tc.add(1, 0)));
		int lv2 = (lv - Tile.getlevel(tc.add(0, 1)));
		int h = Math.max(Math.max(0, lv1),lv2);
		if (is_water && lv-h>=my_lv) 
			h = lv-my_lv+1; 
		if (h==0) h = 1;

		int dyy = -1;
		if (s != null) {
			int i = h - 1;
			for (int l = lv - h + 1; l <= lv; l++) {
				int dy = (i) * TILE_WALL_HEIGHT - lv * TILE_WALL_HEIGHT;
				if (is_water) {
					if (l == lv) {
						if (render_tile_levels && dyy < 0 && mouse_in_tilelevel(sc.add(0, dy))) {
							dyy = dy;
							// проверяем в тайле ли мышь
							Coord ac = s2m(gui.mouse_pos.sub(sc.add(0, dyy)).sub(31, 0));
							if (ac.x >= 0 && ac.x < TILE_SIZE && ac.y >= 0 && ac.y < TILE_SIZE) {
								mouse_map_pos = tc.mul(TILE_SIZE).add(ac);
								mouse_tile_coord = tilify(mouse_map_pos);
							}
						}
						water.draw(sc.add(0, dy));
					} else if (l < lv && l > my_lv) {
						water_wall.draw(sc.add(0, dy + WALL_OFFSET));
					} else if (l == my_lv) {
						if (render_tile_levels && dyy < 0 && mouse_in_tilelevel(sc.add(0, dy))) {
							dyy = dy;
							// проверяем в тайле ли мышь
							Coord ac = s2m(gui.mouse_pos.sub(sc.add(0, dyy)).sub(31, 0));
							if (ac.x >= 0 && ac.x < TILE_SIZE && ac.y >= 0 && ac.y < TILE_SIZE) {
								mouse_map_pos = tc.mul(TILE_SIZE).add(ac);
								mouse_tile_coord = tilify(mouse_map_pos);
							}
						}
						s.draw(sc.add(0, dy));
					} else {
						wall.draw(sc.add(0, dy + WALL_OFFSET));
					}
				} else {
					if (l == lv) {
						if (render_tile_levels && dyy < 0 && mouse_in_tilelevel(sc.add(0, dy))) {
							dyy = dy;
							// проверяем в тайле ли мышь
							Coord ac = s2m(gui.mouse_pos.sub(sc.add(0, dyy)).sub(31, 0));
							if (ac.x >= 0 && ac.x < TILE_SIZE && ac.y >= 0 && ac.y < TILE_SIZE) {
								mouse_map_pos = tc.mul(TILE_SIZE).add(ac);
								mouse_tile_coord = tilify(mouse_map_pos);
							}
						}
						s.draw(sc.add(0, dy));
					} else {
						wall.draw(sc.add(0, dy + WALL_OFFSET));
					}
				}
				i--;
				RenderedTiles++;
			}
		}
		

		
		// теперь надо вывести все объекты на этом тайле
		if (render_tile_levels) {
			render_tile_objects(tc);
//			Coord tt;
//			int l2, l3, l4, l5;
//			l2 = MapCache.getlevel(tc.add(0, 1));
//			l3 = MapCache.getlevel(tc.add(1, 1)); 
//			l4 = MapCache.getlevel(tc.add(1, 0));
//			l5 = MapCache.getlevel(tc.add(1, -1));
//			
//			// если все тайлы вокруг меня выше либо равны мне - выводим свои объекты
//			if (
//					(l2>lv && l3>lv && l4>lv && l5>lv) 
//							
//				) render_tile_objects(tc);
//			
//			
//			// проверяем 5 тайлов выше меня
//			// если истина - выводим его объекты
//			tt = tc.add(-1, 1);
//			if (check_render_tile(tt,tc,lv,4)) render_tile_objects(tt);
//			
//			tt = tc.add(-1, 0);
//			if (check_render_tile(tt,tc,lv,3)) render_tile_objects(tt);
//			
//			tt = tc.add(-1, -1);
//			if (check_render_tile(tt,tc,lv,2)) render_tile_objects(tt);
//			
//			tt = tc.add(0, -1);
//			if (check_render_tile(tt,tc,lv,1)) render_tile_objects(tt);
			
		}
	}
	*/
	
	// попадает ли мышь в тайл
	public boolean mouse_in_tilelevel(Coord sc) {
		// сначала проверим координаты
		if (gui.mouse_pos.x >= sc.x && gui.mouse_pos.x < sc.x+tile_tex_size.x &&
				gui.mouse_pos.y >= sc.y && gui.mouse_pos.y < sc.y+tile_tex_size.y) {
			// теперь надо проверить попала ли в ромб
			return true;
		} else return false;
	}
	
	// проверить тайл - являюсь ли я высшим для него
//	public boolean check_render_tile(Coord tc, Coord mytc, int lv, int p) {
//		int tlv = MapCache.getlevel(tc);
//		int a,b,c,d;
//
//		switch (p) {
//		case 1:
//			a = lv-tlv;
//			b = MapCache.getlevel(tc.add(1, 1));
//			c = MapCache.getlevel(tc.add(1, 0));
//			return (a<=0 && b>0 && c>0);
//		case 2:
//			b = lv-tlv;
//			return b <= 0;
//		case 3:
//			c = lv-tlv;
//			b = MapCache.getlevel(tc.add(1, 1));
//			return (c<=0 && b>0);
//		case 4:
//			d = lv-tlv;
//			b = MapCache.getlevel(tc.add(1, 1));
//			c = MapCache.getlevel(tc.add(1, 0));
//			a = MapCache.getlevel(tc.add(0, 1));
//			return (b>0 && a>0 && c>0 && d<=0);
//		default:
//			return false;
//		}
//
//	}
	
//	public void render_tile_objects(Coord tc) {
//		for (RenderPart p : render_parts) {
//			if (p.tc.equals(tc)) {
//				set_render_part_color(p);
//				p.render();
//				Sprite.setStaticColor();
//				RenderedObjects++;
//			}
//		}
//	}
	
	public void drawtile_grid(Coord tc, Coord sc) {
        Coord c1 = sc;
        Coord c2 = sc.add(m2s(new Coord(0, TILE_SIZE)));
        Coord c4 = sc.add(m2s(new Coord(TILE_SIZE, 0)));
        
        Render2D.ChangeColor(new Color(0.2f,0.2f,0.2f,1.0f));
        Render2D.Line(c2, c1, 1);
        Render2D.Line(c1.add(1, 0), c4.add(1, 0), 1);
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * *   Затемнение    * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
//	private BufferedImage base_light_tex = null;		// Базовая текстура, окрашена в текущее освещение
//	private Sprite current_light_layer = null;
	
//	private void ChangeCurrenLightColor(java.awt.Color light) {
//		base_light_tex = new BufferedImage(Config.ScreenWidth, Config.ScreenHeight, BufferedImage.TYPE_INT_ARGB);
//		Graphics g = base_light_tex.createGraphics();
//		g.setColor(light);
//		g.clearRect(0, 0, base_light_tex.getWidth(), base_light_tex.getWidth());
//	}
//	
	private void DrawLight(Coord screenCoord, int light_lvl, int radius) {
		Sprite s = new Sprite("light");
		s.draw(screenCoord.x-radius/2, 768-screenCoord.y-radius/2, radius, radius);
//		BufferedImage img = new BufferedImage(radius * 2 + 1, radius * 2 + 1, BufferedImage.TYPE_INT_ARGB);
//		for (int x = 0; x < radius * 2; x++)
//			for (int y = 0; y < radius * 2; y++) {
//				int cr = (int)Math.sqrt(Math.pow(x - radius, 2) + Math.pow(y - radius, 2));
//				if (cr < radius) {
//					if (x > 0 && y > 0 && x < img.getWidth() && y < img.getHeight()) {
//						img.setRGB(x, y, new java.awt.Color(55, 55, 55,  128).getRGB());
//					}
//				}
//				else
//				{
//					img.setRGB(x, y, 0);
//				} 
//				
//			}
//		try {
//			new Sprite(org.newdawn.slick.util.BufferedImageUtil.getTexture("", img)).draw(screenCoord);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	public static int LightMap = 0;

	public void DoCreate() {
		IntBuffer tt = BufferUtils.createIntBuffer(1);
		GL11.glGenTextures(tt);
		LightMap = tt.get(0);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, LightMap);
		ByteBuffer bb = BufferUtils.createByteBuffer(1024*1024*4);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 1024, 1024, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, bb);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		Render2D.CheckError();
	}
	
	boolean first = true;
	int myFBOId;
	public void CreateLightMap() {
		
		//Coord screen_size = new Coord(Config.ScreenWidth, Config.ScreenHeight);
		//Coord screen_coord = new Coord(GUI.map.mc).sub(screen_size.div(2));
		List<Coord> lst = new ArrayList<Coord>();
		
		for (RenderPart p : render_parts) {
			Drawable d = p.owner.getattr(Drawable.class);
			if (d != null) {
				if (d.name.contains("fir")) {
						lst.add(p.dc);
				}
				if (d.name.contains("player")) {
					lst.add(p.dc);
			}
			}
		}
		
//		for (Obj o : ObjCache.objs.values()) {
//			Drawable d = o.getattr(Drawable.class);
//			if (d != null) {
//				if (d.name.contains("tree")) {
//					if (o.getpos().in_rect(screen_coord, screen_size))
//						lst.add(o.getpos());
//				}
//			}
//		}
		
		if (first) {
			first = false;
			boolean FBOEnabled = GLContext.getCapabilities().GL_EXT_framebuffer_object;
			if (!FBOEnabled) Log.info("No FBO");
			myFBOId = EXTFramebufferObject.glGenFramebuffersEXT();
			//LightMap = GL11.glGenTextures();
			
			EXTFramebufferObject.glBindFramebufferEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT, myFBOId );
			EXTFramebufferObject.glFramebufferTexture2DEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 
					EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, LightMap, 0);
			EXTFramebufferObject.glBindFramebufferEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0 );
		}
			
		
		
		
		
		//ByteBuffer bb = ByteBuffer.allocate(1024 * 1024 * 4);
		//GL11.glReadPixels(0, 0, 1024, 768, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, bb);
		
		// Используем FBO чтобы рисовать в текстуру
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, myFBOId);
//		GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT);
		// Размер текстуры
//		GL11.glViewport( 0, 0, 1024, 1024 );
//		
		// чистим кадр
		// фоновый цвет затемнения
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glClearColor(0.3f, 0.3f, 0.4f, 1.0f);
		GL11.glClear(GL_COLOR_BUFFER_BIT);
		
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		
		// рендерим все источники света
		for (Coord c : lst)
			DrawLight(c, 100, 100);
		
		/*GL11.glTranslatef(0, 0, -6f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex3f(0, 0, 0);
		GL11.glVertex3f(200, 0, 0);
		GL11.glVertex3f(200, 200, 0);
		GL11.glVertex3f(0, 200, 0);
		GL11.glEnd();*/
//		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		EXTFramebufferObject.glBindFramebufferEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
//		GL11.glPopAttrib();
		
		
//		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
//		
		// биндим лайтмапу
		//GL11.glBindTexture(GL11.GL_TEXTURE_2D, LightMap);
		//Render2D.CheckError();
		
		// копируем в нее содержимое кадра - нарисованное затемнение
		//GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 0, 0, 1024, 1024, 0);
		//GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, 1024, 1024);
		//Render2D.CheckError();
		
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

	}

	public void RenderLightMap() {
		GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
		//Log.info("lightmap: "+LightMap);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, LightMap); //Resource.textures.get("core_skin").getTextureID());
		
		GL11.glTranslatef(0, 0, 0);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		float texwidth = 1024;
		float texheight = 1024;

		float newTextureOffsetX = 0 / texwidth;
		float newTextureOffsetY = 0 / texheight;
		float newTextureWidth = 1024 / texwidth;
		float newTextureHeight = 1024 / texheight;
		
		int w = 1024; 
		int h = 1024;
		
		GL11.glBegin(GL11.GL_QUADS);
		{
			glTexCoord2f(newTextureOffsetX, newTextureOffsetY);
			glVertex2i(0, 0);

			glTexCoord2f(newTextureOffsetX, newTextureOffsetY+newTextureHeight);
			glVertex2i(0, h);

			glTexCoord2f(newTextureOffsetX + newTextureWidth,
					newTextureOffsetY + newTextureHeight);
			glVertex2i(w, h);

			glTexCoord2f(newTextureOffsetX + newTextureWidth,
					newTextureOffsetY);
			glVertex2i(w, 0);
		}
		
		GL11.glEnd();
		
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
	

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
}
