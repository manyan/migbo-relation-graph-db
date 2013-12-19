import java.util.Iterator;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;


public class TestSearch {
	public static void main(String[] args) {
		System.out.println("Starting database ...");
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabase(TestInsert.DB_PATH);
		registerShutdownHook(graphDb);
		System.out.println("Got graphDb reference");
		
		// search for node via label
		Label label = DynamicLabel.label(TestInsert.LABEL_NAME);
		Transaction tx = null;
		// check schema status
		try {
			tx = graphDb.beginTx();
			Schema schema = graphDb.schema();
			IndexDefinition indexDefinition = graphDb.schema().getIndexes(label).iterator().next();
			if (indexDefinition == null) {
				System.out.println("index definition does not exist!");
			} else {
				System.out.println("index state:" + schema.getIndexState(indexDefinition));
			}
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to wait for label index");
		}
		
		try {
			tx = graphDb.beginTx();
			
			Iterator<Node> iter = graphDb.findNodesByLabelAndProperty(label, TestInsert.LABEL_INDEX_NAME, 1).iterator();
			if (iter.hasNext()) {
				Node node = iter.next();
				System.out.println(node + " : " + node.getProperty(TestInsert.LABEL_INDEX_NAME));
			} else {
				System.out.println("Failed to locate node via label");
			}

			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// search for node via index
		IndexManager indexManager = null;
		Index<Node> usersIndex = null;
		try {
			tx =  graphDb.beginTx();
			indexManager = graphDb.index();
			System.out.println("index exist? " + indexManager.existsForNodes("users"));
			usersIndex = indexManager.forNodes("users");
			
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("Failed to get index");
		}
		
		try {
			tx = graphDb.beginTx();
			
			Iterator<Node> iter = usersIndex.get(TestInsert.LABEL_INDEX_NAME, 1).iterator();
			if (iter.hasNext()) {
				Node node = iter.next();
				System.out.println(node + " : " + node.getProperty(TestInsert.LABEL_INDEX_NAME));
			} else {
				System.out.println("Failed to locate node via index");
			}

			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
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
