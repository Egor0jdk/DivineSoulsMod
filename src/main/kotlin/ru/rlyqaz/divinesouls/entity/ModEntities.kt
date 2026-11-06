package ru.rlyqaz.divinesouls.entity

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.VoidDragonEntity
import java.util.function.Supplier

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: ModEntities (Регистратор RPG-мобов и сущностей)
 * ====================================================================
 */
object ModEntities {
    private val REGISTRY: DeferredRegister<EntityType<*>> =
        DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, DivineSoulsMod.MOD_ID)

    // ФИКС: Переписали через явный Supplier, чтобы убрать ошибку Overload resolution ambiguity на первой строчке!
    val VOID_DRAGON: Supplier<EntityType<VoidDragonEntity>> = REGISTRY.register("void_dragon", Supplier {
        EntityType.Builder.of({ type: EntityType<VoidDragonEntity>, level ->
            VoidDragonEntity(type, level)
        }, MobCategory.MONSTER)
            .sized(1.2f, 1.2f)
            .build("void_dragon")
    })

    fun register(eventBus: IEventBus) {
        REGISTRY.register(eventBus)
    }
}
