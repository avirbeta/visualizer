package com.visualizer.client.ui.screens;

import com.visualizer.client.audio.AudioManager;
import com.visualizer.client.config.ConfigManager;
import com.visualizer.client.config.VisualizerConfig;
import com.visualizer.client.hud.HudElement;
import com.visualizer.client.hud.HudManager;
import com.visualizer.client.theme.ThemeManager;
import com.visualizer.client.ui.ButtonRow;
import com.visualizer.client.ui.DrawUtil;
import com.visualizer.client.ui.Particles;
import com.visualizer.client.ui.Row;
import com.visualizer.client.ui.SliderRow;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

/**
 * Drag-to-place HUD editor. Pure visual positioning — no game data is changed.
 */
public class HudEditorScreen extends Screen {
	private static final float GRID_STEP = 0.05F;
	private static final int PANEL_H = 40;

	private final Screen previous;
	private final Particles bg = new Particles();
	private HudElement dragging;
	private HudElement selected;
	private double grabOffsetX;
	private double grabOffsetY;
	private boolean snapToGrid = true;
	private boolean showGrid = true;

	private Row hudScaleRow;
	private Row elementScaleRow;
	private ButtonRow resetBtn;
	private ButtonRow snapBtn;
	private ButtonRow gridBtn;
	private ButtonRow doneBtn;

	public HudEditorScreen(Screen previous) {
		super(new LiteralText("HUD Editor"));
		this.previous = previous;
	}

	@Override
	protected void init() {
		int y = height - PANEL_H - 30;
		int x = 10;
		hudScaleRow = new SliderRow(x, y, 180, "Scale", 0.5F, 2F, "x",
			() -> VisualizerConfig.get().hudScale, v -> VisualizerConfig.get().hudScale = v);
		elementScaleRow = new SliderRow(width - 350, y, 190, "Item Scale", 0.5F, 2F, "x",
			() -> selected != null ? selected.scale() : 1F,
			v -> { if (selected != null) selected.setScale(v); });
		resetBtn = new ButtonRow(x + 200, y, 140, 28, () -> "Reset", false, this::reset);
		snapBtn = new ButtonRow(x + 350, y, 140, 28, () -> snapLabel(), false, this::toggleSnap);
		gridBtn = new ButtonRow(x + 500, y, 140, 28, () -> gridLabel(), false, this::toggleGrid);
		doneBtn = new ButtonRow(width - 150, y + 4, 140, 28, () -> "Done", true, this::done);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		ThemeManager.Theme theme = ThemeManager.get().current();
		int w = width;
		int h = height - PANEL_H;

		DrawUtil.fill(matrices, 0, 0, w, h, theme.background());

		bg.tick(w, h);
		bg.render(matrices, w, h, theme.accent());

		if (showGrid) {
			for (float gx = 0; gx <= 1.001F; gx += GRID_STEP) {
				int x = Math.round(gx * w);
				DrawUtil.fill(matrices, x, 0, 1, h, DrawUtil.withAlpha(theme.border(), 50));
			}
			for (float gy = 0; gy <= 1.001F; gy += GRID_STEP) {
				int y = Math.round(gy * h);
				DrawUtil.fill(matrices, 0, y, w, 1, DrawUtil.withAlpha(theme.border(), 50));
			}
		}

		HudManager.get().render(matrices, w, h, true);

		// bottom control panel
		DrawUtil.roundedRect(matrices, 6, height - PANEL_H, w - 12, PANEL_H, 8,
			DrawUtil.withAlpha(theme.panel(), 235));

		if (dragging != null) {
			DrawUtil.textShadow(matrices, "Editing: " + dragging.id, 18, height - PANEL_H - 12, theme.accent());
		} else if (selected != null) {
			DrawUtil.textShadow(matrices, "Selected: " + selected.id + " — drag or resize", 18, height - PANEL_H - 12, theme.text());
		} else {
			DrawUtil.textShadow(matrices, "Drag an element to move it", 18, height - PANEL_H - 12, theme.textMuted());
		}

		if (hudScaleRow != null) hudScaleRow.render(matrices, mouseX, mouseY, delta);
		if (selected != null && elementScaleRow != null) elementScaleRow.render(matrices, mouseX, mouseY, delta);
		if (resetBtn != null) resetBtn.render(matrices, mouseX, mouseY, delta);
		if (snapBtn != null) snapBtn.render(matrices, mouseX, mouseY, delta);
		if (gridBtn != null) gridBtn.render(matrices, mouseX, mouseY, delta);
		if (doneBtn != null) doneBtn.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			resetBtn.mouseClicked(mouseX, mouseY, button);
			snapBtn.mouseClicked(mouseX, mouseY, button);
			gridBtn.mouseClicked(mouseX, mouseY, button);
			doneBtn.mouseClicked(mouseX, mouseY, button);
			hudScaleRow.mouseClicked(mouseX, mouseY, button);
			if (selected != null && elementScaleRow.mouseClicked(mouseX, mouseY, button)) return true;
			int w = width;
			int h = height - PANEL_H;
			HudElement e = HudManager.get().hitTest(mouseX, mouseY, w, h);
			if (e != null) {
				dragging = e;
				selected = e;
				grabOffsetX = mouseX - e.pixelX(w);
				grabOffsetY = mouseY - e.pixelY(h);
				return true;
			} else {
				selected = null;
			}
		}
		return false;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (hudScaleRow.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
		if (selected != null && elementScaleRow.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
		if (dragging == null) return false;
		int w = width;
		int h = height - PANEL_H;
		float nx = (float) ((mouseX - grabOffsetX - 8) / (double) w);
		float ny = (float) ((mouseY - grabOffsetY - 8) / (double) h);
		if (snapToGrid) {
			nx = Math.max(0F, Math.min(0.96F, Math.round(nx / GRID_STEP) * GRID_STEP));
			ny = Math.max(0F, Math.min(0.86F, Math.round(ny / GRID_STEP) * GRID_STEP));
		} else {
			nx = Math.max(0F, Math.min(0.96F, nx));
			ny = Math.max(0F, Math.min(0.86F, ny));
		}
		dragging.setNormalized(nx, ny);
		return true;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		hudScaleRow.mouseReleased(mouseX, mouseY, button);
		if (selected != null) elementScaleRow.mouseReleased(mouseX, mouseY, button);
		if (dragging != null) {
			dragging = null;
			HudManager.get().saveToConfig();
			ConfigManager.save();
			return true;
		}
		return false;
	}

	@Override
	public void onClose() {
		AudioManager.get().play(AudioManager.UiSound.CLOSE);
		if (client != null) {
			client.openScreen(previous);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void reset() {
		VisualizerConfig.get().resetHudLayout();
		HudManager.get().reloadFromConfig();
		AudioManager.get().play(AudioManager.UiSound.CLICK);
	}

	private void toggleSnap() {
		snapToGrid = !snapToGrid;
		AudioManager.get().play(AudioManager.UiSound.CLICK);
	}

	private void toggleGrid() {
		showGrid = !showGrid;
		AudioManager.get().play(AudioManager.UiSound.CLICK);
	}

	private void done() {
		HudManager.get().saveToConfig();
		ConfigManager.save();
		AudioManager.get().play(AudioManager.UiSound.CLICK);
		onClose();
	}

	private String snapLabel() {
		return snapToGrid ? "Snap: ON" : "Snap: OFF";
	}

	private String gridLabel() {
		return showGrid ? "Grid: ON" : "Grid: OFF";
	}
}