package ru.pvpbots;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Locale;

public class BotEditScreen extends Screen {
    private static final String[] BEH = {"Стоять", "Следовать за вами", "Кружить вокруг вас"};
    private final Screen parent;
    private final BotData b;
    private TextFieldWidget fName, fX, fY, fZ, fDim;
    private ButtonWidget skinBtn, behBtn;

    public BotEditScreen(Screen parent, BotData b) {
        super(Text.literal("Бот: " + b.name));
        this.parent = parent;
        this.b = b;
    }

    private TextFieldWidget field(int x, int y, int w, String v) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, w, 20, Text.empty());
        f.setMaxLength(64);
        f.setText(v);
        addDrawableChild(f);
        return f;
    }

    private static String n(double v) {
        return String.format(Locale.ROOT, "%.2f", v);
    }

    private static double num(TextFieldWidget f, double def) {
        try {
            return Double.parseDouble(f.getText().trim().replace(',', '.'));
        } catch (Exception e) {
            return def;
        }
    }

    @Override
    protected void init() {
        int L = width / 2 - 155, R = width / 2 + 5;
        fName = field(L, 40, 310, b.name);
        fX = field(L, 76, 98, n(b.x));
        fY = field(L + 106, 76, 98, n(b.y));
        fZ = field(L + 212, 76, 98, n(b.z));
        fDim = field(L, 112, 310, b.dimension);

        int y = 140;
        skinBtn = addDrawableChild(ButtonWidget.builder(Text.literal("Скин: " + (b.skin + 1)), btn -> {
            b.skin = (b.skin + 1) % 18;
            skinBtn.setMessage(Text.literal("Скин: " + (b.skin + 1)));
            apply();
        }).dimensions(L, y, 150, 20).build());
        behBtn = addDrawableChild(ButtonWidget.builder(Text.literal("Поведение: " + BEH[b.behavior]), btn -> {
            b.behavior = (b.behavior + 1) % BEH.length;
            behBtn.setMessage(Text.literal("Поведение: " + BEH[b.behavior]));
            BotManager.save();
        }).dimensions(R, y, 150, 20).build());
        y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Применить / ТП бота"), btn -> apply())
                .dimensions(L, y, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Бот ко мне"), btn -> {
            if (client.player != null && client.world != null) {
                fX.setText(n(client.player.getX()));
                fY.setText(n(client.player.getY()));
                fZ.setText(n(client.player.getZ()));
                fDim.setText(BotManager.dimOf(client.world));
                apply();
            }
        }).dimensions(R, y, 150, 20).build());
        y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Тянуть меня к боту"), btn -> {
            apply();
            if (b.entity == null) {
                BotManager.say(client, "§cБот находится в другом измерении");
            } else {
                BotManager.pullTarget = b;
                client.setScreen(null);
            }
        }).dimensions(L, y, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Остановить притяжение"), btn -> BotManager.pullTarget = null)
                .dimensions(R, y, 150, 20).build());
        y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Комплект"), btn -> {
            apply();
            client.setScreen(new KitScreen(this, b));
        }).dimensions(L, y, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Настройки"),
                btn -> client.setScreen(new SettingsScreen(this))).dimensions(R, y, 150, 20).build());
        y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("§cУдалить бота"), btn -> {
            BotManager.remove(client, b);
            client.setScreen(new MainScreen());
        }).dimensions(L, y, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), btn -> close())
                .dimensions(R, y, 150, 20).build());
    }

    private void apply() {
        String nm = fName.getText().trim();
        if (!nm.isEmpty()) b.name = nm;
        b.x = num(fX, b.x);
        b.y = num(fY, b.y);
        b.z = num(fZ, b.z);
        String d = fDim.getText().trim();
        if (Identifier.tryParse(d) != null) b.dimension = d;
        BotManager.refresh(client, b);
        BotManager.save();
    }

    @Override
    public void close() {
        apply();
        client.setScreen(parent instanceof MainScreen ? new MainScreen() : parent);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        int L = width / 2 - 155;
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Имя"), L, 30, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("X"), L, 66, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Y"), L + 106, 66, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Z"), L + 212, 66, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("Измерение (например minecraft:the_nether, minecraft:the_end)"), L, 102, 0xFFAAAAAA);
        if (client != null && client.player != null) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Вы: " + n(client.player.getX()) + " "
                    + n(client.player.getY()) + " " + n(client.player.getZ())), width / 2, height - 20, 0xFFAAAAAA);
        }
    }
}
