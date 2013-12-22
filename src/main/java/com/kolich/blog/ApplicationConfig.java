package com.kolich.blog;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import org.slf4j.Logger;

import java.io.File;
import java.util.Map;

import static com.typesafe.config.ConfigFactory.load;
import static java.lang.System.getProperty;
import static org.slf4j.LoggerFactory.getLogger;

public final class ApplicationConfig {

    private static final Logger logger__ = getLogger(ApplicationConfig.class);

    // Public static's

    private static final String DEV_MODE_PROPERTY =
        "dev-mode";
    private static final String MARKDOWN_ROOT =
        "markdown.root";

    private static final String BLOG_REPO_CLONE_URL_PROPERTY =
        "clone.url";
    private static final String CLONE_ON_STARTUP_PROPERTY =
        "clone.on-startup";
    private static final String CLONE_PATH_DIRECTORY_PROPERTY =
        "clone.path";
    private static final String GIT_UPDATE_REPO_INTERVAL =
        "clone.update-interval";

    // Private static's

    private static final String BLOG_EXTERNAL_CONFIG_FILENAME =
        "blog.conf";
    private static final String CONTAINER_CONF_DIRNAME =
        "conf";
    private static final String JETTY_HOME_SYS_PROPERTY =
        getProperty("jetty.home");
    private static final String CATALINA_HOME_SYS_PROPERTY =
        getProperty("catalina.home");

    private final Config config_;

    private ApplicationConfig() {
        final Config refConfConfig = load();
        // Load the external 'blog.conf' application configuration file
        // specific to the internal Servlet container.
        final Config overrideConfig = loadBlogOverrideConfig();
        // Load and build the application configuration, then attach the
        // loaded immutable config to the servlet context.
        final Config blogConfig;
        if(overrideConfig != null) {
            logger__.debug("Found valid override configuration; " +
                "using override.");
            blogConfig = overrideConfig.withFallback(refConfConfig);
        } else {
            logger__.debug("Found no valid override configuration; " +
                "using default configuration provided by bundled " +
                "application.conf");
            blogConfig = refConfConfig;
        }
        for(final Map.Entry<String,ConfigValue> entry : blogConfig.entrySet()) {
            logger__.trace("Loaded config (key=" + entry.getKey() +
                ", value=" + entry.getValue() + ")");
        }
        config_ = blogConfig;
    }
    private static class LazyHolder {
        private static final ApplicationConfig instance__ =
            new ApplicationConfig();
    }

    /**
     * Returns an immutable, shared, configuration singleton instance that
     * represents the configuration of this Blog application.  Gracefully
     * includes any custom configuration "overrides" placed into the Servlet
     * container's "conf" (configuration) directory.
     */
    public static final Config getConfigInstance() {
        return LazyHolder.instance__.config_.getConfig("blog");
    }

    private static final Config loadBlogOverrideConfig() {
        File configFile = null;
        if(JETTY_HOME_SYS_PROPERTY != null) {
            final File jettyConfigFile = new File(
                new File(JETTY_HOME_SYS_PROPERTY, CONTAINER_CONF_DIRNAME),
                BLOG_EXTERNAL_CONFIG_FILENAME);
            if(jettyConfigFile.exists()) {
                logger__.info("Found Jetty specific " +
                    BLOG_EXTERNAL_CONFIG_FILENAME + " configuration file " +
                    "at: " + jettyConfigFile.getAbsolutePath());
                configFile = jettyConfigFile;
            }
        } else if(CATALINA_HOME_SYS_PROPERTY != null) {
            final File catalinaConfigFile = new File(
                new File(CATALINA_HOME_SYS_PROPERTY, CONTAINER_CONF_DIRNAME),
                BLOG_EXTERNAL_CONFIG_FILENAME);
            if(catalinaConfigFile.exists()) {
                logger__.info("Found Catalina (Tomcat) specific " +
                    BLOG_EXTERNAL_CONFIG_FILENAME + " configuration file " +
                    "at: " + catalinaConfigFile.getAbsolutePath());
                configFile = catalinaConfigFile;
            }
        }
        Config config = null;
        try {
            // If we found a valid config file specific to the supported
            // Servlet container, then load it.  Otherwise, return null meaning
            // no override file was found.
            config = (configFile != null) ?
                // Load the external 'blog.conf' Typesafe configuration
                // from an external file.
                ConfigFactory.parseFile(configFile) :
                // No valid external config file found, return null.
                null;
        } catch (Exception e) {
            // Usually getByName here when the provided blog.conf override file
            // is malformed or something went wrong while loading it.
            logger__.warn("Failed to parse override " +
                BLOG_EXTERNAL_CONFIG_FILENAME + " configuration file.", e);
            config = null;
        }
        return config;
    }

    // ******************************************************************
    // Config property getters (helper methods)
    // ******************************************************************

    public static final Boolean isDevMode() {
        return getConfigInstance().getBoolean(
            DEV_MODE_PROPERTY);
    }

    public static final String getMarkdownRootDir() {
        return getConfigInstance().getString(
            MARKDOWN_ROOT);
    }

    public static final String getBlogRepoCloneUrl() {
        return getConfigInstance().getString(
            BLOG_REPO_CLONE_URL_PROPERTY);
    }

    public static final Boolean shouldCloneOnStartup() {
        return getConfigInstance().getBoolean(
            CLONE_ON_STARTUP_PROPERTY);
    }

    public static final String getClonePath() {
        return getConfigInstance().getString(
            CLONE_PATH_DIRECTORY_PROPERTY);
    }

    public static final Long getGitUpdateInterval() {
        return getConfigInstance().getMilliseconds(
            GIT_UPDATE_REPO_INTERVAL);
    }

}
