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

package dev.httpmarco.polocloud.base.groups;

import dev.httpmarco.osgan.networking.packet.PacketBuffer;
import dev.httpmarco.polocloud.api.CloudAPI;
import dev.httpmarco.polocloud.api.groups.CloudGroup;
import dev.httpmarco.polocloud.api.groups.CloudGroupProvider;
import dev.httpmarco.polocloud.api.packets.general.OperationNumberPacket;
import dev.httpmarco.polocloud.api.packets.general.OperationStatePacket;
import dev.httpmarco.polocloud.api.packets.groups.*;
import dev.httpmarco.polocloud.api.platform.VersionConstruct;
import dev.httpmarco.polocloud.api.services.CloudService;
import dev.httpmarco.polocloud.base.CloudBase;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@Accessors(fluent = true)
public final class CloudGroupProviderImpl extends CloudGroupProvider {

    private final List<CloudGroup> groups;
    private final CloudGroupServiceTypeAdapter groupServiceTypeAdapter = new CloudGroupServiceTypeAdapter();

    public CloudGroupProviderImpl() {

        // register group packet responders
        var transmitter = CloudBase.instance().transmitter();
        transmitter.responder("groups-all", (properties) -> new CloudGroupCollectionPacket(groups()));
        transmitter.responder("group-find", (properties) -> new CloudGroupPacket(group(properties.getString("name"))));
        transmitter.responder("group-exist", (properties) -> new CloudGroupExistResponsePacket(isGroup(properties.getString("name"))));
        transmitter.responder("group-service-online", (properties) -> new OperationNumberPacket(group(properties.getString("name")).onlineAmount()));

        transmitter.responder("group-delete", (properties) -> new OperationStatePacket(deleteGroup(properties.getString("name"))));
        transmitter.responder("group-create", (properties) -> new OperationStatePacket(createGroup(properties.getString("name"),
                properties.getString("platform"),
                properties.getString("version"),
                properties.getInteger("memory"),
                properties.getInteger("minOnlineCount"))));

        transmitter.listen(CloudGroupCreatePacket.class, (channelTransmit, packet) -> this.createGroup(packet.name(), packet.platform(), packet.version(), packet.memory(), packet.minOnlineCount()));
        transmitter.listen(CloudGroupDeletePacket.class, (channelTransmit, packet) -> this.deleteGroup(packet.name()));
        transmitter.listen(CloudGroupUpdatePacket.class, (channelTransmit, packet) -> this.update(packet.group()));

        // load default groups
        this.groups = groupServiceTypeAdapter.readGroups();
        CloudBase.instance().logger().info("Loading following groups&2: &3" + String.join("&2, &3", groups.stream().map(CloudGroup::name).toList()));
    }

    @Override
    public boolean createGroup(String name, String platform, String version, int memory, int minOnlineCount) {
        //todo remove outpoint
        if (isGroup(name)) {
            CloudAPI.instance().logger().info("The group already exists!");
            return false;
        }

        if (memory <= 0) {
            CloudAPI.instance().logger().info("The minimum memory value must be higher then 0. ");
            return false;
        }

        var platformService = CloudBase.instance().platformService();

        if (!platformService.validPlatform(platform)) {
            CloudAPI.instance().logger().info("The platform " + platform + " is an invalid type!");
            return false;
        }

        var versionConstruct = platformService.find(platform).toConstruct(version);

        var group = new CloudGroupImpl(name, versionConstruct, memory, minOnlineCount);
        this.groupServiceTypeAdapter.includeFile(group);
        this.groups.add(group);

        return true;
    }

    @Override
    public boolean deleteGroup(String name) {
        if (!isGroup(name)) {
            return false;
        }

        var group = group(name);
        CloudAPI.instance().serviceProvider().services(group).forEach(CloudService::shutdown);
        this.groupServiceTypeAdapter.excludeFile(group);
        this.groups.remove(group);
        return true;
    }

    @Override
    public boolean isGroup(String name) {
        return groups.stream().anyMatch(it -> it.name().equalsIgnoreCase(name));
    }

    @Override
    public CompletableFuture<Boolean> isGroupAsync(String name) {
        return CompletableFuture.completedFuture(isGroup(name));
    }

    @Override
    public CloudGroup group(String name) {
        return this.groups.stream().filter(it -> it.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public CompletableFuture<CloudGroup> groupAsync(String name) {
        return CompletableFuture.completedFuture(group(name));
    }

    @Override
    public CompletableFuture<List<CloudGroup>> groupsAsync() {
        return CompletableFuture.completedFuture(groups);
    }

    public void update(CloudGroup cloudGroup) {
        this.groupServiceTypeAdapter.updateFile(cloudGroup);
    }

    public CloudGroup fromPacket(PacketBuffer buffer) {
        var name = buffer.readString();

        var platform = buffer.readString();
        var version = buffer.readString();
        var proxy = buffer.readBoolean();

        var platformProxy = buffer.readBoolean();
        var minOnlineServices = buffer.readInt();
        var memory = buffer.readInt();

        return new CloudGroupImpl(name, new VersionConstruct(platform, version, proxy), minOnlineServices, memory);
    }
}
