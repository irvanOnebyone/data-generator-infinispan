package life.genny.datagenerator;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.client.hotrod.marshall.MarshallerUtil;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.infinispan.commons.util.FileLookupFactory;
import org.infinispan.commons.util.Util;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.server.test.junit5.InfinispanServerExtension;
import org.infinispan.server.test.junit5.InfinispanServerExtensionBuilder;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.quarkus.test.junit.QuarkusTest;
import life.genny.datagenerator.data.schemas.BaseEntityAttribute;
import life.genny.datagenerator.data.schemas.BaseEntityAttributeKey;
import life.genny.datagenerator.data.serialization.SchemaInitializerImpl;
import life.genny.datagenerator.utils.CacheUtils; 

@QuarkusTest
@Testcontainers 
public class TestCache {
	
	@Inject
	CacheUtils cacheUtils;
	private static final Logger LOGGER = Logger.getLogger(TestCache.class);
	private static RemoteCacheManager cacheManager;
	private static RemoteCache<String, String> cache;
		
	@BeforeAll
	public static void setup() {
		ConfigurationBuilder builder = new ConfigurationBuilder();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        builder.classLoader(cl);
        // load infinispan properties
        InputStream stream = FileLookupFactory.newInstance().lookupFile(Constant.HOTROD_CLIENT_PROPERTIES, cl);

        try {
            builder.withProperties(loadFromStream(stream));
        } finally {
            Util.close(stream);
        }

        // create cache manager
        getAllSerializationContextInitializers().stream().forEach(builder::addContextInitializer);
        Configuration config = builder.build();
        cacheManager = new RemoteCacheManager(config);
        cacheManager.getConfiguration().marshallerClass();
	}
	
	@AfterEach
	public void clearCache() {
		cache.clear();
	}
	
	private static List<SerializationContextInitializer> getAllSerializationContextInitializers() {
        List<SerializationContextInitializer> serCtxInitList = new LinkedList<>();
        SerializationContextInitializer schemaInitializer = new SchemaInitializerImpl();
        serCtxInitList.add(schemaInitializer);
        return serCtxInitList;
    }
	
    private static Properties loadFromStream(InputStream stream) {
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (Exception e) {
            throw new HotRodClientException("Issues configuring from client hotrod-client.properties", e);
        }
        return properties;
    }
    
    @Test
    public void onCacheBuildTest() throws IOException{
    	URL tableStoreCacheConfig = TestCache.class.getClassLoader().getResource("META-INF/protobuf/tableStore.xml");
    	cacheUtils.setTableStoreCacheConfig(tableStoreCacheConfig);
    	String DBConnectionConfiguration = cacheUtils.replaceDBConnectionConfiguration(tableStoreCacheConfig, "baseentity_attribute", BaseEntityAttributeKey.class, 
    			BaseEntityAttribute.class.getSimpleName(), "be_attribute"+BaseEntityAttribute.class.getSimpleName());
    	
    	cache = cacheManager.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
    			.createCache("be_attribute", new XMLStringConfiguration(DBConnectionConfiguration));
    	
    	assertNotNull(cache);
    }
}
 