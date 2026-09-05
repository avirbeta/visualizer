package com.visualizer.client.animation;

import com.visualizer.client.config.VisualizerConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Central Animation Engine used by every Visualizer UI component.
 * All widgets share this single engine; no per-panel animation systems exist.
 */
public final class AnimationEngine {
	private static final AnimationEngine INSTANCE = new AnimationEngine();

	private final List<Tween> tweens = new ArrayList<>();
	private float globalSpeed = 1F;

	private AnimationEngine() {
	}

	public static AnimationEngine get() {
		return INSTANCE;
	}

	public void setGlobalSpeed(float speed) {
		this.globalSpeed = Math.max(0.05F, speed);
	}

	public void tween(float from, float to, float durationSec,
	                  Tween.EasingFn easing, Consumer<Float> onUpdate, Runnable onComplete) {
		if (!VisualizerConfig.get().animationsEnabled || !VisualizerConfig.get().performanceAnimations) {
			if (onUpdate != null) onUpdate.accept(to);
			if (onComplete != null) onComplete.run();
			return;
		}
		float speed = VisualizerConfig.get().animationSpeed * globalSpeed;
		tweens.add(new Tween(from, to, durationSec / Math.max(0.05F, speed), easing, onUpdate, onComplete));
	}

	public void fade(float from, float to, float duration, Consumer<Float> onUpdate) {
		tween(from, to, duration, Easing::easeOutCubic, onUpdate, null);
	}

	public void scale(float from, float to, float duration, Consumer<Float> onUpdate) {
		tween(from, to, duration, Easing::easeOutBack, onUpdate, null);
	}

	public void slide(float from, float to, float duration, Consumer<Float> onUpdate) {
		tween(from, to, duration, Easing::easeInOutCubic, onUpdate, null);
	}

	public void spring(float from, float to, float duration, Consumer<Float> onUpdate) {
		tween(from, to, duration, Easing::spring, onUpdate, null);
	}

	public void tick(float deltaSec) {
		if (tweens.isEmpty()) return;
		Iterator<Tween> it = tweens.iterator();
		while (it.hasNext()) {
			Tween tween = it.next();
			tween.tick(deltaSec);
			if (!tween.active()) {
				it.remove();
			}
		}
	}

	public void clear() {
		for (Tween tween : tweens) {
			tween.cancel();
		}
		tweens.clear();
	}

	/** Smoothly approach a target each frame (used for hover / continuous motion). */
	public static float approach(float current, float target, float speed, float delta) {
		float diff = target - current;
		if (Math.abs(diff) < 0.0005F) return target;
		return current + diff * Math.min(1F, speed * delta);
	}
}