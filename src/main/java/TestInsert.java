import java.util.Iterator;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.IndexDefinition;


public class TestInsert {
	public static final String DB_PATH = "/var/lib/neo4j/data/test.db";
	public static final String LABEL_NAME = "user";
	public static final String LABEL_INDEX_NAME = "userid";
	public static final String INDEX_FOR_USER = "user";
	public static final String INDEX_FOR_RELATION = "relation";
	public static final String USERNAME = "username";

	public static enum RelTypes implements RelationshipType {
		FOLLOWS, FRIENDS
	}

	public static void main(String[] args) {
		System.out.println("Starting database ...");
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabase(DB_PATH);
		registerShutdownHook(graphDb);
		System.out.println("Got graphDb reference");
		
		// assign each node a label, and create a index on the label for searching
		Label label = DynamicLabel.label(LABEL_NAME);
		Transaction tx =  graphDb.beginTx();
		try {
			Iterator<IndexDefinition> iter = graphDb.schema().getIndexes(label).iterator();
			if (iter.hasNext()) {
				System.out.println("User label index already exists, skip creating");
			} else {
				System.out.println("Creating index on user label");
				graphDb.schema().indexFor(label).on(LABEL_INDEX_NAME).create();
			}
			
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to create label index");
		} finally {
			tx.finish();
		}
		
		// create Index
		IndexManager indexManager = null;
		Index<Node> usersIndex = null;
		try {
			tx =  graphDb.beginTx();
			System.out.println("Creating node");
			indexManager = graphDb.index();
			usersIndex = indexManager.forNodes("users");
			
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("Failed to start transaction for creating index");
		}
		
		// create node
		try {
			tx = graphDb.beginTx();
			
			Node follower = graphDb.createNode(label);
			follower.setProperty(LABEL_INDEX_NAME, 1);
			follower.setProperty(USERNAME, "xxmajia");

			usersIndex.add(follower, LABEL_INDEX_NAME, 1);
			usersIndex.add(follower, USERNAME, "xxmajia");
			
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to create node");
		}
		
		// search for node
		// create node
		try {
			tx = graphDb.beginTx();
			
			Iterator<Node> iter = graphDb.findNodesByLabelAndProperty(label, LABEL_INDEX_NAME, 1).iterator();
			if (iter.hasNext()) {
				Node node = iter.next();
				System.out.println(node + " : " + node.getProperty(LABEL_INDEX_NAME));
			} else {
				System.out.println("Failed to locate node");
			}

			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to create node");
		}
		
		try {
			tx = graphDb.beginTx();
			
			Iterator<Node> iter = usersIndex.get(LABEL_INDEX_NAME, 1).iterator();
			if (iter.hasNext()) {
				Node node = iter.next();
				System.out.println(node + " : " + node.getProperty(LABEL_INDEX_NAME));
			} else {
				System.out.println("Failed to locate node");
			}

			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to create node");
		}
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}
}
