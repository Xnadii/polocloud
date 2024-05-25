package dev.httpmarco.polocloud.base.groups.platforms;

import com.google.gson.JsonObject;
import dev.httpmarco.osgan.files.json.JsonUtils;
import dev.httpmarco.polocloud.base.services.LocalCloudService;
import dev.httpmarco.polocloud.runner.RunnerBootstrap;
import lombok.SneakyThrows;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public final class PaperMCPlatform extends Platform {

    private static final String VERSION_URL = "https://api.papermc.io/v2/projects/%s";
    private static final String BUILD_URL = "https://api.papermc.io/v2/projects/%s/versions/%s/builds";
    private static final String DOWNLOAD_URL = "https://api.papermc.io/v2/projects/%s/versions/%s/builds/%d/downloads/%s-%s.jar";

    private final String product;

    public PaperMCPlatform(String product) {
        this.product = product;
        for (var version : readPaperInformation(VERSION_URL.formatted(product)).get("versions").getAsJsonArray()) {
            possibleVersions().add(product + "-" + version.getAsString());
        }
    }


    @SneakyThrows
    private JsonObject readPaperInformation(String link) {
        return JsonUtils.getGson().fromJson(downloadStringContext(link), JsonObject.class);
    }

    @Override
    public String[] platformsArguments() {
        return new String[]{"nogui", "noconsole"};
    }

    @Override
    public String[] platformsEnvironment() {
        return new String[]{};
    }

    @Override
    @SneakyThrows
    public void download(String version) {

        var orgVersion = version.replace(product + "-", "");

        // search for the current build version
        var builds = JsonUtils.getGson().fromJson(downloadStringContext(BUILD_URL.formatted(product, orgVersion)), JsonObject.class).get("builds").getAsJsonArray().asList();
        var buildIndex = builds.get(builds.size() - 1).getAsJsonObject().get("build").getAsInt();

        final var url = URI.create(DOWNLOAD_URL.formatted(product, orgVersion, buildIndex, version, buildIndex)).toURL();
        Files.copy(url.openConnection().getInputStream(), Path.of("local/platforms/" + version + ".jar"));
    }

    @Override
    @SneakyThrows
    public void prepare(LocalCloudService localCloudService) {
        // accept eula without cringe logs
        Files.writeString(localCloudService.runningFolder().resolve("eula.txt"), "eula=true");

        // manipulate only paper under services
        if (product.equalsIgnoreCase("paper")) {
            var serverProperties = localCloudService.runningFolder().resolve("server.properties").toFile();

            if (!Files.exists(serverProperties.toPath())) {
                // copy file from storage
                Files.copy(Objects.requireNonNull(RunnerBootstrap.LOADER.getResourceAsStream("server.properties")), localCloudService.runningFolder().resolve("server.properties"));

            }

            var properties = new Properties();

            try (var fileReader = new FileReader(serverProperties)) {
                properties.load(fileReader);
            }

            properties.setProperty("server-name", localCloudService.name());
            properties.setProperty("server-port", String.valueOf(localCloudService.port()));

            try (var fileWriter = new FileWriter(serverProperties)) {
                properties.store(fileWriter, null);
            }
        }
    }

    @SneakyThrows
    //todo osgan
    private String downloadStringContext(String link) {
        var url = new URI(link).toURL();
        var stream = url.openStream();
        var context = new String(stream.readAllBytes());
        stream.close();
        return context;
    }
}
