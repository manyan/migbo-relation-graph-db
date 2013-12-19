import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;


public class CreateIndexHardcoded {
	public static final String DB_PATH = "/var/lib/neo4j/data/test_index.db";
	public static final String LABEL_INDEX_KEY = "userid";
	
	public static void main(String[] args) {
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
		.newEmbeddedDatabase(DB_PATH);
		
		// create label index, should init once
		Label LABEL = DynamicLabel.label("users"); 
		boolean exist = false;
		IndexDefinition indexDefinition = null;
		try {
			Transaction tx =  graphDb.beginTx();
			
			Iterator<IndexDefinition> iter = graphDb.schema().getIndexes(LABEL).iterator();
			if (iter.hasNext()) {
				for (String key : iter.next().getPropertyKeys()) {
					if (LABEL_INDEX_KEY.equals(key)) {
						exist = true;
						break;
					}
				}
			}
			
			if (!exist) {
				System.out.println("Init label index");
				indexDefinition = graphDb.schema().indexFor(LABEL).on(LABEL_INDEX_KEY).create();
			} else {
				System.out.println("Label index exists, skip creating");
			}
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to create label index");
		}
		
		if (!exist && indexDefinition != null) {
			try {
				Transaction tx =  graphDb.beginTx();
				System.out.println("wait for label index come online!");
				Schema schema = graphDb.schema();
				schema.awaitIndexOnline( indexDefinition, 600, TimeUnit.SECONDS );
				tx.success();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Failed to wait for label index");
			}
		}
		
		if (!exist && indexDefinition != null) {
			try {
				Transaction tx =  graphDb.beginTx();
				System.out.println("wait for label index come online!");
				Schema schema = graphDb.schema();
				System.out.println("index state:" + schema.getIndexState(indexDefinition));
				tx.success();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Failed to wait for label index");
			}
		}
		
		// create Index
		IndexManager indexManager = null;
		Index<Node> usersIndex = null;
		try {
			Transaction tx =  graphDb.beginTx();
			System.out.println("Creating node");
			indexManager = graphDb.index();
			usersIndex = indexManager.forNodes("users");
			
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("Failed to start transaction for creating index");
		}
		
		// create a node
		try {
			Transaction tx =  graphDb.beginTx();
			Node node = graphDb.createNode(LABEL);
			node.setProperty(LABEL_INDEX_KEY, 1);
			node.setProperty("username", "xxmajia");
			
			// index
			usersIndex.add(node, "userid", 1);
			usersIndex.add(node, "username", "xxmajia");
			
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("Failed to add node");
		}
		
		// search via index in the same JVM
		IndexHits<Node> hit = usersIndex.get("userid", 1);
		ResourceIterator<Node> nodes = hit.iterator();
		if (nodes.hasNext()) {
			Node node = nodes.next();
			System.out.println("result: " + node);
			System.out.println("userid: " + node.getProperty("userid"));
			System.out.println("username: " + node.getProperty("username"));
		} else {
			System.out.println("No found");
		}
		
		// search via label in the same JVM
		// search for nodes
		try {
			Transaction tx =  graphDb.beginTx();
			ResourceIterator<Node> iter = graphDb.findNodesByLabelAndProperty(LABEL, "userid", 1).iterator();
			if (!iter.hasNext()) {
				System.out.println("Failed to find userid");
			} else {
				Node cur = iter.next();
				System.out.println("Find node: " + cur + " with userid: " + cur.getProperty("userid")) ;
			}
			
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to create label index");
		} 
	}
}
