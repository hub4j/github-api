package org.kohsuke.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class GHAppTest {

    @Rule
    public WireMockRule githubApi = new WireMockRule(WireMockConfiguration.options()
            .dynamicPort().usingFilesUnderClasspath("api"));
    public GitHub github;

    @Before
    public void prepareMockGitHub() throws Exception {
        githubApi.stubFor(get(urlMatching(".*")).atPriority(10));
        github = new GitHubBuilder().withJwtToken("bogus").withEndpoint("http://localhost:" + githubApi.port()).build();
    }

    @Test
    public void getGitHubApp() throws IOException {
        JsonNode rootNode = readPayload("/body-mapping-githubapp-app.json");
        JsonNode ownerNode = rootNode.get("owner");

        GHApp app = github.getApp();
        assertThat(app.id, is(rootNode.get("id").asLong()));
        assertThat(app.getOwner().id, is(ownerNode.get("id").asLong()));
        assertThat(app.getOwner().login, is(ownerNode.get("login").asText()));
        assertThat(app.getName(), is(rootNode.get("name").asText()));
        assertThat(app.getDescription(), is(rootNode.get("description").asText()));
        assertThat(app.getExternalUrl(), is(rootNode.get("external_url").asText()));
        assertThat(app.getHtmlUrl().toString(), is(rootNode.get("html_url").asText()));
        assertThat(app.getCreatedAt(), is(GitHub.parseDate(rootNode.get("created_at").asText())));
        assertThat(app.getUpdatedAt(), is(GitHub.parseDate(rootNode.get("updated_at").asText())));
        assertThat(app.getPermissions().size(), is(rootNode.get("permissions").size()));
        assertThat(app.getEvents().size(), is(rootNode.get("events").size()));
        assertThat(app.getInstallationsCount(), is(rootNode.get("installations_count").asLong()));
    }

    @Test
    public void listInstallations() throws IOException {
        JsonNode rootNode = readPayload("/body-mapping-githubapp-installations.json");

        GHApp app = github.getApp();
        List<GHAppInstallation> installations = app.listInstallations().asList();
        assertThat(installations.size(), is(rootNode.size()));

        GHAppInstallation appInstallation = installations.get(0);
        JsonNode installationNode = rootNode.get(0);
        testAppInstallation(installationNode, appInstallation);
    }

    @Test
    public void getInstallationById() throws IOException {
        JsonNode rootNode = readPayload("/body-mapping-githubapp-installation-by-id.json");

        GHApp app = github.getApp();
        GHAppInstallation installation = app.getInstallationById(1111111);
        testAppInstallation(rootNode, installation);
    }

    @Test
    public void getInstallationByOrganization() throws IOException {
        JsonNode rootNode = readPayload("/body-mapping-githubapp-installation-by-organization.json");

        GHApp app = github.getApp();
        GHAppInstallation installation = app.getInstallationByOrganization("bogus");
        testAppInstallation(rootNode, installation);
    }

    @Test
    public void getInstallationByRepository() throws IOException {
        JsonNode rootNode = readPayload("/body-mapping-githubapp-installation-by-organization.json");

        GHApp app = github.getApp();
        GHAppInstallation installation = app.getInstallationByRepository("bogus", "bogus");
        testAppInstallation(rootNode, installation);
    }

    @Test
    public void getInstallationByUser() throws IOException {
        JsonNode rootNode = readPayload("/body-mapping-githubapp-installation-by-user.json");

        GHApp app = github.getApp();
        GHAppInstallation installation = app.getInstallationByUser("bogus");
        testAppInstallation(rootNode, installation);
    }

    @Test
    public void deleteInstallation() throws IOException {
        GHApp app = github.getApp();
        GHAppInstallation installation = app.getInstallationByUser("bogus");
        try {
            installation.deleteInstallation();
        } catch (IOException e) {
            fail("deleteInstallation wasn't suppose to fail in this test");
        }
    }

    @Test
    public void createToken() throws IOException {
        JsonNode rootNode = readPayload("/body-githubapp-create-installation-accesstokens.json");

        GHApp app = github.getApp();
        GHAppInstallation installation = app.getInstallationByUser("bogus");

        Map<String, GHPermissionType> permissions = new HashMap<String, GHPermissionType>();
        permissions.put("checks", GHPermissionType.WRITE);
        permissions.put("pull_requests", GHPermissionType.WRITE);
        permissions.put("contents", GHPermissionType.READ);
        permissions.put("metadata", GHPermissionType.READ);

        GHAppInstallationToken installationToken = installation.createToken(permissions)
                .repositoryIds(Arrays.asList(111111111))
                .create();

        assertThat(installationToken.getToken(), is(rootNode.get("token").asText()));
        assertThat(installation.getPermissions(), is(convertToMap(rootNode.get("permissions"), GHPermissionType.class)));
        assertThat(installationToken.getRepositorySelection(),
                is(convertToEnum(rootNode.get("repository_selection").asText(), GHRepositorySelection.class)));
        assertThat(installationToken.getExpiresAt(), is(GitHub.parseDate(rootNode.get("expires_at").asText())));

        ArrayNode repositoriesNode = (ArrayNode) rootNode.get("repositories");
        JsonNode repositoryNode = repositoriesNode.get(0);
        GHRepository repository = installationToken.getRepositories().get(0);
        assertThat(installationToken.getRepositories().size(), is(repositoriesNode.size()));
        assertThat(repository.getId(), is(repositoryNode.get("id").asLong()));
        assertThat(repository.getName(), is(repositoryNode.get("name").asText()));
    }

    private void testAppInstallation(JsonNode installationNode, GHAppInstallation appInstallation) throws IOException {
        Map<String, GHPermissionType> appPermissions = appInstallation.getPermissions();
        GHUser appAccount = appInstallation.getAccount();
        JsonNode accountNode = installationNode.get("account");
        JsonNode permissionsNode = installationNode.get("permissions");

        assertThat(appInstallation.id, is(installationNode.get("id").asLong()));
        assertThat(appAccount.id, is(accountNode.get("id").asLong()));
        assertThat(appAccount.login, is(accountNode.get("login").asText()));
        assertThat(appInstallation.getRepositorySelection(),
                is(convertToEnum(installationNode.get("repository_selection").asText(), GHRepositorySelection.class)));
        assertThat(appInstallation.getAccessTokenUrl(), is(installationNode.get("access_tokens_url").asText()));
        assertThat(appInstallation.getRepositoriesUrl(), is(installationNode.get("repositories_url").asText()));
        assertThat(appInstallation.getAppId(), is(installationNode.get("app_id").asLong()));
        assertThat(appInstallation.getTargetId(), is(installationNode.get("target_id").asLong()));
        assertThat(appInstallation.getTargetType(),
                is(convertToEnum(installationNode.get("target_type").asText(), GHTargetType.class)));
        assertThat(appPermissions, is(convertToMap(permissionsNode, GHPermissionType.class)));

        List<GHEvent> events = convertToEnumList((ArrayNode) installationNode.get("events"), GHEvent.class);
        assertThat(appInstallation.getEvents(), containsInAnyOrder(events.toArray(new GHEvent[0])));
        assertThat(appInstallation.getCreatedAt(), is(GitHub.parseDate(installationNode.get("created_at").asText())));
        assertThat(appInstallation.getUpdatedAt(), is(GitHub.parseDate(installationNode.get("updated_at").asText())));
        assertNull(appInstallation.getSingleFileName());
    }

    private JsonNode readPayload(String relativeFilePath) throws IOException {
        String payload = "/api/__files/".concat(relativeFilePath);
        return new ObjectMapper().readTree(this.getClass().getResourceAsStream(payload));
    }

    private <T extends Enum<T>> List<T> convertToEnumList(ArrayNode jsonValues, Class<T> valueType) {
        List<T> retList = new ArrayList<T>(jsonValues.size());
        for (int i = 0; i < jsonValues.size(); i++) {
            retList.add(convertToEnum(jsonValues.get(i).asText(), valueType));
        }
        return retList;
    }

    private <V extends Enum<V>> Map<String, V> convertToMap(JsonNode jsonNode, Class<V> valueType) {
        Map<String, V> retMap = new HashMap<String, V>(jsonNode.size());
        Iterator<String> iterator = jsonNode.fieldNames();
        while (iterator.hasNext()) {
            String current = iterator.next();
            retMap.put(current, convertToEnum(jsonNode.get(current).asText(), valueType));
        }
        return retMap;
    }

    private <T extends Enum<T>> T convertToEnum(String text, Class<T> valueType) {
        // by convention Java constant names are upper cases, but github uses
        // lower-case constants. GitHub also uses '-', which in Java we always
        // replace by '_'
        String value = text.toUpperCase(Locale.ENGLISH).replace('-', '_');
        // special treatment this sdk has provided certain enums with
        if (value.equals("*")) value = "ALL";
        return Enum.valueOf(valueType, value);
    }

}
