package ru.pvpbots;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class KitScreen extends Screen {
    private final Screen parent;
    private final BotData b;
    private TextFieldWidget fMain, fHead, fChest, fLegs, fFeet;

    public KitScreen(Screen parent, BotData b) {
        super(Text.literal("Редактор комплекта: " + b.name));
        this.parent = parent;
        this.b = b;
    }

    private TextFieldWidget field(int y, String v) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, width / 2 - 100, y, 200, 20, Text.empty());
        f.setMaxLength(100);
        f.setText(v);
        addDrawableChild(f);
        return f;
    }

    private void preset(String main, String h, String c, String l, String f) {
        fMain.setText(main);
        fHead.setText(h);
        fChest.setText(c);
        fLegs.setText(l);
        fFeet.setText(f);
    }

    @Override
    protected void init() {
        fMain = field(44, b.mainHand);
        fHead = field(80, b.head);
        fChest = field(116, b.chest);
        fLegs = field(152, b.legs);
        fFeet = field(188, b.feet);
        int cx = width / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Алмаз"), btn -> preset("minecraft:diamond_sword",
                "minecraft:diamond_helmet", "minecraft:diamond_chestplate", "minecraft:diamond_leggings",
                "minecraft:diamond_boots")).dimensions(cx - 100, 216, 64, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Незерит"), btn -> preset("minecraft:netherite_sword",
                "minecraft:netherite_helmet", "minecraft:netherite_chestplate", "minecraft:netherite_leggings",
                "minecraft:netherite_boots")).dimensions(cx - 32, 216, 64, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Пусто"), btn -> preset("minecraft:air",
                "minecraft:air", "minecraft:air", "minecraft:air", "minecraft:air"))
                .dimensions(cx + 36, 216, 64, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Булава"), btn -> fMain.setText("minecraft:mace"))
                .dimensions(cx - 100, 240, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Сохранить"), btn -> save())
                .dimensions(cx - 100, 264, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), btn -> close())
                .dimensions(cx + 2, 264, 98, 20).build());
    }

    private String ok(TextFieldWidget f, String old) {
        String s = f.getText().trim();
        if (s.equals("minecraft:air") || BotManager.validItem(s)) return s;
        BotManager.say(client, "§cНеизвестный предмет: " + s);
        return old;
    }

    private void save() {
        b.mainHand = ok(fMain, b.mainHand);
        b.head = ok(fHead, b.head);
        b.chest = ok(fChest, b.chest);
        b.legs = ok(fLegs, b.legs);
        b.feet = ok(fFeet, b.feet);
        BotManager.refresh(client, b);
        BotManager.save();
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        int x = width / 2 - 100;
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Предмет в руке"), x, 34, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Шлем"), x, 70, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Нагрудник"), x, 106, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Поножи"), x, 142, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Ботинки"), x, 178, 0xFFAAAAAA);
    }
}
