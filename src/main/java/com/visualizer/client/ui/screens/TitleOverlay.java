package com.visualizer.client.ui.screens;

import com.visualizer.client.animation.AnimationEngine;
import com.visualizer.client.animation.TimeUtil;
import com.visualizer.client.audio.AudioManager;
import com.visualizer.client.config.VisualizerConfig;
import com.visualizer.client.theme.ThemeManager;
import com.visualizer.client.ui.DrawUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;

/**
 * A single small, unobtrusive corner button on the vanilla title screen.
 * The vanilla main menu stays fully intact — nothing is covered or replaced.
 */
public final class TitleOverlay {
	private static final TitleOverlay INSTANCE = new TitleOverlay();

	private TitleScreen screen;
	private boolean installed;
	private float hoverAnim;
	private boolean wasHover;

	private TitleOverlay() {
	}

	public static TitleOverlay get() {
		return INSTANCE;
	}

	public void install(TitleScreen screen) {
		this.screen = screen;
		this.installed = true;
	}

	public void uninstall() {
		this.installed = false;
		this.screen = null;
	}

	private int bx(int w) {
		return w - 162;
	}

	private int by() {
		return 10;
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (!installed) return;
		MinecraftClient mc = MinecraftClient.getInstance();
		int w = mc.getWindow().getScaledWidth();

		int bw = 150;
		int bh = 30;
		int bx = bx(w);
		int by = by();

		boolean hover = mouseX >= bx && mouseY >= by && mouseX <= bx + bw && mouseY <= by + bh;
		VisualizerConfig cfg = VisualizerConfig.get();
		float target = hover ? 1F : 0F;
		float dt = Math.max(TimeUtil.delta(), 0.001F);
		if (cfg.hoverAnimation && cfg.animationsEnabled) {
			hoverAnim = AnimationEngine.approach(hoverAnim, target, 12F, dt);
		} else {
			hoverAnim = target;
		}
		if (hover && !wasHover) {
			AudioManager.get().play(AudioManager.UiSound.HOVER);
		}
		wasHover = hover;

		ThemeManager.Theme theme = ThemeManager.get().current();

		int glow = DrawUtil.withAlpha(theme.accent(), (int) (28 + 55 * hoverAnim));
		DrawUtil.roundedRect(matrices, bx - 2, by - 2, bw + 4, bh + 4, 11, glow);
		DrawUtil.roundedRect(matrices, bx, by, bw, bh, 9,
			hover ? theme.panelHoverArgb() : DrawUtil.withAlpha(theme.panel(), 190));
		DrawUtil.text(matrices, "Visualizer", bx + 16, by + 11, theme.text());
		DrawUtil.fill(matrices, bx + 12, by + bh - 3, 22 + (int) (18 * hoverAnim), 2, theme.accent());
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!installed || button != 0) return false;
		MinecraftClient mc = MinecraftClient.getInstance();
		int w = mc.getWindow().getScaledWidth();
		int bx = bx(w);
		int by = by();
		boolean hit = mouseX >= bx && mouseY >= by && mouseX <= bx + 150 && mouseY <= by + 30;
		if (!hit) return false;
		AudioManager.get().play(AudioManager.UiSound.OPEN);
		mc.openScreen(new VisualizerMenuScreen(screen));
		return true;
	}
}