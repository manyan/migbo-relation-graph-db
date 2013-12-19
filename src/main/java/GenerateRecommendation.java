import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;


public class GenerateRecommendation {
	public static void main(String[] args) {
		if (args == null || args.length != 1) {
			System.out.println("You need to input a userid");
			return;
		}
		
		String value = args[0];
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
		.newEmbeddedDatabase(BuildRelation.DB_PATH);
		
		Label label = DynamicLabel.label(BuildRelation.LABEL_NAME);
		System.out.println("Locate Node for: " + value);

		Node node = null;
		Transaction tx = graphDb.beginTx();
		try {
			ResourceIterator<Node> iter = graphDb.findNodesByLabelAndProperty(label, BuildRelation.LABEL_INDEX_NAME, value).iterator();
			if (!iter.hasNext()) {
				System.out.println("Failed to find userid: " + value);
				return;
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
		
		System.out.println("Generate recommendations userid: " + value);
		HashMap<Node, Integer> counts = new HashMap<Node, Integer>();
		Set<Node> directFollowing = new HashSet<Node>(); // userid
		
		// get all the direct following
		System.out.println("Generate direct following");
		tx = graphDb.beginTx();
		try {
			for ( Relationship relationship : node.getRelationships(BuildRelation.RelTypes.FOLLOWS, Direction.OUTGOING) ){
	            Node other = relationship.getOtherNode(node);
	            System.out.println("Adding " + other + " to set");
	            directFollowing.add(other);
	        }
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to create label index");
		} finally {
			tx.finish();
		}
		
		// get all the direct following
		System.out.println("Generate direct following");
		tx = graphDb.beginTx();
		try {
			for (Node cur : directFollowing) {
				for ( Relationship relationship : cur.getRelationships(BuildRelation.RelTypes.FOLLOWS, Direction.OUTGOING) ){
		            Node other = relationship.getOtherNode(cur);
		            if (!directFollowing.contains(other)) {
		            	if (counts.containsKey(other)) {
		            		counts.put(other, counts.get(other) + 1);
		            	} else {
		            		counts.put(other, 1);
		            	}
		            }
		        }
			}
			
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to create label index");
		} finally {
			tx.finish();
		}
		
		// out put
		System.out.println("Genrate recommendation");
		tx = graphDb.beginTx();
		try {
			for (Entry<Node, Integer> entry : counts.entrySet()) {
				System.out.println(entry.getKey().getProperty("userid") + " : " + entry.getValue());
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
