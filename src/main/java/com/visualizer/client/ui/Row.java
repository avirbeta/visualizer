package com.visualizer.client.ui;

import com.visualizer.client.animation.AnimationEngine;
import com.visualizer.client.animation.TimeUtil;
import com.visualizer.client.audio.AudioManager;
import com.visualizer.client.config.VisualizerConfig;
import com.visualizer.client.theme.ThemeManager;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Base for Visualizer settings rows: shared hover behaviour, background and sound.
 */
public abstract class Row implements VzWidget {
	protected int x;
	protected int y;
	protected final int width;
	protected final int height;

	protected boolean hovered;
	protected boolean wasHovered;
	protected float hoverAnim;

	protected Row(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean isInside(double mx, double my) {
		return mx >= x && my >= y && mx <= x + width && my <= y + height;
	}

	/** Must be called at the top of render(): updates hover state, animation and hover sound. */
	protected void updateHover(int mouseX, int mouseY) {
		hovered = isInside(mouseX, mouseY);
		VisualizerConfig cfg = VisualizerConfig.get();
		float target = hovered ? 1F : 0F;
		if (cfg.hoverAnimation && cfg.animationsEnabled) {
			hoverAnim = AnimationEngine.approach(hoverAnim, target, 14F, TimeUtil.delta());
		} else {
			hoverAnim = target;
		}
		if (hovered && !wasHovered) {
			AudioManager.get().play(AudioManager.UiSound.HOVER);
		}
		wasHovered = hovered;
	}

	protected void drawBackground(MatrixStack matrices) {
		ThemeManager.Theme theme = ThemeManager.get().current();
		int bg = hovered
			? theme.panelHoverArgb()
			: com.visualizer.client.ui.DrawUtil.withAlpha(theme.panel(), 170);
		DrawUtil.roundedRect(matrices, x, y, width, height, 8, bg);
	}
}