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
import a1.utils.Resource;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;

import static org.lwjgl.opengl.GL11.*;

public class Sprite {
	private static Color static_color = null;
	public static int binded_texture_id = -1;
    public static Sprite dummy = new Sprite("");

	private Texture		texture = null;
	private String 		texture_name = "";
	
	// texture coords
	private int			tx;
	private int			ty;
	private int			tw;
	private int			th;
	private int			offx;
	private int			offy;
	public Sprite(Texture texture) {
            this.texture = texture;
            tx = 0;
            ty = 0;
            tw = texture.getImageWidth();
            th = texture.getImageHeight();
	}
	
	public Sprite(String tex_name) {
        if (tex_name.equals("")) return;
		this.texture_name = tex_name;
		texture = Resource.textures.get(tex_name);
		tx = 0;
		ty = 0;
		tw = texture.getImageWidth();
		th = texture.getImageHeight();
	}
	
	public Sprite(String tex_name, int offx, int offy, int tx, int ty, int tw, int th) {
		texture = Resource.textures.get(tex_name);
		this.tx = tx;
		this.ty = ty;
		this.tw = tw;
		this.th = th;
		this.offx = offx;
		this.offy = offy;
	}
	
	public Sprite(String tex_name, int tx, int ty, int tw, int th) {
		this.texture_name = tex_name;
		texture = Resource.textures.get(tex_name);
		this.tx = tx;
		this.ty = ty;
		this.tw = tw;
		this.th = th;
		this.offx = 0;
		this.offy = 0;
	}
	
	public String getTextureName() {
		return texture_name;
	}

	public Texture getTexture() {
		return texture;
	}
	
	public void ChangeTexture(Texture new_tex) {
		this.texture = new_tex;
	}
	 
	public int getPixelAlpha(int x, int y) {
		if (texture == null) return 0;
		if (!texture.hasAlpha()) return (byte) 0;
			       
	    x += tx; 
	    y += ty;  
	       
	    byte a = Resource.getTextureData(texture).pixel_data[((x + (y * texture.getImageWidth()))*4)+3];
	    return a & 0xff;
	}
	
	public static void setStaticColor(Color col) {
		static_color = col;
	}
	
	public static void setStaticColor() {
		static_color = null;
	}
	
	public int getWidth() {
		return tw;
	}

	public int getHeight() {
		return th;
	}
	
	public boolean isError() {
		return texture == null;
	}

	// draw all sprite in coord
	public void draw(Coord sc) {
		draw(sc.x, sc.y, tw, th, tx, ty, tw, th, Color.white);
	}
	
	public void draw(int x, int y) {
		draw(x,y,tw, th, tx, ty, tw, th, Color.white);
	}
	// draw sprite with scale
	public void draw(int x, int y, int w, int h) {
		draw(x, y, w, h, tx, ty, tw, th, Color.white);
	}

	public void draw(int x, int y, int w, int h, int tx, int ty, int tw, int th, Color col) {
		if (texture == null) return;
		float texwidth = texture.getTextureWidth();
		float texheight = texture.getTextureHeight();

		float newTextureOffsetX = tx / texwidth;
		float newTextureOffsetY = ty / texheight;
		float newTextureWidth = tw / texwidth;
		float newTextureHeight = th / texheight;
		 
		// bind to the appropriate texture for this sprite
		if (binded_texture_id != texture.getTextureID()) {
			texture.bind();
			binded_texture_id = texture.getTextureID();
		}

		// translate to the right location and prepare to draw
		glPushMatrix(); // push pop - FASTER than translate
		glTranslatef(x-offx, y-offy, 0);
		if (static_color != null)
			col = static_color;
//		else
//			col = Color.white;
		glColor4f(col.r, col.g, col.b, col.a);
		// draw a quad textured to match the sprite
		glBegin(GL_QUADS);
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
		glEnd();
		//glTranslatef(-x+offx, -y+offy, 0);
		glPopMatrix();

	}

	public void draw_map_vbo(Coord sc) {
        if (texture == null) return;
		if (GUI_Map.tile_vboSize < GUI_Map.tile_Offset + 16)
			return;
		
		float texwidth = texture.getTextureWidth();
		float texheight = texture.getTextureHeight();
		
		float newTextureOffsetX = tx / texwidth;
		float newTextureOffsetY = ty / texheight;
		float newTextureWidth = tw / texwidth;
		float newTextureHeight = th / texheight;
		
		map_vbo_putVert(sc.x, sc.y);
		map_vbo_putTex(newTextureOffsetX, newTextureOffsetY);
		
		map_vbo_putVert(sc.x, sc.y + th);
		map_vbo_putTex(newTextureOffsetX, newTextureOffsetY + newTextureHeight);
		
		map_vbo_putVert(sc.x + tw, sc.y + th);
		map_vbo_putTex(newTextureOffsetX + newTextureWidth, newTextureOffsetY + newTextureHeight);
		
		map_vbo_putVert(sc.x + tw, sc.y);
		map_vbo_putTex(newTextureOffsetX + newTextureWidth, newTextureOffsetY);
	}

	private void map_vbo_putTex(float x, float y) {
		GUI_Map.tile_vboUpdate[GUI_Map.tile_Offset++] = x;
		GUI_Map.tile_vboUpdate[GUI_Map.tile_Offset++] = y;
	}

	private void map_vbo_putVert(int x, int y) {
		GUI_Map.tile_vboUpdate[GUI_Map.tile_Offset++] = x;
		GUI_Map.tile_vboUpdate[GUI_Map.tile_Offset++] = y;
	}
}