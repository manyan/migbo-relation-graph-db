import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;


public class SearchByLabel {
	public static void main(String[] args) {
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabase(BuildRelation.DB_PATH);

		if (args != null && args.length == 1) {
			searchByLabel(graphDb, args[0]);
			return;
		}
		
		// registerShutdownHook(graphDb);
		System.out.println("starting graphdb for creating label index");
		final Label label = DynamicLabel.label("users");
		
		// search for nodes
		for (int i = 1; i <= 20; i++) {
			Transaction tx = graphDb.beginTx();
			try {
				ResourceIterator<Node> iter = graphDb.findNodesByLabelAndProperty(label, "userid", i).iterator();
				if (!iter.hasNext()) {
					System.out.println("Failed to find userid: " + i);
				} else {
					Node cur = iter.next();
					System.out.println("Find node: " + cur + " with userid: " + cur.getProperty("userid")) ;
				}
				
				tx.success();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Failed to create label index");
			} finally {
				tx.finish();
			}
		}
	}
	
	public static void searchByLabel (GraphDatabaseService graphDb, String value) {
		System.out.println("Locate Node for: " + value);
	//	int intVal = Integer.valueOf(value);
		Label label = DynamicLabel.label(BuildRelation.LABEL_NAME);
		Node node = null;
		Transaction tx = graphDb.beginTx();
		try {
			ResourceIterator<Node> iter = graphDb.findNodesByLabelAndProperty(label, BuildRelation.LABEL_INDEX_NAME, value).iterator();
			if (!iter.hasNext()) {
				System.out.println("Failed to find userid: " + value);
			} else {
				node = iter.next();
				System.out.println("Find node: " + node + " with userid: " + node.getProperty("userid")) ;
			}
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to create label index");
		} finally {
			tx.finish();
			tx = null;
		}
		
		System.out.println("Find all followings of userid: " + value);
		tx = graphDb.beginTx();
		try {
			for ( Relationship relationship : node.getRelationships(BuildRelation.RelTypes.FOLLOWS, Direction.OUTGOING) ){
	            Node other = relationship.getOtherNode(node);
	            System.out.println("Following: " + other.getProperty("userid"));
	        }
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to create label index");
		} finally {
			tx.finish();
		}
		
	}
}
