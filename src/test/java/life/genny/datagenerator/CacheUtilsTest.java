//package life.genny.datagenerator;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.io.IOException;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//
//import javax.inject.Inject;
//
//import org.apache.commons.io.IOUtils;
//import org.infinispan.client.hotrod.RemoteCache;
//import org.infinispan.client.hotrod.RemoteCacheManager;
//import org.infinispan.client.hotrod.exceptions.HotRodClientException;
//import org.infinispan.commons.api.CacheContainerAdmin;
//import org.infinispan.commons.configuration.XMLStringConfiguration;
//import org.infinispan.server.test.junit5.InfinispanServerExtension;
//import org.infinispan.server.test.junit5.InfinispanServerExtensionBuilder;
//import org.jboss.logging.Logger;
//
//import life.genny.datagenerator.data.schemas.BaseEntityAttribute;
//import life.genny.datagenerator.data.schemas.BaseEntityAttributeKey;
//import life.genny.datagenerator.data.schemas.MessageKey;
//import life.genny.datagenerator.service.InfinispanCatalogueConfig;
//import life.genny.datagenerator.utils.CacheUtils;
//
//import org.junit.Before;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.RegisterExtension;
//
//import io.quarkus.test.junit.QuarkusTest;
//
//@QuarkusTest
//class CacheUtilsTest {
//	
//	@Inject
//    private CacheUtils cacheUtils;
////    private URL tableStoreCacheConfig;
////    private String DBConnectionConfiguration;
////    private static final Logger LOGGER = Logger.getLogger(CacheUtilsTest.class);
//	
//    @Inject
//    private RemoteCacheManager cacheManager;
//
//    @Inject
//    private InfinispanCatalogueConfig infinispanConfig;
//    
//	@RegisterExtension
//	private InfinispanServerExtension server = InfinispanServerExtensionBuilder.server();
//	
//	@Before
//	private void setup() throws IOException {
////		tableStoreCacheConfig = CacheUtilsTest.class.getClassLoader().getResource("META-INF/protobuf/tableStore.xml");
////		DBConnectionConfiguration = DBConnectionConfiguration(tableStoreCacheConfig, "baseentity_attribute", BaseEntityAttributeKey.class, 
////				BaseEntityAttribute.class.getSimpleName(), "be_"+BaseEntityAttribute.class.getSimpleName());
////		cacheUtils.setTableStoreCacheConfig(tableStoreCacheConfig);
//    }
//	
////	private String DBConnectionConfiguration(URL cacheConfig, String table, Class<? extends MessageKey> key, String message, String indexPath) throws IOException {
////		LOGGER.debug("replaceDBConnectionConfiguration"+"\n"+cacheConfig);
////        String config = IOUtils.toString(cacheConfig, StandardCharsets.UTF_8)
////                .replace("TABLE_NAME", table)
////                .replace("MESSAGE_KEY", key.getSimpleName())
////                .replace("MESSAGE_NAME", message)
////                .replace("PACKAGE", infinispanConfig.packageName())
////                .replace("CONNECTION_URL", infinispanConfig.connectionUrl())
////                .replace("USERNAME", infinispanConfig.username())
////                .replace("PASSWORD", infinispanConfig.password())
////                .replace("DIALECT", infinispanConfig.dialect())
////                .replace("DRIVER", infinispanConfig.driver())
////                .replace("INDEXED_PATH", indexPath);
////        return config;
////    }
//	
//	@Test
//	private void testCreateCache() {
//		try {
////			RemoteCache<String, String> cache = cacheUtils.createCache("be", "baseentity_attribute", BaseEntityAttributeKey.class,
////					BaseEntityAttribute.class.getSimpleName());
////			cacheManager = server.hotrod().createRemoteCacheManager();
////			RemoteCache<String, String> entityCache = cacheManager.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
////                    .createCache("be", new XMLStringConfiguration(DBConnectionConfiguration));
////			assertNotNull(entityCache);
//		}catch(HotRodClientException e) {
////			LOGGER.error(e.getMessage());
//			e.printStackTrace();
//		}
//	}
//
//	@Test
//	void testGetEntityFromCache() {
//		
//	}
//
//	@Test
//	void testPutEntityIntoCache() {
//		
//	}
//
//}
