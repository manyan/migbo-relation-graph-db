package main.resources;

import main.resources.BuildRelation.RelTypes;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.kernel.Traversal;

public class GetRecommendations {
	public static void main(String[] args) {
		if (args == null || args.length != 1) {
			System.out.println("Need to specify a userid");
			return;
		}
		
		// connect to db
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( BuildRelation.DB_PATH );
		
		// locate the node
		String userid = args[0];
		Label label = DynamicLabel.label(BuildRelation.LABEL_NAME);
		Node startNode = null;
		
		try {
			ResourceIterator<Node> users = graphDb.findNodesByLabelAndProperty(label, BuildRelation.LABEL_INDEX_NAME, userid).iterator();
			if (users.hasNext()) {
				startNode = users.next();
			} else {
				System.out.println("Can not to locate node, exit");
				return;
			}
		} catch (Exception e) {
			System.out.println("Fail to locate node");
			e.printStackTrace();
		}
		
		TraversalDescription traversal = Traversal.description().breadthFirst().relationships(RelTypes.FOLLOWS).uniqueness(Uniqueness.NODE_GLOBAL);
		for (Node node : traversal.traverse(startNode).nodes()) {
			System.out.println(String.format("userid:%s, username:%s", node.getProperty("userid"), node.getProperty("username")));
		}
		
		System.out.println("///////////////////////////////////");
		System.out.println("Print the path:");
		
		String output = "";
		for ( Path path : traversal.evaluator(Evaluators.fromDepth(2)).evaluator(Evaluators.toDepth(3)).traverse(startNode) ) {
			output += path + "\n";
		}
		System.out.println(output);
	}
}
