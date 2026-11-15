package ru.pvpbots;

import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class BotData {
    public String name = "Бот";
    public int skin = 0;                       // 0..17 стандартные скины
    public String dimension = "minecraft:overworld";
    public double x, y, z;
    public String mainHand = "minecraft:diamond_sword";
    public String head = "minecraft:diamond_helmet";
    public String chest = "minecraft:diamond_chestplate";
    public String legs = "minecraft:diamond_leggings";
    public String feet = "minecraft:diamond_boots";
    public int behavior = 0;                   // 0 стоять, 1 следовать, 2 кружить
    public double speed = 0.15;                // блоков за тик

    public transient OtherClientPlayerEntity entity;
    public transient Vec3d vel = Vec3d.ZERO;
}
