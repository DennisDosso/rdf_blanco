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

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.*;

import java.io.*;

/** Tutorial 7 - selecting the VCARD resources
 */
public class Tutorial07 extends Object {
    
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
        
        // select all the resources with a VCARD.FN property
        ResIterator iter = model.listResourcesWithProperty(VCARD.FN);//iteratore su tutte le triple con la proprietà VCARD.FN come predicato
        if (iter.hasNext()) {
            System.out.println("The database contains vcards for:");
            while (iter.hasNext()) {
                System.out.println("  " + iter.nextResource()//si posiziona sulla risorsa dopo (soggetto)
                                              .getRequiredProperty(VCARD.FN)//dal soggetto, ottengo tutta la tripla
                                              .getString() );//in questo modo, accedo all'oggetto dello tripla come stringa
                
                //NB: per iterare su tutti gli attributi di un soggetto, 
                //si può usare listProperty() senza argomenti
            }
        } else {
            System.out.println("No vcards were found in the database");
        }            
    }
}