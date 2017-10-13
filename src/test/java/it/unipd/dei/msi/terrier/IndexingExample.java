package it.unipd.dei.msi.terrier;

import java.util.Arrays;

import org.terrier.indexing.Collection;
import org.terrier.indexing.SimpleFileCollection;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.utility.ApplicationSetup;

/**Classe di test di terrier per vedere se fa tutto come promesso.
 * Eseguiamo lettura dei file da cartella, indicizzazione*/
public class IndexingExample {

	public static void main(String[] args) throws Exception {

		// Directory containing files to index
		//il cosiddetto corpus
		String aDirectoryToIndex = "/Users/dennisdosso/eclipse-workspace/terrier-core/share/vaswani_npl/corpus";
		String destinationIndex = "/Users/dennisdosso/eclipse-workspace/terrier-core/share/vaswani_npl";
		
		// Configure Terrier
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "200");

		//setup degli indicizzatori. Ognuno ha la sua peculiarità, guardare
		//documentazione per capirle
		BasicIndexer indexer = new BasicIndexer(destinationIndex, "data");
		Collection coll = new SimpleFileCollection(Arrays.asList(aDirectoryToIndex), true);
		indexer.index(new Collection[]{coll});
		
		Index index = IndexOnDisk.createIndex(destinationIndex, "dataIndex");
//		System.out.println("We have indexed " + index.getCollectionStatistics().getNumberOfDocuments() + " documents");
//		indexer.close();
	}
}
