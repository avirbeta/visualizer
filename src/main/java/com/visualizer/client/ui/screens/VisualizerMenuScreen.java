package com.visualizer.client.ui.screens;


import com.visualizer.client.audio.AudioManager;
import com.visualizer.client.config.ConfigManager;
import com.visualizer.client.config.VisualizerConfig;
import com.visualizer.client.hud.HudManager;
import com.visualizer.client.input.KeybindManager;
import com.visualizer.client.theme.ThemeManager;
import com.visualizer.client.ui.ButtonRow;
import com.visualizer.client.ui.CycleRow;
import com.visualizer.client.ui.DrawUtil;
import com.visualizer.client.ui.Particles;
import com.visualizer.client.ui.Row;
import com.visualizer.client.ui.SliderRow;
import com.visualizer.client.ui.ToggleRow;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Visualizer settings menu (replaces the vanilla options UX inside Visualizer screens).
 */
public class VisualizerMenuScreen extends Screen {
	private enum Section {
		APPEARANCE("Appearance"),
		HUD("HUD"),
		ANIMATION("Animation"),
		SOUND("Sound"),
		KEYBINDS("Keybinds"),
		PERFORMANCE("Performance"),
		ABOUT("About");

		final String label;

		Section(String label) {
			this.label = label;
		}
	}

	private final Screen previous;
	private VisualizerConfig cfg;
	private Section section = Section.APPEARANCE;

	private final List<Row> rows = new ArrayList<>();
	private final List<ButtonRow> navButtons = new ArrayList<>();
	private final Particles bg = new Particles();
	private ButtonRow exitBtn;
	private int scroll;
	private int contentX;
	private int contentY;
	private int contentW;
	private int contentH;
	private int sidebarW = 160;
	private static final int SLOT = 42;

	public VisualizerMenuScreen(Screen previous) {
		super(new LiteralText("Visualizer"));
		this.previous = previous;
	}

	@Override
	protected void init() {
		this.cfg = VisualizerConfig.get();
		this.scroll = 0;
		refreshGeometry();
		buildNav();
		buildRows();
		layout();
		buildExit();
	}

	private void buildExit() {
		exitBtn = new ButtonRow(width - 46, 14, 34, 28, () -> "X", true, this::closeMenu);
	}

	private void closeMenu() {
		AudioManager.get().play(AudioManager.UiSound.CLOSE);
		if (client != null) client.openScreen(previous);
	}

	private void refreshGeometry() {
		this.contentX = sidebarW + 24;
		this.contentW = width - contentX - 24;
		this.contentH = height - 88;
		this.contentY = 60;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		ThemeManager.Theme theme = ThemeManager.get().current();

		DrawUtil.fill(matrices, 0, 0, width, height, theme.background());
		bg.tick(width, height);
		bg.render(matrices, width, height, theme.accent());

		DrawUtil.roundedRect(matrices, sidebarW - 8, 0, 40, height, 20, DrawUtil.withAlpha(theme.accent(), 60));
		DrawUtil.text(matrices, "Visualizer", 18, 26, theme.text());
		DrawUtil.fill(matrices, 18, 48, 90, 2, theme.accent());
		DrawUtil.text(matrices, "HUD  •  themes  •  animations", 18, 56, theme.textMuted());

		for (ButtonRow nav : navButtons) {
			nav.render(matrices, mouseX, mouseY, delta);
		}

		DrawUtil.roundedRect(matrices, contentX - 8, contentY - 8, contentW + 16, contentH + 16, 12, theme.panelArgb());

		for (Row row : rows) {
			row.render(matrices, mouseX, mouseY, delta);
		}

		DrawUtil.text(matrices, section.label.toUpperCase(), contentX, contentY - 26, theme.accent());

		if (exitBtn != null) exitBtn.render(matrices, mouseX, mouseY, delta);

		if (KeybindManager.get().isCapturing()) {
			DrawUtil.textShadow(matrices, "Press a key to bind...", contentX + 12, contentY + 8, theme.accent());
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (exitBtn != null && exitBtn.mouseClicked(mouseX, mouseY, button)) return true;
		for (int i = navButtons.size() - 1; i >= 0; i--) {
			if (navButtons.get(i).mouseClicked(mouseX, mouseY, button)) return true;
		}
		for (int i = rows.size() - 1; i >= 0; i--) {
			if (rows.get(i).mouseClicked(mouseX, mouseY, button)) return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		for (Row row : rows) {
			if (row.mouseReleased(mouseX, mouseY, button)) return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		for (Row row : rows) {
			if (row.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		int maxScroll = Math.max(0, rows.size() * SLOT - contentH + 16);
		scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (amount * 20)));
		layout();
		return true;
	}

	@Override
	public void onClose() {
		AudioManager.get().play(AudioManager.UiSound.CLOSE);
		if (client != null) {
			client.openScreen(previous);
		}
	}

	private void buildNav() {
		navButtons.clear();
		int y = 64;
		for (Section s : Section.values()) {
			final Section sec = s;
			boolean selected = s == section;
			ButtonRow b = new ButtonRow(18, y, sidebarW - 36, 28,
				() -> sec.label, selected, () -> select(sec));
			navButtons.add(b);
			y += 36;
		}
		ButtonRow back = new ButtonRow(18, height - 66, sidebarW - 36, 28,
			() -> "< Back", false, this::onClose);
		navButtons.add(back);
	}

	private void select(Section s) {
		if (s == section) return;
		section = s;
		scroll = 0;
		AudioManager.get().play(AudioManager.UiSound.CLICK);
		buildRows();
		buildNav();
		layout();
	}

	private void buildRows() {
		rows.clear();
		VisualizerConfig c = cfg;
		int top = contentY;
		switch (section) {
			case APPEARANCE:
				add(new CycleRow(contentX, top, rowsW(), "Theme",
					Arrays.asList("dark", "light", "midnight", "forest", "sunrise", "graphite", "candy"), () -> c.theme, v -> c.theme = v));
				add(new CycleRow(contentX, top, rowsW(), "Accent",
					Arrays.asList("emerald", "amber", "sky", "rose", "violet", "mint", "orange", "pink", "gold", "aqua", "chroma"),
					() -> c.accent, v -> c.accent = v));
				add(new SliderRow(contentX, top, rowsW(), "Transparency", 0.35F, 1F, "%",
					() -> c.transparency, v -> c.transparency = v));
				add(new SliderRow(contentX, top, rowsW(), "UI Scale", 0.5F, 2F, "x",
					() -> c.uiScale, v -> c.uiScale = v));
				add(new SliderRow(contentX, top, rowsW(), "Corner Radius", 0F, 28F, "px",
					() -> c.roundedCorners, v -> c.roundedCorners = v));
				add(new ToggleRow(contentX, top, rowsW(), "Shadows", () -> c.shadows, v -> c.shadows = v));
				add(new ToggleRow(contentX, top, rowsW(), "Glow", () -> c.glow, v -> c.glow = v));
				break;
			case HUD:
				add(new ToggleRow(contentX, top, rowsW(), "HUD Enabled", () -> c.hudVisible, v -> c.hudVisible = v));
				add(new ToggleRow(contentX, top, rowsW(), "FPS Counter", () -> c.hudFps, v -> c.hudFps = v));
				add(new ToggleRow(contentX, top, rowsW(), "Ping", () -> c.hudPing, v -> c.hudPing = v));
				add(new ToggleRow(contentX, top, rowsW(), "Coordinates", () -> c.hudCoordinates, v -> c.hudCoordinates = v));
				add(new ToggleRow(contentX, top, rowsW(), "Clock", () -> c.hudClock, v -> c.hudClock = v));
				add(new ToggleRow(contentX, top, rowsW(), "Direction", () -> c.hudDirection, v -> c.hudDirection = v));
				add(new ToggleRow(contentX, top, rowsW(), "Keystrokes", () -> c.hudKeystrokes, v -> c.hudKeystrokes = v));
				add(new ToggleRow(contentX, top, rowsW(), "Armor", () -> c.hudArmor, v -> c.hudArmor = v));
				add(new ToggleRow(contentX, top, rowsW(), "Status", () -> c.hudStatus, v -> c.hudStatus = v));
				add(new ToggleRow(contentX, top, rowsW(), "Server", () -> c.hudServer, v -> c.hudServer = v));
				add(new ToggleRow(contentX, top, rowsW(), "Day / Time", () -> c.hudDay, v -> c.hudDay = v));
				add(new ToggleRow(contentX, top, rowsW(), "Biome", () -> c.hudBiome, v -> c.hudBiome = v));
				add(new ToggleRow(contentX, top, rowsW(), "Light Level", () -> c.hudLight, v -> c.hudLight = v));
				add(new ToggleRow(contentX, top, rowsW(), "FPS Graph", () -> c.hudFpsGraph, v -> c.hudFpsGraph = v));
				add(new ToggleRow(contentX, top, rowsW(), "Watermark", () -> c.hudWatermark, v -> c.hudWatermark = v));
				add(new ToggleRow(contentX, top, rowsW(), "Notifications", () -> c.hudNotifications, v -> c.hudNotifications = v));
				add(new SliderRow(contentX, top, rowsW(), "HUD Scale", 0.5F, 2F, "x", () -> c.hudScale, v -> c.hudScale = v));
				add(new ButtonRow(contentX, top, rowsW(), 28, () -> "Open HUD Editor", true, this::openEditor));
				add(new ButtonRow(contentX, top, rowsW(), 28, () -> "Reset HUD layout", false, this::resetLayout));
				break;
			case ANIMATION:
				add(new ToggleRow(contentX, top, rowsW(), "All Animations", () -> c.animationsEnabled, v -> c.animationsEnabled = v));
				add(new SliderRow(contentX, top, rowsW(), "Speed", 0.1F, 3F, "x", () -> c.animationSpeed, v -> {
					c.animationSpeed = v;
					com.visualizer.client.animation.AnimationEngine.get().setGlobalSpeed(v);
				}));
				add(new ToggleRow(contentX, top, rowsW(), "Scale", () -> c.uiScaleAnimation, v -> c.uiScaleAnimation = v));
				add(new ToggleRow(contentX, top, rowsW(), "Fade", () -> c.fadeAnimation, v -> c.fadeAnimation = v));
				add(new ToggleRow(contentX, top, rowsW(), "Hover", () -> c.hoverAnimation, v -> c.hoverAnimation = v));
				add(new ToggleRow(contentX, top, rowsW(), "Panel", () -> c.panelAnimation, v -> c.panelAnimation = v));
				add(new ToggleRow(contentX, top, rowsW(), "Notification", () -> c.notificationAnimation, v -> c.notificationAnimation = v));
				break;
			case SOUND:
				add(new ToggleRow(contentX, top, rowsW(), "UI Sounds", () -> c.uiSounds, v -> c.uiSounds = v));
				add(new SliderRow(contentX, top, rowsW(), "Master Volume", 0F, 1F, "%", () -> c.masterUiVolume, v -> c.masterUiVolume = v));
				add(new SliderRow(contentX, top, rowsW(), "Hover Volume", 0F, 1F, "%", () -> c.hoverVolume, v -> c.hoverVolume = v));
				add(new SliderRow(contentX, top, rowsW(), "Click Volume", 0F, 1F, "%", () -> c.clickVolume, v -> c.clickVolume = v));
				add(new ToggleRow(contentX, top, rowsW(), "Hover Sound", () -> c.hoverSound, v -> c.hoverSound = v));
				add(new ToggleRow(contentX, top, rowsW(), "Click Sound", () -> c.clickSound, v -> c.clickSound = v));
				add(new ToggleRow(contentX, top, rowsW(), "Open Sound", () -> c.openSound, v -> c.openSound = v));
				add(new ToggleRow(contentX, top, rowsW(), "Close Sound", () -> c.closeSound, v -> c.closeSound = v));
				add(new ToggleRow(contentX, top, rowsW(), "Notification Sound", () -> c.notificationSound, v -> c.notificationSound = v));
				break;
			case KEYBINDS:
				add(new ButtonRow(contentX, top, rowsW(), 28, () -> "Open Visualizer   [" + KeybindManager.keyName(c.keyOpenVisualizer) + "]",
					false, () -> captureBegin(KeybindManager.CaptureTarget.OPEN)));
				add(new ButtonRow(contentX, top, rowsW(), 28, () -> "Toggle HUD   [" + KeybindManager.keyName(c.keyToggleHud) + "]",
					false, () -> captureBegin(KeybindManager.CaptureTarget.TOGGLE_HUD)));
				add(new ButtonRow(contentX, top, rowsW(), 28, () -> "HUD Editor   [" + KeybindManager.keyName(c.keyToggleHudEditor) + "]",
					false, () -> captureBegin(KeybindManager.CaptureTarget.HUD_EDITOR)));
				add(new ButtonRow(contentX, top, rowsW(), 28, () -> "Compact Mode   [" + KeybindManager.keyName(c.keyToggleCompact) + "]",
					false, () -> captureBegin(KeybindManager.CaptureTarget.COMPACT)));
				add(new ButtonRow(contentX, top, rowsW(), 28, () -> "Quick Settings   [" + KeybindManager.keyName(c.keyOpenSettings) + "]",
					false, () -> captureBegin(KeybindManager.CaptureTarget.SETTINGS)));
				break;
			case PERFORMANCE:
				add(new ToggleRow(contentX, top, rowsW(), "Animations", () -> c.performanceAnimations, v -> c.performanceAnimations = v));
				add(new ToggleRow(contentX, top, rowsW(), "Blur", () -> c.performanceBlur, v -> c.performanceBlur = v));
				add(new ToggleRow(contentX, top, rowsW(), "Glow", () -> c.performanceGlow, v -> c.performanceGlow = v));
				add(new ToggleRow(contentX, top, rowsW(), "Shadows", () -> c.performanceShadows, v -> c.performanceShadows = v));
				add(new ToggleRow(contentX, top, rowsW(), "Fancy UI", () -> c.fancyUi, v -> c.fancyUi = v));
				break;
			case ABOUT:
				add(new ButtonRow(contentX, top, rowsW(), 28, () -> "Visualizer v1.0.0", true, () -> {
				}));
				add(new ButtonRow(contentX, top, rowsW(), 36, () -> "Client-side visual HUD, themes and animations.\nPure display — gameplay untouched.", false, () -> {
				}));
				add(new ButtonRow(contentX, top, rowsW(), 28, () -> "Test Notification", false, this::testNotification));
				add(new ButtonRow(contentX, top, rowsW(), 28, () -> "Reset to Defaults", false, this::resetDefaults));
				break;
		}
	}

	private void add(Row row) {
		rows.add(row);
	}

	private int rowsW() {
		return Math.max(120, contentW);
	}

	private void captureBegin(KeybindManager.CaptureTarget target) {
		AudioManager.get().play(AudioManager.UiSound.CLICK);
		KeybindManager.get().beginCapture(target);
	}

	private void openEditor() {
		ConfigManager.save();
		if (client != null) client.openScreen(new HudEditorScreen(this));
	}

	private void resetLayout() {
		cfg.resetHudLayout();
		HudManager.get().reloadFromConfig();
		ConfigManager.save();
		AudioManager.get().play(AudioManager.UiSound.CLICK);
	}

	private void testNotification() {
		com.visualizer.client.notifications.NotificationManager.get().push("Custom notifications. Now with style!");
		AudioManager.get().play(AudioManager.UiSound.CLICK);
	}

	private void resetDefaults() {
		ConfigManager.reset();
		cfg = VisualizerConfig.get();
		HudManager.get().reloadFromConfig();
		buildRows();
		AudioManager.get().play(AudioManager.UiSound.CLICK);
	}

	private void layout() {
		contentX = sidebarW + 24;
		contentW = width - contentX - 24;
		contentY = 60;
		contentH = height - contentY - 28;
		int y = contentY - scroll;
		for (Row row : rows) {
			row.setY(y);
			y += SLOT;
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}