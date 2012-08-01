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
package a1.utils;

import a1.Render2D;

public class Align {
	public static boolean isBottom(byte value) {
		return Render2D.Align_Bottom == (value & Render2D.Align_VStretch);
	}
	public static boolean isCenter(byte value) {
		return Render2D.Align_Center == (value & Render2D.Align_Stretch);
	}
	public static boolean isDefault(byte value) {
		return Render2D.Align_Default == (value & Render2D.Align_Stretch);
	}
	public static boolean isHCenter(byte value) {
		return Render2D.Align_HCenter == (value & Render2D.Align_HStretch);
	}
	public static boolean isHStretch(byte value) {
		return Render2D.Align_HStretch == (value & Render2D.Align_HStretch);
	}
	public static boolean isLeft(byte value) {
		return Render2D.Align_Left == (value & Render2D.Align_HStretch);
	}
	public static boolean isRight(byte value) {
		return Render2D.Align_Right == (value & Render2D.Align_HStretch);
	}
	public static boolean isStretch(byte value) {
		return Render2D.Align_Stretch == (value & Render2D.Align_Stretch);
	}
	public static boolean isTop(byte value) {
		return Render2D.Align_Top == (value & Render2D.Align_VStretch);
	}
	public static boolean isVCenter(byte value) {
		return Render2D.Align_VCenter == (value & Render2D.Align_VStretch);
	}
	public static boolean isVStretch(byte value) {
		return Render2D.Align_VStretch == (value & Render2D.Align_VStretch);
	}

	
}
