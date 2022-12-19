package io.gingersnapproject;

import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.model.Scope;

public class CustomResourceDefinitions {
    static final CustomResourceDefinitionContext CACHES = new CustomResourceDefinitionContext.Builder()
            .withName("caches.gingersnap-project.io")
            .withGroup("gingersnap-project.io")
            .withVersion("v1alpha1")
            .withPlural("caches")
            .withScope(Scope.NAMESPACED.value())
            .withStatusSubresource(true)
            .build();

    static final CustomResourceDefinitionContext EAGER_CACHE_RULES = new CustomResourceDefinitionContext.Builder()
            .withGroup("gingersnap-project.io")
            .withVersion("v1alpha1")
            .withKind("EagerCacheRule")
            .build();

    static final CustomResourceDefinitionContext LAZY_CACHE_RULES = new CustomResourceDefinitionContext.Builder()
            .withGroup("gingersnap-project.io")
            .withVersion("v1alpha1")
            .withKind("LazyCacheRule")
            .build();
}
