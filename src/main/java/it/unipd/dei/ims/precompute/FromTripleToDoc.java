package it.unipd.dei.ims.precompute;

import java.util.Arrays;

import org.terrier.indexing.Collection;
import org.terrier.indexing.SimpleFileCollection;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.utility.ApplicationSetup;

/**Questa classe si occupa di leggere un file 
 * "corretto" rdf in formato n-triple e di produrre
 * un nuovo tipo di file json 
 * */
public class FromTripleToDoc {

	public static void main(String[] args) {
		
		
		String aDirectoryToIndex = "/Users/dennisdosso/Desktop/original-workspace/terrier-core/share/vaswani_npl/corpus";
		String pathToMyIndex = "/Users/dennisdosso/Desktop/original-workspace/terrier-core/share/vaswani_npl/result";
		
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
        ApplicationSetup.setProperty("indexer.meta.forward.keylens", "200");
        
        Indexer indexer = new BasicIndexer(pathToMyIndex, "data");
        Collection coll = new SimpleFileCollection(Arrays.asList(aDirectoryToIndex), true);
        indexer.index(new Collection[]{coll});
//        indexer.close();
        
	}
}
