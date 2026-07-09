package com.example.cape.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class ServerPlayerMixin {
    private static final String TEXTURES_VALUE =
        "ewogICJ0aW1lc3RhbXAiIDogMTc4MzYxOTcyNjAxMSwKICAicHJvZmlsZUlkIiA6ICI4NTNjODBlZjNjMzc0OWZkYWE0OTkzOGI2NzRhZGFlNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJqZWJfIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdmZDliYTQyYTdjODFlZWVhMjJmMTUyNDI3MWFlODVhOGUwNDVjZTBhZjVhNmFlMTZjNjQwNmFlOTE3ZTY4YjUiCiAgICB9LAogICAgIkNBUEUiIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzllNTA3YWZjNTYzNTk5NzhhM2ViM2UzMjM2NzA0MmI4NTNjZGRkMDk5NWQxN2QwZGE5OTU2NjI5MTNmYjAwZjciCiAgICB9CiAgfQp9";

    private static final String TEXTURES_SIGNATURE =
        "RgIPF4d/iTDWJVnEWgQuBEjBFrk7XwwSCu1OEDvzVBqkDfS+8r+v6Wmd/Nls9PPQdkdUakvLhuLAjT2cBQ1VLYe/ZiN7znoarzb8H+auzYA9cyeGmm8zf5a/+3TVGF7LyQVPuUnp3vyCadMfw+tJLFGeIVJGbXF+51clslDiNPxCuuRUHhH9QJ/9emZliK/buOQZ7zLh2lk83MrqsddyHKZaOXsNFg2CB/2BVjn8hVeXgHNEbhRinQ701gwKOMtnaKVHNFaF93nhcREDKLjlNmEmNS0lT59NrXxlOGOiFGhw81d8s0cYdOfG6AV2R5x6ZAJFzM7dKmcpR6gJNRnW5VQZBcdiQ1TDI56apnQabIW+e7BAwh0vuSuzc+f3yuCPwfD8l4HJDwGCY9Lc6se0qRas3a+i/sGYCalmc1134qTeu59o/MWizg4D8Z3RexcaK/JDpQrppRoSp8r4O6LfYpA2wRI9RhcCWNzyBEplC71hyqm1lz9T8cjT4sIKMa/z97lixc5/T7eK0AXVks4Z+4usJGjXBO9lAd44yjjpfKWi2ci51ebop3J9zeViifJfCuQER3t/1ZGJ3Wx4MIcEDpYO0g1WZwb+1+8a3zh2cwT3XVEOsdPIq6Gp9rP9QYoaqoTwXk5+vC2QWZGH6Ls0XQ1P7tZPQBFLqt0oCk7WWc4=";

    @Inject(method = "getGameProfile()Lcom/mojang/authlib/GameProfile;", at = @At("RETURN"), cancellable = true)
    private void injectCape(CallbackInfoReturnable<GameProfile> cir) {
        Player self = (Player) (Object) this;
        if (!(self instanceof ServerPlayer serverPlayer)) return;
        MinecraftServer server = ((ServerPlayerAccessor) serverPlayer).getServer();
        if (!(server instanceof IntegratedServer)) return;

        GameProfile host = server.getSingleplayerProfile();
        GameProfile original = cir.getReturnValue();
        if (host == null || !host.name().equals(original.name())) return;

        ImmutableMultimap.Builder<String, Property> b = ImmutableMultimap.builder();
        for (Property p : original.properties().values()) {
            if (!p.name().equals("textures")) {
                b.put(p.name(), p);
            }
        }
        b.put("textures", new Property("textures", TEXTURES_VALUE, TEXTURES_SIGNATURE));
        cir.setReturnValue(new GameProfile(original.id(), original.name(), new PropertyMap(b.build())));
    }
}
