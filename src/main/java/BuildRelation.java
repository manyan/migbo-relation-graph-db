import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;

/*
 * below code depends on neo4j 2.0. but its not compatible with our data store, so just comment them first, will fix it later
 * */
public class BuildRelation {
	public static final String DB_PATH = "/var/lib/neo4j/data/migbo.db";
	public static final String LABEL_NAME = "user";
	public static final String LABEL_INDEX_NAME = "userid";
	public static final String INDEX_FOR_USER = "user";
	public static final String INDEX_FOR_RELATION = "relation";
	public static final String USERNAME = "username";

	public static enum RelTypes implements RelationshipType {
		FOLLOWS, FRIENDS
	}

	public static void main(String[] args) {
		BufferedReader br = null;
		try {
			System.out.println("Starting database ...");
			GraphDatabaseService graphDb = new GraphDatabaseFactory()
					.newEmbeddedDatabase(DB_PATH);
			registerShutdownHook(graphDb);
			System.out.println("Got graphDb reference");
			
			// assign each node a label, and create a index on the label for searching
			Label label = DynamicLabel.label(LABEL_NAME);
			
			// create index
			Transaction tx =  graphDb.beginTx();
			try {
				Iterator<IndexDefinition> iter = graphDb.schema().getIndexes(label).iterator();
				if (iter.hasNext()) {
					System.out.println("User label index already exists, skip creating");
				} else {
					System.out.println("Creating index on user label");
					graphDb.schema().indexFor(label).on(LABEL_INDEX_NAME).create();
				}
				
				tx.success();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Failed to create label index");
			} finally {
				tx.finish();
			}

			String sCurrentLine;
			String fileLocation = args == null || args.length == 0 ? "relation.txt.bak"
					: args[0];
			
			System.out.println("Creating relationship based on file: " + fileLocation);
			br = new BufferedReader(new FileReader(fileLocation));
			while ((sCurrentLine = br.readLine()) != null) {
				String[] values = sCurrentLine.split(",");
				if (values == null || values.length != 4) {
					continue;
				}

				// userid1 | username1 | userid2 | username2
				// create nodes for user1 and user2, and a relationship between
				// them, lets say its a follow
				try {
					tx = graphDb.beginTx();
					System.out.println("start transation for: " + sCurrentLine);
							
					// check whether we already have the node
					// userid is a string, do not need to transfer it to Integer
					ResourceIterator<Node> iter = graphDb.findNodesByLabelAndProperty(label, "userid", values[0]).iterator();
					Node follower = null;
					if (!iter.hasNext()) {
						System.out.println("Failed to find userid: " + values[0] + ", Creating new node");
						follower = graphDb.createNode(label);
						follower.setProperty(LABEL_INDEX_NAME, values[0]);
						follower.setProperty(USERNAME, values[1]);
					} else {
						follower = iter.next();
						System.out.println("Find node: " + follower + " with userid: " + follower.getProperty("userid") + ", skip creating node") ;
					}


					iter = graphDb.findNodesByLabelAndProperty(label, "userid", values[2]).iterator();
					Node followee = null;
					if (!iter.hasNext()) {
						System.out.println("Failed to find userid: " + values[2] + ", Creating new node");
						followee = graphDb.createNode(label);
						followee.setProperty(LABEL_INDEX_NAME, values[2]);
						followee.setProperty(USERNAME, values[3]);
					} else {
						followee = iter.next();
						System.out.println("Find node: " + followee + " with userid: " + followee.getProperty("userid") + ", skip creating node") ;
					}

					boolean exist = false;
					for ( Relationship relationship : follower.getRelationships(RelTypes.FOLLOWS, Direction.OUTGOING) ){
			            Node other = relationship.getOtherNode(follower);
			            if (other.getProperty(LABEL_INDEX_NAME).equals(followee.getProperty(LABEL_INDEX_NAME))) {
			            	exist = true;
			            	break;
			            }
				    }
					
					if (!exist) {
						Relationship relationship = follower
								.createRelationshipTo(followee,
										RelTypes.FOLLOWS);
						relationship.setProperty("timestamp",
								System.currentTimeMillis());
						
						System.out
								.println("Succesfully create relationship for "
										+ follower.getProperty("userid")
										+ " FOLLOWS "
										+ followee.getProperty("userid"));
					} else {
						System.out.println(String.format(
								"%s and %s already has the relationship, skip",
								values[0], values[2]));
					}

					tx.success();
					System.out.println("Persists: " + sCurrentLine);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Failed to create nodes for: + ["
							+ sCurrentLine + "]");
				} finally {
					tx.finish();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
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
