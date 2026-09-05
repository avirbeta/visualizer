package com.visualizer.mixin;

import com.visualizer.client.ui.screens.TitleOverlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds a small Visualizer corner button to the vanilla title screen.
 * The original menu buttons are left untouched — the screen is never covered.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	protected TitleScreenMixin() {
		super(null);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void visualizerInit(CallbackInfo ci) {
		TitleOverlay.get().install((TitleScreen) (Object) this);
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void visualizerRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		TitleOverlay.get().render(matrices, mouseX, mouseY, delta);
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void visualizerMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (TitleOverlay.get().mouseClicked(mouseX, mouseY, button)) {
			cir.setReturnValue(true);
		}
	}
}