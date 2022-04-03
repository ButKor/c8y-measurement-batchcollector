package config;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.classpath.ClasspathConfigurationSource;
import org.cfg4j.source.context.environment.DefaultEnvironment;
import org.cfg4j.source.context.environment.Environment;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.reload.strategy.ImmediateReloadStrategy;

import java.nio.file.Paths;
import java.util.Collections;

public class Configuration {

    private static ConfigurationProvider provider;
    private static String cfgFileName = "application.yaml";

    public static ConfigurationProvider configurationProvider() {
        if (provider != null){
            return provider;
        }

        ConfigFilesProvider configFilesProvider = () -> Collections.singletonList(Paths.get(cfgFileName));
        provider = new ConfigurationProviderBuilder()
                .withConfigurationSource(new ClasspathConfigurationSource(configFilesProvider))
                .withReloadStrategy(new ImmediateReloadStrategy())
                .withEnvironment(new DefaultEnvironment())
                .build();
        return provider;
    }

}
