package life.genny.datagenerator.utils;

import io.quarkus.runtime.Startup;
import life.genny.datagenerator.data.schemas.MessageEntity;
import life.genny.datagenerator.data.schemas.MessageKey;
import life.genny.datagenerator.service.InfinispanCatalogueConfig;
import org.apache.commons.io.IOUtils;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class CacheUtils {

    private static final Logger LOGGER = Logger.getLogger(CacheUtils.class);

    @Inject
    private RemoteCacheManager cacheManager;

    @Inject
    private InfinispanCatalogueConfig infinispanConfig;

    public URL tableStoreCacheConfig;

    public void setTableStoreCacheConfig(URL tableStoreCacheConfig) {
        this.tableStoreCacheConfig = tableStoreCacheConfig;
    }

    public RemoteCache<String, String> createCache(String cacheName, String table, Class<? extends MessageKey> key, String message) {
        RemoteCache<String, String> entityCache = null;
        try {
            String cacheConfig = replaceDBConnectionConfiguration(tableStoreCacheConfig, table, key, message, message + "_" + cacheName);
            entityCache = cacheManager.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
                    .getOrCreateCache(cacheName, new XMLStringConfiguration(cacheConfig));
        } catch (IOException e) {
            LOGGER.error("IOException", e);
        }

        return entityCache;
    }

    public String replaceDBConnectionConfiguration(URL cacheConfig, String table, Class<? extends MessageKey> key, String message, String indexPath) throws IOException {
    	LOGGER.debug("replaceDBConnectionConfiguration"+"\n"+cacheConfig);
        String config = IOUtils.toString(cacheConfig, StandardCharsets.UTF_8)
                .replace("TABLE_NAME", table)
                .replace("MESSAGE_KEY", key.getSimpleName())
                .replace("MESSAGE_NAME", message)
                .replace("PACKAGE", infinispanConfig.packageName())
                .replace("CONNECTION_URL", infinispanConfig.connectionUrl())
                .replace("USERNAME", infinispanConfig.username())
                .replace("PASSWORD", infinispanConfig.password())
                .replace("DIALECT", infinispanConfig.dialect())
                .replace("DRIVER", infinispanConfig.driver())
                .replace("INDEXED_PATH", indexPath);
        return config;
    }

    private static RemoteCache<MessageKey, MessageEntity> findCache(RemoteCacheManager cacheManager, String cacheName) {
        RemoteCache<MessageKey, MessageEntity> cache = cacheManager.getCache(cacheName);
        if (cache == null) throw new NullPointerException("Could not find a cache called " + cacheName);
        return cache;
    }

    public static MessageEntity getEntityFromCache(RemoteCacheManager cacheManager, String cacheName, MessageKey key) {
        RemoteCache<MessageKey, MessageEntity> cache = findCache(cacheManager, cacheName);
        MessageEntity entity = cache.get(key);
        if (entity == null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return getEntityFromCache(cacheManager, cacheName, key);
        }
        return entity;
    }

    public static boolean putEntityIntoCache(RemoteCacheManager cacheManager, String cacheName, MessageKey key, MessageEntity value) {
        RemoteCache<MessageKey, MessageEntity> cache = findCache(cacheManager, cacheName);
        try {
            if (value != null) {
                cache.put(key, value);
                Thread.sleep(20);
            } else {
                LOGGER.warn("[" + cacheName + "]: Value for " + key.getKeyString() + " is null, nothing to be added.");
            }
        } catch (Exception e) {
            LOGGER.error("Exception when inserting entity into cache: " + e.getMessage());
            LOGGER.error(e.getStackTrace());
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
