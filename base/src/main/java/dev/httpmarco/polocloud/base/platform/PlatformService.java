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
import dev.httpmarco.osgan.utils.types.MessageUtils;
import dev.httpmarco.polocloud.api.common.YamlValidateWriter;
import dev.httpmarco.polocloud.base.common.GsonDownloaderCommon;
import dev.httpmarco.polocloud.base.services.LocalCloudService;
import dev.httpmarco.polocloud.runner.RunnerBootstrap;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
public final class PlatformService {

    public static final String PROXY_SECRET = MessageUtils.randomString(8);

    public static final Path PLATFORM_DIR = Path.of("local/platforms");
    private static final String VERSIONS_URL = "https://raw.githubusercontent.com/HttpMarco/polocloud/master/cloud_verisons.json";
    private final Set<Platform> platforms;

    @SneakyThrows
    public PlatformService() {
        if (!Files.exists(PLATFORM_DIR)) {
            Files.createDirectory(PLATFORM_DIR);
        }

        var content = GsonDownloaderCommon.downloadStringContext(VERSIONS_URL);
        this.platforms = Arrays.stream(OsganGsonContext.GSON.fromJson(content.getAsJsonArray("platform"), Platform[].class)).collect(Collectors.toSet());
    }

    public boolean validPlatform(String platform) {
        return platforms.stream().anyMatch(it -> it.id().equals(platform));
    }

    public Platform find(String platform) {
        return platforms.stream().filter(it -> it.id().equals(platform)).findFirst().orElse(null);
    }

    @SneakyThrows
    public void preparePlatform(@NotNull LocalCloudService service) {
        var construct = service.group().version();
        var platform = find(construct.platform());
        // check if platform folder is existing
        var versionDir = PlatformService.PLATFORM_DIR.resolve(construct.platform()).resolve(construct.version());
        if (!Files.exists(versionDir)) {
            versionDir.toFile().mkdirs();
        }

        // check if current jar is present
        var platformVersionJar = versionDir.resolve(construct + ".jar");

        if (!Files.exists(platformVersionJar)) {
            var versionObject = platform.versions().stream().filter(it -> it.version().equalsIgnoreCase(construct.version())).findFirst().orElse(null);

            if (versionObject == null) {
                return;
            }
            var url = URI.create(versionObject.downloadUrl()).toURL();
            Files.copy(url.openConnection().getInputStream(), platformVersionJar);
        }

        // copy to service
        Files.copy(platformVersionJar, service.runningFolder().resolve(construct + ".jar"));

        // start config modifications
        for (var manipulation : platform.configManipulation()) {
            var file = new File(manipulation.file());
            var serviceFile = service.runningFolder().resolve(file.toPath());
            // copy file from resources

            for (var action : manipulation.actions()) {

                var content = action.content()
                        .replaceAll("%proxy_secret%", PROXY_SECRET)
                        .replaceAll("%server_name%", service.name())
                        .replaceAll("%hostname%", service.hostname())
                        .replaceAll("%port%", String.valueOf(service.port()));


                if (action.type() == PlatformConfigManipulationAction.Type.OVERWRITE) {
                    var tomlFile = serviceFile.toFile().getName().endsWith(".toml");

                    if (!Files.exists(serviceFile)) {
                        Files.copy(Objects.requireNonNull(RunnerBootstrap.LOADER.getResourceAsStream(file.getName())), serviceFile);
                    }

                    if (action.keyword() == null) {
                        continue;
                    }

                    var propertySeparator = (tomlFile ||  serviceFile.toFile().getName().endsWith(".properties")) ? " = " : ": ";

                    YamlValidateWriter.validateYaml(serviceFile.toFile(), s -> {
                        if (s.startsWith(action.keyword()) || s.trim().startsWith(action.keyword())) {
                            return action.keyword() + propertySeparator + (tomlFile ? "\"" + content + "\"" : content);
                        }
                        return s;
                    });
                }
                if (action.type() == PlatformConfigManipulationAction.Type.GENERATE_AND_FILL_FILE) {
                    Files.writeString(serviceFile, content);
                }
            }
        }
    }
}