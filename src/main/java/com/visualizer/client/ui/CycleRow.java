package com.visualizer.client.ui;

import com.visualizer.client.audio.AudioManager;
import com.visualizer.client.config.ConfigManager;
import com.visualizer.client.theme.ThemeManager;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Cycles through a list of string options.
 */
public class CycleRow extends Row {
	private final String label;
	private final List<String> options;
	private final Supplier<String> getter;
	private final Consumer<String> setter;

	public CycleRow(int x, int y, int width, String label, List<String> options,
	                Supplier<String> getter, Consumer<String> setter) {
		super(x, y, width, 28);
		this.label = label;
		this.options = options;
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		updateHover(mouseX, mouseY);
		ThemeManager.Theme theme = ThemeManager.get().current();
		drawBackground(matrices);

		DrawUtil.text(matrices, label, x + 12, y + 10, theme.text());
		String value = getter.get();
		int vw = DrawUtil.textWidth(value);
		DrawUtil.text(matrices, value, x + width - 14 - vw, y + 10, theme.accent());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || !isInside(mouseX, mouseY)) return false;
		String current = getter.get();
		int idx = 0;
		for (int i = 0; i < options.size(); i++) {
			if (options.get(i).equalsIgnoreCase(current)) {
				idx = i;
				break;
			}
		}
		String next = options.get((idx + 1) % options.size());
		setter.accept(next);
		ConfigManager.save();
		AudioManager.get().play(AudioManager.UiSound.CLICK);
		return true;
	}
}