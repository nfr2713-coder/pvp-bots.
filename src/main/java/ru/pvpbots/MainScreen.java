package ru.pvpbots;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class MainScreen extends Screen {
    public MainScreen() {
        super(Text.literal("PvP Боты"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = 36;
        List<BotData> bots = BotManager.store.bots;
        for (int i = 0; i < Math.min(bots.size(), 8); i++) {
            BotData b = bots.get(i);
            String mark = b.entity != null ? "§a●" : "§7○";
            addDrawableChild(ButtonWidget.builder(
                    Text.literal(mark + " §f" + b.name + " §7[" + b.dimension + "]"),
                    btn -> client.setScreen(new BotEditScreen(this, b)))
                    .dimensions(cx - 120, y, 240, 20).build());
            y += 24;
        }
        int by = height - 52;
        addDrawableChild(ButtonWidget.builder(Text.literal("Создать бота"), btn -> {
            BotData b = BotManager.create(client);
            client.setScreen(new BotEditScreen(this, b));
        }).dimensions(cx - 120, by, 118, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Удалить всех"), btn -> {
            BotManager.removeAll(client);
            client.setScreen(new MainScreen());
        }).dimensions(cx + 2, by, 118, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Настройки"),
                btn -> client.setScreen(new SettingsScreen(this)))
                .dimensions(cx - 120, by + 24, 118, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), btn -> close())
                .dimensions(cx + 2, by + 24, 118, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("PvP Боты"), width / 2, 14, 0xFFFFFFFF);
        int n = BotManager.store.bots.size();
        if (n == 0) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Ботов пока нет. Нажмите «Создать бота»."),
                    width / 2, 50, 0xFFAAAAAA);
        } else if (n > 8) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Показаны первые 8 из " + n),
                    width / 2, 26, 0xFFAAAAAA);
        }
    }
}
