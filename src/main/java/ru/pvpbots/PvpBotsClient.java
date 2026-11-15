package ru.pvpbots;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PvpBotsClient implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("pvpbots");

    @Override
    public void onInitializeClient() {
        BotManager.load();

        KeyBinding open = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pvpbots.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G,
                KeyBinding.Category.create(Identifier.of("pvpbots", "main"))));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (open.wasPressed()) {
                if (mc.currentScreen == null && mc.world != null) mc.setScreen(new MainScreen());
            }
            BotManager.tick(mc);
        });

        // Бьём бота локально и отменяем действие, чтобы пакет с фейковым id не ушёл на сервер.
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient()) {
                BotData b = BotManager.find(entity);
                if (b != null) {
                    BotManager.hit(MinecraftClient.getInstance(), b, player);
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient() && BotManager.find(entity) != null) return ActionResult.FAIL;
            return ActionResult.PASS;
        });
    }
}
