package com.visualizer.client.config;

/**
 * All Visualizer settings. Purely visual / audio / keybind — no gameplay fields.
 */
public final class VisualizerConfig {
	private static VisualizerConfig INSTANCE = new VisualizerConfig();

	public static VisualizerConfig get() {
		return INSTANCE;
	}

	public static void set(VisualizerConfig config) {
		INSTANCE = config != null ? config : new VisualizerConfig();
	}

	// Appearance
	public String theme = "dark";
	public String accent = "emerald";
	public float transparency = 0.85F;
	public float uiScale = 1.0F;
	public float roundedCorners = 14F;
	public boolean shadows = true;
	public boolean glow = true;
	public boolean blur = false;

	// HUD toggles
	public boolean hudFps = true;
	public boolean hudPing = true;
	public boolean hudCoordinates = true;
	public boolean hudClock = true;
	public boolean hudDirection = true;
	public boolean hudKeystrokes = false;
	public boolean hudArmor = true;
	public boolean hudStatus = true;
	public boolean hudServer = true;
	public boolean hudDay = true;
	public boolean hudBiome = true;
	public boolean hudLight = false;
	public boolean hudWatermark = true;
	public boolean hudFpsGraph = true;
	public boolean hudNotifications = true;
	public boolean hudVisible = true;

	// HUD layout (normalized 0-1 positions)
	public float hudFpsX = 0.02F;
	public float hudFpsY = 0.02F;
	public float hudPingX = 0.02F;
	public float hudPingY = 0.06F;
	public float hudCoordsX = 0.02F;
	public float hudCoordsY = 0.10F;
	public float hudClockX = 0.02F;
	public float hudClockY = 0.14F;
	public float hudDirectionX = 0.02F;
	public float hudDirectionY = 0.18F;
	public float hudServerX = 0.02F;
	public float hudServerY = 0.26F;
	public float hudDayX = 0.02F;
	public float hudDayY = 0.30F;
	public float hudBiomeX = 0.02F;
	public float hudBiomeY = 0.34F;
	public float hudLightX = 0.02F;
	public float hudLightY = 0.38F;
	public float hudKeystrokesX = 0.02F;
	public float hudKeystrokesY = 0.70F;
	public float hudArmorX = 0.02F;
	public float hudArmorY = 0.55F;
	public float hudStatusX = 0.02F;
	public float hudStatusY = 0.22F;
	public float hudFpsGraphX = 0.02F;
	public float hudFpsGraphY = 0.42F;
	public float hudWatermarkX = 0.74F;
	public float hudWatermarkY = 0.96F;
	public float hudScale = 1.0F;

	// Animation
	public boolean animationsEnabled = true;
	public float animationSpeed = 1.0F;
	public boolean uiScaleAnimation = true;
	public boolean fadeAnimation = true;
	public boolean hoverAnimation = true;
	public boolean panelAnimation = true;
	public boolean notificationAnimation = true;

	// Sound
	public boolean uiSounds = true;
	public float masterUiVolume = 0.6F;
	public float hoverVolume = 1.0F;
	public float clickVolume = 1.0F;
	public boolean hoverSound = true;
	public boolean clickSound = true;
	public boolean openSound = true;
	public boolean closeSound = true;
	public boolean notificationSound = true;

	// Performance
	public boolean performanceAnimations = true;
	public boolean performanceBlur = false;
	public boolean performanceGlow = true;
	public boolean performanceShadows = true;
	public boolean fancyUi = true;

	// Compact / enabled
	public boolean compactMode = false;
	public boolean visualizerEnabled = true;

	// Keybinds (GLFW key codes; -1 = unbound)
	public int keyOpenVisualizer = 344;     // RIGHT_SHIFT
	public int keyToggleHud = 291;          // F10
	public int keyToggleHudEditor = 292;    // F11
	public int keyToggleCompact = 67;       // C
	public int keyOpenSettings = 345;       // RIGHT_CONTROL

	public void resetHudLayout() {
		hudFpsX = 0.02F; hudFpsY = 0.02F;
		hudPingX = 0.02F; hudPingY = 0.06F;
		hudCoordsX = 0.02F; hudCoordsY = 0.10F;
		hudClockX = 0.02F; hudClockY = 0.14F;
		hudDirectionX = 0.02F; hudDirectionY = 0.18F;
		hudServerX = 0.02F; hudServerY = 0.26F;
		hudDayX = 0.02F; hudDayY = 0.30F;
		hudBiomeX = 0.02F; hudBiomeY = 0.34F;
		hudLightX = 0.02F; hudLightY = 0.38F;
		hudKeystrokesX = 0.02F; hudKeystrokesY = 0.70F;
		hudArmorX = 0.02F; hudArmorY = 0.55F;
		hudStatusX = 0.02F; hudStatusY = 0.22F;
		hudFpsGraphX = 0.02F; hudFpsGraphY = 0.42F;
		hudWatermarkX = 0.74F; hudWatermarkY = 0.96F;
		hudScale = 1.0F;
	}
}