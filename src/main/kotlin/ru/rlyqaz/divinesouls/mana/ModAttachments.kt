package ru.rlyqaz.divinesouls.mana

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import ru.rlyqaz.divinesouls.DivineSoulsMod
import java.util.function.Supplier

data class ManaData(
    val mana: Int = 100,
    val maxMana: Int = 100,
    val regenDelayTicks: Int = 0
) {
    companion object {
        val CODEC: Codec<ManaData> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("mana").forGetter { it.mana },
                Codec.INT.fieldOf("maxMana").forGetter { it.maxMana },
                Codec.INT.fieldOf("regenDelayTicks").forGetter { it.regenDelayTicks }
            ).apply(instance, ::ManaData)
        }
    }
}

object ModAttachments {
    val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, DivineSoulsMod.MOD_ID)

    val MANA: Supplier<AttachmentType<ManaData>> = ATTACHMENT_TYPES.register("mana", Supplier {
        AttachmentType.builder(Supplier { ManaData() })
            .serialize(ManaData.CODEC)
            .copyOnDeath() // Максимум маны сохраняется после смерти!
            .build()
    })

    fun register(modEventBus: IEventBus) {
        ATTACHMENT_TYPES.register(modEventBus)
    }
}
