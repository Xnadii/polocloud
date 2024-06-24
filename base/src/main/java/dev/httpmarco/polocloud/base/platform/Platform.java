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

package dev.httpmarco.polocloud.base.platform;

import dev.httpmarco.polocloud.api.platform.VersionConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public final class Platform {

    private String id;
    private final boolean proxy;
    private final List<Version> versions;
    private final PlatformConfigManipulation[] configManipulation;
    private final String[] startingArguments;

    public Platform() {
        // default gson value if proxy property not exists
        this.proxy = false;
        this.versions = new ArrayList<>();
        this.startingArguments = new String[0];
        this.configManipulation = new PlatformConfigManipulation[0];
    }

    @Contract("_ -> new")
    public @NotNull VersionConstruct toConstruct(String version) {
        return new VersionConstruct(id, version, proxy);
    }
}
