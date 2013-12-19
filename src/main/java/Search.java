import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;


public class Search {
	public static void main(String[] args) {
		if (args == null || args.length != 1) {
			System.out.println("Need to input a userid");
			return;
		}
		
		String userid = args[0];
		System.out.println("Getting info for userid: " + userid);
		
		// create index
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabase(BuildRelation.DB_PATH);
	//	registerShutdownHook(graphDb);
		System.out.println("starting graphdb for creating index");
		
		IndexManager indexManager = null;
		Index<Node> users = null;
		RelationshipIndex relations = null;
		try {
			Transaction tx = graphDb.beginTx();
			
			indexManager = graphDb.index();
			System.out.println("user index exist: " + indexManager.existsForNodes("users"));
			users = indexManager.forNodes("users");
			System.out.println("user index exist: " + indexManager.existsForNodes("users"));
			
			System.out.println("user index exist: " + indexManager.existsForRelationships("relations"));
			relations = indexManager.forRelationships("relations");
			System.out.println("user index exist: " + indexManager.existsForRelationships("relations"));
			
			IndexHits<Node> nodes = users.query("*:*");
			ResourceIterator<Node> iter = nodes.iterator();
			
			if (!iter.hasNext()) {
				System.out.println("No Found!");
			} else {
				while (iter.hasNext()) {
					Node result = iter.next();
					System.out.println("result: " + result);
					System.out.println("userid: " + result.getProperty("userid"));
					System.out.println("username: " + result.getProperty("username"));
				}
			}
			
			
//			IndexHits<Node> hit = users.get("userid", "2");
//			Node result = hit.getSingle();
//			if (result == null) {
//				System.out.println("No Found!");
//			} else {
//				System.out.println("result: " + result);
//				System.out.println("userid: " + result.getProperty("userid"));
//				System.out.println("username: " + result.getProperty("username"));
//			}
			
			tx.success();
			tx.finish();
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("Failed to start transaction for creating index");
		}
		
//		IndexHits<Node> hit = users.get("userid", userid);
//		Node result = hit.getSingle();
//		System.out.println("result: " + result);
//		System.out.println("userid: " + result.getProperty("userid"));
//		System.out.println("username: " + result.getProperty("username"));
		
//		try {
//			IndexHits<Node> hit = users.get("userid", userid);
//			Node result = hit.getSingle();
//			System.out.println("result: " + result);
//			System.out.println("userid: " + result.getProperty("userid"));
//			System.out.println("username: " + result.getProperty("username"));
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out
//					.println("Failed to start transaction for creating index");
//		}
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
