package com.visualizer.client.notifications;

import com.visualizer.client.animation.Easing;
import com.visualizer.client.animation.TimeUtil;
import com.visualizer.client.audio.AudioManager;
import com.visualizer.client.config.VisualizerConfig;
import com.visualizer.client.theme.ThemeManager;
import com.visualizer.client.ui.DrawUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * In-game HUD toasts. Pure visuals with float-in / float-out animation.
 */
public final class NotificationManager {
	private static final NotificationManager INSTANCE = new NotificationManager();
	private static final int MAX_NOTES = 3;
	private static final float SHOW_SECONDS = 4F;

	private final List<Note> notes = new ArrayList<>();

	private NotificationManager() {
	}

	public static NotificationManager get() {
		return INSTANCE;
	}

	public void register() {
		push("Visualizer ready");
	}

	public void push(String message) {
		notes.add(new Note(message));
		while (notes.size() > MAX_NOTES) {
			notes.remove(0);
		}
		AudioManager.get().play(AudioManager.UiSound.NOTIFICATION);
	}

	public void render(MatrixStack matrices, int screenW, int screenH) {
		VisualizerConfig cfg = VisualizerConfig.get();
		if (!cfg.visualizerEnabled || !cfg.hudNotifications) return;

		float dt = TimeUtil.delta();
		if (dt > 0F) {
			for (Note n : notes) {
				n.age += dt;
			}
		}
		Iterator<Note> it = notes.iterator();
		while (it.hasNext()) {
			if (it.next().done()) it.remove();
		}

		ThemeManager.Theme theme = ThemeManager.get().current();
		int width = 224;
		int x = screenW - width - 16;
		int y = 24;
		for (Note n : notes) {
			n.render(matrices, x, y, width, theme);
			y += 34;
		}
	}

	private final class Note {
		private static final int WIDTH = 224;
		private final String message;
		private String label;
		private boolean truncated;
		private float age;

		private Note(String message) {
			this.message = message;
		}

		private String label() {
			if (!truncated && MinecraftClient.getInstance().textRenderer != null) {
				label = truncate(message);
				truncated = true;
			}
			return truncated ? label : message;
		}

		private String truncate(String message) {
			String label = message;
			int tw = DrawUtil.textWidth(label);
			int limit = WIDTH - 22;
			if (tw > limit) {
				while (tw > limit && label.length() > 3) {
					label = label.substring(0, label.length() - 2);
					tw = DrawUtil.textWidth(label + "…");
				}
				label = label + "…";
			}
			return label;
		}

		private boolean done() {
			return age > SHOW_SECONDS;
		}

		private void render(MatrixStack m, int x, int y, int width, ThemeManager.Theme theme) {
			float in = Math.min(1F, age / 0.25F);
			float out = Math.max(0F, Math.min(1F, (SHOW_SECONDS - age) / 0.5F));
			float alpha = Math.min(in, out);
			if (alpha <= 0F) return;

			boolean animated = VisualizerConfig.get().notificationAnimation;
			float slide = animated ? (1F - Easing.easeOutCubic(in)) * 28F : 0F;
			int sx = x + Math.round(slide);

			DrawUtil.roundedRect(m, sx, y, width, 26, 8,
				DrawUtil.withAlpha(theme.panel(), (int) (225 * alpha)));
			DrawUtil.roundedRect(m, sx, y + 5, 3, 16, 1,
				DrawUtil.withAlpha(theme.accent(), (int) (255 * alpha)));

			MinecraftClient.getInstance().textRenderer.draw(m, label(), sx + 12, y + 9,
				DrawUtil.withAlpha(theme.text(), (int) (255 * alpha)));
		}
	}
}