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
import org.neo4j.graphdb.index.RelationshipIndex;

public class CreateIndex {
	public static void main(String[] args) {
		// create index
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabase(BuildRelation.DB_PATH);
		
	//	registerShutdownHook(graphDb);
		System.out.println("starting graphdb for creating index");
		
		IndexManager indexManager = null;
		Index<Node> users = null;
		RelationshipIndex relations = null;
		// init index
		try {
			Transaction tx = graphDb.beginTx();
			
			indexManager = graphDb.index();
			System.out.println("user index exist: " + indexManager.existsForNodes("users"));
			users = indexManager.forNodes("users");
			System.out.println("user index exist: " + indexManager.existsForNodes("users"));
			
			System.out.println("user index exist: " + indexManager.existsForRelationships("relations"));
			relations = indexManager.forRelationships("relations");
			System.out.println("user index exist: " + indexManager.existsForRelationships("relations"));
			
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("Failed to start transaction for creating index");
		} 
		
		// Create a text node
		try {
			Transaction tx = graphDb.beginTx();
			Label label = DynamicLabel.label("user");
			Node follower = graphDb.createNode(label);
		//	Node follower = graphDb.createNode();
			follower.setProperty("userid", 1);
			follower.setProperty("username", "xxmajia");
			
			users.add(follower, "userid", 1);
			users.add(follower, "username", "xxmajia");
			
			follower = graphDb.createNode();
			follower.setProperty("userid", 2);
			follower.setProperty("username", "firegunes");
			
			users.add(follower, "userid", 2);
			users.add(follower, "username", "firegunes");

			tx.success();
			tx.finish();
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("Failed to start transaction for index");
		}
		
		// search
		IndexHits<Node> hit = users.get("userid", 2);
		ResourceIterator<Node> nodes = hit.iterator();
		if (nodes.hasNext()) {
			Node node = nodes.next();
			System.out.println("result: " + node);
			System.out.println("userid: " + node.getProperty("userid"));
			System.out.println("username: " + node.getProperty("username"));
		} else {
			System.out.println("No found");
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
