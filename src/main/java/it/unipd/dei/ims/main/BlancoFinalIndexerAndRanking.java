package it.unipd.dei.ims.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.terrier.indexing.Collection;
import org.terrier.indexing.SimpleXMLCollection;
import org.terrier.matching.ResultSet;
import org.terrier.querying.Manager;
import org.terrier.querying.SearchRequest;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.classical.BasicIndexer;

/**In this class we collect the xml document representing
 * the subgraphs of E retrieved by the Blanco retrieving algorithm
 * and ranking them with the LM  Blanco ranking model
 * 
 * 
 * */
public class BlancoFinalIndexerAndRanking {

	/**String identifying the xml file with all the retrieved documents from E*/
	private static final String XML_FILE = "xml_file";
	private static final String COLLECTION_SPEC = "collection.spec";
	private static final String TERRIER_HOME = "terrier_home";
	private static final String TERRIER_ETC = "terrier_etc";
	private static final String INDEX_DIRECTORY = "index_directory";
	private static final String INDEX_NAME = "index_name";
	private static final String QUERY = "query";
	
	private static final String MODEL = "BlancoElbassuoniLM";

	public static void main (String [] args) {

		//getting the necessary data (path, properties etc.)
		Map<String, String> dataMap = getData();

		//setting system properties for terrier to find the terrier_home and the /etc directory
		System.setProperty("terrier.home", dataMap.get(TERRIER_HOME));
		System.setProperty("terrier.etc", dataMap.get(TERRIER_ETC));

		//list of all the files what we want to be indexed (in this case only one)
		List<String> filesToProcess = new ArrayList<String>();
		filesToProcess.add(dataMap.get(XML_FILE));
		
		//creating the collection
		Collection coll = new SimpleXMLCollection(filesToProcess);
		
		//indexing the collection
		Indexer indexer = new BasicIndexer(dataMap.get(INDEX_DIRECTORY), dataMap.get(INDEX_NAME));
		indexer.index(new Collection[]{ coll });
		
		//open the newly created index
		Index index = IndexOnDisk.createIndex(dataMap.get(INDEX_DIRECTORY), dataMap.get(INDEX_NAME));
		System.out.println("We have indexed " + index.getCollectionStatistics().getNumberOfDocuments() + " documents");
		
		// Create a new manager run queries
        Manager queryingManager = new Manager(index);
        
        SearchRequest srq = queryingManager.newSearchRequestFromQuery(dataMap.get(QUERY));
//        srq.addMatchingModel("Matching","DirichletLM");
        srq.addMatchingModel("Matching",MODEL);
        
        queryingManager.runSearchRequest(srq);
        
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
        
        System.out.println("done");
	}

	
	
	
	
	/**Returns a Map with the required paths
	 * 
	 * */
	private static Map<String, String> getData() {
		Properties prop = new Properties();
		Map<String, String> map = new HashMap<String, String> ();

		try {
			InputStream input = new FileInputStream("properties/path.properties");
			prop.load(input);

			//setting the xml file with all the documents
			map.put(XML_FILE, prop.getProperty("retrievedGraphsInXmlPath"));

			//setting the path to the collection.spec file
			map.put(COLLECTION_SPEC, prop.getProperty("collectionSpecPath"));
			//setting the terrier home path
			map.put(TERRIER_HOME, prop.getProperty("terrierHomePath"));
			//setting the path to the etc diretory
			map.put(TERRIER_ETC, prop.getProperty("terrierEtcPath"));
			
			map.put(INDEX_DIRECTORY, prop.getProperty("indexDirectoryPath"));
			
			input.close();
			//other data from another properties file
			input = new FileInputStream("properties/terrier_data.properties");
			prop.load(input);
			
			map.put(INDEX_NAME, prop.getProperty("indexName"));
			map.put(QUERY, prop.getProperty("query"));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return map;
	}
}
