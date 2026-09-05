package com.visualizer.client.animation;

import java.util.function.Consumer;

/**
 * Single tween driven by AnimationEngine.
 */
public final class Tween {
	public enum State {
		RUNNING, COMPLETED, CANCELLED
	}

	@FunctionalInterface
	public interface EasingFn {
		float apply(float t);
	}

	private final float from;
	private final float to;
	private final float durationSec;
	private final EasingFn easing;
	private final Consumer<Float> onUpdate;
	private final Runnable onComplete;

	private float elapsed;
	private State state = State.RUNNING;
	private float value;

	public Tween(float from, float to, float durationSec, EasingFn easing,
	             Consumer<Float> onUpdate, Runnable onComplete) {
		this.from = from;
		this.to = to;
		this.durationSec = Math.max(0.001F, durationSec);
		this.easing = easing != null ? easing : Easing::easeOutCubic;
		this.onUpdate = onUpdate;
		this.onComplete = onComplete;
		this.value = from;
	}

	public void tick(float deltaSec) {
		if (state != State.RUNNING) return;
		elapsed += deltaSec;
		float t = Math.min(1F, elapsed / durationSec);
		value = Easing.lerp(from, to, easing.apply(t));
		if (onUpdate != null) onUpdate.accept(value);
		if (t >= 1F) {
			state = State.COMPLETED;
			if (onComplete != null) onComplete.run();
		}
	}

	public void cancel() {
		state = State.CANCELLED;
	}

	public float value() {
		return value;
	}

	public State state() {
		return state;
	}

	public boolean active() {
		return state == State.RUNNING;
	}
}