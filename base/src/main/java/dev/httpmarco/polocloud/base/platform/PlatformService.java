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

import dev.httpmarco.osgan.files.OsganGsonContext;
import dev.httpmarco.polocloud.base.common.GsonDownloaderCommon;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
public final class PlatformService {

    private static final String VERSIONS_URL = "https://raw.githubusercontent.com/HttpMarco/polocloud/master/cloud_verisons.json";
    private final Set<Platform> platforms;

    public PlatformService() {
        var content = GsonDownloaderCommon.downloadStringContext(VERSIONS_URL);
        this.platforms = Arrays.stream(OsganGsonContext.GSON.fromJson(content.getAsJsonArray("platform"), Platform[].class)).collect(Collectors.toSet());
    }

    public boolean validPlatform(String platform) {
        return platforms.stream().anyMatch(it -> it.id().equals(platform));
    }

    public Platform find(String platform) {
        return platforms.stream().filter(it -> it.id().equals(platform)).findFirst().orElse(null);
    }

}
