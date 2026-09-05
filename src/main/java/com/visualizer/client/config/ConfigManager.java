package com.visualizer.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.visualizer.VisualizerMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and saves Visualizer settings only. Never touches Minecraft game options.
 */
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
		.getConfigDir().resolve("visualizer.json");

	private ConfigManager() {
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			VisualizerConfig.set(new VisualizerConfig());
			save();
			return;
		}
		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			VisualizerConfig loaded = GSON.fromJson(reader, VisualizerConfig.class);
			VisualizerConfig.set(loaded != null ? loaded : new VisualizerConfig());
		} catch (Exception e) {
			VisualizerMod.LOGGER.warn("Failed to load Visualizer config, using defaults", e);
			VisualizerConfig.set(new VisualizerConfig());
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(VisualizerConfig.get(), writer);
			}
		} catch (IOException e) {
			VisualizerMod.LOGGER.warn("Failed to save Visualizer config", e);
		}
	}

	public static void reset() {
		VisualizerConfig.set(new VisualizerConfig());
		save();
	}
}