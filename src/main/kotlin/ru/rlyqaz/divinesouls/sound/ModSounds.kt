package ru.rlyqaz.divinesouls.sound

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import ru.rlyqaz.divinesouls.DivineSoulsMod

object ModSounds {
    val REGISTRY: DeferredRegister<SoundEvent> = DeferredRegister.create(Registries.SOUND_EVENT, DivineSoulsMod.MOD_ID)

    // ЖЕСТКИЙ ФИКС: Явно указываем тип DeferredHolder<SoundEvent, SoundEvent> перед '='!
    val SORROW_WRAITH_AMBIENT_1: DeferredHolder<SoundEvent, SoundEvent> = REGISTRY.register("sorrow_wraith_ambient_1", java.util.function.Supplier {
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "sorrow_wraith_ambient_1"))
    })

    val SORROW_WRAITH_AMBIENT_2: DeferredHolder<SoundEvent, SoundEvent> = REGISTRY.register("sorrow_wraith_ambient_2", java.util.function.Supplier {
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "sorrow_wraith_ambient_2"))
    })

    val SORROW_WRAITH_HURT: DeferredHolder<SoundEvent, SoundEvent> = REGISTRY.register("sorrow_wraith_hurt", java.util.function.Supplier {
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "sorrow_wraith_hurt"))
    })

    val SORROW_WRAITH_DEATH: DeferredHolder<SoundEvent, SoundEvent> = REGISTRY.register("sorrow_wraith_death", java.util.function.Supplier {
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, "sorrow_wraith_death"))
    })

    fun register(eventBus: IEventBus) {
        REGISTRY.register(eventBus)
    }
}
