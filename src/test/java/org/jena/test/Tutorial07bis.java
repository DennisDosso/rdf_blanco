/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jena.test ;

import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.VCARD;

/** Tutorial 7 - selecting the VCARD resources
 * 
 * Esercizio suggerito dalla guida in cui si usa il Selector.
 * E' stato necessario introdurre un nuovo costruttore
 * nella libreria perché funzionasse.
 */
public class Tutorial07bis extends Object {
    
    static final String inputFileName = "vc-db-1.rdf";
    
    public static void main (String args[]) {
        // create an empty model
        Model model = ModelFactory.createDefaultModel();
       
        // use the FileManager to find the input file
        InputStream in = FileManager.get().open(inputFileName);
        if (in == null) {
            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
        }
        
        // read the RDF/XML file
        model.read( in, "");
        
        //si crea un selector che matcherà tutte le tripe con VCARD.FN come predicato
        Selector selector = new SimpleSelector(null, VCARD.FN, (RDFNode) null);
//        Selector selector2 = new SimpleSelector(null, null, null);
        
        StmtIterator iter = model.listStatements(selector);
        
   
        
        // select all the resources with a VCARD.FN property
//        ResIterator iter = model.listResourcesWithProperty(VCARD.FN);//iteratore su tutte le triple con la proprietà VCARD.FN come predicato
        if (iter.hasNext()) {
            System.out.println("The database contains these objects:");
            while (iter.hasNext()) {
                System.out.println("  " + iter.next()//si posiziona sulla risorsa dopo (soggetto)
                                              .getObject()
                                              .toString());//in questo modo, accedo all'oggetto dello tripla come stringa
                
                //NB: per iterare su tutti gli attributi di un soggetto, 
                //si può usare listProperty() senza argomenti
            }
        } else {
            System.out.println("No vcards were found in the database");
        }            
    }
}
