package ru.rlyqaz.divinesouls.sound

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import ru.rlyqaz.divinesouls.DivineSoulsMod
import java.util.function.Supplier

object ModSounds {
    val REGISTRY: DeferredRegister<SoundEvent> = DeferredRegister.create(Registries.SOUND_EVENT, DivineSoulsMod.MOD_ID)

    private fun registerSound(name: String): DeferredHolder<SoundEvent, SoundEvent> =
        REGISTRY.register(name, Supplier {
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DivineSoulsMod.MOD_ID, name))
        })

    // ==================== ДУХ ПЕЧАЛИ (SORROW WRAITH) ====================
    val SORROW_WRAITH_AMBIENT_1: DeferredHolder<SoundEvent, SoundEvent> = registerSound("sorrow_wraith_ambient_1")
    val SORROW_WRAITH_AMBIENT_2: DeferredHolder<SoundEvent, SoundEvent> = registerSound("sorrow_wraith_ambient_2")
    val SORROW_WRAITH_HURT: DeferredHolder<SoundEvent, SoundEvent> = registerSound("sorrow_wraith_hurt")
    val SORROW_WRAITH_DEATH: DeferredHolder<SoundEvent, SoundEvent> = registerSound("sorrow_wraith_death")

    // ==================== ПЕСЧАНЫЙ КРАБ (SANDY CRAB) ====================
    val SANDY_CRAB_AMBIENT: DeferredHolder<SoundEvent, SoundEvent> = registerSound("sandy_crab_ambient")
    val SANDY_CRAB_HURT: DeferredHolder<SoundEvent, SoundEvent> = registerSound("sandy_crab_hurt")
    val SANDY_CRAB_DEATH: DeferredHolder<SoundEvent, SoundEvent> = registerSound("sandy_crab_death")

    fun register(eventBus: IEventBus) {
        REGISTRY.register(eventBus)
    }
}
