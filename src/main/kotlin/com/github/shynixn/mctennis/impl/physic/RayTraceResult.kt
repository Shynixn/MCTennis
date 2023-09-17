package com.github.shynixn.mctennis.impl.physic
import com.github.shynixn.mcutils.common.Vector3d
import org.bukkit.block.Block

class RayTraceResult(var hitBlock: Boolean, var targetPosition: Vector3d,
                     var blockDirection: com.github.shynixn.mctennis.impl.physic.BlockDirection, var block: Block? = null)
