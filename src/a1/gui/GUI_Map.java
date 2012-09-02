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

package a1.gui;

import a1.*;
import a1.utils.Resource;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GLContext;
import org.newdawn.slick.Color;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static a1.MapCache.TILE_SIZE;
import static a1.net.NetGame.SEND_map_click;
import static org.lwjgl.opengl.GL11.*;

public class GUI_Map extends GUI_Control {

    public static int VBO_SIZE = 80000;
	public static int tile_vboSize = VBO_SIZE * 4 * 4;
	//
	public static int tileGrid_vboSize = VBO_SIZE * 4 * 2;
	//
	public static int claim_vboSize = 1000 * 6 * 4;

	public static float[] tile_vboUpdate = new float[tile_vboSize];
	public static float[] tileGrid_vboUpdate = new float[tile_vboSize];
	public static float[] claim_vboUpdate = new float[tile_vboSize];
	public static int tile_Offset = 0;
	public static int tileGrid_Offset = 0;
	public static int claim_Offset = 0;
	//
	public static int tile_drawCount = 0;
	public static int tileGrid_drawCount = 0;
	public static int claim_drawCount = 0;
	private static int tile_vbo;
	private static int tileGrid_vbo;
	private static int claim_vbo;
	
	///////////////
	public static boolean render_rects = false;
	public static boolean render_lightmap = false;
    public static boolean render_claims = true;
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
    private boolean mouse_middle_pressed = false;
    private Coord mouse_middle_press_coord;
    private boolean render_tiles = true;
    private boolean render_objects = true;
    private boolean gui_map_rendered = true;
    public static float scale = 1.0f;
    Coord scaled_size;
    public static boolean needUpdateView = true;

	static {
		cameras.put("fixed", FixedCam.class);
	}

	public GUI_Map(GUI_Control parent) {
		super(parent);
		GUI.map = this;
		SetSize(Config.getScreenWidth(), Config.getScreenHeight());
        scale = 1.0f;
        scaled_size = new Coord(Config.getScreenWidth(), Config.getScreenHeight()).mul(1/scale);
		set_camera("fixed");
		initVBO();
	}

	private void initVBO() {
		tile_vbo = GL15.glGenBuffers();
		tileGrid_vbo = GL15.glGenBuffers();
		claim_vbo = GL15.glGenBuffers();
		
		FloatBuffer data = BufferUtils.createFloatBuffer(tile_vboSize);	
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tile_vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_DYNAMIC_DRAW);

		data = BufferUtils.createFloatBuffer(tileGrid_vboSize);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tileGrid_vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_DYNAMIC_DRAW);

		data = BufferUtils.createFloatBuffer(claim_vboSize);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, claim_vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_DYNAMIC_DRAW);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);		
	}

	// обработчик апдейта
	public void DoUpdate() {
		mouse_map_pos = s2m(gui.mouse_pos.mul(1 / scale).add(viewoffset(scaled_size, this.mc).inverse()));
		mouse_tile_coord = tilify(mouse_map_pos);
		
		FlyText.Update();
		
		Coord my = my_coord();
		if (!player_coord.equals(my)) {
			player_coord = my;
			needUpdateView = true;
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

        if (Input.KeyHit(Keyboard.KEY_G) && Input.isCtrlPressed()) {
            Config.tile_grid = !Config.tile_grid;
            needUpdateView = true;
        }
        if (gui.focused_control == null) {
            if (Input.KeyHit(Keyboard.KEY_HOME) && !Input.isCtrlPressed() && camera != null) {
                camera.reset();
            }
            if (Input.KeyHit(Keyboard.KEY_HOME) && Input.isCtrlPressed()) {
                scale = 1.0f;
                updateScale();
            }

            if (Config.debug && Input.KeyHit(Keyboard.KEY_T) && Input.isCtrlPressed()) {
                render_tiles = !render_tiles;
            }

            if (Config.debug && Input.KeyHit(Keyboard.KEY_Y) && Input.isCtrlPressed()) {
                render_objects = !render_objects;
            }

            if (Config.debug && Input.KeyHit(Keyboard.KEY_U) && Input.isCtrlPressed()) {
                gui_map_rendered = !gui_map_rendered;
            }
        }
		
		if (camera != null) {
			if (get_player() != null)
				camera.setpos(this, get_player(), scaled_size);
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

    public boolean DoMouseWheel(boolean isUp, int len) {
        if (!MouseInMe() || !Config.zoom_by_wheel)
            return false;

        float olds = scale;
        scale = scale + (isUp?1:-1)*0.1f*len;
        if (scale < 0.4f) scale = 0.4f;
        if (scale > 5.0f) scale = 5.0f;
        if (camera != null && Config.zoom_over_mouse) camera.scaled(this, olds);
        updateScale();
        return true;
    }

    private void updateScale() {
        needUpdateView = true;
        scaled_size = new Coord(Config.getScreenWidth(), Config.getScreenHeight()).mul(1/scale);
    }

	@Override
	public void DoDestroy() {
		GL15.glDeleteBuffers(tile_vbo);
		GL15.glDeleteBuffers(tileGrid_vbo);
		GL15.glDeleteBuffers(claim_vbo);
		super.DoDestroy();
	}

	// обработчик рендера
	public void DoRender() {
        if (!gui_map_rendered) return;

		if (render_lightmap) CreateLightMap();
		
		RenderedObjects = 0;
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1.0f);
		if (render_tiles) DrawTiles();
		if (render_objects) DrawObjects();
		DrawFlyText();
		
		if (render_lightmap) RenderLightMap();
        GL11.glPopMatrix();
		
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
				Render2D.Text("", 10, 300, "mouse_tile_type="+Integer.toString(MapCache.GetTile(tc)));
			}
			
			if (player_rect != null)
				Render2D.Text("", 10, 320, "player_rect="+player_rect.toString());
            Render2D.Text("", 10, 340, "scale="+String.valueOf(scale));
		}
	}

	private void updateVBO() {
		
		tile_drawCount = tile_Offset / 4;
		tileGrid_drawCount = tileGrid_Offset / 2;
		claim_drawCount = claim_Offset / 6;

		FloatBuffer data = BufferUtils.createFloatBuffer(tile_Offset);
		data.put(tile_vboUpdate, 0, tile_Offset);
		data.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tile_vbo);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, data);

		data = BufferUtils.createFloatBuffer(tileGrid_Offset);
		data.put(tileGrid_vboUpdate, 0, tileGrid_Offset);
		data.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tileGrid_vbo);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, data);

		data = BufferUtils.createFloatBuffer(claim_Offset);
		data.put(claim_vboUpdate, 0, claim_Offset);
		data.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, claim_vbo);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, data);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);	

		tile_Offset = 0;
		tileGrid_Offset = 0;
		claim_Offset = 0;
	}

	// обработчик нажатия кнопок мыши
	public boolean DoMouseBtn(int btn, boolean down) {
        if (btn == Input.MB_LEFT) mouse_left_pressed = MouseInMe() && down;
		if (!MouseInMe() && down) return false;

        if (btn == Input.MB_MIDDLE && MouseInMe())
            if (down) {
                mouse_middle_pressed = true;
                mouse_middle_press_coord = gui.mouse_pos.clone();
            }else {
                if (mouse_middle_pressed && gui.mouse_pos.dist(mouse_middle_press_coord) < 5) {
                    scale = 1.0f;
                    updateScale();
                }
            }
		
		// обработаем камеру
		if (camera != null) {
            if (camera.DoMouseBtn(this, gui.mouse_pos.mul(1 / scale), mc, btn, down)) {
                needUpdateView = true;
                return true;
            }
        }

		// если и в камеру не попали - шлем клик по карте на сервер
		if (down) {
            mouse_map_pos = s2m(gui.mouse_pos.mul(1 / scale).add(
                    					viewoffset(scaled_size, this.mc).inverse()));
			if (mouse_map_pos != null) SendMapClick(btn);
			needUpdateView = true;
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
                last_mouse_pos = new Coord(mv.gui.mouse_pos.mul(1 / scale));
				mouse_move(mv, last_mouse_pos);
			}
		}

		public void mouse_move(GUI_Map mv, Coord sc) {}

		public void moved(GUI_Map mv) {}

		//		public static void borderize(GUI_Map mv, Obj player, Coord sz, Coord border) {}

		public void reset() {}

        public void scaled(GUI_Map mv, float old_scale) {}
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
				needUpdateView = true;
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
            needUpdateView = true;
		}
        public void scaled(GUI_Map mv, float old_scale) {
            Coord d2 = mv.size.div(2);
            Coord old = s2m(d2.mul(1/old_scale));
            Coord ns = s2m(d2.mul(1/scale));
            Coord ds = ns.sub(old);
            Coord m1 = s2m(d2);
            Coord m2 = s2m(mv.gui.mouse_pos);
            Coord mmc = m1.sub(m2);
            float dx = (float)mmc.x / (float)m1.x;
            float dy = (float)mmc.y / (float)m1.y;
            ds = ds.mul( dx, dy );
            mv.mc = mv.mc.add(ds);
            setoff = true;
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
	
	/*	матрица2x2:
	 * 	|2,	-2|
	 * 	|1,	 1|
	 */	
	public static Coord m2s(Coord c) {
		return(new Coord((c.x * 2) - (c.y * 2), c.x + c.y));
	}

	/*  матрица2х2:
	 * 	|0.25,	0.5|
	 * 	|-0.25,	0.5|
	 */
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

	public void DrawTiles() {
		int x, y, i;
		int stw, sth;
		Coord oc, tc, ctc, sc;

		RenderedTiles = 0;
		Render2D.ChangeColor();
		Sprite.setStaticColor();

		// размеры тайла для расчетов
		stw = (TILE_SIZE * 4) - 2;
		sth = TILE_SIZE * 2;
		// оффсет центра экрана
        Coord sz = scaled_size;
		oc = viewoffset(sz, mc);
		// начальный тайл для отрисовки
		tc = mc.div(TILE_SIZE);
		tc.x += -(sz.x / (2 * stw)) - (sz.y / (2 * sth)) - 2;
		tc.y += (sz.x / (2 * stw)) - (sz.y / (2 * sth));

		if (needUpdateView) {

			needUpdateView = false;

			for (y = 0; y < (sz.y / sth) + 13; y++) {
				for (x = 0; x < (sz.x / stw) + 3; x++) {
					for (i = 0; i < 2; i++) {
						// координаты текущего тайла
						ctc = tc.add(new Coord(x + y, y - x + i));
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
				for (y = 0; y < (sz.y / sth) + 2; y++) {
					for (x = 0; x < (sz.x / stw) + 3; x++) {
						for (i = 0; i < 2; i++) {
							ctc = tc.add(new Coord(x + y, -x + y + i));
							sc = m2s(ctc.mul(TILE_SIZE)).add(oc);
							drawtile_grid(sc);
						}
					}
				}
			}

            if (render_claims) {
				Color col;
				for (ClaimPersonal claim : Claims.claims.values()) {


					if (Player.CharID == claim.owner_id) {
						col = new Color(0f, 1f, 1f, 0.25f);
//						if (Claims.expand_claim != null)
//							continue;
					}
					else
						col = new Color(1f, 0f, 0f, 0.25f);

                    putClaim(claim, oc, col);
                }

				if (Claims.expand_claim != null) {
					col = new Color(0f, 0f, 0.8f, 0.17f);

					putClaim(Claims.expand_claim, oc, col);
				}
            }

			//
			updateVBO();
		}

		Color.white.bind();
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tile_vbo);
		GL11.glVertexPointer(2, GL11.GL_FLOAT, 16, 0L);
		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 16, 8L);

		Resource.textures.get("tiles").bind();

		GL11.glDrawArrays(GL11.GL_QUADS, 0, tile_drawCount);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		if (render_claims) {
			GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, claim_vbo);
			GL11.glColorPointer(4, GL11.GL_FLOAT, 24, 0L);
			GL11.glVertexPointer(2, GL11.GL_FLOAT, 24, 16L);
			GL11.glDrawArrays(GL11.GL_QUADS, 0, claim_drawCount);
			GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
		}

		if (Config.tile_grid) {
			GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.4f);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tileGrid_vbo);
			GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, 0L);
			GL11.glDrawArrays(GL11.GL_LINES, 0, tileGrid_drawCount);
		}

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

	}

    private void putClaim(ClaimPersonal c, Coord oc, Color col) {

		if (claim_vboSize < claim_Offset + 24 )
			return;

        Coord lt = m2s(c.lt).mul(TILE_SIZE).add(oc);
        Coord lb = m2s(new Coord(c.lt.x, c.rb.y + 1)).mul(
                TILE_SIZE).add(oc);
        Coord rt = m2s(new Coord(c.rb.x + 1, c.lt.y)).mul(
                TILE_SIZE).add(oc);
        Coord rb = m2s(c.rb.add(1, 1)).mul(TILE_SIZE).add(oc);

        putColor(col);
        putPoint(lt);

        putColor(col);
        putPoint(rt);

        putColor(col);
        putPoint(rb);

        putColor(col);
        putPoint(lb);
    }

	private void putColor(Color color) {
		claim_vboUpdate[claim_Offset++] = color.r;
		claim_vboUpdate[claim_Offset++] = color.g;
		claim_vboUpdate[claim_Offset++] = color.b;
		claim_vboUpdate[claim_Offset++] = color.a;
	}

	private void putPoint(Coord point) {
		claim_vboUpdate[claim_Offset++] = point.x;
		claim_vboUpdate[claim_Offset++] = point.y;
	}

	public void PrepareRenderParts() {
		Coord oc = viewoffset(scaled_size, mc);
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
		return !(dc.x < -100 || dc.y < -100 || dc.x > Config.getScreenWidth()+100 || dc.y > Config.getScreenHeight() + 100);
	}
	
	public Color get_render_part_color(RenderPart p) {
		// если ставим объект - делаем его слегка прозрачным
		if (p.owner!=null) {
			if (p.owner == place_obj)
				return new Color(1.0f,1.0f,1.0f,0.8f);
			else
				// подсвечиваем зеленым объект под мышкой
				if (p.owner == mouse_in_object) 
					return new Color(201,236,10,255);
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
				
				if (Config.hide_overlapped && (!p.ignore_overlap || p.is_my_player)) {
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
                    if (p.owner != place_obj && p.check_hit(gui.mouse_pos.mul(1 / scale))) {
		        		mouse_in_object = p.owner;
		        		break;
		        	}
		        }
		        if (mouse_in_object == null && Config.hide_overlapped) {
		        	ignore_overlapped = true;
                    for (RenderPart p : render_parts) {
                        if (p.is_overlapped && p.owner != place_obj && p.check_hit(gui.mouse_pos.mul(1 / scale))) {
                            mouse_in_object = p.owner;
                            break;
                        }
                    }
		        }
	        }

		}
		Sprite.setStaticColor();
		 
		Coord oc = viewoffset(scaled_size, mc);
		// выводим атрибуты объектов
		for (Obj o : ObjCache.objs.values()) {
            Coord dc = m2s(o.getpos()).add(oc).mul(scale);
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
			if (kin != null) {
				nick =  kin.name;
            }
		
			///////////
			if (Config.debug) {
                nick += " objid="+o.id;
				nick += " dir="+o.direction;
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
			if (kin != null){// || Config.debug) {
                GL11.glPushMatrix();
                GL11.glLoadIdentity();
				Render2D.Text("default", dc.x, dc.y-10-Math.round(38*(scale)),0,0, Render2D.Align_Center, nick, Color.white);
                GL11.glPopMatrix();
            }
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
				int by = dc.y -bh - 15 - Math.round(38*scale);
                GL11.glPushMatrix();
                GL11.glLoadIdentity();
				getSkin().Draw("baloon_left", bx, by, w1, bh);
				getSkin().Draw("baloon_center", bx+w1, by, w2, bh);
				getSkin().Draw("baloon_right", bx+w1+w2, by, w3, bh);
				
				Render2D.Text("default", bx, by,w,27, Render2D.Align_Center, m, Color.white);
                GL11.glPopMatrix();
			}
			
			// выводим эффекты объекта
			o.render_effects(dc);
		}
	}
	
	public void DrawFlyText() {
		Sprite.setStaticColor();		
		Coord oc = viewoffset(scaled_size, mc);

		for (FlyText.FlyTextItem i : FlyText.items) {
            Coord dc = m2s(i.getpos()).add(oc).mul(scale);
			if (CheckDrawCoord(dc)) {
                GL11.glPushMatrix();
                GL11.glLoadIdentity();
				i.Render(dc);
                GL11.glPopMatrix();
			}
		}
	}

	public void drawtile(Coord tc, Coord sc) {	
		Sprite s = MapCache.getground(tc);
		if (s != null) { 
			s.draw_map_vbo(sc);
			RenderedTiles++;
		}
		
		Sprite[] ss = MapCache.gettrans(tc);
		if (ss != null)
		for (Sprite s1 : ss) {
			if (s1 != null) {
				s1.draw_map_vbo(sc);
				RenderedTiles++;
			}
		}
	}
	
	public void drawtile_grid(Coord sc) {
        if (tileGrid_vboSize < tileGrid_Offset + 16)
            return;
        Coord c2 = sc.add(m2s(new Coord(0, TILE_SIZE)));
		Coord c4 = sc.add(m2s(new Coord(TILE_SIZE, 0)));
        
		putLine(c2, sc);
		putLine(sc.add(1, 0), c4.add(1, 0));
	}
	
	private void putLine(Coord v1, Coord v2) {
		tileGrid_vboUpdate[tileGrid_Offset++] = v1.x;
		tileGrid_vboUpdate[tileGrid_Offset++] = v1.y;

		tileGrid_vboUpdate[tileGrid_Offset++] = v2.x;
		tileGrid_vboUpdate[tileGrid_Offset++] = v2.y;
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * *   Затемнение    * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
//	private BufferedImage base_light_tex = null;		// Базовая текстура, окрашена в текущее освещение
//	private Sprite current_light_layer = null;
	
//	private void ChangeCurrenLightColor(java.awt.Color light) {
//		base_light_tex = new BufferedImage(Config.getScreenWidth(), Config.getScreenHeight(), BufferedImage.TYPE_INT_ARGB);
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
		
		//Coord screen_size = new Coord(Config.getScreenWidth(), Config.getScreenHeight());
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


    public void DoResolutionChanged() {
        scaled_size = new Coord(Config.getScreenWidth(), Config.getScreenHeight()).mul(1/scale);
        SetSize(Config.getScreenWidth(), Config.getScreenHeight());
        needUpdateView = true;
    }
}
