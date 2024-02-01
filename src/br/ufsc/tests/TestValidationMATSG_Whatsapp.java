/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsc.tests;


import br.ufsc.methods.MATSG_Validation;
import java.io.IOException;
import java.text.ParseException;

/**
 *
 * @author vanes
 */
public class TestValidationMATSG_Whatsapp {

    public static String filename;
    public static String extension;
    public static String dir;

    public static void main(String[] args) throws IOException, ParseException, CloneNotSupportedException {

        dir = "datasets/";
        dir += "Whatsapp/";
        filename = "Whatsapp";
        extension = ".csv";



        
        //informando lista de att a ser forçados como categoricos, mesmo contendo números
        String[] lstCategoricalsPreDefined = {"PRICE"};
        

        String SEPARATOR = ",";
        String[] valuesNulls = {"Unknown", "*-1", "-1"};
        
        String[] lstIgnoreColumns = null;

        float threshold_rc = 0.25f;
        float threshold_rv = 0.25f;
        
        String patternDateIn = "?"; //For minutes time (integer value) inform '?' character
        
        MATSG_Validation method = new MATSG_Validation();
//            method.notConsiderNulls();
            method.setFilenameFullDataset(filename+"_complete");

        
        method.execute(dir,filename, extension, lstCategoricalsPreDefined, SEPARATOR, valuesNulls, lstIgnoreColumns, patternDateIn, threshold_rc, threshold_rv);
        
    }
}
