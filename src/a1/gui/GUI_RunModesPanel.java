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

import a1.net.NetGame;

public class GUI_RunModesPanel extends GUI_Control {
	GUI_Button btn1, btn2, btn3, btn4;
	public int CurrentMode = 0;
	
	
	public GUI_RunModesPanel(GUI_Control parent) {
		super(parent);
		
		btn1 = new GUI_Button(this) {
			
			public void DoClick() {
				SetRunMode(1);
			}
			
			protected boolean getPressed() {
				return CurrentMode == 1;
			}
		};
		btn2 = new GUI_Button(this){
			
			public void DoClick() {
				SetRunMode(2);
			}
			
			protected boolean getPressed() {
				return CurrentMode == 2;
			}
		};
		btn3 = new GUI_Button(this){
			
			public void DoClick() {
				SetRunMode(3);
			}
			
			protected boolean getPressed() {
				return CurrentMode == 3;
			}
		};
		btn4 = new GUI_Button(this){
			
			public void DoClick() {
				SetRunMode(4);
			}
			
			protected boolean getPressed() {
				return CurrentMode == 4;
			}
		};
		
		btn1.caption = "1";
		btn2.caption = "2";
		btn3.caption = "3";
		btn4.caption = "4";
		
		btn1.SetSize(15, 15);
		btn2.SetSize(15, 15);
		btn3.SetSize(15, 15);
		btn4.SetSize(15, 15);
		
		btn1.SetPos(0, 0);
		btn2.SetPos(15, 0);
		btn3.SetPos(30, 0);
		btn4.SetPos(45, 0);
		
		SetSize(60, 14);
	}

	// сервер установил режим
	public void HandleRunMode(int mode) {
		CurrentMode = mode;
	}
	
	// кликнули и хотим установить режим
	public void SetRunMode(int mode) {
		NetGame.SEND_speed(mode);
		CurrentMode = mode;
	}
	

}
