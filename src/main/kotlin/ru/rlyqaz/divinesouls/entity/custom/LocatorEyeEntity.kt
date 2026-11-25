package ru.rlyqaz.divinesouls.entity.custom

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.ItemSupplier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import ru.rlyqaz.divinesouls.entity.ModEntities
import ru.rlyqaz.divinesouls.item.ModItems

class LocatorEyeEntity(entityType: EntityType<out LocatorEyeEntity>, level: Level) : Entity(entityType, level), ItemSupplier {

    companion object {
        private val DATA_ITEM_STACK: EntityDataAccessor<ItemStack> =
            SynchedEntityData.defineId(LocatorEyeEntity::class.java, EntityDataSerializers.ITEM_STACK)
    }

    private var targetX = 0.0
    private var targetY = 0.0
    private var targetZ = 0.0
    private var life = 0

    constructor(level: Level, x: Double, y: Double, z: Double) : this(ModEntities.LOCATOR_EYE.get(), level) {
        setPos(x, y, z)
    }

    fun setItem(stack: ItemStack) {
        if (!stack.isEmpty) {
            entityData.set(DATA_ITEM_STACK, stack.copyWithCount(1))
        }
    }

    override fun getItem(): ItemStack {
        val stack = entityData.get(DATA_ITEM_STACK)
        return if (stack.isEmpty) ItemStack(ModItems.LOCATOR_CORE.get()) else stack
    }

    fun signalTo(pos: BlockPos) {
        val dx = pos.x + 0.5 - x
        val dz = pos.z + 0.5 - z
        val dist = kotlin.math.sqrt(dx * dx + dz * dz)

        if (dist > 12.0) {
            targetX = x + dx / dist * 12.0
            targetZ = z + dz / dist * 12.0
            targetY = y + 8.0
        } else {
            targetX = pos.x + 0.5
            targetY = pos.y + 0.5
            targetZ = pos.z + 0.5
        }
        life = 0
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        builder.define(DATA_ITEM_STACK, ItemStack(ModItems.LOCATOR_CORE.get()))
    }

    override fun tick() {
        super.tick()

        var motion = deltaMovement
        val nextX = x + motion.x
        val nextY = y + motion.y
        val nextZ = z + motion.z

        val horizontalDist = motion.horizontalDistance()
        yRot = lerpRotation(yRotO, (Mth.atan2(motion.x, motion.z) * (180.0 / Math.PI)).toFloat())
        xRot = lerpRotation(xRotO, (Mth.atan2(motion.y, horizontalDist) * (180.0 / Math.PI)).toFloat())

        if (!level().isClientSide) {
            val dx = targetX - nextX
            val dz = targetZ - nextZ
            val dist = kotlin.math.sqrt(dx * dx + dz * dz).toFloat()
            val angle = Mth.atan2(dz, dx).toFloat()
            var speed = Mth.lerp(0.0025, horizontalDist, dist.toDouble())
            var vy = motion.y

            if (dist < 1.0f) {
                speed *= 0.8
                vy *= 0.8
            }

            val targetVy = if (y < targetY) 1.0 else -1.0
            motion = Vec3(
                kotlin.math.cos(angle.toDouble()) * speed,
                vy + (targetVy - vy) * 0.015,
                kotlin.math.sin(angle.toDouble()) * speed
            )
            deltaMovement = motion
        }

        if (isInWater) {
            for (i in 0 until 4) {
                level().addParticle(
                    ParticleTypes.BUBBLE,
                    nextX - motion.x * 0.25,
                    nextY - motion.y * 0.25,
                    nextZ - motion.z * 0.25,
                    motion.x, motion.y, motion.z
                )
            }
        } else {
            level().addParticle(
                ParticleTypes.PORTAL,
                nextX - motion.x * 0.25 + random.nextDouble() * 0.6 - 0.3,
                nextY - motion.y * 0.25 - 0.5,
                nextZ - motion.z * 0.25 + random.nextDouble() * 0.6 - 0.3,
                motion.x, motion.y, motion.z
            )
        }

        if (!level().isClientSide) {
            setPos(nextX, nextY, nextZ)
            life++
            if (life > 80) {
                playSound(SoundEvents.ENDER_EYE_DEATH, 1.0f, 1.0f)
                level().levelEvent(2003, blockPosition(), 0)
                discard()
            }
        } else {
            setPosRaw(nextX, nextY, nextZ)
        }
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        if (compound.contains("Item", 10)) {
            setItem(ItemStack.parse(registryAccess(), compound.getCompound("Item")).orElse(ItemStack.EMPTY))
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        val stack = item
        if (!stack.isEmpty) {
            compound.put("Item", stack.save(registryAccess()))
        }
    }

    override fun getLightLevelDependentMagicValue(): Float = 1.0f

    override fun isAttackable(): Boolean = false

    private fun lerpRotation(current: Float, target: Float): Float {
        var cur = current
        while (target - cur < -180.0f) cur -= 360.0f
        while (target - cur >= 180.0f) cur += 360.0f
        return Mth.lerp(0.2f, cur, target)
    }
}
