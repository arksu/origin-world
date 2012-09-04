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


import a1.utils.BitmapFont;
import a1.utils.Rect;
import a1.utils.Resource;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

//--------------------------------------------
//---------------------------------------------------


public class Render2D {
	public static final int Align_HCenter   = 0;
	public static final int Align_VCenter   = 0;
	public static final int Align_Center    = Align_HCenter + Align_VCenter;

	public static final int Align_Left      = 2;
	public static final int Align_Right     = 4;
	public static final int Align_HStretch  = Align_Left + Align_Right;

	public static final int Align_Top       = 8;
	public static final int Align_Bottom    = 16;
	public static final int Align_VStretch  = Align_Top + Align_Bottom;

	public static final int Align_Stretch   = Align_HStretch + Align_VStretch;
	public static final int Align_Default   = Align_Left + Align_Top;
	
	private static Stack<Rect> scissors = new Stack<Rect>();
	private static Rect current_scissor = null;
	private static boolean enabled2d = false;
	private static Color col = Color.white;
	private static Map<String, BitmapFont> fonts = new HashMap<String, BitmapFont>();
	
	public static class GLException extends RuntimeException {
		private static final long serialVersionUID = 1388848249060682236L;
		public int code;
		public String str;

		public GLException(int code) {
			super("OpenGL Error: " + code + " (" + GLU.gluErrorString(code)
					+ ")");
			this.code = code;
			this.str = GLU.gluErrorString(code);
		}
	}

	static public void CheckError() {
		int error = GL11.glGetError();
		if(error != 0)
		    throw(new GLException(error));
	}

	static public void PushScissor(Rect s) {
		if (current_scissor == null)
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
		Rect NewRect;
		if (current_scissor == null)
			NewRect = CompareScissorRects(s, s);
		else
			NewRect = CompareScissorRects(s, current_scissor);
		
		scissors.push(current_scissor);
		current_scissor = NewRect;
		GL11.glScissor(NewRect.x, Config.getScreenHeight()-NewRect.y-NewRect.h, NewRect.w, NewRect.h);
		//GL11.glScissor(s.x, Config.ScreenHeight-s.y-s.h, s.w, s.h);
	}
	
	static public void PopScissor() {
		if (scissors.size() < 1) return;
		current_scissor = scissors.pop();
		if (current_scissor != null)
			GL11.glScissor(current_scissor.x, current_scissor.y, current_scissor.w, current_scissor.h);
		else
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
	
	static public Rect CompareScissorRects(Rect new_rect, Rect old_rect) {
		Rect result = new_rect;
		if (result.x < old_rect.x) result.x = old_rect.x;
		if (result.y < old_rect.y) result.y = old_rect.y;
		if (result.Right() > old_rect.Right()) result.SetRight(old_rect.Right());
		if (result.Bottom() > old_rect.Bottom()) result.SetBottom(old_rect.Bottom());

		if (result.x < 0) result.x = 0;
		if (result.y < 0) result.y = 0;
		return result;
	}
	

	
	static public void ChangeColor(int r, int g, int b, int a) {
		col.a = a / 255;
		col.b = b / 255;
		col.g = g / 255;
		col.r = r / 255;
	}
	
	static public void GLColor() {
		GL11.glColor4f(col.r, col.g, col.b, col.a);
	}
	
	static public void Vertex(Coord v) {
		GL11.glVertex2i(v.x, v.y);
	}
	
	static public void Line(Coord c1, Coord c2, float w) {
		GL11.glLineWidth(w);
		GL11.glBegin(GL11.GL_LINES);
		GLColor();
		Vertex(c1);
		Vertex(c2);
		GL11.glEnd();
		CheckError();
	}
	
	static public void FillRect(Coord c, Coord sz) {
		GLColor();
		GL11.glBegin(GL11.GL_QUADS);
		Vertex(c);
		Vertex(c.add(new Coord(sz.x, 0)));
		Vertex(c.add(sz));
		Vertex(c.add(new Coord(0, sz.y)));
		GL11.glEnd();
		CheckError();
	}


    static public void Rectangle(Coord ul, Coord sz, int width) {
        Coord ur, bl, br;
        ur = new Coord(ul.x + sz.x - width, ul.y);
        bl = new Coord(ul.x, ul.y + sz.y - width);
        br = new Coord(ur.x, bl.y);
        Line(ul, ur, width);
        Line(ur, br, width);
        Line(br, bl, width);
        Line(bl, ul, width);
    }

//---------------------------------------------------------------------------------------------------------------
  /*
    static public void ChangeColor() {
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    static public void ChangeColor(Color color) {
        glColor4f(color.r, color.g, color.b, color.a);
    }

    static public void FillRect(Coord position, Coord size, Color color) {
        glColor4f(color.r, color.g, color.b, color.a);
        GL11.glBegin(GL11.GL_QUADS);
        glVertex2i(position.x, position.y);
        glVertex2i(position.x + size.x, position.y);
        glVertex2i(position.x + size.x, position.y + size.y);
        glVertex2i(position.x, position.y + size.y);
        GL11.glEnd();
    }

    static public void Enable2D() {
        if (!enabled2d) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            Sprite.binded_texture_id = -1;
            enabled2d = true;
        }
    }

    static public void Disable2D() {
        if (enabled2d) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            Sprite.binded_texture_id = -1;
            enabled2d = false;
        }
    }

    static public void FillEllipse(Coord c, Coord r, int a1, int a2, Color color) {
        glColor4f(color.r, color.g, color.b, color.a);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        glVertex2i(c.x, c.y);
        for (int i = a1; i <= a2; i += 5) {
            double a = (i * Math.PI * 2) / 360.0;
            GL11.glVertex2d(c.x + Math.cos(a) * r.x, c.y - Math.sin(a) * r.y);
        }
        GL11.glEnd();
    }

    static public void FillEllipse(Coord c, Coord r) {
        FillEllipse(c, r, 0, 360, Color.white);
    }

    static public void Rectangle(Coord position, Coord size) {
        Rectangle(position, size, Color.white);
    }

    static public void Rectangle(Coord position, Coord size, Color color) {
        glColor4f(color.r, color.g, color.b, color.a);

        GL11.glBegin(GL11.GL_LINES);

        glVertex2i(position.x, position.y);
        glVertex2i(position.x + size.x, position.y);

        glVertex2i(position.x + size.x, position.y);
        glVertex2i(position.x + size.x, position.y + size.y);

        glVertex2i(position.x + size.x, position.y + size.y);
        glVertex2i(position.x, position.y + size.y);

        glVertex2i(position.x, position.y + size.y);
        glVertex2i(position.x, position.y);
        GL11.glEnd();
    }
                  */
//----------------------------------------------------------------------------------------------------------------


	static public void ChangeColor() {
		ChangeColor(Color.white);
	}

	static public void ChangeColor(Color c) {
		col = new Color(c.r, c.g, c.b, c.a);
	}

    static public void Enable2D() {
        if (!enabled2d) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            Sprite.binded_texture_id = -1;
            enabled2d = true;
        }
    }
    static public void Disable2D() {
    if (enabled2d) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Sprite.binded_texture_id = -1;
        enabled2d = false;
    }
}

     static public void FillRect(Coord c, Coord sz, Color col) {
        ChangeColor(col);
        FillRect(c, sz);
    }

    static public void FillRect(Coord c1, Coord c2, Coord c3, Coord c4) {
        GLColor();
        GL11.glBegin(GL11.GL_QUADS);
        Vertex(c1);
        Vertex(c2);
        Vertex(c3);
        Vertex(c4);
        GL11.glEnd();
        CheckError();
    }
    static public void FillEllipse(Coord c, Coord r, int a1, int a2) {
    GLColor();
    GL11.glBegin(GL11.GL_TRIANGLE_FAN);
    Vertex(c);
    for(int i = a1; i <= a2; i += 5) {
        double a = (i * Math.PI * 2) / 360.0;
        Vertex(c.add((int)(Math.cos(a) * r.x), -(int)(Math.sin(a) * r.y)));
    }
    GL11.glEnd();
    CheckError();
}

    static public void FillEllipse(Coord c, Coord r) {
        FillEllipse(c, r, 0, 360);
    }

    static public void Rectangle(Coord ul, Coord sz) {
        Rectangle(ul, sz, 1);
    }



//----------------------------------------------------------------------------------------------------------------

	static public Color GetColor() {
		return col;
	}
	
	static public void Text(String font_name, int x, int y, String text) {
		Text(font_name, x, y, text, Color.white);
	}
	
	static public void Text(String font_name, int x, int y, String text, Color col) {
		Sprite.binded_texture_id = -1;
		BitmapFont f;
		if (font_name.length() == 0)
			f = fonts.get("system");
		else
			f = fonts.get(font_name);
		if (f == null) f = fonts.get("system");
		if (f != null) {
            f.drawString(x+1, y+1, text, new Color(32, 45, 60));
			f.drawString(x, y, text, col);
		}
	}
	
	static public void Text(String font_name, int x, int y, int w, int h, int align, String text, Color col) {
		int ax = x + (w - GetTextWidth(font_name, text)) / 2;
		int ay = y + (h - GetTextHeight(font_name, text)) / 2;
		if ((align & Align_HStretch) != Align_HStretch) {
			if ((align & Align_Left) > 0) 
		      ax = x;
		    else if ((align & Align_Right) > 0)
		      ax = x + w - GetTextWidth(font_name, text);
		}
		if ((align & Align_VStretch) != Align_VStretch) {
			if ((align & Align_Top) > 0) 
		      ay = y;
		    else if ((align & Align_Bottom) > 0)
		      ay = y + h - GetTextHeight(font_name, text);
		}		
		Text(font_name, ax, ay, text, col);
	}
	
	static public int GetTextWidth(String font_name, String text) {
		BitmapFont f = font_name.length()==0?fonts.get("system"):fonts.get(font_name);
		if (f == null) f = fonts.get("system");
		if (f != null) {
			return f.getWidth(text);
		}
		return 0;
	}

    static public int GetTextHeight(String font_name) {
        return GetTextHeight(font_name, "AbcdeqrWklrt");
    }
	static public int GetTextHeight(String font_name, String text) {
		BitmapFont f = font_name.length()==0?fonts.get("system"):fonts.get(font_name);
		if (f == null) f = fonts.get("system");
		if (f != null) {
            return f.getHeight("AbqprW");
		}
		return 0;
	}
	
	static public void AddFont(String font_name, BitmapFont font) {
		fonts.put(font_name, font);
	}
	
	static public void LoadSystemFont() {
		BitmapFont f;
		try {
			f = new BitmapFont("font_system", 
					Main.class.getResourceAsStream("/etc/system.fnt"),
					Main.class.getResourceAsStream("/etc/system_0.png"),
					true);
			fonts.put("system", f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static public void LoadFont(String name) {
		BitmapFont f;
		try {
			f = new BitmapFont("font_"+name, 
					Resource.binary.get("font_"+name+"_fnt").get_stream(), 
					Resource.binary.get("font_"+name+"_tex").get_stream(), 
					true);
			fonts.put(name, f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static BitmapFont getFont(String font_name) {
		BitmapFont f;
		if (font_name.length() == 0)
			f = Render2D.fonts.get("system");
		else
			f = Render2D.fonts.get(font_name);
		if (f == null) f = Render2D.fonts.get("system");
		return f;
	}
	
	public static Coord getfontMetrics(String font, String text) {
		return new Coord(getFont(font).getWidth(text), getFont(font).getHeight(text));
	}
	
	static public void InitFonts() {
		LoadFont("smallfont");
	}
}
