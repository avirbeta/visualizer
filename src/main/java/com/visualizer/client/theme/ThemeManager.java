package com.visualizer.client.theme;

import com.visualizer.client.config.VisualizerConfig;

/**
 * Theme colors and derived paint values for Visualizer UI.
 */
public final class ThemeManager {
	private static final ThemeManager INSTANCE = new ThemeManager();
	private static final long CACHE_TTL_MS = 100L;

	private Theme cached;
	private long cachedAt;

	private ThemeManager() {
	}

	public static ThemeManager get() {
		return INSTANCE;
	}

	/** Themes are rebuilt at most 10x/sec, cached in between. */
	public Theme current() {
		long now = System.currentTimeMillis();
		Theme t = cached;
		if (t != null && now - cachedAt < CACHE_TTL_MS) return t;
		t = compute();
		cached = t;
		cachedAt = now;
		return t;
	}

	private Theme compute() {
		VisualizerConfig cfg = VisualizerConfig.get();
		String name = cfg.theme == null ? "dark" : cfg.theme.toLowerCase();
		Theme base;
		if ("light".equals(name)) {
			base = Theme.light();
		} else if ("midnight".equals(name)) {
			base = Theme.midnight();
		} else if ("forest".equals(name)) {
			base = Theme.forest();
		} else if ("sunrise".equals(name)) {
			base = Theme.sunrise();
		} else if ("graphite".equals(name)) {
			base = Theme.graphite();
		} else if ("candy".equals(name)) {
			base = Theme.candy();
		} else {
			base = Theme.dark();
		}
		float alpha = Math.max(0.35F, Math.min(1F, cfg.transparency));
		return new Theme(
			base.background(),
			base.panel(),
			base.panelHover(),
			base.text(),
			base.textMuted(),
			base.border(),
			accentColor(cfg.accent),
			alpha,
			cfg.roundedCorners,
			cfg.shadows && cfg.performanceShadows,
			cfg.glow && cfg.performanceGlow,
			cfg.blur && cfg.performanceBlur
		);
	}

	private static int accentColor(String name) {
		if (name == null) return 0xFF2ECC71;
		String n = name.toLowerCase();
		if ("chroma".equals(n)) return chroma();
		if ("amber".equals(n)) return 0xFFF39C12;
		if ("sky".equals(n)) return 0xFF3498DB;
		if ("rose".equals(n)) return 0xFFE74C3C;
		if ("violet".equals(n)) return 0xFF9B59B6;
		if ("mint".equals(n)) return 0xFF1ABC9C;
		if ("orange".equals(n)) return 0xFFE67E22;
		if ("pink".equals(n)) return 0xFFEC4899;
		if ("gold".equals(n)) return 0xFFF1C40F;
		if ("aqua".equals(n)) return 0xFF00C2A8;
		return 0xFF2ECC71;
	}

	/** Rainbows through the hue wheel — cycles continuously. */
	private static int chroma() {
		float h = ((System.currentTimeMillis() % 7200L) / 7200F) * 360F;
		return hsvToRgb(h, 0.75F, 0.95F);
	}

	private static int hsvToRgb(float h, float s, float v) {
		float c = v * s;
		float x = c * (1F - Math.abs((h / 60F) % 2F - 1F));
		float m = v - c;
		float r, g, b;
		if (h < 60F) {
			r = c; g = x; b = 0F;
		} else if (h < 120F) {
			r = x; g = c; b = 0F;
		} else if (h < 180F) {
			r = 0F; g = c; b = x;
		} else if (h < 240F) {
			r = 0F; g = x; b = c;
		} else if (h < 300F) {
			r = x; g = 0F; b = c;
		} else {
			r = c; g = 0F; b = x;
		}
		return 0xFF000000
			| ((int) Math.round((r + m) * 255F) << 16)
			| ((int) Math.round((g + m) * 255F) << 8)
			| (int) Math.round((b + m) * 255F);
	}

	/** Immutable theme palette. */
	public static final class Theme {
		private final int background;
		private final int panel;
		private final int panelHover;
		private final int text;
		private final int textMuted;
		private final int border;
		private final int accent;
		private final float panelAlpha;
		private final float cornerRadius;
		private final boolean shadows;
		private final boolean glow;
		private final boolean blur;

		private Theme(int background, int panel, int panelHover, int text, int textMuted,
		              int border, int accent, float panelAlpha, float cornerRadius,
		              boolean shadows, boolean glow, boolean blur) {
			this.background = background;
			this.panel = panel;
			this.panelHover = panelHover;
			this.text = text;
			this.textMuted = textMuted;
			this.border = border;
			this.accent = accent;
			this.panelAlpha = panelAlpha;
			this.cornerRadius = cornerRadius;
			this.shadows = shadows;
			this.glow = glow;
			this.blur = blur;
		}

		public static Theme dark() {
			return new Theme(0xFF0E1116, 0xFF1A1F29, 0xFF242B38, 0xFFF2F4F8, 0xFF9AA3B2,
				0x33FFFFFF, 0xFF2ECC71, 0.85F, 14F, true, true, false);
		}

		public static Theme light() {
			return new Theme(0xFFF4F6FA, 0xFFFFFFFF, 0xFFF0F3F8, 0xFF1A1F29, 0xFF5C667A,
				0x22000000, 0xFF2ECC71, 0.92F, 14F, true, false, false);
		}

		public static Theme midnight() {
			return new Theme(0xFF070B14, 0xFF121826, 0xFF1B2436, 0xFFE8EEF8, 0xFF8B97AD,
				0x28FFFFFF, 0xFF5DADE2, 0.88F, 16F, true, true, false);
		}

		public static Theme forest() {
			return new Theme(0xFF0C1410, 0xFF152018, 0xFF1E2C22, 0xFFEAF5EC, 0xFF8FA897,
				0x28FFFFFF, 0xFF58D68D, 0.88F, 14F, true, true, false);
		}

		public static Theme sunrise() {
			return new Theme(0xFF14100B, 0xFF211A12, 0xFF32281B, 0xFFF8F2E8, 0xFFA99679,
				0x28FFFFFF, 0xFFE67E22, 0.88F, 16F, true, true, false);
		}

		public static Theme graphite() {
			return new Theme(0xFF0C0D10, 0xFF17181D, 0xFF23252C, 0xFFE8E9EC, 0xFF8A8D96,
				0x28FFFFFF, 0xFFAEB6C1, 0.9F, 10F, false, false, false);
		}

		public static Theme candy() {
			return new Theme(0xFF120B17, 0xFF1E1426, 0xFF2C1C3A, 0xFFF6EFFC, 0xFFA98FBF,
				0x28FFFFFF, 0xFFEC4899, 0.9F, 14F, true, true, false);
		}

		public int background() { return background; }
		public int panel() { return panel; }
		public int panelHover() { return panelHover; }
		public int text() { return text; }
		public int textMuted() { return textMuted; }
		public int border() { return border; }
		public int accent() { return accent; }
		public float panelAlpha() { return panelAlpha; }
		public float cornerRadius() { return cornerRadius; }
		public boolean shadows() { return shadows; }
		public boolean glow() { return glow; }
		public boolean blur() { return blur; }

		public int panelArgb() {
			int a = Math.round(panelAlpha * 255F) & 0xFF;
			return (a << 24) | (panel & 0x00FFFFFF);
		}

		public int panelHoverArgb() {
			int a = Math.round(Math.min(1F, panelAlpha + 0.08F) * 255F) & 0xFF;
			return (a << 24) | (panelHover & 0x00FFFFFF);
		}
	}
}