package it.unipd.dei.msi.terrier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.terrier.indexing.Collection;
import org.terrier.indexing.SimpleXMLCollection;
import org.terrier.matching.ResultSet;
import org.terrier.querying.Manager;
import org.terrier.querying.SearchRequest;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.utility.ApplicationSetup;

public class SimpleXMLCollectionTest {

	private static final String PATH = "/Users/dennisdosso/workspace-rdf/mvn_prova/datasets/retrieved_graphs_in_xml/retrieved_graphs_xml.xml";
	private static final String COLLECTION_PATH = "/Users/dennisdosso/workspace-rdf/mvn_prova/etc/collection.spec";
	private static final String TERRIER_HOME = "/Users/dennisdosso/Documents/developer/terrier_versions/terrier-core";
	private static final String TERRIER_ETC = "/Users/dennisdosso/Documents/developer/terrier_versions/terrier-core/etc";
	
	
	public static void main( String[] args ) {
		
		System.setProperty("terrier.home", TERRIER_HOME);
		System.setProperty("terrier.etc", TERRIER_ETC);
		
		
		//list of all the files what we want to be indexed (in this case only one)
		List<String> filesToProces = new ArrayList<String>();
		
		filesToProces.add(PATH);
		
		//set the properties to read an xml file with a collection of documents
//		ApplicationSetup.setProperty("xml.doctag", "document");
//		ApplicationSetup.setProperty("xml.idtags", "docno");
//		ApplicationSetup.setProperty("xml.terms", "object,predicate,subject");
//		ApplicationSetup.setProperty("stopwords.filename", "/Users/dennisdosso/Documents/developer/terrier_versions/terrier-core/share/stopword-list.txt");
//		ApplicationSetup.setProperty("termpipelines", "Stopwords, PorterStemmer");
//		
		
		//collection of xml documents
		Collection coll = new SimpleXMLCollection(filesToProces);
		
		// indexing
		Indexer indexer = new BasicIndexer("/Users/dennisdosso/workspace-rdf/mvn_prova/datasets/index", "data");
		indexer.index(new Collection[]{ coll });
		
		//the index is now created and open for reading
		Index index = IndexOnDisk.createIndex("/Users/dennisdosso/workspace-rdf/mvn_prova/datasets/index", "data");
		System.out.println("We have indexed " + index.getCollectionStatistics().getNumberOfDocuments() + " documents");
		
		// Create a new manager run queries
        Manager queryingManager = new Manager(index);
        //TODO EXCEPTION HERE
        SearchRequest srq = queryingManager.newSearchRequestFromQuery("The Cook, the Thief, His Wife and Her Lover");
        srq.addMatchingModel("Matching","BM25");
        
        queryingManager.runSearchRequest(srq);
        
        // Get the result set
        ResultSet results = srq.getResultSet();
        
        // Print the results
        System.out.println(results.getExactResultSize()+" documents were scored");
        System.out.println("The top "+results.getResultSize()+" of those documents were returned");
        System.out.println("Document Ranking");
        for (int i =0; i< results.getResultSize(); i++) {
            int docid = results.getDocids()[i];
            double score = results.getScores()[i];
            System.out.println("   Rank "+i+": "+docid+" "+results.getMetaItem("docno", docid)+" "+score);
        }
        
        try {
			index.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("done");
	}
}
