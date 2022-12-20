package io.gingersnapproject;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static io.gingersnapproject.CustomResourceDefinitions.CACHES;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(KubernetesClientResolver.class)
public class ExampleTest {

    static final String DB_NAMESPACE = "database";
    static final String TEST_NAMESPACE = "test";

    @BeforeAll
    public static void deployDB(KubernetesClient k8sClient) {
        waitForNamespaceDeletion(k8sClient, DB_NAMESPACE);
        createNamespace(k8sClient, DB_NAMESPACE);

        var resources = k8sClient
                .load(ExampleTest.class.getResourceAsStream("/kubernetes/database/mysql.yaml"))
                .inNamespace(DB_NAMESPACE)
                .create();

        k8sClient.resourceList(resources).waitUntilReady(4, TimeUnit.MINUTES);
    }

    @AfterAll
    public static void teardownDB(KubernetesClient k8sClient) {
        k8sClient.namespaces().withName(DB_NAMESPACE).delete();
    }

    @BeforeEach
    public void init(KubernetesClient k8sClient) {
        waitForNamespaceDeletion(k8sClient, TEST_NAMESPACE);
        createNamespace(k8sClient, TEST_NAMESPACE);
    }

    @AfterEach
    public void cleanup(KubernetesClient k8sClient) {
        k8sClient.namespaces().withName(TEST_NAMESPACE).delete();
    }

    @Test
    public void helloTests(KubernetesClient k8sClient) {
        System.out.println("Hello World");

        k8sClient.resource(
                new SecretBuilder()
                        .withNewMetadata()
                        .withName("db-credential-secret")
                        .endMetadata()
                        .withStringData(
                                Map.of(
                                        "type", "mysql",
                                        "host", "mysql.database.svc.cluster.local",
                                        "port", "3306",
                                        "username", "gingersnap_user",
                                        "password", "password"
                                )
                        )
                        .build()
        )
                .inNamespace(TEST_NAMESPACE)
                .create();

        var cache = createCache(k8sClient, "cache.yaml");
        var meta = cache.getMetadata();
        assertFalse(meta.getUid().isEmpty());

        // TODO replace with status condition check
        k8sClient.apps()
                .daemonSets()
                .inNamespace(TEST_NAMESPACE)
                .withName(meta.getName())
                .waitUntilCondition(ds -> ds != null && ds.getStatus().getNumberReady() > 0, 1, TimeUnit.MINUTES);
    }

    private static void createNamespace(KubernetesClient k8sClient, String namespace) {
        k8sClient.namespaces()
                .resource(
                        new NamespaceBuilder()
                                .withNewMetadata()
                                .withName(namespace)
                                .endMetadata()
                                .build()
                ).create();
    }

    private static GenericKubernetesResource createCache(KubernetesClient k8sClient, String resource) {
        return k8sClient.genericKubernetesResources(CACHES)
                .inNamespace(TEST_NAMESPACE)
                .load(ExampleTest.class.getResourceAsStream("/kubernetes/gingersnap/" + resource))
                .create();
    }

    private static void waitForNamespaceDeletion(KubernetesClient k8sClient, String namespace) {
        k8sClient.namespaces().withName(namespace).waitUntilCondition(Objects::isNull, 4, TimeUnit.MINUTES);
    }
}
