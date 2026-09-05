package com.visualizer.client.ui;

import com.visualizer.client.audio.AudioManager;
import com.visualizer.client.config.ConfigManager;
import com.visualizer.client.config.VisualizerConfig;
import com.visualizer.client.theme.ThemeManager;
import net.minecraft.client.util.math.MatrixStack;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Settings row with a smooth ON/OFF switch.
 */
public class ToggleRow extends Row {
	private final String label;
	private final BooleanSupplier getter;
	private final Consumer<Boolean> setter;
	private float knobAnim;

	public ToggleRow(int x, int y, int width, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
		super(x, y, width, 28);
		this.label = label;
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		updateHover(mouseX, mouseY);
		ThemeManager.Theme theme = ThemeManager.get().current();
		drawBackground(matrices);

		boolean on = getter.getAsBoolean();
		float target = on ? 1F : 0F;
		if (VisualizerConfig.get().animationsEnabled) {
			knobAnim = com.visualizer.client.animation.AnimationEngine.approach(knobAnim, target, 18F, delta);
		} else {
			knobAnim = target;
		}

		DrawUtil.text(matrices, label, x + 12, y + 10, theme.text());

		int trackW = 40;
		int trackX = x + width - 52;
		int trackY = y + 7;
		int trackCol = on ? theme.accent() : DrawUtil.withAlpha(0xFF000000, 90);
		DrawUtil.roundedRect(matrices, trackX, trackY, trackW, 14, 7, trackCol);
		int travel = 22;
		int knobX = trackX + 2 + Math.round(travel * knobAnim);
		DrawUtil.roundedRect(matrices, knobX, trackY + 2, 14, 10, 5, 0xFFFFFFFF);

		DrawUtil.text(matrices, on ? "ON" : "OFF", x + width - 78, y + 10, theme.textMuted());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || !isInside(mouseX, mouseY)) return false;
		boolean next = !getter.getAsBoolean();
		setter.accept(next);
		ConfigManager.save();
		AudioManager.get().play(AudioManager.UiSound.CLICK);
		return true;
	}
}