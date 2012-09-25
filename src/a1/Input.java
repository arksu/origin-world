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
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public class Input {
	private GUI gui;
	private static boolean[] Keys = new boolean[256];
	private static boolean[] HitKeys = new boolean[256];
	private static int minDWheel = 0;
	public static boolean[] MouseBtns = new boolean[3];
	
	public static int MouseX = 0;
	public static int MouseY = 0;
	public static int MouseWheel = 0;
	
	public static final int MB_LEFT = 0;
	public static final int MB_RIGHT = 1;
	public static final int MB_MIDDLE = 2;
	public static final int MB_DOUBLE = 3;

    public static String debug_str = "";
	
	public Input(GUI gui) {
		this.gui = gui;
	}
	
	public void Update() {
		boolean old;
		int mw;
        Keyboard.poll();
		for (int i = 0; i < 256; i++) {
			old = Keys[i];
			Keys[i] = Keyboard.isKeyDown(i);
			HitKeys[i] = (!old && Keys[i]);
		}
		
		MouseX = Mouse.getX();
		MouseY = Display.getHeight() - Mouse.getY();
		mw = Mouse.getDWheel();
		
		// in MacOS wheel some stranged!
		if (mw != 0) {
			if (minDWheel == 0) 
				minDWheel = Math.abs(mw);
			if (Math.abs(mw) < minDWheel) {
				minDWheel = Math.abs(mw);
			}
			MouseWheel = mw / minDWheel;
		} else
			MouseWheel = 0;
			
		
		for (int i = 0; i < 3; i++) {
			MouseBtns[i] = Mouse.isButtonDown(i);
		}
		
		// обрабатываем введеный текст
		while (Keyboard.next()) {
			gui.HandleKey(	Lang.GetChar(Keyboard.getEventCharacter()),
							Keyboard.getEventKey(), 
							Keyboard.getEventKeyState()
						 );

            debug_str = "char="+Keyboard.getEventCharacter()+" key="+Keyboard.getEventKey()+" state="+Keyboard.getEventKeyState();
		}		
	} 
	
	public static void RemoveHit(int key) {
		HitKeys[key] = false;
	}
	
	static public boolean KeyHit(int key) {
		return HitKeys[key];
	}
	
	static public boolean KeyDown(int key) {
		return Keys[key];
	}
	
	static public boolean isCtrlPressed() {
		return KeyDown(Keyboard.KEY_LCONTROL) || KeyDown(Keyboard.KEY_RCONTROL);
	}
	
	static public boolean isShiftPressed() {
		return KeyDown(Keyboard.KEY_LSHIFT) || KeyDown(Keyboard.KEY_RSHIFT);
	}

	static public boolean isAltPressed() {
		return KeyDown(Keyboard.KEY_LMENU) || KeyDown(Keyboard.KEY_RMENU);
	}
	
	static public int GetKeyState() {
		return (isCtrlPressed()?1:0) + (isShiftPressed()?2:0) + (isAltPressed()?4:0);
	}
	
	static public boolean isWheelUpdated() {
		return MouseWheel != 0;
	}
}
