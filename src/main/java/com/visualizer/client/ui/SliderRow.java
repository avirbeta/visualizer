package com.visualizer.client.ui;

import com.visualizer.client.audio.AudioManager;
import com.visualizer.client.config.ConfigManager;
import com.visualizer.client.theme.ThemeManager;
import net.minecraft.client.util.math.MatrixStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Horizontal slider row for float settings.
 */
public class SliderRow extends Row {
	private final String label;
	private final Supplier<Float> getter;
	private final Consumer<Float> setter;
	private final float min;
	private final float max;
	private final String suffix;
	private boolean dragging;

	public SliderRow(int x, int y, int width, String label, float min, float max, String suffix,
	                 Supplier<Float> getter, Consumer<Float> setter) {
		super(x, y, width, 36);
		this.label = label;
		this.min = min;
		this.max = max;
		this.suffix = suffix;
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		updateHover(mouseX, mouseY);
		ThemeManager.Theme theme = ThemeManager.get().current();
		if (dragging) {
			DrawUtil.roundedRect(matrices, x, y, width, height, 8, theme.panelHoverArgb());
		} else {
			drawBackground(matrices);
		}

		float value = getter.get();
		DrawUtil.text(matrices, label + "  " + format(value) + suffix, x + 12, y + 6, theme.text());

		int trackX = x + 12;
		int trackW = width - 24;
		int trackY = y + 24;
		DrawUtil.roundedRect(matrices, trackX, trackY, trackW, 4, 2, DrawUtil.withAlpha(0xFF000000, 100));
		float t = (value - min) / (max - min);
		t = Math.max(0F, Math.min(1F, t));
		int fillW = Math.round(trackW * t);
		if (fillW > 0) {
			DrawUtil.roundedRect(matrices, trackX, trackY, fillW, 4, 2, theme.accent());
		}
		DrawUtil.roundedRect(matrices, trackX + fillW - 5, trackY - 3, 10, 10, 5, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || !isInside(mouseX, mouseY)) return false;
		dragging = true;
		applyMouse(mouseX);
		AudioManager.get().play(AudioManager.UiSound.CLICK);
		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (!dragging) return false;
		applyMouse(mouseX);
		return true;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (!dragging) return false;
		dragging = false;
		ConfigManager.save();
		return true;
	}

	private void applyMouse(double mouseX) {
		int trackX = x + 12;
		int trackW = width - 24;
		float t = (float) ((mouseX - trackX) / (double) trackW);
		t = Math.max(0F, Math.min(1F, t));
		setter.accept(min + (max - min) * t);
	}

	private String format(float v) {
		if (max <= 1.01F && min >= -0.01F) {
			return String.valueOf(Math.round(v * 100F));
		}
		if (Math.abs(max - min) <= 3F) {
			return String.format("%.2f", v);
		}
		return String.valueOf(Math.round(v));
	}
}