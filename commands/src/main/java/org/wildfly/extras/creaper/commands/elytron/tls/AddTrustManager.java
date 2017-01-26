package org.wildfly.extras.creaper.commands.elytron.tls;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddTrustManager implements OnlineCommand, OfflineCommand {

    private final String name;
    private final String algorithm;
    private final String keyStore;
    private final String provider;
    private final String providerLoader;
    private final boolean replaceExisting;

    private AddTrustManager(Builder builder) {
        this.name = builder.name;
        this.algorithm = builder.algorithm;
        this.keyStore = builder.keyStore;
        this.provider = builder.provider;
        this.providerLoader = builder.providerLoader;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address trustManagerAddress = Address.subsystem("elytron").and("trust-managers", name);
        if (replaceExisting) {
            ops.removeIfExists(trustManagerAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(trustManagerAddress, Values.empty()
            .and("name", name)
            .and("algorithm", algorithm)
            .and("key-store", keyStore)
            .andOptional("provider", provider)
            .andOptional("provider-loader", providerLoader));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddTrustManager.class)
                .subtree("elytronSubsystem", Subtree.subsystem("elytron"))
                .parameter("atrName", name)
                .parameter("atrAlgorithm", algorithm)
                .parameter("atrKeyStore", keyStore)
                .parameter("atrProvider", provider)
                .parameter("atrProviderLoader", providerLoader)
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    public static final class Builder {

        private final String name;
        private String algorithm;
        private String keyStore;
        private String provider;
        private String providerLoader;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name of the trust-manager must be specified as non empty value");
            }
            this.name = name;
        }

        public Builder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Builder keyStore(String keyStore) {
            this.keyStore = keyStore;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder providerLoader(String providerLoader) {
            this.providerLoader = providerLoader;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddTrustManager build() {
            if (algorithm == null || algorithm.isEmpty()) {
                throw new IllegalArgumentException("Algorithm of the trust-manager must be specified as non empty value");
            }
            return new AddTrustManager(this);
        }
    }
}