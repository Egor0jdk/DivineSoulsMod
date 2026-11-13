package ru.rlyqaz.divinesouls.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ElytraItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ElytraItem.class)
public class ElytraItemMixin {

    // Перехватываем ванильный запрос "в какой слот брони вставляется этот предмет?"
    @Inject(method = "getEquipmentSlot", at = @At("HEAD"), cancellable = true)
    private void disableChestSlot(CallbackInfoReturnable<EquipmentSlot> cir) {
        // Возвращаем MAINHAND вместо CHEST — слот нагрудника перестаёт принимать элитры,
        // и они просто остаются висеть на курсоре, как шлем над ботинками!
        cir.setReturnValue(EquipmentSlot.MAINHAND);
    }
}
