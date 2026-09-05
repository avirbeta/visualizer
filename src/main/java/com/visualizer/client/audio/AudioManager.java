package com.visualizer.client.audio;

import com.visualizer.VisualizerMod;
import com.visualizer.client.config.VisualizerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Lightweight Audio Manager for Visualizer UI sounds only.
 * Plays our own generated sound resources and never touches Minecraft audio settings.
 */
public final class AudioManager {
	public enum UiSound {
		HOVER, CLICK, OPEN, CLOSE, NOTIFICATION
	}

	private static final AudioManager INSTANCE = new AudioManager();

	private SoundEvent hover;
	private SoundEvent click;
	private SoundEvent open;
	private SoundEvent close;
	private SoundEvent notification;

	private AudioManager() {
	}

	public static AudioManager get() {
		return INSTANCE;
	}

	public void registerSounds() {
		hover = register("hover");
		click = register("click");
		open = register("open");
		close = register("close");
		notification = register("notification");
	}

	private static SoundEvent register(String name) {
		Identifier id = VisualizerMod.id(name);
		SoundEvent event = new SoundEvent(id);
		Registry.register(Registry.SOUND_EVENT, id, event);
		return event;
	}

	public void play(UiSound type) {
		if (hover == null) return;
		VisualizerConfig cfg = VisualizerConfig.get();
		if (!cfg.uiSounds || cfg.masterUiVolume <= 0.001F) return;

		boolean allowed;
		switch (type) {
			case HOVER: allowed = cfg.hoverSound; break;
			case CLICK: allowed = cfg.clickSound; break;
			case OPEN: allowed = cfg.openSound; break;
			case CLOSE: allowed = cfg.closeSound; break;
			case NOTIFICATION: allowed = cfg.notificationSound; break;
			default: return;
		}
		if (!allowed) return;

		SoundEvent event;
		float pitch;
		switch (type) {
			case HOVER: event = hover; pitch = 1.35F; break;
			case CLICK: event = click; pitch = 1.0F; break;
			case OPEN: event = open; pitch = 1.1F; break;
			case CLOSE: event = close; pitch = 0.9F; break;
			case NOTIFICATION: event = notification; pitch = 1.0F; break;
			default: return;
		}

		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc == null || mc.getSoundManager() == null) return;

		float volume = cfg.masterUiVolume
			* (type == UiSound.HOVER ? cfg.hoverVolume * 0.35F
				: type == UiSound.CLICK ? cfg.clickVolume * 0.7F : 0.7F);
		mc.getSoundManager().play(new PositionedSoundInstance(event.getId(), SoundCategory.MASTER,
			volume, pitch, false, 0, SoundInstance.AttenuationType.NONE, 0.0D, 0.0D, 0.0D, false));
	}
}