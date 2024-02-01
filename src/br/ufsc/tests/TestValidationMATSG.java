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
public class TestValidationMATSG {

    public static String filename;
    public static String extension;
    public static String dir;

    public static void main(String[] args) throws IOException, ParseException, CloneNotSupportedException {

        dir = "datasets/";
        dir += args[4]+"\\";
        filename = args[0];
        extension = ".csv";



        
        //informando lista de att a ser forçados como categoricos, mesmo contendo números
        // Pisa = null
//        String[] lstCategoricalsPreDefined = null;
//        String[] lstCategoricalsPreDefined = {"price"};
        String[] lstCategoricalsPreDefined = {"poi"};
        for (int i = 0; i < lstCategoricalsPreDefined.length; i++) {
            lstCategoricalsPreDefined[i] = lstCategoricalsPreDefined[i].toUpperCase();
        }

        String SEPARATOR = ",";
        String[] valuesNulls = {"Unknown", "-999", "-999.0", "*-999"};
        
        // Foursquare
//        String[] lstIgnoreColumns = {"label", "poi"};
        // Gowalla
        String[] lstIgnoreColumns = {"label"};
        for (int i = 0; i < lstIgnoreColumns.length; i++) {
            lstIgnoreColumns[i] = lstIgnoreColumns[i].toUpperCase();
        }

        float threshold_rc = Float.parseFloat(args[1]);
        float threshold_rv = Float.parseFloat(args[2]);
        
        String patternDateIn = "?"; //For minutes time (integer value) inform '?' character
//        String patternDateIn = "yyyy-MM-dd HH:mm:SS";  // Pisa format
        
        MATSG_Validation method = new MATSG_Validation();
        method.setFilenameFullDataset(args[3]);
        
        method.execute(dir,filename, extension, lstCategoricalsPreDefined, SEPARATOR, valuesNulls, lstIgnoreColumns, patternDateIn, threshold_rc, threshold_rv);
        
    }
}
