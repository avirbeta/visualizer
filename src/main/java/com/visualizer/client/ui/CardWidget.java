package com.visualizer.client.ui;

import com.visualizer.client.animation.AnimationEngine;
import com.visualizer.client.animation.TimeUtil;
import com.visualizer.client.audio.AudioManager;
import com.visualizer.client.config.VisualizerConfig;
import com.visualizer.client.theme.ThemeManager;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Animated card button used by the Visualizer main menu overlay.
 * Hover: scale up + glow; click: press animation + sound.
 */
public class CardWidget implements VzWidget {
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final String title;
	private final String subtitle;
	private final Runnable onClick;

	private float hoverAnim;
	private float clickAnim;
	private boolean wasHovered;

	public CardWidget(int x, int y, int width, int height, String title, String subtitle, Runnable onClick) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.title = title;
		this.subtitle = subtitle;
		this.onClick = onClick;
	}

	public boolean isInside(double mx, double my) {
		return mx >= x && my >= y && mx <= x + width && my <= y + height;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		VisualizerConfig cfg = VisualizerConfig.get();
		float dt = TimeUtil.delta();
		boolean hovered = isInside(mouseX, mouseY);

		float target = hovered ? 1F : 0F;
		if (cfg.hoverAnimation && cfg.animationsEnabled) {
			hoverAnim = AnimationEngine.approach(hoverAnim, target, 12F, dt);
		} else {
			hoverAnim = target;
		}
		clickAnim = AnimationEngine.approach(clickAnim, 0F, 10F, dt);

		if (hovered && !wasHovered) {
			AudioManager.get().play(AudioManager.UiSound.HOVER);
		}
		wasHovered = hovered;

		ThemeManager.Theme theme = ThemeManager.get().current();
		float scale = 1F + hoverAnim * 0.035F - clickAnim * 0.04F;
		if (!cfg.uiScaleAnimation) scale = 1F;

		int cx = x + width / 2;
		int cy = y + height / 2;
		matrices.push();
		matrices.translate(cx, cy, 0);
		matrices.scale(scale, scale, 1);
		matrices.translate(-cx, -cy, 0);

		int fill = DrawUtil.lerpColor(theme.panelArgb(), theme.panelHoverArgb(), hoverAnim);
		int radius = Math.round(theme.cornerRadius());

		if (theme.glow() && cfg.fancyUi) {
			int glow = DrawUtil.withAlpha(theme.accent(), (int) (40 + 50 * hoverAnim));
			DrawUtil.roundedRect(matrices, x - 1, y - 1, width + 2, height + 2, radius + 1, glow);
		}
		DrawUtil.roundedRect(matrices, x, y, width, height, radius, fill);

		if (theme.shadows()) {
			DrawUtil.fill(matrices, x + 3, y + height - 2, width - 6, 6,
				DrawUtil.withAlpha(0xFF000000, (int) (30 + 20 * hoverAnim)));
		}

		int accentH = 3;
		DrawUtil.fill(matrices, x + 14, y + height - 10 - accentH, 36 + (int) (20 * hoverAnim), accentH, theme.accent());

		DrawUtil.text(matrices, title, x + 16, y + 16, theme.text());
		if (subtitle != null && !subtitle.isEmpty()) {
			DrawUtil.text(matrices, subtitle, x + 16, y + 34, theme.textMuted());
		}

		matrices.pop();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || !isInside(mouseX, mouseY) || onClick == null) return false;
		clickAnim = 1F;
		AudioManager.get().play(AudioManager.UiSound.CLICK);
		onClick.run();
		return true;
	}
}