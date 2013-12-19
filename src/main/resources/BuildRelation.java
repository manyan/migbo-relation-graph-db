package main.resources;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

public class BuildRelation {
	public static final String DB_PATH = "relation/users";
	public static final String LABEL_NAME = "User";
	public static final String LABEL_INDEX_NAME = "userid";

	/*
	 * define our own relationship
	 */
	public static enum RelTypes implements RelationshipType {
		FOLLOWS, FRIENDS
	}

	public static void main(String[] args) {
		BufferedReader br = null;
		try {
			System.out.println( "Starting database ..." );
			GraphDatabaseService graphDb = new GraphDatabaseFactory()
					.newEmbeddedDatabase(DB_PATH);
			registerShutdownHook(graphDb);
			
			// create index
			IndexDefinition indexDefinition;
			try {
				Transaction tx = graphDb.beginTx();
				
				Schema schema = graphDb.schema();
				Iterator<IndexDefinition> iter = schema.getIndexes().iterator();
				boolean needToCreate = true;
				while (iter.hasNext()) {
					IndexDefinition cur = iter.next();
					if (cur.getLabel().name().equals(LABEL_NAME)) {
						needToCreate = false;
						break;
					}
				}
				
				// check before create
				if (needToCreate) {
					indexDefinition = schema.indexFor(DynamicLabel.label(LABEL_NAME)).on(LABEL_INDEX_NAME).create();
					// wait for creating index for the label
					schema.awaitIndexOnline( indexDefinition, 10, TimeUnit.SECONDS );
				}
				tx.success();
			} catch (Exception e) {
				System.out.println( "Failed to start transaction for creating index" );
			}

			String sCurrentLine;
			String fileLocation = args == null || args.length == 0 ? "/Users/xxmajia/Downloads/relation.txt"
					: args[0];
			br = new BufferedReader(new FileReader(fileLocation));
			while ((sCurrentLine = br.readLine()) != null) {
				String[] values = sCurrentLine.split(",");
				if (values == null || values.length != 4) {
					continue;
				}

				// userid1 | username1 | userid2 | username2
				// create nodes for user1 and user2, and a relationship between
				// them, lets say its a follow
				
				Label label = DynamicLabel.label( LABEL_NAME );
				try {
					Transaction tx = graphDb.beginTx();
					Node follower = graphDb.createNode(label);
					follower.setProperty("userid", values[0]);
					follower.setProperty("username", values[1]);
					
					Node followee = graphDb.createNode(label);
					followee.setProperty("userid", values[2]);
					followee.setProperty("username", values[3]);
					
					Relationship relationship = follower.createRelationshipTo(followee, RelTypes.FOLLOWS);
					relationship.setProperty("timestamp", System.currentTimeMillis());
					
					System.out.println("Succesfully create relationship for " + follower.getProperty("userid") + " FOLLOWS " + followee.getProperty("userid") );
					tx.success();
				} catch (Exception e) {
					System.out.println( "Failed to create nodes for: + [" + sCurrentLine + "]");
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
