package ru.pvpbots;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;

public class SettingsScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget fSpeed, fLaunch;
    private ButtonWidget maceBtn;

    public SettingsScreen(Screen parent) {
        super(Text.literal("Настройки"));
        this.parent = parent;
    }

    private String mace() {
        return "Подброс булавой: " + (BotManager.store.maceEnabled ? "§aвкл" : "§cвыкл");
    }

    @Override
    protected void init() {
        int cx = width / 2;
        fSpeed = new TextFieldWidget(textRenderer, cx - 100, 50, 200, 20, Text.empty());
        fSpeed.setText(String.format(Locale.ROOT, "%.1f", BotManager.store.pullSpeed));
        addDrawableChild(fSpeed);
        fLaunch = new TextFieldWidget(textRenderer, cx - 100, 94, 200, 20, Text.empty());
        fLaunch.setText(String.format(Locale.ROOT, "%.2f", BotManager.store.maceLaunch));
        addDrawableChild(fLaunch);
        maceBtn = addDrawableChild(ButtonWidget.builder(Text.literal(mace()), btn -> {
            BotManager.store.maceEnabled = !BotManager.store.maceEnabled;
            maceBtn.setMessage(Text.literal(mace()));
        }).dimensions(cx - 100, 124, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Готово"), btn -> close())
                .dimensions(cx - 100, 156, 200, 20).build());
    }

    private static double num(TextFieldWidget f, double def) {
        try {
            return Double.parseDouble(f.getText().trim().replace(',', '.'));
        } catch (Exception e) {
            return def;
        }
    }

    @Override
    public void close() {
        BotManager.store.pullSpeed = MathHelper.clamp(num(fSpeed, BotManager.store.pullSpeed), 1, 500);
        BotManager.store.maceLaunch = MathHelper.clamp(num(fLaunch, BotManager.store.maceLaunch), 0.1, 10);
        BotManager.save();
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        int cx = width / 2;
        ctx.drawCenteredTextWithShadow(textRenderer, title, cx, 14, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Скорость притяжения к боту (1–500 блоков/сек)"),
                cx - 100, 38, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Сила подброса булавой (0.1–10, по умолчанию 1.5)"),
                cx - 100, 82, 0xFFAAAAAA);
    }
}
