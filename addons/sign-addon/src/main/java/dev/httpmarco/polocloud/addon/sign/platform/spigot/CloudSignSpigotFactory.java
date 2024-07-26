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

import dev.httpmarco.polocloud.addon.sign.CloudSign;
import dev.httpmarco.polocloud.addon.sign.CloudSignFactory;
import dev.httpmarco.polocloud.addon.sign.CloudSignService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.jetbrains.annotations.NotNull;

public final class CloudSignSpigotFactory extends CloudSignFactory {

    @Override
    public void pre(@NotNull CloudSign cloudSign) {
        //todo duplicated
        var pos = new Location(Bukkit.getWorld(cloudSign.world()), cloudSign.x(), cloudSign.y(), cloudSign.z());

        var block = pos.getBlock();
        var sign = (Sign) block.getState();

        if (CloudSignSpigotVersion.BASE_VERSION_INTEGER >= 20) {
            //block editing of sign
            sign.setWaxed(true);
            sign.update(true, false);
        } else {
            sign.update();
        }
    }

    @Override
    public void print(@NotNull CloudSign cloudSign) {
        var pos = new Location(Bukkit.getWorld(cloudSign.world()), cloudSign.x(), cloudSign.y(), cloudSign.z());

        var block = pos.getBlock();
        var sign = (Sign) block.getState();

        var layout = CloudSignService.instance().signAnimationRunner().currentTickedLayout().get(cloudSign.state());
        for (int i = 0; i < 4; i++) {

            var line = layout.lines()[i];
            if (cloudSign.cloudService() != null) {
                line = line.replaceAll("%server%", cloudSign.cloudService().name())
                    .replaceAll("%players%", String.valueOf(cloudSign.cloudService().onlinePlayersCount()));
            }

            if (CloudSignSpigotVersion.BASE_VERSION_INTEGER >= 20) {
                sign.getSide(Side.FRONT).line(i, Component.text(line));
            } else {
                sign.setLine(i, line);
            }
        }

        if (CloudSignSpigotVersion.BASE_VERSION_INTEGER >= 20) {
            sign.update(true, false);
        } else {
            sign.update();
        }
    }

    @Override
    public void clear() {

    }
}
