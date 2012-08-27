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

import a1.gui.GUI;
import a1.utils.FlyParam;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

public class ObjExp extends ObjEffect {
	public int combat = 0;
	public int industry = 0;
	public int nature = 0;
	
	private long life_time = 0;
	
	private static int MAX_LIFE_TIME = 1400;
	
	public ObjExp(int combat, int industry, int nature) {
		this.combat=combat;
		this.industry=industry;
		this.nature=nature;
	}
	
	public void update() {
		life_time += Main.dt;
		if (life_time > MAX_LIFE_TIME) alive = false;
	}

	@SuppressWarnings("AccessStaticViaInstance")
    public void render(Coord dc) {
		double t = (float)life_time/(float)MAX_LIFE_TIME;
		int a = FlyParam.GetAlpha(t);
		
		String s1 = Integer.toString(combat);
		String s2 = Integer.toString(industry);
		String s3 = Integer.toString(nature);
		String ss = "+"+s1+"/"+s2+"/"+s3;
		String s;
		int tw = Render2D.GetTextWidth("", ss);
		int x = dc.x-tw/2 - 7;
		int y = (int) (dc.y-(FlyParam.GetY(t)+35) - 38*GUI.map.scale);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		s = "+"; Render2D.Text("default", x, y,  
				s, new Color(255, 255, 255, a)); x+=Render2D.GetTextWidth("", s)+2;
		s = s1; Render2D.Text("default", x, y,  
				s, new Color(255, 40, 40, a)); x+=Render2D.GetTextWidth("", s)+4;
		s = "/"; Render2D.Text("default", x, y,  
				s, new Color(255, 255, 255, a)); x+=Render2D.GetTextWidth("", s)+2;
		s = s2; Render2D.Text("default", x, y,  
				s, new Color(100, 209, 232, a)); x+=Render2D.GetTextWidth("", s)+4;
		s = "/"; Render2D.Text("default", x, y,  
				s, new Color(255, 255, 255, a)); x+=Render2D.GetTextWidth("", s)+2;
		s = s3; Render2D.Text("default", x, y,  
				s, new Color(40, 255, 40, a));
		GL11.glPopMatrix();
	}
}
