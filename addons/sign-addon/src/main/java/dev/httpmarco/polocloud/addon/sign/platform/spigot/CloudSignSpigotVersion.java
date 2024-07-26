package dev.httpmarco.polocloud.addon.sign.platform.spigot;

import org.bukkit.Bukkit;

public class CloudSignSpigotVersion {

    public static String KNOWN_MINECRAFT_VERSION;   //for example: 1.8.8-R0.1-SNAPSHOT
    public static String NMS_VERSION;               //for example: v1_8_R3
    public static int BASE_VERSION_INTEGER;         //for example: 8

    public static void detectSpigotVersion(CloudSignSpigotBootstrap plugin) {
        final var bukkitVersion = Bukkit.getServer().getBukkitVersion();
        plugin.getLogger().info("Server version detected: " + bukkitVersion);
        KNOWN_MINECRAFT_VERSION = bukkitVersion;
        NMS_VERSION = convertToServerVersions();
        BASE_VERSION_INTEGER = convertBaseVersionInteger();
    }

    private static String convertToServerVersions() {
        final var packageName = Bukkit.getServer().getClass().getPackage().getName();
        final var splitLength = packageName.split("\\.").length;

        if (splitLength >= 4) {
            return packageName.split("\\.")[3];
        } else if (splitLength == 3) {
            return switch (KNOWN_MINECRAFT_VERSION) {
                case "1.20.5-R0.1-SNAPSHOT", "1.20.6-R0.1-SNAPSHOT" -> "v1_20_R4";
                case "1.21-R0.1-SNAPSHOT" -> "v1_21_R1";
                default -> KNOWN_MINECRAFT_VERSION;
            };
        }

        return KNOWN_MINECRAFT_VERSION;
    }

    private static int convertBaseVersionInteger() {
        if (NMS_VERSION.split("_").length == 3) {
            return Integer.parseInt(NMS_VERSION.split("_")[1]);
        } else {
            return Integer.parseInt(KNOWN_MINECRAFT_VERSION.split("\\.")[1]);
        }
    }

}
