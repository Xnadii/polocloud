/*
    CloudAPI.instance().groupProvider().groups().stream()
            .filter(it -> it.platform().proxy())
            .map(it -> it.platform().version())
            .distinct()
            .forEach(platformVersion -> {
                try {
                    var platform = CloudBase.instance().groupProvider().platformService().find(platformVersion);
                    if (platform instanceof VelocityPlatform) {
                        // manipulate velocity secret if
                        var configPath = localCloudService.runningFolder().resolve("config");
                        var globalPaperProperty = configPath.resolve("paper-global.yml");

                        if (!Files.exists(globalPaperProperty)) {
                            globalPaperProperty.toFile().getParentFile().mkdirs();
                            Files.createFile(globalPaperProperty);
                            Files.writeString(globalPaperProperty, String.join("\n", List.of("proxies:", " velocity:", "    enabled: true", "    secret: " + CloudGroupPlatformService.PROXY_SECRET)));
                        } else {
                            YamlValidateWriter.validateYaml(globalPaperProperty.toFile(), s -> {
                                if (s.startsWith("    enabled: false")) {
                                    return "    enabled: true";
                                }
                                if (s.replaceAll(" ", "").startsWith("secret:")) {
                                    return "    secret: " + CloudGroupPlatformService.PROXY_SECRET;
                                }
                                return s;
                            });
                        }
                        return;
                    }

                    if (platform instanceof BungeeCordPlatform) {
                        var globalPaperProperty = localCloudService.runningFolder().resolve("spigot.yml");

                        YamlValidateWriter.validateYaml(globalPaperProperty.toFile(), s -> {
                            if (s.replaceAll(" ", "").startsWith("bungeecord:")) {
                                return "  bungeecord: true";
                            } else {
                                return s;
                            }
                        });
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            });
 */