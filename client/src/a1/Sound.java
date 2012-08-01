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
package a1;

import a1.utils.Resource;

public class Sound { 
	private static Resource.ResSound current_music = null;
	
	
	public static void Play(String name) {
		if (!Config.SoundEnabled) return;
		
		Resource.ResSound sound = Resource.sound.get(name);
		if (sound != null)
			sound.oggEffect.playAsSoundEffect(1.0f, 1.0f, false);
	}
	
	public static void Play(String name, boolean loop) {
		if (!Config.SoundEnabled) return;
		
		Resource.ResSound sound = Resource.sound.get(name);
		if (sound != null)
			sound.oggEffect.playAsSoundEffect(1.0f, 1.0f, loop);		
	}
	
	public static void PlayMusic(String name) {
		if (!Config.SoundEnabled) return;
		
		Resource.ResSound sound = Resource.sound.get(name);
		if (sound != null) {
			sound.oggEffect.playAsMusic(1.0f, 1.0f, true);
			current_music = sound;
		}
	}
	
	public static void StopMusic() {
		if (current_music != null)
			current_music.oggEffect.stop();
		current_music = null;
	}
	
	public static void StopAll() {
		if (current_music != null)
			current_music.oggEffect.stop();
	}
}
