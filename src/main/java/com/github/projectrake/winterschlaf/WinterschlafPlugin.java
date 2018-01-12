package com.github.projectrake.winterschlaf;

import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created on 22.12.2017.
 */
public class WinterschlafPlugin extends JavaPlugin {
    private static WinterschlafPlugin instance;
    private StandardServiceRegistry standardRegistry;
    private Set<String> classNames = new HashSet<>();
    private Configuration configuration;
    private SessionFactory sessionFactory;

    @Override
    public void onEnable() {
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());

        instance = this;
        getLogger().info("Enabling Hibernate layer.");

        constructSessionFactory();

        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }

    @Override
    public void onDisable() {
        instance = null;
        sessionFactory.close();
    }

    public void addClassesByName(String... classNames) {
        addClassesByName(Arrays.asList(classNames));
    }

    public void addClassesByName(Collection<String> classNames) {
        this.classNames.addAll(classNames);
        constructSessionFactory();
    }

    public void addClasses(Class<?>... classes) {
        addClasses(Arrays.asList(classes));
    }

    public void addClasses(Collection<Class<?>> classes) {
        addClassesByName(classes.stream().map(Class::getName).collect(Collectors.toList()));
    }

    private void constructSessionFactory() {
        getLogger().info("Reconstructing session factory.");
        StandardServiceRegistryBuilder standardRegistryBuilder = new StandardServiceRegistryBuilder();
        standardRegistryBuilder.applySettings(getConfiguration().getHibernateSettings());

        getLogger().info("Using hibernate settings: " + getConfiguration().getHibernateSettings());

        StandardServiceRegistry standardRegistry = standardRegistryBuilder.build();
        MetadataSources sources = new MetadataSources(standardRegistry);
        classNames.forEach(sources::addAnnotatedClassName);

        MetadataBuilder metadataBuilder = sources.getMetadataBuilder();
        metadataBuilder.applyImplicitNamingStrategy(ImplicitNamingStrategyJpaCompliantImpl.INSTANCE);
        //metadataBuilder.applyImplicitSchemaName("rake");
        Metadata metadata = metadataBuilder.build();
        SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();
        sessionFactory = sessionFactoryBuilder.build();

    }

    private Configuration getConfiguration() {
        if (configuration == null) {
            try {
                configuration = loadConfiguration();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return configuration;
    }

    public static Session getEntityManager() {
        return getInstance().sessionFactory.openSession();
    }

    public static void withEntityManager(Consumer<Session> consumer) {
        Session en = getEntityManager();
        try {
            consumer.accept(en);
        } finally {
            en.close();
        }
    }

    public static <T> T withEntityManager(Function<Session, T> consumer) {
        Session en = getEntityManager();
        T returnValue = null;
        try {
            returnValue = consumer.apply(en);
        } finally {
            en.close();
        }

        return returnValue;
    }


    public static WinterschlafPlugin getInstance() {
        return Objects.requireNonNull(instance, "Plugin not initialized.");
    }

    private Configuration loadConfiguration() throws IOException {
        Path configPath = Paths.get(getDataFolder().getPath(), "config.yml");
        if (Files.exists(Paths.get(".debug_plugins"))) {
            getLogger().info(".debug_plugins file detected, deleting configuration.");
            Files.deleteIfExists(configPath);
        }

        saveDefaultConfig();
        return new Yaml().loadAs(new FileInputStream(configPath.toFile()), Configuration.class);
    }
}
