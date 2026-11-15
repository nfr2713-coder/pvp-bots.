package ru.pvpbots;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

/** Все боты живут только в ClientWorld. Ни одного пакета на сервер они не создают. */
public final class BotManager {
    public static Store store = new Store();
    public static BotData pullTarget;

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pvpbots.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ClientWorld lastWorld;
    private static int nextId = -100000;

    private BotManager() {}

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                Store s = GSON.fromJson(Files.readString(FILE), Store.class);
                if (s != null) store = s;
            }
        } catch (Exception e) {
            PvpBotsClient.LOG.error("Не удалось загрузить конфиг", e);
        }
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(store));
        } catch (Exception e) {
            PvpBotsClient.LOG.error("Не удалось сохранить конфиг", e);
        }
    }

    public static Vec3d pos(Entity e) {
        return new Vec3d(e.getX(), e.getY(), e.getZ());
    }

    public static String dimOf(ClientWorld w) {
        return w.getRegistryKey().getValue().toString();
    }

    public static BotData find(Entity e) {
        for (BotData b : store.bots) if (b.entity == e) return b;
        return null;
    }

    public static BotData create(MinecraftClient mc) {
        BotData b = new BotData();
        b.name = "Бот " + (store.bots.size() + 1);
        b.skin = store.bots.size() % 18;
        if (mc.player != null && mc.world != null) {
            Vec3d look = mc.player.getRotationVec(1f);
            b.x = mc.player.getX() + look.x * 3;
            b.y = mc.player.getY();
            b.z = mc.player.getZ() + look.z * 3;
            b.dimension = dimOf(mc.world);
        }
        store.bots.add(b);
        refresh(mc, b);
        save();
        return b;
    }

    public static void remove(MinecraftClient mc, BotData b) {
        despawn(mc, b);
        store.bots.remove(b);
        if (pullTarget == b) pullTarget = null;
        save();
    }

    public static void removeAll(MinecraftClient mc) {
        for (BotData b : new ArrayList<>(store.bots)) despawn(mc, b);
        store.bots.clear();
        pullTarget = null;
        save();
    }

    public static void despawn(MinecraftClient mc, BotData b) {
        if (b.entity != null && mc.world != null) {
            mc.world.removeEntity(b.entity.getId(), Entity.RemovalReason.DISCARDED);
        }
        b.entity = null;
    }

    /** Пересоздаёт бота (применяет имя, скин, комплект, координаты, измерение). */
    public static void refresh(MinecraftClient mc, BotData b) {
        despawn(mc, b);
        b.vel = Vec3d.ZERO;
        if (mc.world == null) return;
        if (!b.dimension.equals(dimOf(mc.world))) return; // появится, когда вы зайдёте в это измерение
        GameProfile gp = new GameProfile(new UUID(0L, Math.floorMod(b.skin, 18)), b.name);
        OtherClientPlayerEntity e = new OtherClientPlayerEntity(mc.world, gp);
        e.setUuid(UUID.randomUUID());
        e.setId(nextId--);
        e.refreshPositionAndAngles(b.x, b.y, b.z, 0f, 0f);
        applyKit(e, b);
        mc.world.addEntity(e);
        b.entity = e;
    }

    public static ItemStack stack(String id) {
        Identifier i = Identifier.tryParse(id == null ? "" : id.trim());
        if (i == null || !Registries.ITEM.containsId(i)) return ItemStack.EMPTY;
        return new ItemStack(Registries.ITEM.get(i));
    }

    public static boolean validItem(String id) {
        Identifier i = Identifier.tryParse(id == null ? "" : id.trim());
        return i != null && Registries.ITEM.containsId(i);
    }

    public static void applyKit(OtherClientPlayerEntity e, BotData b) {
        e.equipStack(EquipmentSlot.MAINHAND, stack(b.mainHand));
        e.equipStack(EquipmentSlot.HEAD, stack(b.head));
        e.equipStack(EquipmentSlot.CHEST, stack(b.chest));
        e.equipStack(EquipmentSlot.LEGS, stack(b.legs));
        e.equipStack(EquipmentSlot.FEET, stack(b.feet));
    }

    public static void tick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            lastWorld = null;
            pullTarget = null;
            return;
        }
        if (mc.world != lastWorld) {
            lastWorld = mc.world;
            pullTarget = null;
            for (BotData b : store.bots) {
                b.entity = null;
                refresh(mc, b);
            }
        }
        for (BotData b : store.bots) {
            if (b.entity != null && !b.entity.isRemoved()) tickBot(mc, b);
        }
        tickPull(mc);
    }

    private static void tickBot(MinecraftClient mc, BotData b) {
        OtherClientPlayerEntity e = b.entity;
        PlayerEntity p = mc.player;
        double dx = p.getX() - e.getX(), dz = p.getZ() - e.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (MathHelper.atan2(dz, dx) * 180.0 / Math.PI) - 90f;
        e.setYaw(yaw);
        e.setHeadYaw(yaw);
        e.setBodyYaw(yaw);

        Vec3d want = Vec3d.ZERO;
        if (dist > 0.001) {
            double nx = dx / dist, nz = dz / dist;
            if (b.behavior == 1 && dist > 2.5) {
                want = new Vec3d(nx * b.speed, 0, nz * b.speed);
            } else if (b.behavior == 2) {
                double radial = dist > 3.5 ? 1 : (dist < 2.5 ? -1 : 0);
                want = new Vec3d((-nz + nx * radial) * b.speed, 0, (nx + nz * radial) * b.speed);
            }
        }
        double vy = (b.vel.y - 0.08) * 0.98;
        double hf = e.isOnGround() ? 0.6 : 0.91;
        b.vel = new Vec3d(b.vel.x * hf, vy, b.vel.z * hf);
        Vec3d total = b.vel.add(want);
        e.move(MovementType.SELF, total);
        if (e.isOnGround() && b.vel.y < 0) b.vel = new Vec3d(b.vel.x, 0, b.vel.z);
        if (e.getY() < -100) e.refreshPositionAndAngles(b.x, b.y, b.z, yaw, 0f);
    }

    private static void tickPull(MinecraftClient mc) {
        if (pullTarget == null) return;
        BotData b = pullTarget;
        if (b.entity == null || b.entity.isRemoved()) { pullTarget = null; return; }
        PlayerEntity p = mc.player;
        Vec3d t = pos(b.entity);
        Vec3d d = t.subtract(pos(p));
        double dist = d.length();
        double step = Math.max(1.0, store.pullSpeed) / 20.0;
        if (dist <= step) {
            p.setPosition(t.x, t.y, t.z);
            p.setVelocity(Vec3d.ZERO);
            pullTarget = null;
            return;
        }
        Vec3d np = pos(p).add(d.normalize().multiply(step));
        p.setPosition(np.x, np.y, np.z);
        p.setVelocity(Vec3d.ZERO);
    }

    /** Удар по боту: только локальный эффект, на сервер ничего не уходит. */
    public static void hit(MinecraftClient mc, BotData b, PlayerEntity p) {
        OtherClientPlayerEntity e = b.entity;
        if (e == null) return;
        double dx = e.getX() - p.getX(), dz = e.getZ() - p.getZ();
        double len = Math.max(0.001, Math.sqrt(dx * dx + dz * dz));
        b.vel = b.vel.add(dx / len * 0.5, 0.4, dz / len * 0.5);
        e.handleStatus((byte) 2); // анимация получения урона
        if (store.maceEnabled && p.getMainHandStack().isOf(Items.MACE)) {
            Vec3d v = p.getVelocity();
            p.setVelocity(v.x, store.maceLaunch, v.z);
        }
    }

    public static void say(MinecraftClient mc, String s) {
        if (mc.player != null) mc.player.sendMessage(Text.literal(s), true);
    }
}
