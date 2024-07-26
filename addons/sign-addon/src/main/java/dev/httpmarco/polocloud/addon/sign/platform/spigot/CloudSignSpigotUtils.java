package dev.httpmarco.polocloud.addon.sign.platform.spigot;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class CloudSignSpigotUtils {

    private final Logger logger;

    public CloudSignSpigotUtils(CloudSignSpigotBootstrap plugin) {
        this.logger = plugin.getLogger();
    }

    public boolean notASign(Block possibleSignBlock) {
        if (CloudSignSpigotVersion.BASE_VERSION_INTEGER >= 19) {
            return !possibleSignBlock.getType().data.equals(WallSign.class);
        } else {
            try {
                return possibleSignBlock.getType() != Material.valueOf("SIGN_POST");
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    public Block getTargetBlock(Player player) {
        if (CloudSignSpigotVersion.BASE_VERSION_INTEGER >= 14) {
            return player.getTargetBlockExact(8);
        }

        try {
            final var getTargetBlock = player.getClass().getDeclaredMethod("getTargetBlock", int.class);
            getTargetBlock.setAccessible(true);
            return (Block) getTargetBlock.invoke(player, 8);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            this.logger.warning("Failed to get target block: " + e.getMessage());
        }
        return null;
    }

}
