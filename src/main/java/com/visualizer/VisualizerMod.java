package com.visualizer;

import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Visualizer entrypoint. Client-only visual layer — never touches gameplay.
 */
public final class VisualizerMod {
	public static final String MOD_ID = "visualizer";
	public static final Logger LOGGER = LogManager.getLogger("Visualizer");

	private VisualizerMod() {
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}