package it.unipd.dei.ims.main;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**Classe che mi tiene cose utili, come costanti, stringhe, valori e metodi
 * statici vari.
 * */
public class Utilities {

	
	public static final String NT = "N-TRIPLE";/** Rappresenta la stringa da dare come parametro ai metodi Jena
	 * per leggere e scrivere i file in formato .nt (triple)*/
	
	public final static Charset ENCODING = StandardCharsets.UTF_8;
	
	public final static String SUBJECT = "subject";
	
	public final static String PREDICATE = "predicate";
	
	public final static String OBJECT = "object";
	
	public final static String ID = "docno";
	
	
	
	/** Legge dal file di properties character.properties 
	 * e ritorna l'array con tutti i caratteri e loro sostituzione.
	 * */
	public static List<Pair<String, String>> retrieveCharactersProperties() {
		
		InputStream input = null;
		Properties prop = new Properties();
		
		List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		
		try {
			input = new FileInputStream("properties/characters.properties");
			prop.load(input);
			Pair<String, String> par = null;
			
			//prendo tutte le chiavi
			Enumeration<?> enumeration = prop.propertyNames();
			while(enumeration.hasMoreElements()) {
				String key = (String) enumeration.nextElement();
				String value = prop.getProperty(key);
				String[] couple = value.split("_");
				par = Pair.of(couple[0], couple[1]);
				
				list.add(par);
			}
			
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
}
