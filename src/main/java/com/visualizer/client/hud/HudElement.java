package com.visualizer.client.hud;

import com.visualizer.client.config.VisualizerConfig;
import com.visualizer.client.theme.ThemeManager;
import com.visualizer.client.ui.DrawUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A single movable HUD element. Read-only — never modifies player state.
 */
public class HudElement {
	/** Global text refresh rate, raised under load by HudManager. */
	public static int THROTTLE_MS = 120;

	public final String id;
	private final Supplier<String> text;
	private final BooleanSupplier visible;
	private float normX;
	private float normY;
	private float scale = 1F;

	private long lastTextTime;
	private String cachedText = "";
	private int cachedWidth;

	public HudElement(String id, float normX, float normY, Supplier<String> text, BooleanSupplier visible) {
		this.id = id;
		this.normX = normX;
		this.normY = normY;
		this.text = text;
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible.getAsBoolean();
	}

	public float normX() {
		return normX;
	}

	public float normY() {
		return normY;
	}

	public float scale() {
		return scale;
	}

	public void setNormalized(double x, double y) {
		this.normX = (float) x;
		this.normY = (float) y;
	}

	public void setScale(float scale) {
		this.scale = Math.max(0.5F, Math.min(2F, scale));
	}

	public int pixelX(int screenW) {
		return Math.round(normX * screenW);
	}

	public int pixelY(int screenH) {
		return Math.round(normY * screenH);
	}

	private String currentText() {
		long now = Util.getMeasuringTimeMs();
		if (now - lastTextTime >= THROTTLE_MS) {
			String t = text.get();
			cachedText = t == null ? "" : t;
			cachedWidth = MinecraftClient.getInstance().textRenderer.getWidth(cachedText) + 16;
			lastTextTime = now;
		}
		return cachedText;
	}

	public int width() {
		currentText();
		return cachedWidth;
	}

	public int height() {
		return 18;
	}

	public void render(MatrixStack matrices, int screenW, int screenH, boolean editor) {
		if (!isVisible() && !editor) return;
		ThemeManager.Theme theme = ThemeManager.get().current();
		String t = currentText();
		int x = pixelX(screenW);
		int y = pixelY(screenH);
		int w = MinecraftClient.getInstance().textRenderer.getWidth(t) + 16;
		int h = 18;
		float s = scale * VisualizerConfig.get().hudScale;

		matrices.push();
		matrices.translate(x, y, 0);
		matrices.scale(s, s, 1);

		int bg = editor
			? DrawUtil.withAlpha(theme.accent(), 60)
			: DrawUtil.withAlpha(theme.panel(), (int) (180 * theme.panelAlpha()));
		DrawUtil.roundedRect(matrices, 0, 0, w, h, 6, bg);
		if (editor) {
			DrawUtil.outline(matrices, 0, 0, w, h, theme.accent());
		}
		DrawUtil.text(matrices, t, 8, 5, theme.text());

		matrices.pop();
	}

	/** First pass of the two-phase gameplay render — background into the open batch. */
	public void renderBackground(MatrixStack matrices, int screenW, int screenH) {
		if (!isVisible()) return;
		ThemeManager.Theme theme = ThemeManager.get().current();
		currentText();
		int x = pixelX(screenW);
		int y = pixelY(screenH);
		float s = scale * VisualizerConfig.get().hudScale;
		DrawUtil.batchRounded(x, y, cachedWidth * s, 18 * s, 6 * s,
			DrawUtil.withAlpha(theme.panel(), (int) (180 * theme.panelAlpha())));
	}

	/** Second pass of the two-phase gameplay render — font pass. */
	public void renderText(MatrixStack matrices, int screenW, int screenH) {
		if (!isVisible()) return;
		ThemeManager.Theme theme = ThemeManager.get().current();
		String t = currentText();
		int x = pixelX(screenW);
		int y = pixelY(screenH);
		float s = scale * VisualizerConfig.get().hudScale;
		matrices.push();
		matrices.translate(x, y, 0);
		matrices.scale(s, s, 1);
		DrawUtil.text(matrices, t, 8, 5, theme.text());
		matrices.pop();
	}

	public boolean contains(double mx, double my, int screenW, int screenH) {
		int x = pixelX(screenW);
		int y = pixelY(screenH);
		float s = scale * VisualizerConfig.get().hudScale;
		return mx >= x && my >= y && mx <= x + width() * s && my <= y + height() * s;
	}
}