package com.visualizer.client.animation;

import net.minecraft.util.Util;

/**
 * Frame-time helpers for driving animations with the real clock.
 */
public final class TimeUtil {
	private static long last = Util.getMeasuringTimeMs();

	private TimeUtil() {
	}

	/** Seconds elapsed since the previous call, clamped so a hitch cannot teleport an animation. */
	public static float delta() {
		long now = Util.getMeasuringTimeMs();
		float d = Math.min(0.1F, Math.max(0F, (now - last) / 1000F));
		last = now;
		return d;
	}

	public static long now() {
		return Util.getMeasuringTimeMs();
	}
}