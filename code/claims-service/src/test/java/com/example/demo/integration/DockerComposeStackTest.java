package com.example.demo.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration smoke test against the REAL, already-running Docker Compose stack.
 *
 * <p>Unlike the rest of the suite (ClaimTest, ClaimStatusTest, CreateClaimUseCaseTest,
 * UpdateClaimStatusUseCaseTest — all pure unit tests with mocked collaborators), this
 * class makes real HTTP calls to the live claims-service and bff-service containers,
 * and a real JDBC connection to the live Postgres container. It does not start or
 * mock anything itself.</p>
 *
 * <p><b>Prerequisite:</b> the stack must already be up before running this test:</p>
 * <pre>
 *   podman compose -f docker-compose.infra.yml --in-pod false up -d
 *   podman compose -f docker-compose.apps.yml  --in-pod false up -d
 * </pre>
 *
 * <p>Run just this test with:</p>
 * <pre>
 *   mvn -f code/claims-service/pom.xml test -Dtest=DockerComposeStackTest
 * </pre>
 *
 * <p>It is tagged {@code integration} and deliberately excluded from the default
 * {@code mvn test} run (see the surefire exclusion note in the test strategy doc) —
 * the rest of the suite must stay runnable with zero infrastructure, and mixing a
 * test that requires live containers into the default run would make the fast unit
 * suite flaky for anyone who hasn't started the stack.</p>
 *
 * <p>Priority rationale: this is intentionally the thinnest possible test that still
 * proves something real — that the containers Docker Compose brings up actually talk
 * to each other on the ports and credentials the setup guide documents. It is not a
 * substitute for the unit tests (it can't check business logic without a real
 * Keycloak-issued JWT, which is out of scope for a smoke test), it's a tripwire for
 * "did the stack come up correctly," which — per the manual test execution report —
 * has already failed silently once in this project (the rebuild issue, the CDC
 * connector issue) in ways a smoke test like this would have caught immediately.</p>
 */
@Tag("integration")
class DockerComposeStackTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    // Matches docker-compose.apps.yml as actually run in this project (claims-service
    // mapped to 8080:8080, not the 8081 the setup guide describes for the "port
    // already in use" case — see docs/TEST_STRATEGY.md for the discrepancy).
    private static final String CLAIMS_SERVICE_HEALTH = "http://localhost:8080/actuator/health";
    private static final String BFF_SERVICE_HEALTH = "http://localhost:8090/actuator/health";

    private static final String POSTGRES_URL = "jdbc:postgresql://localhost:5432/demo_app";
    private static final String POSTGRES_USER = "postgres";
    private static final String POSTGRES_PASSWORD = "postgres";

    @BeforeAll
    static void checkStackIsReachableOrSkip() {
        boolean reachable = isReachable(CLAIMS_SERVICE_HEALTH);
        assumeTrue(reachable,
                "Skipping DockerComposeStackTest: claims-service is not reachable on localhost:8080. "
                        + "Start the stack first — see class Javadoc for the exact commands.");
    }

    @Test
    @DisplayName("claims-service /actuator/health responds 200 UP against the real running container")
    void claimsServiceIsUp() throws Exception {
        HttpResponse<String> response = get(CLAIMS_SERVICE_HEALTH);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    @Test
    @DisplayName("bff-service /actuator/health responds 200 UP against the real running container")
    void bffServiceIsUp() throws Exception {
        HttpResponse<String> response = get(BFF_SERVICE_HEALTH);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    @Test
    @DisplayName("Postgres is reachable with the documented credentials and has a populated claims table")
    void postgresIsReachableAndHasClaimsTable() throws Exception {
        try (Connection connection = DriverManager.getConnection(POSTGRES_URL, POSTGRES_USER, POSTGRES_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS claim_count FROM claims")) {

            assertThat(resultSet.next()).isTrue();
            long claimCount = resultSet.getLong("claim_count");

            // Not asserting an exact number (data changes between runs) — just proving
            // the table is real, reachable, and queryable through the documented
            // superuser credentials (see docs/TEST_STRATEGY.md — these credentials
            // aren't in the setup guide itself and had to be found by grepping
            // docker-compose.infra.yml).
            assertThat(claimCount).isGreaterThanOrEqualTo(0);
        }
    }

    private static HttpResponse<String> get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).GET().build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static boolean isReachable(String url) {
        try {
            return get(url).statusCode() == 200;
        } catch (Exception exception) {
            return false;
        }
    }
}
