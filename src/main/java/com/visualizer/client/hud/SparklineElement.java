package com.visualizer.client.hud;

import com.visualizer.client.config.VisualizerConfig;
import com.visualizer.client.theme.ThemeManager;
import com.visualizer.client.ui.DrawUtil;
import net.minecraft.client.util.math.MatrixStack;

import java.util.function.Supplier;

/**
 * Small live FPS sparkline. Read-only — the samples are fed by HudManager.
 */
public final class SparklineElement extends HudElement {
	private static final int W = 64;
	private static final int H = 26;
	private static final int BARS = 28;

	private final Supplier<float[]> samples;

	public SparklineElement(String id, float normX, float normY, Supplier<float[]> samples) {
		super(id, normX, normY, () -> "", () -> VisualizerConfig.get().hudFpsGraph);
		this.samples = samples;
	}

	@Override
	public int width() {
		return W;
	}

	@Override
	public int height() {
		return H;
	}

	@Override
	public void render(MatrixStack matrices, int screenW, int screenH, boolean editor) {
		if (editor) {
			renderEditor(matrices, screenW, screenH);
			return;
		}
		renderBackground(matrices, screenW, screenH);
		renderText(matrices, screenW, screenH);
	}

	@Override
	public void renderBackground(MatrixStack matrices, int screenW, int screenH) {
		if (!isVisible()) return;
		ThemeManager.Theme theme = ThemeManager.get().current();
		int x = pixelX(screenW);
		int y = pixelY(screenH);
		float s = scale() * VisualizerConfig.get().hudScale;
		DrawUtil.batchRounded(x, y, W * s, H * s, 6 * s,
			DrawUtil.withAlpha(theme.panel(), (int) (180 * theme.panelAlpha())));
		float[] arr = samples.get();
		int n = arr == null ? 0 : arr.length;
		int start = Math.max(0, n - BARS);
		int count = Math.min(BARS, n);
		for (int i = 0; i < count; i++) {
			int v = (int) arr[start + i];
			int barH = (int) (Math.min(60F, Math.max(0F, v)) / 60F * 16F);
			DrawUtil.batchQuad(x + (6 + i * 2) * s, y + (22 - barH) * s, s, barH * s, theme.accent());
		}
	}

	@Override
	public void renderText(MatrixStack matrices, int screenW, int screenH) {
		if (!isVisible()) return;
		ThemeManager.Theme theme = ThemeManager.get().current();
		int x = pixelX(screenW);
		int y = pixelY(screenH);
		float s = scale() * VisualizerConfig.get().hudScale;
		matrices.push();
		matrices.translate(x, y, 0);
		matrices.scale(s, s, 1);
		DrawUtil.text(matrices, "FPS", 4, 3, theme.text());
		matrices.pop();
	}

	private void renderEditor(MatrixStack matrices, int screenW, int screenH) {
		ThemeManager.Theme theme = ThemeManager.get().current();
		int x = pixelX(screenW);
		int y = pixelY(screenH);
		float s = scale() * VisualizerConfig.get().hudScale;

		matrices.push();
		matrices.translate(x, y, 0);
		matrices.scale(s, s, 1);

		DrawUtil.roundedRect(matrices, 0, 0, W, H, 6, DrawUtil.withAlpha(theme.accent(), 60));
		DrawUtil.outline(matrices, 0, 0, W, H, theme.accent());
		DrawUtil.text(matrices, "FPS", 4, 3, theme.text());

		float[] arr = samples.get();
		int n = arr == null ? 0 : arr.length;
		int start = Math.max(0, n - BARS);
		int count = Math.min(BARS, n);
		for (int i = 0; i < count; i++) {
			int v = (int) arr[start + i];
			int barH = (int) (Math.min(60F, Math.max(0F, v)) / 60F * 16F);
			DrawUtil.fill(matrices, 6 + i * 2, 22 - barH, 1, barH, theme.accent());
		}

		matrices.pop();
	}
}