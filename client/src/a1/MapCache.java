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
package a1;

import a1.gui.GUI_Map;
import a1.utils.Resource.ResTile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static a1.utils.Resource.tile_sets;

public class MapCache {
	// сколько единиц координат в одном тайле
	public static final int TILE_SIZE = 12;
	// размер одного грида в тайлах
	public static final int GRID_SIZE = 100;
    // размер одного грида в байтах для передачи по сети
    public static final int GRID_SIZE_BYTES = GRID_SIZE*GRID_SIZE*2;
    // тайлы воды
    public static final byte TILE_WATER_DEEP = 1;
    public static final byte TILE_WATER_LOW = 2;

    public static List<Grid> grids = new LinkedList<Grid>();
	public static final Coord map_size = new Coord(GRID_SIZE, GRID_SIZE);
	
	static Random gen = new Random();
	static Grid last = null;
	
	static public void RecvMapData(Coord[] coords , int x, int y, byte[] data) {
        Grid cg = null;
		// если какой то грид вышел за пределы видимости - удаляем его
		for (Grid g : grids) {
			if (g.gc.equals(new Coord(x,y))) {
                cg = g;
				g.set_data(data);
				GUI_Map.needUpdateView = true;
				break;
			}
		}

        if (cg == null) {
            cg = new Grid(new Coord(x,y), data);
		    grids.add(cg);
        }

		// апдейтим минимапу
//		if (dlg_Minimap.dlg != null) {
//			dlg_Minimap.dlg.minimap.UpdateMinimap(cg);
//		}

		// удаляем старые гриды которых нет в новых присланных сервером
		int i = 0;
		while (i < grids.size()) {
			Grid grid = grids.get(i);
			//grid.reset();
			boolean f = false;
			for (Coord c : coords) {
				if (grid.gc.equals(c)) {
					f = true;
					break;
				}
			}
			if (!f) {
				Log.debug("remove grid "+grid.toString());
				grids.remove(i);
			} else i++;
		}
	}

	static public Grid get_grid(Coord c) {
		for (Grid g : grids) {
			if (g.gc.equals(c)) 
				return g;
		}
		return null;
	}
	
	static public Sprite getground(Coord tc) {
		Grid g;
		Coord gc = tc.div(GRID_SIZE).mul(GRID_SIZE*TILE_SIZE);
		if((last != null) && last.gc.equals(gc))
			g = last;
		else
			last = g = get_grid(gc);
		if(g == null)
			return(null);
		Coord gtc = tc.mod(GRID_SIZE);
		if(g.ground_cache[gtc.x][gtc.y] == null) {
			ResTile rt = tile_sets[g.gettile(gtc)];
			if (rt != null)
				g.ground_cache[gtc.x][gtc.y] = rt.ground.pick(randoom(tc));
		}
		return(g.ground_cache[gtc.x][gtc.y]);
	}
		
	static public int getlevel(Coord tc) {
		//return 0;
		Grid g;
		Coord gc = tc.div(GRID_SIZE).mul(GRID_SIZE*TILE_SIZE);
		if((last != null) && last.gc.equals(gc))
			g = last;
		else
			last = g = get_grid(gc);
		if(g == null)
			return(0);
		Coord gtc = tc.mod(GRID_SIZE);
		return g.getlevel(gtc);
	}
	
	static public Sprite[] gettrans(Coord tc) {
		Grid g;
		Coord gc = tc.div(GRID_SIZE).mul(GRID_SIZE*TILE_SIZE);
		if((last != null) && last.gc.equals(gc))
			g = last;
		else
			last = g = get_grid(gc);
		if(g == null)
			return(null);
		Coord gtc = tc.mod(GRID_SIZE);
		if(g.trans_cache[gtc.x][gtc.y] == null) {
			int tr[][] = new int[3][3];
			for(int y = -1; y <= 1; y++) {
				for(int x = -1; x <= 1; x++) {
					if((x == 0) && (y == 0))
						continue;
					int tn = GetTile(tc.add(new Coord(x, y)));
					if(tn == 0)
						return(null);
					tr[x + 1][y + 1] = tn;
				}
			}
			if(tr[0][0] >= tr[1][0]) tr[0][0] = -1;
			if(tr[0][0] >= tr[0][1]) tr[0][0] = -1;
			if(tr[2][0] >= tr[1][0]) tr[2][0] = -1;
			if(tr[2][0] >= tr[2][1]) tr[2][0] = -1;
			if(tr[0][2] >= tr[0][1]) tr[0][2] = -1;
			if(tr[0][2] >= tr[1][2]) tr[0][2] = -1;
			if(tr[2][2] >= tr[2][1]) tr[2][2] = -1;
			if(tr[2][2] >= tr[1][2]) tr[2][2] = -1;
			int bx[] = {0, 1, 1, 2};
			int by[] = {1, 0, 2, 1};
			int cx[] = {0, 0, 2, 2};
			int cy[] = {0, 2, 2, 0};
			ArrayList<Sprite> buf = new ArrayList<Sprite>();
			for (int i = GetTile(tc) - 1; i >= 0; i--) {
				if ((tile_sets[i] == null) || (tile_sets[i].border_trans == null) || (tile_sets[i].corner_trans == null))
					continue;
				int bm = 0, cm = 0;
				for (int o = 0; o < 4; o++) {
					if(tr[bx[o]][by[o]] == i)
						bm |= 1 << o;
					if(tr[cx[o]][cy[o]] == i)
						cm |= 1 << o;
				}
				if (bm != 0)
					buf.add(tile_sets[i].border_trans[bm - 1].pick(randoom(tc)));
				if (cm != 0)
					buf.add(tile_sets[i].corner_trans[cm - 1].pick(randoom(tc)));
			}
			g.trans_cache[gtc.x][gtc.y] = buf.toArray(new Sprite[0]);
		}
		return(g.trans_cache[gtc.x][gtc.y]);
	}
	
	static public byte GetTile(Coord tc) {
		Coord c = tc.div(GRID_SIZE).mul(GRID_SIZE*TILE_SIZE);
		Coord gc = tc.mod(GRID_SIZE);
		Grid g = null;
		if ((last != null) && last.gc.equals(c))
			g = last;
		else
			last = g = get_grid(c);
		if (g != null)
			return (byte) (g.gettile(gc));
		else 
			return 0;
	}
	
	static public void Reset() {
		for (Grid g : grids) {
			g.reset();
		}
	}
	
	
	private static void initrandoom(Random r, Coord c) {
		r.setSeed(c.x);
		r.setSeed(r.nextInt() * c.y);
	}

	public static int randoom(Coord c) {
		int ret;

		synchronized(gen) {
			initrandoom(gen, c);
			ret = Math.abs(gen.nextInt());
			return(ret);
		}
	}

	public static int randoom(Coord c, int r) {
		return(randoom(c) % r);
	}

	public static Random mkrandoom(Coord c) {
		Random ret = new Random();
		initrandoom(ret, c);
		return(ret);
	}

}
