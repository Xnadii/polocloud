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

package dev.httpmarco.polocloud.api.packets.service;

import dev.httpmarco.osgan.networking.Packet;
import dev.httpmarco.osgan.networking.codec.CodecBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Accessors(fluent = true)
public class CloudServiceRegisterPacket extends Packet {

    private UUID uuid;

    public CloudServiceRegisterPacket(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void onRead(CodecBuffer codecBuffer) {
        this.uuid = codecBuffer.readUniqueId();
    }

    @Override
    public void onWrite(CodecBuffer codecBuffer) {
        codecBuffer.writeUniqueId(this.uuid);
    }
}
