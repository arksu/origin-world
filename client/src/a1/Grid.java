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

import static a1.MapCache.GRID_SIZE;

public class Grid {
	public byte[][] tiles;
	public byte[][] levels;
	public Sprite[][] ground_cache = null;
	public Sprite[][][] trans_cache = null;
	public byte ol[][];
	public Coord gc;

	public Grid(Coord c, byte[] data) {
		this.gc = c;
        ground_cache = new Sprite[GRID_SIZE][GRID_SIZE];
        trans_cache = new Sprite[GRID_SIZE][GRID_SIZE][];
		tiles = new byte[GRID_SIZE][GRID_SIZE];
		levels = new byte[GRID_SIZE][GRID_SIZE];
        fill_tiles(data);
		fill_levels(data);
//        save_debug();
	}

    public void set_data(byte[] data) {
        ground_cache = new Sprite[GRID_SIZE][GRID_SIZE];
        trans_cache = new Sprite[GRID_SIZE][GRID_SIZE][];
        tiles = new byte[GRID_SIZE][GRID_SIZE];
        levels = new byte[GRID_SIZE][GRID_SIZE];
        fill_tiles(data);
        fill_levels(data);
//        save_debug();
    }
	
	private void fill_tiles(byte [] data) {
		for (int i=0; i < GRID_SIZE; i++) {
			System.arraycopy(data, i*GRID_SIZE, tiles[i], 0, GRID_SIZE);
		}
	}
	
	private void fill_levels(byte [] data) {
		for (int i=0; i < GRID_SIZE; i++) {
			System.arraycopy(data, 10000 + i*GRID_SIZE, levels[i], 0, GRID_SIZE);
		}
	}
	
	public int gettile(Coord tc) {
		return(tiles[tc.y][tc.x] & 0xff);
	}
	
	public int gettile(int x, int y) {
		// yep, its right.
		return(tiles[y][x] & 0xff);
	}

	public int getlevel(Coord tc) {
		// yep, its right.
		return ((levels[tc.y][tc.x]) & 0xff);
	}
		
	public int getol(Coord tc) {
		return(ol[tc.x][tc.y]);
	}
	
	public void reset() {
		ground_cache = null;
		trans_cache = null;
		ground_cache = new Sprite[GRID_SIZE][GRID_SIZE];
		trans_cache = new Sprite[GRID_SIZE][GRID_SIZE][];
	}

    /*
    public void save_debug() {
        FileWriter fstream = null;
        try {
            fstream = new FileWriter(gc.toString());
            BufferedWriter out = new BufferedWriter(fstream);
            for (int y = 0; y<GRID_SIZE; y++) {
                for (int x=0; x<GRID_SIZE; x++) {
                    out.write(String.valueOf(tiles[x][y]));
                }
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    */
	
	public String toString() {
		return gc.toString();
	}

}
