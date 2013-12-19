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

public class SearchHardcoded {
	public static void main(String[] args) {
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(CreateIndexHardcoded.DB_PATH);

		IndexManager indexManager = null;
		Index<Node> usersIndex = null;

		try {
			Transaction tx = graphDb.beginTx();
			indexManager = graphDb.index();
			System.out.println("index exist: "
					+ indexManager.existsForNodes("users"));
			usersIndex = indexManager.forNodes("users");

			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to start transaction for creating index");
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
		Label LABEL = DynamicLabel.label("users");
		try {
			Transaction tx = graphDb.beginTx();
			ResourceIterator<Node> iter = graphDb.findNodesByLabelAndProperty(LABEL, "userid", 1).iterator();
			if (!iter.hasNext()) {
				System.out.println("Failed to find userid");
			} else {
				Node cur = iter.next();
				System.out.println("Find node: " + cur + " with userid: " + cur.getProperty("userid"));
			}

			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to create label index");
		}
	}
}
