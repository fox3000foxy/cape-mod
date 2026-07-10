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
        "ewogICJ0aW1lc3RhbXAiIDogMTc4MzY2NjMxNjI2OSwKICAicHJvZmlsZUlkIiA6ICJkOTBiNjhiYzgxNzI0MzI5YTA0N2YxMTg2ZGNkNDMzNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJha3Jvbm1hbjEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2U2ZGVmY2I3ZGU1YTBlMDVjNzUyNWM2Y2Q0NmU0YjliNDE2YjkyZTBjZjRiYWExZTBhOWUyMTJhODg3ZjNmNyIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzBlZmZmYWY4NmZlNWJjMDg5NjA4ZDNjYjI5N2QzZTI3NmI5ZWI3YThmOWYyZmU2NjU5YzIzYTJkOGIxOGVkZiIKICAgIH0KICB9Cn0=";

    private static final String TEXTURES_SIGNATURE =
        "oxoAfZRLVNSfXYFMNbDKZ9XxrTHmz/k2yxzOxksXY3f6aDhY3gCyFCCtDreEWI7fpG9BXirJeaFJw+bGOAVcShiHWEsD/dbP2XWfQ+uwhFkvIGn4i9phQ+MN+a3mD578bmQlM/Cnw1Dpfj0vcs+PBtURQJPAbon9lxR+++MDQqEcT+9mezWFSY0H9HR4f1OdjfZwh7K8gwTtcm8X6lLRYyK6f31myavBc66Jal3V3vIFzhPrmL0p73pBb1ug2XEKCu31lDa4KCFu6nJWRTqMlLKVXYg75KsB89g0lDX0waQSOvEouX8VQaUK8Se1hTqChMUTuJxeTBYMBtq+tg7yACFK2ULCl/03c+oL3oGZqzZ6ID1nXiArfai1K4j42rtko6rn+y5SzoX+Rb/yju+bjeJFWt5mDhkOcfA+DP5OkyO0BDaZucBwPqKd5gkW5+VG+Ew0camXFwRcunbqs7IkaibXOZkQ+3Z9N80XBI336DfwzpYM6eRkUVPGmI7c/M1VdUVwFVdEdqRYS/CFjGVWCAVI2U0LawYXxAwc2RIThyYSguSruveoY8h1xQF3IkZr6ZOkC2VXGM8xPbk539AhVKciaha1pimDRIW22z5mtOchOW3jzUPhxwH90Ox8Fe/Fg0RGxVOzI/4GY8kukoB4CXWQ2vApY22IcljSNuEpkAk=";

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
