package it.unipd.dei.ims.ranking;

import java.util.Arrays;

import org.terrier.indexing.Collection;
import org.terrier.indexing.SimpleFileCollection;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.classical.BasicIndexer;

/**This class tries to index the xml document containing the documents
 * obtained from the subgraph creation over the query graph E.
 * 
 * */
public class FinalXmlIndexer {

	public static void main ( String[] args) {
		
		//referencing the collection of documents (only one document in this case)
		Collection coll = new SimpleFileCollection(Arrays.asList("/Users/dennisdosso/workspace-rdf/mvn_prova/datasets/retrieved_graphs_in_xml"), true);
		Indexer indexer = new BasicIndexer("/Users/dennisdosso/workspace-rdf/mvn_prova/datasets/index", "data");
		indexer.index(new Collection[]{ coll });
		
		Index index = IndexOnDisk.createIndex("/Users/dennisdosso/workspace-rdf/mvn_prova/datasets/index", "data");
		System.out.println("We have indexed " + index.getCollectionStatistics().getNumberOfDocuments() + " documents");
	}
}
