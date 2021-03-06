package net.boomerangplatform;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.boomerangplatform.config.BoomerangTestConfiguration;

public abstract class AbstractBoomerangTest {
  private static boolean initialized;

  private static final Logger LOGGER = Logger.getLogger(BoomerangTestConfiguration.class.getName());

  @Autowired
  private MongoTemplate mongoTemplate;

  protected abstract Map<String, List<String>> getData();

  protected abstract String[] getCollections();

  @Before
  public void setUp() {
    if (!initialized) {
      setInitialized();
      init();
    }

    setupDB();
  }

  @After
  public void tearDown() {
    clearDB();
  }

  protected String parseToJson(final Object template) throws JsonProcessingException {
    return TestUtil.parseToJson(template);
  }

  protected String loadResourceAsString(String fileName) {
    return TestUtil.loadResourceAsString(fileName);
  }

  private void insertDataIntoCollection(String collectionName, String filePath) throws IOException {
    MongoDatabase db = mongoTemplate.getDb();

    final MongoCollection<Document> collection = db.getCollection(collectionName);
    final Document doc = Document.parse(getMockFile(filePath));
    collection.insertOne(doc);
  }

  private static void setInitialized() {
    initialized = true;
  }

  protected static String getMockFile(String path) throws IOException {
    return TestUtil.getMockFile(path);
  }

  private void init() {
    MongoDatabase db = mongoTemplate.getDb();

    for (String collection : getCollections()) {
      db.createCollection(collection);
    }
  }

  private void setupDB() {
    getData().entrySet().stream()
        .forEach(collection -> insertDataForEntity(collection.getKey(), collection.getValue()));
  }

  private void insertDataForEntity(String entity, List<String> values) {
    values.forEach(filePath -> {
      try {
        insertDataIntoCollection(entity, filePath);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Error insert data!", e);
      }
    });
  }

  protected void clearDB() {
    MongoDatabase db = mongoTemplate.getDb();
    for (String collection : getCollections()) {
      db.getCollection(collection).drop();
    }
  }

  protected void clearColection(String collectionName) {
    MongoDatabase db = mongoTemplate.getDb();
    final MongoCollection<Document> collection = db.getCollection(collectionName);
    collection.deleteMany(new Document());
  }

}
