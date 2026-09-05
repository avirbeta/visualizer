package com.visualizer.client.ui;

import com.visualizer.client.audio.AudioManager;
import com.visualizer.client.theme.ThemeManager;
import net.minecraft.client.util.math.MatrixStack;

import java.util.function.Supplier;

/**
 * A compact action/highlight button used for navigation, capture and menu actions.
 */
public class ButtonRow extends Row {
	private final Supplier<String> label;
	private final boolean highlight;
	private final Runnable onClick;

	public ButtonRow(int x, int y, int width, int height, Supplier<String> label,
	                 boolean highlight, Runnable onClick) {
		super(x, y, width, height);
		this.label = label;
		this.highlight = highlight;
		this.onClick = onClick;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		updateHover(mouseX, mouseY);
		ThemeManager.Theme theme = ThemeManager.get().current();

		int bg;
		if (highlight) {
			bg = DrawUtil.withAlpha(theme.accent(), 70);
		} else {
			bg = (hovered ? theme.panelHoverArgb() : DrawUtil.withAlpha(theme.panel(), 170));
		}
		DrawUtil.roundedRect(matrices, x, y, width, height, 8, bg);

		DrawUtil.text(matrices, label.get(), x + 12, y + (height - 9) / 2F,
			highlight ? theme.accent() : theme.text());

		if (highlight) {
			DrawUtil.fill(matrices, x + 8, y + height - 3, width - 16, 2, DrawUtil.withAlpha(theme.accent(), 190));
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || !isInside(mouseX, mouseY)) return false;
		AudioManager.get().play(AudioManager.UiSound.CLICK);
		if (onClick != null) onClick.run();
		return true;
	}
}