package com.visualizer.client.animation;

/**
 * Shared easing curves for the Visualizer Animation Engine.
 */
public final class Easing {
	private Easing() {
	}

	public static float linear(float t) {
		return clamp01(t);
	}

	public static float smoothstep(float t) {
		t = clamp01(t);
		return t * t * (3F - 2F * t);
	}

	public static float easeOutCubic(float t) {
		t = clamp01(t);
		float inv = 1F - t;
		return 1F - inv * inv * inv;
	}

	public static float easeOutQuad(float t) {
		t = clamp01(t);
		return 1F - (1F - t) * (1F - t);
	}

	public static float easeInOutCubic(float t) {
		t = clamp01(t);
		return t < 0.5F ? 4F * t * t * t : 1F - (float) Math.pow(-2F * t + 2F, 3) / 2F;
	}

	public static float easeOutExpo(float t) {
		t = clamp01(t);
		return t >= 1F ? 1F : 1F - (float) Math.pow(2F, -10F * t);
	}

	public static float easeOutBack(float t) {
		t = clamp01(t);
		float c1 = 1.70158F;
		float c3 = c1 + 1F;
		return 1F + c3 * (float) Math.pow(t - 1F, 3) + c1 * (float) Math.pow(t - 1F, 2);
	}

	public static float spring(float t) {
		t = clamp01(t);
		return 1F - (float) Math.exp(-6F * t) * (float) Math.cos(t * Math.PI * 2.2F);
	}

	public static float clamp01(float t) {
		return Math.max(0F, Math.min(1F, t));
	}

	public static float lerp(float a, float b, float t) {
		return a + (b - a) * clamp01(t);
	}
}