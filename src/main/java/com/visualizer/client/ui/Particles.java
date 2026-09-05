package com.visualizer.client.ui;

import com.visualizer.client.animation.TimeUtil;
import com.visualizer.client.ui.DrawUtil;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Random;

/**
 * Ultra-light ambient particle layer for Visualizer screens.
 * ~40 small drifting accents — one fill call per particle, fully frame-driven.
 */
public final class Particles {
	private static final int N = 40;

	private final float[] x = new float[N];
	private final float[] y = new float[N];
	private final float[] vy = new float[N];
	private final float[] size = new float[N];
	private final float[] phase = new float[N];
	private final Random wrap = new Random();

	public Particles() {
		Random r = new Random(0x5943L);
		for (int i = 0; i < N; i++) {
			reset(i, r.nextFloat(), 0F);
		}
	}

	private void reset(int i, float px, float py) {
		Random r = new Random(i * 131L + 7L);
		x[i] = px;
		y[i] = py;
		vy[i] = 0.015F + r.nextFloat() * 0.045F;
		size[i] = 1 + r.nextInt(3);
		phase[i] = r.nextFloat() * (float) (Math.PI * 2);
	}

	public void tick(float w, float h) {
		float dt = TimeUtil.delta();
		if (dt <= 0F) return;
		for (int i = 0; i < N; i++) {
			y[i] -= vy[i] * dt;
			if (y[i] < -0.04F) {
				reset(i, wrap.nextFloat(), 1.04F);
			}
		}
	}

	public void render(MatrixStack m, int w, int h, int accentColor) {
		long t = TimeUtil.now();
		DrawUtil.beginBatch(m);
		for (int i = 0; i < N; i++) {
			int px = Math.round(x[i] * w);
			int py = Math.round(y[i] * h);
			float a = (float) ((Math.sin(phase[i] + t * 0.0012) + 1D) / 2D);
			int alpha = (int) (24 + 42 * a);
			DrawUtil.batchQuad(px, py, (int) size[i], (int) size[i], DrawUtil.withAlpha(accentColor, alpha));
		}
		DrawUtil.flushBatch();
	}
}