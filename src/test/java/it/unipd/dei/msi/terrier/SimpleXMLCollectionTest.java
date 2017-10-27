package it.unipd.dei.msi.terrier;

import java.util.ArrayList;
import java.util.List;

import org.terrier.indexing.Collection;
import org.terrier.indexing.Document;
import org.terrier.indexing.SimpleXMLCollection;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.utility.ApplicationSetup;

public class SimpleXMLCollectionTest {

	private static final String PATH = "/Users/dennisdosso/workspace-rdf/mvn_prova/datasets/retrieved_graphs_in_xml/retrieved_graphs_xml.xml";
	private static final String COLLECTION_PATH = "/Users/dennisdosso/workspace-rdf/mvn_prova/etc/collection.spec";
	
	public static void main( String[] args ) {
		
		List<String> filesToProces = new ArrayList<String>();
		
		filesToProces.add(PATH);
		
		ApplicationSetup.setProperty("xml.doctag", "document");
		ApplicationSetup.setProperty("xml.idtags", "docno");
		ApplicationSetup.setProperty("xml.terms", "object,predicate,subject");
		
		Collection coll = new SimpleXMLCollection(filesToProces);
		
		//controlla che i documenti siano stati correttamente letti
//		while(coll.nextDocument()) {
//			Document doc = coll.getDocument();
//			System.out.println(doc);
//		}
		
		Indexer indexer = new BasicIndexer("/Users/dennisdosso/workspace-rdf/mvn_prova/datasets/index", "data");
		indexer.index(new Collection[]{ coll });
		
		Index index = IndexOnDisk.createIndex("/Users/dennisdosso/workspace-rdf/mvn_prova/datasets/index", "data");
		System.out.println("We have indexed " + index.getCollectionStatistics().getNumberOfDocuments() + " documents");
		
		System.out.println("done");
	}
}
