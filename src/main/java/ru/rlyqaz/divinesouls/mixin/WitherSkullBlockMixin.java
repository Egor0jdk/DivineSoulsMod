package ru.rlyqaz.divinesouls.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherSkullBlock.class)
public class WitherSkullBlockMixin {

    // Полностью отключаем ванильную проверку Т-образной структуры на 3 черепа
    @Inject(method = "checkSpawn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/SkullBlockEntity;)V", at = @At("HEAD"), cancellable = true)
    private static void disableVanillaWitherSpawn(Level level, BlockPos pos, SkullBlockEntity blockEntity, CallbackInfo ci) {
        ci.cancel();
    }
}
