package com.visualizer.client;

import com.visualizer.VisualizerMod;
import com.visualizer.client.animation.AnimationEngine;
import com.visualizer.client.audio.AudioManager;
import com.visualizer.client.config.ConfigManager;
import com.visualizer.client.config.VisualizerConfig;
import com.visualizer.client.hud.HudManager;
import com.visualizer.client.input.KeybindManager;
import com.visualizer.client.notifications.NotificationManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Client bootstrap for the Visualizer visual systems.
 */
public final class VisualizerClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ConfigManager.load();

		AudioManager.get().registerSounds();
		AnimationEngine.get().setGlobalSpeed(VisualizerConfig.get().animationSpeed);
		HudManager.get().register();
		NotificationManager.get().register();
		KeybindManager.get().register();

		ClientTickEvents.END_CLIENT_TICK.register(client ->
			AnimationEngine.get().tick(0.05F));

		VisualizerMod.LOGGER.info("Visualizer client systems ready.");
	}
}