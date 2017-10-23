package it.unipd.dei.ims.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	/** Legge da fule di properties tutti i valori e ritorna una lista
	 * di stringhe da essi formata
	 * 
	 * @param propertyName path all'interno del progetto Eclipse 
	 * del file di property da leggere. 
	 * */
	public static List<String> getPropertiesValues(String propertyName) {

		Properties prop = new Properties();
		InputStream input = null;
		List<String> list = new ArrayList<String>();

		try {
			input = new FileInputStream(propertyName);
			prop.load(input);
			
			//si leggono tutti gli elementi nel file di properties
			Enumeration<?> enumeration = prop.propertyNames();
			while(enumeration.hasMoreElements()) {
				String key = (String) enumeration.nextElement();
				String value = prop.getProperty(key);

				list.add(value);
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e ) {
			e.printStackTrace();
		}
		
		return list;

	}
	
	/**Prende la stringa a parametro, passata in formato RDF n-triple, 
	 * e la converte in stringa. Il risultato è restituito in una mappa
	 * 
	 *  @param line la stringa da trasformare in documento.
	 *  
	 *  @return una Mappa contenente coppie chiave-valore, entrambe stringhe. 
	 *  Le chiavi possibili sono subject, object, predicate.
	 *  */
	public static Map<String, String> createBagOfWords(String line) {

		Map<String, String> map = new HashMap<String, String>();
		
		//divido le stringhe. Due casi mi interessa: uri tra <...> o 
		//literal tra "..."
		//usiamo le fighissime espressioni regolari che spaccano
		
		//espressione regolare
		String patternString = "<(.*?)>|\"(.*)\"(\\^\\^<(.*?)>)?|([0-9]+)$";
		/*Questa regex prende gli url tra le brackets, le stringhe e anche l'id*/

		MutablePair<String, String> pair = MutablePair.of("", "");
		String bagOfWord = "";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(line);
		
		int counter = 0;//contatore che tiene conto se siamo al soggetto, a predicato o all'oggetto
		
		
		while(matcher.find()) {
			String work = matcher.group();
			
			
			if(work.equals(""))//tanto per sicurezza nel caso l'espressione porti a epsilon
				continue;
			else if(work.charAt(0) == '<') {//è un url
				//è un url
				//lo prendo senza le parentesi
				work = matcher.group(1);
				//lavoro sull'URI
				work = elaborateUri(work);
				
				//si rimpiazzano possibili caratteri non accettabili dentro un tag xml
				work = work.replaceAll("\\&", "&amp;");
				work = work.replaceAll("<", "&lt;");
				work = work.replaceAll(">", "&gt;");
				work = work.replaceAll("'", "&apos;");
				work = work.replaceAll("\"", "&quot;");
				
				
				if(counter==0) {
					//è soggetto
					map.put(Utilities.SUBJECT, work.trim());
					counter++;
				}
				else if(counter==1) {
					//è predicato
					map.put(Utilities.PREDICATE, work.trim());
					counter++;
				}
				else if(counter==2) {
					//è oggetto
					map.put(Utilities.OBJECT, work.trim());
					counter++;
				}
			}
			else if(work.charAt(0) == '"') {
				//è un literal e dovrebbe essere solo oggetto
				work = matcher.group(2);
				work = work.replaceAll("\\&", "&amp;");
				work = work.replaceAll("<", "&lt;");
				work = work.replaceAll(">", "&gt;");
				
				//si può aggiungere subito quando trovato alla bag of words
				bagOfWord = bagOfWord + " " + work;
				
				map.put(Utilities.OBJECT, work.trim());
			}
			else { 
				//è un id
				work = matcher.group();
				//lo setto come nome del documento
				pair.setLeft(work);
				
				map.put(Utilities.ID, work);
			}
		}
		
		//completata l'elaborazione della riga, possiamo settare il campo
		pair.setRight(bagOfWord);
		
		return map;
	}
	
	/**Prende l'URI e, trattandolo come un url, ritorna la stringa che è l'ultimo
	 * elemento del path.*/
	public static String elaborateUri(String elaborandum) {
		
		try {
			URL urlString = new URL(elaborandum);
			String elaboratum = urlString.getPath();
			
			//potrebbe anche esserci una reference alla fine dell'URL. 
			//è quella che ci interessa
			String ref = urlString.getRef();
			if(ref != null)
				elaboratum = ref;
			
			String[] splitStrings = elaboratum.split("/");//prima prendiamo l'ultima parte del path
			
			//questa potrebbe essere composta da più parole separate da '_', che si dividono
			String[] secondSplit = splitStrings[splitStrings.length-1].split("_");
			
			//si mettono i caratteri in un'unica stringa, separati da spazi
			String returnandum = "";
			for(String s : secondSplit) {
				returnandum = returnandum + " " + s;
			}
			
			return returnandum;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "";
		}
	}
}
