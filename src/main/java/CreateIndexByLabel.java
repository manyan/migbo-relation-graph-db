import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class CreateIndexByLabel {
	public static void main(String[] args) {
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabase(BuildRelation.DB_PATH);

		// registerShutdownHook(graphDb);
		System.out.println("starting graphdb for creating label index");
		final Label label = DynamicLabel.label("users");
		
		// create index
		Transaction tx =  graphDb.beginTx();
		try {
			graphDb.schema().indexFor(label).on("userid").create();
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to create label index");
		} finally {
			tx.finish();
		}
		
		// create 10 nodes
		for (int i = 1; i <= 10; i++) {
			tx = graphDb.beginTx();
			try {
				Node node = graphDb.createNode(label);
				node.setProperty("userid", i);
				
				tx.success();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Failed to create label index");
			} finally {
				tx.finish();
			}
		}
		
		// search for nodes
		for (int i = 1; i <= 10; i++) {
			tx = graphDb.beginTx();
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
}
