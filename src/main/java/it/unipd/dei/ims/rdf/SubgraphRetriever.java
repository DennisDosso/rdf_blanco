package it.unipd.dei.ims.rdf;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**Questa classe opera nel query graph E generato in precedenza ed estrapola tutti i sottografi necessari.*/
public class SubgraphRetriever {
	
	public static void main (String[] args) {
		
		//piglio i path di cui necessito
		Map<String, String> stringMap = getPaths();
	}
	
	private static Map<String, String> getPaths() {
		prop = new Properties();
		input = null;

		try {
			//tenta di leggere il file di configuration dove ci sono tutti i dati
			input = new FileInputStream("properties/path.properties");

			// load the properties file
			prop.load(input);

			String path = "";

			path = prop.getProperty("rdfPurifiedFilePath");
			return path;

		} catch (IOException ex) {
			ex.printStackTrace();
			log.error("IOException reading configuration file to retrieve file path");
			throw new IOException("file not found");
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
					log.error("IOExcpetion closing configuration file");
					throw new IOException("file not found");
				}
			}
		}
	}
	}

}
