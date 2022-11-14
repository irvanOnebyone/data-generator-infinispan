package life.genny.datagenerator.service;

import io.quarkus.arc.Priority;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import life.genny.datagenerator.Constant;
import life.genny.datagenerator.data.schemas.*;
import life.genny.datagenerator.data.serialization.SchemaInitializerImpl;
import life.genny.datagenerator.utils.CacheUtils;
import life.genny.datagenerator.utils.PersonGenerator;
import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.commons.util.FileLookupFactory;
import org.infinispan.commons.util.Util;
import org.infinispan.protostream.SerializationContextInitializer;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

@ApplicationScoped
@Startup
public class InfinispanCacheLoader {

    private static final Logger LOGGER = Logger.getLogger(InfinispanCacheLoader.class);


    private RemoteCacheManager remoteCacheManager;
    private RemoteCache<String, String> beCache;
    private RemoteCache<String, String> beAttrCache;

    @Inject
    CacheUtils cacheUtils;

    void onStart (@Observes StartupEvent event) {
        onSchemaBuild();
        onCacheBuild();
        onDataLoad();
    }


    private void onSchemaBuild() {
        LOGGER.debug("onSchemaBuild");
        initRemoteCacheManager();
    }

    private void onCacheBuild(){
        LOGGER.debug("onCacheBuild");
        URL tableStoreCacheConfig = CacheUtils.class.getClassLoader().getResource("META-INF/protobuf/tableStore.xml");
        LOGGER.debug("Config..........."+tableStoreCacheConfig);
        cacheUtils.setTableStoreCacheConfig(tableStoreCacheConfig);
        beCache = getBECache();
        beAttrCache = getBEAttrCache();
    }

    private void onDataLoad() {
        LOGGER.debug("onDataLoad");
        for (int i = 1; i <= 10; i++) {
            BaseEntity be = PersonGenerator.generate();
            be.setId((long) i);
            insertDataIntoInfinispan(be);
        }
    }

    private void initRemoteCacheManager() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        builder.classLoader(cl);

        // load infinispan properties
        InputStream stream = FileLookupFactory.newInstance().lookupFile(Constant.HOTROD_CLIENT_PROPERTIES, cl);

        if (stream == null) {
            LOGGER.error("Could not find infinispan hotrod client properties file: " + Constant.HOTROD_CLIENT_PROPERTIES);
            return;
        }

        try {
            builder.withProperties(loadFromStream(stream));
        } finally {
            Util.close(stream);
        }

        // create cache manager
        getAllSerializationContextInitializers().stream().forEach(builder::addContextInitializer);
        Configuration config = builder.build();
        remoteCacheManager = new RemoteCacheManager(config);
        remoteCacheManager.getConfiguration().marshallerClass();
    }

    private List<SerializationContextInitializer> getAllSerializationContextInitializers() {
        List<SerializationContextInitializer> serCtxInitList = new LinkedList<>();
        SerializationContextInitializer schemaInitializer = new SchemaInitializerImpl();
        serCtxInitList.add(schemaInitializer);
        return serCtxInitList;
    }

    private Properties loadFromStream(InputStream stream) {
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (Exception e) {
            throw new HotRodClientException("Issues configuring from client hotrod-client.properties", e);
        }
        return properties;
    }

    private RemoteCache<String, String> getBECache() {
        return cacheUtils.createCache(Constant.BASEENTITY_CACHE_NAME, "baseentity", BaseEntityKey.class, BaseEntity.class.getSimpleName());
    }

    private RemoteCache<String, String> getBEAttrCache() {
        return cacheUtils.createCache(Constant.ATTRIBUTE_CACHE_NAME, "baseentity_attribute", BaseEntityAttributeKey.class,
                BaseEntityAttribute.class.getSimpleName());
    }

    private void insertDataIntoInfinispan(BaseEntity be) {
        //LOGGER.debug(be.toString());
        List<BaseEntityAttribute> beAttrs = be.getAttributes();
        boolean success = CacheUtils.putEntityIntoCache(remoteCacheManager, Constant.BASEENTITY_CACHE_NAME, be.getMessageKey(), be);
        if (!success) LOGGER.error("Failed to insert: " + be.toString());
        for (int i =0; i < beAttrs.size(); i++) {
        	//LOGGER.debug(i+" bea");
            BaseEntityAttribute beAttr = beAttrs.get(i);
            beAttr.setBASEENTITY_ID(be.getId());
            beAttr.setATTRIBUTE_ID((long) i);
            CacheUtils.putEntityIntoCache(remoteCacheManager, Constant.ATTRIBUTE_CACHE_NAME, beAttr.getMessageKey(), beAttr);
        }
    }

}
