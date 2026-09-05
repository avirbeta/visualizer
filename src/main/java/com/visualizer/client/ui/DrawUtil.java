package com.visualizer.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

/**
 * Low-level drawing helpers used everywhere in Visualizer.
 * All colors are ARGB.
 */
public final class DrawUtil {
	private static boolean batchOpen;
	private static Matrix4f batchMat;

	private DrawUtil() {
	}

	public static int withAlpha(int rgb, int alpha) {
		return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0x00FFFFFF);
	}

	/** Open a single tessellator pass for a whole batch of quads. */
	public static void beginBatch(MatrixStack m) {
		if (batchOpen) return;
		batchOpen = true;
		batchMat = m.peek().getModel();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableTexture();
		Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
	}

	public static void flushBatch() {
		if (!batchOpen) return;
		batchOpen = false;
		Tessellator.getInstance().draw();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	public static boolean isBatching() {
		return batchOpen;
	}

	private static BufferBuilder batchBuffer() {
		return Tessellator.getInstance().getBuffer();
	}

	public static int lerpColor(int a, int b, float t) {
		t = Math.max(0F, Math.min(1F, t));
		int aa = (a >> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
		int ba = (b >> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
		return ((int) (aa + (ba - aa) * t) << 24)
			| ((int) (ar + (br - ar) * t) << 16)
			| ((int) (ag + (bg - ag) * t) << 8)
			| (int) (ab + (bb - ab) * t);
	}

	public static void fill(MatrixStack m, int x, int y, int w, int h, int color) {
		if (w <= 0 || h <= 0 || ((color >> 24) & 0xFF) == 0) return;
		DrawableHelper.fill(m, x, y, x + w, y + h, color);
	}

	/** Feed the open batch with a plain quad. */
	public static void batchQuad(float x, float y, float w, float h, int color) {
		if (!batchOpen || w <= 0 || h <= 0) return;
		int a = (color >> 24) & 0xFF;
		if (a == 0) return;
		verts(batchBuffer(), batchMat, x, y, x + w, y + h,
			(color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, a);
	}

	public static void outline(MatrixStack m, int x, int y, int w, int h, int color) {
		fill(m, x, y, w, 1, color);
		fill(m, x, y + h - 1, w, 1, color);
		fill(m, x, y, 1, h, color);
		fill(m, x + w - 1, y, 1, h, color);
	}

	public static int textWidth(String s) {
		return MinecraftClient.getInstance().textRenderer.getWidth(s);
	}

	public static void text(MatrixStack m, String s, float x, float y, int color) {
		MinecraftClient.getInstance().textRenderer.draw(m, s, x, y, color);
	}

	public static void textShadow(MatrixStack m, String s, float x, float y, int color) {
		MinecraftClient.getInstance().textRenderer.drawWithShadow(m, s, x, y, color);
	}

	public static void centered(MatrixStack m, String s, int centerX, int y, int color) {
		DrawableHelper.drawCenteredText(m, MinecraftClient.getInstance().textRenderer, s, centerX, y, color);
	}

	/**
	 * Filled rounded rectangle with real curved corners, rendered in one pass.
	 * Falls back to a plain rect for radius <= 1.
	 */
	public static void roundedRect(MatrixStack m, float x, float y, float w, float h, float radius, int color) {
		int a = (color >> 24) & 0xFF;
		if (a == 0) return;
		float r = Math.max(0F, Math.min(radius, Math.min(w, h) / 2F - 0.01F));
		if (r <= 0.5F) {
			fill(m, Math.round(x), Math.round(y), Math.round(w), Math.round(h), color);
			return;
		}
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableTexture();
		BufferBuilder buf = Tessellator.getInstance().getBuffer();
		buf.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
		roundedGeometry(buf, m.peek().getModel(), x, y, w, h, r, color);
		Tessellator.getInstance().draw();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	/** Same as {@link #roundedRect} but feeds the open batch, no GL state switches. */
	public static void batchRounded(float x, float y, float w, float h, float radius, int color) {
		if (!batchOpen) return;
		int a = (color >> 24) & 0xFF;
		if (a == 0) return;
		float r = Math.max(0F, Math.min(radius, Math.min(w, h) / 2F - 0.01F));
		if (r <= 0.5F) {
			batchQuad(x, y, w, h, color);
			return;
		}
		roundedGeometry(batchBuffer(), batchMat, x, y, w, h, r, color);
	}

	private static void roundedGeometry(BufferBuilder buf, Matrix4f mat,
	                                    float x, float y, float w, float h, float r, int color) {
		int rr = (color >> 16) & 0xFF;
		int gg = (color >> 8) & 0xFF;
		int bb = color & 0xFF;
		int a = (color >> 24) & 0xFF;

		float x0 = x;
		float y0 = y;
		float x1 = x + w;
		float y1 = y + h;

		// center + four bars
		verts(buf, mat, x0 + r, y0 + r, x1 - r, y1 - r, rr, gg, bb, a);
		verts(buf, mat, x0 + r, y0, x1 - r, y0 + r, rr, gg, bb, a);
		verts(buf, mat, x0 + r, y1 - r, x1 - r, y1, rr, gg, bb, a);
		verts(buf, mat, x0, y0 + r, x0 + r, y1 - r, rr, gg, bb, a);
		verts(buf, mat, x1 - r, y0 + r, x1, y1 - r, rr, gg, bb, a);

		// four corner quarter-circles
		arc(buf, mat, x0 + r, y0 + r, r, (float) Math.PI, (float) (Math.PI * 1.5), rr, gg, bb, a);
		arc(buf, mat, x1 - r, y0 + r, r, (float) (Math.PI * 1.5), (float) (Math.PI * 2), rr, gg, bb, a);
		arc(buf, mat, x1 - r, y1 - r, r, 0F, (float) (Math.PI * 0.5), rr, gg, bb, a);
		arc(buf, mat, x0 + r, y1 - r, r, (float) (Math.PI * 0.5), (float) Math.PI, rr, gg, bb, a);
	}

	private static void verts(BufferBuilder buf, Matrix4f mat,
	                          float l, float t, float right, float b,
	                          int r, int g, int bl, int a) {
		buf.vertex(mat, l, b, 0).color(r, g, bl, a).next();
		buf.vertex(mat, right, b, 0).color(r, g, bl, a).next();
		buf.vertex(mat, right, t, 0).color(r, g, bl, a).next();
		buf.vertex(mat, l, t, 0).color(r, g, bl, a).next();
	}

	private static void arc(BufferBuilder buf, Matrix4f mat, float cx, float cy, float r,
	                        float a0, float a1, int rr, int gg, int bb, int a) {
		int seg = 2;
		for (int i = 0; i < seg; i++) {
			float t0 = i / (float) seg;
			float t1 = (i + 1) / (float) seg;
			float ang0 = a0 + (a1 - a0) * t0;
			float ang1 = a0 + (a1 - a0) * t1;
			float ax = cx + (float) Math.cos(ang0) * r;
			float ay = cy + (float) Math.sin(ang0) * r;
			float bx = cx + (float) Math.cos(ang1) * r;
			float by = cy + (float) Math.sin(ang1) * r;
			buf.vertex(mat, cx, cy, 0).color(rr, gg, bb, a).next();
			buf.vertex(mat, ax, ay, 0).color(rr, gg, bb, a).next();
			buf.vertex(mat, bx, by, 0).color(rr, gg, bb, a).next();
			buf.vertex(mat, bx, by, 0).color(rr, gg, bb, a).next();
		}
	}
}