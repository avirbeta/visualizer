package com.visualizer.client.hud;

import com.visualizer.client.config.VisualizerConfig;
import com.visualizer.client.notifications.NotificationManager;
import com.visualizer.client.ui.DrawUtil;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.LightType;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Client HUD overlay. Read-only display — never modifies player state.
 */
public final class HudManager {
	private static final HudManager INSTANCE = new HudManager();
	private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final String VERSION = FabricLoader.getInstance()
		.getModContainer("visualizer")
		.map(container -> container.getMetadata().getVersion().getFriendlyString())
		.orElse("1.1.0");
	private static final int SAMPLES = 60;
	private static final int DROP_FPS = 55;
	private static final int RESTORE_FPS = 62;
	private static final int RECOVER_FRAMES = 40;

	/** Costly HUD elements, temporarily dropped when the game is under load. */
	private static final Set<String> HEAVY = new HashSet<>(
		Arrays.asList("biome", "light", "keystrokes", "fpsgraph", "day"));

	private final List<HudElement> elements = new ArrayList<>();
	private final float[] fpsSamples = new float[SAMPLES];
	private int sampleIndex;
	private long lastFpsUpdate;
	private long fpsTimer;
	private int fps;
	private int frames;
	private boolean dropped;
	private int recoverFrames;

	private HudManager() {
	}

	public static HudManager get() {
		return INSTANCE;
	}

	public void register() {
		reloadFromConfig();
		HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.options.hudHidden) return;
			VisualizerConfig config = VisualizerConfig.get();
			if (!config.visualizerEnabled || !config.hudVisible) return;
			if (mc.currentScreen != null) return;
			int w = mc.getWindow().getScaledWidth();
			int h = mc.getWindow().getScaledHeight();
			render(matrices, w, h, false);
			NotificationManager.get().render(matrices, w, h);
		});
	}

	public void reloadFromConfig() {
		VisualizerConfig c = VisualizerConfig.get();
		elements.clear();
		elements.add(new HudElement("fps", c.hudFpsX, c.hudFpsY, this::fpsText, () -> c.hudFps));
		elements.add(new HudElement("ping", c.hudPingX, c.hudPingY, this::pingText, () -> c.hudPing));
		elements.add(new HudElement("coords", c.hudCoordsX, c.hudCoordsY, this::coordsText, () -> c.hudCoordinates));
		elements.add(new HudElement("clock", c.hudClockX, c.hudClockY,
			() -> LocalTime.now().format(CLOCK), () -> c.hudClock));
		elements.add(new HudElement("direction", c.hudDirectionX, c.hudDirectionY, this::directionText, () -> c.hudDirection));
		elements.add(new HudElement("keystrokes", c.hudKeystrokesX, c.hudKeystrokesY, this::keystrokesText, () -> c.hudKeystrokes));
		elements.add(new HudElement("armor", c.hudArmorX, c.hudArmorY, this::armorText, () -> c.hudArmor));
		elements.add(new HudElement("status", c.hudStatusX, c.hudStatusY, this::statusText, () -> c.hudStatus));
		elements.add(new HudElement("server", c.hudServerX, c.hudServerY, this::serverText, () -> c.hudServer));
		elements.add(new HudElement("day", c.hudDayX, c.hudDayY, this::dayText, () -> c.hudDay));
		elements.add(new HudElement("biome", c.hudBiomeX, c.hudBiomeY, this::biomeText, () -> c.hudBiome));
		elements.add(new HudElement("light", c.hudLightX, c.hudLightY, this::lightText, () -> c.hudLight));
		elements.add(new SparklineElement("fpsgraph", c.hudFpsGraphX, c.hudFpsGraphY, () -> fpsSamples));
		elements.add(new HudElement("watermark", c.hudWatermarkX, c.hudWatermarkY,
			() -> "Visualizer v" + VERSION, () -> c.hudWatermark));
	}

	public void saveToConfig() {
		VisualizerConfig c = VisualizerConfig.get();
		for (HudElement e : elements) {
			if ("fps".equals(e.id)) {
				c.hudFpsX = e.normX();
				c.hudFpsY = e.normY();
			} else if ("ping".equals(e.id)) {
				c.hudPingX = e.normX();
				c.hudPingY = e.normY();
			} else if ("coords".equals(e.id)) {
				c.hudCoordsX = e.normX();
				c.hudCoordsY = e.normY();
			} else if ("clock".equals(e.id)) {
				c.hudClockX = e.normX();
				c.hudClockY = e.normY();
			} else if ("direction".equals(e.id)) {
				c.hudDirectionX = e.normX();
				c.hudDirectionY = e.normY();
			} else if ("keystrokes".equals(e.id)) {
				c.hudKeystrokesX = e.normX();
				c.hudKeystrokesY = e.normY();
			} else if ("armor".equals(e.id)) {
				c.hudArmorX = e.normX();
				c.hudArmorY = e.normY();
			} else if ("status".equals(e.id)) {
				c.hudStatusX = e.normX();
				c.hudStatusY = e.normY();
			} else if ("server".equals(e.id)) {
				c.hudServerX = e.normX();
				c.hudServerY = e.normY();
			} else if ("day".equals(e.id)) {
				c.hudDayX = e.normX();
				c.hudDayY = e.normY();
			} else if ("biome".equals(e.id)) {
				c.hudBiomeX = e.normX();
				c.hudBiomeY = e.normY();
			} else if ("light".equals(e.id)) {
				c.hudLightX = e.normX();
				c.hudLightY = e.normY();
			} else if ("fpsgraph".equals(e.id)) {
				c.hudFpsGraphX = e.normX();
				c.hudFpsGraphY = e.normY();
			} else if ("watermark".equals(e.id)) {
				c.hudWatermarkX = e.normX();
				c.hudWatermarkY = e.normY();
			}
		}
	}

	public void render(MatrixStack matrices, int w, int h, boolean editor) {
		updateFps();
		if (editor) {
			for (HudElement e : elements) {
				e.render(matrices, w, h, true);
			}
			return;
		}
		boolean heavy = updateAdaptive();
		if (heavy) {
			HudElement.THROTTLE_MS = 350;
		} else {
			HudElement.THROTTLE_MS = 120;
		}
		DrawUtil.beginBatch(matrices);
		for (HudElement e : elements) {
			if (heavy && HEAVY.contains(e.id)) continue;
			if (e.isVisible()) e.renderBackground(matrices, w, h);
		}
		DrawUtil.flushBatch();
		for (HudElement e : elements) {
			if (heavy && HEAVY.contains(e.id)) continue;
			if (e.isVisible()) e.renderText(matrices, w, h);
		}
	}

	public HudElement hitTest(double mx, double my, int w, int h) {
		for (int i = elements.size() - 1; i >= 0; i--) {
			HudElement e = elements.get(i);
			if (e.contains(mx, my, w, h)) return e;
		}
		return null;
	}

	private void updateFps() {
		long now = System.currentTimeMillis();
		frames++;
		if (now - fpsTimer >= 250) {
			fps = Math.round(frames * 1000F / Math.max(1L, now - fpsTimer));
			frames = 0;
			fpsTimer = now;
			lastFpsUpdate = now;
			fpsSamples[sampleIndex] = fps;
			sampleIndex = (sampleIndex + 1) % SAMPLES;
		}
	}

	/** Drops heavy elements while the game is slow, restores once it recovers. */
	private boolean updateAdaptive() {
		if (!dropped) {
			if (fps < DROP_FPS) {
				dropped = true;
				recoverFrames = 0;
			}
		} else {
			if (fps >= RESTORE_FPS) {
				recoverFrames++;
			} else {
				recoverFrames = 0;
			}
			if (recoverFrames > RECOVER_FRAMES) {
				dropped = false;
				recoverFrames = 0;
			}
		}
		return dropped;
	}

	private String fpsText() {
		return "FPS " + fps;
	}

	private String pingText() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.getNetworkHandler() == null || mc.player == null) return "Ping —";
		PlayerListEntry info = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
		if (info == null) return "Ping —";
		return "Ping " + info.getLatency() + "ms";
	}

	private String coordsText() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null) return "XYZ —";
		return String.format("XYZ %.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
	}

	private String directionText() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null) return "Dir —";
		Direction d = mc.player.getHorizontalFacing();
		return "Dir " + d.asString().toUpperCase();
	}

	private String keystrokesText() {
		MinecraftClient mc = MinecraftClient.getInstance();
		long win = mc.getWindow().getHandle();
		StringBuilder sb = new StringBuilder("Keys ");
		if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) sb.append('W');
		if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) sb.append('A');
		if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) sb.append('S');
		if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) sb.append('D');
		if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) sb.append('_');
		return sb.toString();
	}

	private String armorText() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null) return "Armor —";
		int pieces = 0;
		for (net.minecraft.item.ItemStack stack : mc.player.getArmorItems()) {
			if (!stack.isEmpty()) pieces++;
		}
		return "Armor " + pieces + "/4";
	}

	private String statusText() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null) return "Status —";
		return String.format("HP %.0f  Food %d", mc.player.getHealth(), mc.player.getHungerManager().getFoodLevel());
	}

	private String serverText() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.getCurrentServerEntry() == null) return "Server —";
		String name = mc.getCurrentServerEntry().name;
		String address = mc.getCurrentServerEntry().address;
		String shown = (name != null && !name.isEmpty()) ? name : (address != null ? address : "?");
		return "Server " + shown;
	}

	private String dayText() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.world == null) return "Day —";
		long ticks = mc.world.getTimeOfDay();
		int day = (int) (ticks / 24000L) + 1;
		int tod = (int) (ticks % 24000L);
		int h = tod / 1000;
		int m = (tod % 1000) * 60 / 1000;
		return String.format("Day %d %02d:%02d", day, h, m);
	}

	private String biomeText() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.world == null || mc.player == null) return "Biome —";
		net.minecraft.util.registry.Registry<net.minecraft.world.biome.Biome> biomes =
			((net.minecraft.client.world.ClientWorld) mc.world).getRegistryManager().get(Registry.BIOME_KEY);
		if (biomes == null) return "Biome —";
		Identifier id = biomes.getId(mc.world.getBiome(mc.player.getBlockPos()));
		if (id == null || id.getPath().isEmpty()) return "Biome —";
		String[] parts = id.getPath().split("_");
		StringBuilder sb = new StringBuilder("Biome ");
		for (int i = 0; i < parts.length; i++) {
			String p = parts[i];
			if (!p.isEmpty()) sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
		}
		return sb.toString().trim();
	}

	private String lightText() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.world == null || mc.player == null) return "Light —";
		net.minecraft.util.math.BlockPos pos = mc.player.getBlockPos();
		int sky = mc.world.getLightLevel(LightType.SKY, pos);
		int block = mc.world.getLightLevel(LightType.BLOCK, pos);
		return "Light " + Math.max(sky, block) + " (s" + sky + "/b" + block + ")";
	}
}