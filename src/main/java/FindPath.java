import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.Traversal;


public class FindPath {
	public static void main(String[] args) {
		if (args == null || args.length != 2) {
			System.out.println("You need to input follower and followee");
			return;
		}
		
		String followerStr = args[0];
		String followeeStr= args[1];
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
		.newEmbeddedDatabase(BuildRelation.DB_PATH);
		
		Label label = DynamicLabel.label(BuildRelation.LABEL_NAME);
		System.out.println("Locate follower: " + followerStr);
		Node follower = locate(graphDb, label, followerStr);
		System.out.println("Locate followee: " + followerStr);
		Node followee = locate(graphDb, label, followeeStr);
		
		Transaction tx = graphDb.beginTx();
		try {
			PathFinder<Path> finder = GraphAlgoFactory.shortestPath(Traversal.expanderForTypes( BuildRelation.RelTypes.FOLLOWS, Direction.OUTGOING ), 2 );
			Iterable<Path> paths = finder.findAllPaths( follower, followee );
			int count = 0;
			for (Path path : paths) {
				count++;
				System.out.println("Path[" + count + "]: " + path);
			}
			
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to create label index");
		} finally {
			tx.finish();
			tx = null;
		}
		
		
	}
	
	public static Node locate(GraphDatabaseService graphDb, Label label, String value) {
		Transaction tx = graphDb.beginTx();
		Node node = null;
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
		
		return node;
	}
}
