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
import a1.net.NetGame;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GUI_Minimap extends GUI_Control {
	private Sprite MinimapSprite; 			// Спрайт миникарты
	private Coord currentGrid; 				// Координаты текущего грида (полные)
	private Coord MMTopLeftCoords;			// Экранные координаты левого верхнего угла ТЕКСТУРЫ миникарты
	private boolean rebuild_needed = false;	// Нужен ли ребилд
	private static int TIMER_TICK = 2;		// Время тика таймера (в секундах)
	
	// Инициализация миникарты
	private void InitMMaps() {
		try {
			MMTopLeftCoords = new Coord(0, 0);
			// Установка текущего грида
			currentGrid = GUI.map.mc.div(MapCache.TILE_SIZE * MapCache.GRID_SIZE).mul(MapCache.TILE_SIZE * MapCache.GRID_SIZE);
			// Добавляем себя в отрисовку
			AddDrawableObject(null, GUI.map.mc, 0);
			// Сформировать карту
			RebuildMinimapSprite();
			// Установить размер =)
			SetSize(200, 200);
		} catch (Exception ex) {
			Log.info("Minimap InitMMaps Error: " + ex.getMessage());
		}
	}
	
	public GUI_Minimap(GUI_Control parent) {
		super(parent);
		InitMMaps();
	}
	
	// Обновление миникарты (по получению клиентом нового грида)
	public void UpdateMinimap(Grid newgrid) {
		try {
			rebuild_needed = true;
		} catch (Exception ex) {
			Log.info("Minimap UpdateMinimap Error: " + ex);
		}
	}
	
	// Формирование спрайта
	private void RebuildMinimapSprite() {
		try {
			UpdateCurrentGrid();
			// Массив тайлов
			byte[][] byteBuffer = new byte[MapCache.GRID_SIZE * 3][MapCache.GRID_SIZE * 3];
			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++) {
					Grid current = MapCache.get_grid(currentGrid.sub(MapCache.GRID_SIZE * MapCache.TILE_SIZE).add(new Coord(i, j).mul(MapCache.GRID_SIZE * MapCache.TILE_SIZE)));
					if (current != null) {
						for (int x = i * MapCache.GRID_SIZE; x < (i + 1) * MapCache.GRID_SIZE; x++)
							for (int y = j * MapCache.GRID_SIZE; y < (j + 1) * MapCache.GRID_SIZE; y++)
							{
								byteBuffer[x][y] = (byte)current.gettile(x - i * MapCache.GRID_SIZE, y - j * MapCache.GRID_SIZE);
							}
					}
				}
			BufferedImage result = createBufferedImage(byteBuffer);
			MinimapSprite = new Sprite(org.newdawn.slick.util.BufferedImageUtil.getTexture("", result));
		} catch (Exception ex) {
			Log.info("Minimap RebuildMinimapSprite Error: " + ex);
		}
	}
	
	// Обновление координат текущего грида
	private void UpdateCurrentGrid() {
		UpdateDrawableObject(0, GUI.map.mc);
		// Бэкап старых координат
		Coord old = new Coord(currentGrid);
		// Обновление координат
		currentGrid = GUI.map.mc.div(MapCache.TILE_SIZE * MapCache.GRID_SIZE).mul(MapCache.TILE_SIZE * MapCache.GRID_SIZE);
		// Если мы перешли в другой грид - обновить спрайт
		if (!old.equals(currentGrid)) {
			rebuild_needed = true;
		}
	}
	
	// Возвращает число в пределах (0, topBound) 
	private int returnBounds(int x, int topBound) {
		int ret = x;
		if (x < 0) ret = 0;
		if (x > topBound) ret = topBound;
		return ret;
	}
	
	// Получает из полных игровых координат экранные координаты 
	// относительно левого верхнего угла миникарты (скрытого)
	// в пределах экранного размера миникарты
	private Coord GetRealCoordFromIngame(Coord c) {
		Coord topleftCorner = currentGrid.div(MapCache.TILE_SIZE).sub(MapCache.GRID_SIZE);
		Coord delta = c.div(MapCache.TILE_SIZE).sub(topleftCorner);
		if (delta.in_rect(new Coord(0, 0), new Coord(MapCache.GRID_SIZE * 3, MapCache.GRID_SIZE * 3))) {
			return delta;
		} else {
			return new Coord(returnBounds(delta.x, MapCache.GRID_SIZE * 3), returnBounds(delta.y, MapCache.GRID_SIZE * 3));
		}
	}
	
	private Coord GetIngameCoordFromReal(Coord c) {
		Coord topleftCorner = currentGrid.div(MapCache.TILE_SIZE).sub(MapCache.GRID_SIZE);
		return topleftCorner.add(c).mul(MapCache.TILE_SIZE);
	}
	
	// Обновляет экранные координаты ТЕКСТУРЫ миникарты (сдвиг при движении)
	private void UpdatePos() {
		UpdateCurrentGrid();
		Coord currentGridCenter = currentGrid.add((new Coord(1, 1).mul(MapCache.TILE_SIZE * MapCache.GRID_SIZE)).div(2));
		Coord dc = currentGridCenter.sub(GUI.map.mc);
		MMTopLeftCoords = dc.div(MapCache.TILE_SIZE).sub(MapCache.GRID_SIZE / 2);
	}
	
	/* * * * * * * * * Интерфейс для добавления объектов показа на миникарте * * * * * * * * */
	// Класс рисуемых объектов
	private class MDrawableObject {
		public Sprite MDOSprite; 	// Спрайт
		public Coord MDOCoord;		// Относительные координаты объекта
		public int MDOID;			// ID объекта
		
		public MDrawableObject(Sprite s, Coord c, int id) {
			this.MDOCoord = c;
			this.MDOSprite = s;
			this.MDOID = id;
		}
		
		public void DrawNullSprite(org.newdawn.slick.Color clr, Coord c) {
			Render2D.ChangeColor(clr);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			Render2D.FillRect(c.sub(5), new Coord(10, 10));
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}
	
	// Список объектов
	private List<MDrawableObject> MDObjects = new LinkedList<MDrawableObject>();
	
	// Добавление объекта 
	public void AddDrawableObject(Sprite s, Coord c, int id) {
		MDrawableObject obj = new MDrawableObject(s, GetRealCoordFromIngame(c), id);
		MDObjects.add(obj);
	}
	
	// обновление позиции объекта
	public void UpdateDrawableObject(int id, Coord c) {
		for (MDrawableObject obj : MDObjects) {
			if (obj.MDOID == id) obj.MDOCoord = GetRealCoordFromIngame(c);
		}
	}
	
	// Удаление объекта
	public void DeleteDrawableObject(int id) {
		int remid = -1;
		for (int i = 0; i < MDObjects.size(); i++) {
			if (MDObjects.get(i).MDOID == id) remid = i;
		}
		if (remid >= 0) MDObjects.remove(remid);
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	long last = 0;
	// Таймер обновления
	private void UpdateTimer() {
		long now = System.currentTimeMillis();
		if (now - last > 1000 * TIMER_TICK) {
			last = now;
			rebuild_needed = true;
		}
		if (rebuild_needed) {
			RebuildMinimapSprite();
			rebuild_needed = false;
		}
	}
	
	// Перерисовка контрола
	public void DoRender() {
		try {
			UpdateTimer();
			// Обновляем позицию текстры
			UpdatePos();
			// Получаем координаты для риосвания относительно положения контрола
			Coord drawCoord = abs_pos.add(MMTopLeftCoords);
			// Ножнечги
			Render2D.PushScissor(new IntCoord(abs_pos.x + 1, abs_pos.y + 1, size.x - 15, size.y - 15));
			if (MinimapSprite != null) MinimapSprite.draw(drawCoord);
			// Отрисовка всех показываемых объектов
			for (MDrawableObject obj : MDObjects) {
				if (obj.MDOSprite != null) obj.MDOSprite.draw(drawCoord.add(obj.MDOCoord));
				else obj.DrawNullSprite(org.newdawn.slick.Color.red, drawCoord.add(obj.MDOCoord));
			}
			// Ножнечги офф
			Render2D.PopScissor();
		} catch (Exception ex) {
			Log.info("Minimap DoRender Error: " + ex.getMessage());
		}
	}
	
	public void DoClick(int x, int y) {
		Coord real = new Coord(x, y).sub(abs_pos.add(MMTopLeftCoords));
		Coord ingame = GetIngameCoordFromReal(real);
		NetGame.SEND_map_click(0, ingame.x, ingame.y, 0, 0, 0, 0);
	}
	
	
	// Логика нажатия ЛКМ
	boolean pressed = false;
	public boolean DoMouseBtn(int btn, boolean down) {
		if (!enabled) return false;
		
		if (btn == Input.MB_LEFT)
			if (down) {
				if (MouseInMe()){
					pressed = true;
					return true;
				}
			} else {
				if (pressed && MouseInMe()) {
					DoClick(Input.MouseX, Input.MouseY);
					pressed = false;
					return true;
				}
				pressed = false;
			}
		return false;
	}
	
/* * * * * * * * * Обработка изображений * * * * * * * * */
	private Color[] arr = new Color[]{ new Color(0, 0, 0), new Color(68, 115, 36), new Color(113, 109, 73)};
	private double blend_koef = 0.6;
	
	private Color Blend(Color col, double val) {
        return new Color((int)(col.getRed() * val), (int)(col.getGreen() * val), (int)(col.getBlue() * val));
    }
	
	// Brick: (x0, y0) [(x1, y1) (x2, y2)] (x3, y3)
    private void formBrick(byte[][] img, int x1, int y1, int x2, int y2, int x0, int y0, int x3, int y3, BufferedImage ob) {
    	if (img[x1][y1] < img[x2][y2]) {
            ob.setRGB(x1, y1, Color.BLACK.getRGB());
            ob.setRGB(x0, y0, Blend(arr[img[x0][y0]], blend_koef).getRGB());
            ob.setRGB(x2, y2, Blend(arr[img[x2][y2]], blend_koef).getRGB());
        } else if (img[x1][y1] > img[x2][y2]) {
            ob.setRGB(x2, y2, Color.BLACK.getRGB());
            ob.setRGB(x1, y1, Blend(arr[img[x1][y1]], blend_koef).getRGB());
            ob.setRGB(x3, y3, Blend(arr[img[x3][y3]], blend_koef).getRGB());
        }
    }
    
    private byte[][] tree_mask = new byte[][]	
           {{0, 0, 1, 0, 0},
    		{0, 1, 1, 1, 0},
    		{0, 1, 1, 1, 0},
    		{0, 1, 1, 1, 0},
    		{1, 1, 1, 1, 1},
    		{1, 1, 1, 1, 1},
    		{1, 1, 1, 1, 1},
    		{1, 1, 1, 1, 1},
    		{1, 0, 1, 0, 1},
    		{0, 0, 1, 0, 0}};
    
    private void DrawTree(BufferedImage img, Coord c) {
    	for (int i = 0; i < 5; i++)
    		for (int j = 9; j >= 0; j--) {
    			int xc = c.x + i - 2;
    			int yc = c.y + j;
    			if (xc > 0 && yc > 0 && xc < MapCache.GRID_SIZE * 3 && yc < MapCache.GRID_SIZE * 3) {
    				if (tree_mask[j][i] > 0) img.setRGB(xc, yc, Color.BLACK.getRGB());
    			}
    		}
    }
	
    // Создание изображения по гриду
	private BufferedImage createBufferedImage(byte[][] img) {
		try {
			BufferedImage minimap = new BufferedImage(MapCache.GRID_SIZE * 3, MapCache.GRID_SIZE * 3, BufferedImage.TYPE_INT_RGB);
			for (int i = 0; i < MapCache.GRID_SIZE * 3; i++)
				for (int j = 0; j < MapCache.GRID_SIZE * 3; j++) {
					minimap.setRGB(i, j, arr[img[i][j]].getRGB());
				}
			for (int i = 1; i < MapCache.GRID_SIZE * 3 - 2; i++)
				for (int j = 11; j < MapCache.GRID_SIZE * 3 - 2; j++) {
					formBrick(img, i, j, i + 1, j, i - 1, j, i + 2, j, minimap);
                    formBrick(img, i, j, i, j + 1, i, j - 1, i, j + 2, minimap);
				}
			List<Coord> lst = new ArrayList<Coord>();
			for (Obj o : ObjCache.objs.values()) {
				Drawable d = o.getattr(Drawable.class);
				if (d != null) {
					if (d.name.contains("tree")) {
						Coord tc = GetRealCoordFromIngame(o.getpos());
						lst.add(tc);
					}
				}
			}
			for (Coord c : lst)
				DrawTree(minimap, c);
			return minimap;
		} catch (Exception ex) {
			Log.info("Minimap BufferedImage Error: " + ex);
			return null;
		}
	}
}
