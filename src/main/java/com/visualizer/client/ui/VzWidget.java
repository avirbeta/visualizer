package com.visualizer.client.ui;

import net.minecraft.client.util.math.MatrixStack;

/**
 * A lightweight interactive UI element used by every Visualizer screen.
 * Screens manage these manually (self-contained — no vanilla widget API needed).
 */
public interface VzWidget {
	void render(MatrixStack matrices, int mouseX, int mouseY, float delta);

	boolean mouseClicked(double mouseX, double mouseY, int button);

	default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return false;
	}

	default boolean mouseReleased(double mouseX, double mouseY, int button) {
		return false;
	}
}