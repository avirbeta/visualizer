package com.visualizer.client.input;

import com.visualizer.client.audio.AudioManager;
import com.visualizer.client.config.ConfigManager;
import com.visualizer.client.config.VisualizerConfig;
import com.visualizer.client.notifications.NotificationManager;
import com.visualizer.client.ui.screens.HudEditorScreen;
import com.visualizer.client.ui.screens.VisualizerMenuScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.lwjgl.glfw.GLFW;

/**
 * Visualizer-only keybinds. Never binds gameplay actions.
 */
public final class KeybindManager {
	public enum CaptureTarget {
		NONE, OPEN, TOGGLE_HUD, HUD_EDITOR, COMPACT, SETTINGS
	}

	private static final KeybindManager INSTANCE = new KeybindManager();

	private CaptureTarget capture = CaptureTarget.NONE;
	private final boolean[] wasDown = new boolean[512];

	private KeybindManager() {
	}

	public static KeybindManager get() {
		return INSTANCE;
	}

	public void register() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
	}

	public void beginCapture(CaptureTarget target) {
		this.capture = target;
	}

	public boolean isCapturing() {
		return capture != CaptureTarget.NONE;
	}

	private void onTick(MinecraftClient client) {
		if (client == null || client.getWindow() == null) return;
		long handle = client.getWindow().getHandle();
		VisualizerConfig cfg = VisualizerConfig.get();

		if (isCapturing()) {
			for (int key = 32; key < 350; key++) {
				boolean down = GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
				if (down && !wasDown[key]) {
					assign(cfg, capture, key);
					wasDown[key] = true;
					capture = CaptureTarget.NONE;
					ConfigManager.save();
					AudioManager.get().play(AudioManager.UiSound.CLICK);
					break;
				}
			}
			snapshot(handle);
			return;
		}

		if (pressed(handle, cfg.keyOpenVisualizer) || pressed(handle, cfg.keyOpenSettings)) {
			if (client.currentScreen instanceof VisualizerMenuScreen) {
				client.openScreen(null);
				AudioManager.get().play(AudioManager.UiSound.CLOSE);
			} else if (client.currentScreen == null || client.currentScreen instanceof TitleScreen) {
				client.openScreen(new VisualizerMenuScreen(client.currentScreen));
				AudioManager.get().play(AudioManager.UiSound.OPEN);
			}
		}
		if (pressed(handle, cfg.keyToggleHud)) {
			cfg.hudVisible = !cfg.hudVisible;
			ConfigManager.save();
			AudioManager.get().play(AudioManager.UiSound.CLICK);
			NotificationManager.get().push(cfg.hudVisible ? "HUD shown" : "HUD hidden");
		}
		if (pressed(handle, cfg.keyToggleHudEditor) && client.player != null) {
			client.openScreen(new HudEditorScreen(client.currentScreen));
			AudioManager.get().play(AudioManager.UiSound.OPEN);
		}
		if (pressed(handle, cfg.keyToggleCompact)) {
			cfg.compactMode = !cfg.compactMode;
			ConfigManager.save();
			AudioManager.get().play(AudioManager.UiSound.CLICK);
			NotificationManager.get().push(cfg.compactMode ? "Compact mode ON" : "Compact mode OFF");
		}

		snapshot(handle);
	}

	private void assign(VisualizerConfig cfg, CaptureTarget target, int key) {
		switch (target) {
			case OPEN: cfg.keyOpenVisualizer = key; break;
			case TOGGLE_HUD: cfg.keyToggleHud = key; break;
			case HUD_EDITOR: cfg.keyToggleHudEditor = key; break;
			case COMPACT: cfg.keyToggleCompact = key; break;
			case SETTINGS: cfg.keyOpenSettings = key; break;
			default: break;
		}
	}

	private boolean pressed(long handle, int key) {
		if (key < 0 || key >= wasDown.length) return false;
		boolean down = GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
		return down && !wasDown[key];
	}

	private void snapshot(long handle) {
		if (isCapturing()) {
			for (int key = 32; key < 350; key++) {
				wasDown[key] = GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
			}
			return;
		}
		VisualizerConfig cfg = VisualizerConfig.get();
		record(handle, cfg.keyOpenVisualizer);
		record(handle, cfg.keyOpenSettings);
		record(handle, cfg.keyToggleHud);
		record(handle, cfg.keyToggleHudEditor);
		record(handle, cfg.keyToggleCompact);
	}

	private void record(long handle, int key) {
		if (key < 0 || key >= wasDown.length) return;
		wasDown[key] = GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
	}

	public static String keyName(int key) {
		if (key < 0) return "None";
		String name = GLFW.glfwGetKeyName(key, 0);
		if (name != null) return name.toUpperCase();
		switch (key) {
			case GLFW.GLFW_KEY_RIGHT_SHIFT: return "RSHIFT";
			case GLFW.GLFW_KEY_LEFT_SHIFT: return "LSHIFT";
			case GLFW.GLFW_KEY_RIGHT_CONTROL: return "RCTRL";
			case GLFW.GLFW_KEY_LEFT_CONTROL: return "LCTRL";
			case GLFW.GLFW_KEY_F10: return "F10";
			case GLFW.GLFW_KEY_F11: return "F11";
			default: return "KEY" + key;
		}
	}
}