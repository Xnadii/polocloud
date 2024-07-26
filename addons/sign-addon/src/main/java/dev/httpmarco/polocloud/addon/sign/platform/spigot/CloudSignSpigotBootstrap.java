/*
 * Copyright 2024 Mirco Lindenau | HttpMarco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.httpmarco.polocloud.addon.sign.platform.spigot;

import dev.httpmarco.polocloud.addon.sign.CloudSignService;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class CloudSignSpigotBootstrap extends JavaPlugin implements Listener {

    private CloudSignService signService;
    private CloudSignSpigotUtils cloudSignSpigotUtils;

    @Override
    public void onEnable() {
        this.signService = new CloudSignService(new CloudSignSpigotFactory());
        this.cloudSignSpigotUtils = new CloudSignSpigotUtils(this);
        CloudSignSpigotVersion.detectSpigotVersion(this);

        getCommand("cloudsign").setExecutor(new CloudSignSpigotCommand(this.cloudSignSpigotUtils));

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskTimer(this, this.signService::tick, 1, 0);
    }

    @EventHandler
    public void handle(@NotNull PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();

        if (block != null && !this.cloudSignSpigotUtils.notASign(block)) {
            this.signService.connectPlayer(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), event.getPlayer().getUniqueId());
        }
    }

}
