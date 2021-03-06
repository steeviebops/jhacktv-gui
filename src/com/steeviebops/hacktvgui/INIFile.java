/*
 * Copyright (C) 2020 Stephen McGarry
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.steeviebops.hacktvgui;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Basic INI file reader/writer.
 * Uses regular expressions to read/write the contents of an INI file.
 * @author Stephen McGarry
 */

public class INIFile {
    
    public static boolean getBooleanFromINI(String fileName, String section, String setting) {
        String v = getINIValue(fileName, section, setting, "");
        if ((v.equals("0")) || (v.equals("false"))) {
            return false;
        }
        else if ((v.equals("1")) || (v.equals("true"))) {
            return true;
        }
        else {
            return false;
        }
    }
    
    public static Integer getIntegerFromINI(String fileName, String section, String setting) {
        try {
            return Integer.parseInt(getINIValue(fileName, section, setting, ""));
        }
        catch (NumberFormatException e) {
            return null;
        }
    }
    
    public static Double getDoubleFromINI(String fileName, String section, String setting) {
        try {
            return Double.parseDouble(getINIValue(fileName, section, setting, ""));
        }
        catch (NumberFormatException e) {
            return null;
        }
    }    
    
    public static String getStringFromINI(String fileName, String section, String setting, String defaultValue, Boolean preserveCase) {
        if (!preserveCase) {
            return getINIValue(fileName, section, setting, defaultValue).toLowerCase();
        }
        else {
            return (getINIValue(fileName, section, setting, defaultValue)); 
        }
    }    
    
    public static String getINIValue(String fileName, String section, String setting, String defaultValue) {
        // Load the specified file to a string named fileContents
        File f = new File(fileName);
        String fileContents;
        try {
            fileContents = Files.readString(f.toPath(), StandardCharsets.US_ASCII);
        }
        catch (IOException e) {
            return defaultValue;
        }
        // Extract the specified section from fileContents to parsedSection
        String parsedSection = null;
        String r1 = "(?i)(?<=";
        String r2;
        // Check if the file uses Windows or Unix-style line breaks
        // The second part of the regex varies depending on which it has
        if (fileContents.contains("\r\n")) {
            r2 = "\\]\\r\\n)[^\\[]*";
        }
        else {
            r2 = "\\]\\n)[^\\[]*";
        }
        Pattern pattern = Pattern.compile(r1 + section + r2, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(fileContents);
        while (matcher.find()) {
            parsedSection = matcher.group(0);
        }
        
        // If the specified section was not found, return the default value
        if (parsedSection == null) return defaultValue;
        
        // If the section is commented out, return the default value
        if ( (parsedSection.contains(";" + setting) || 
                (parsedSection.contains("; " + setting) )) )
            return defaultValue;
        
        // Extract the required setting from parsedSection and return its value
        // If value is null, return the specified default value instead
        String result = null;
        
        String regex1 = "(?i)(?<=\\b";
        String regex2 = "=)[^\r\n]*";

        Pattern p = Pattern.compile(regex1 + setting + regex2);
        Matcher m = p.matcher(parsedSection);

        while (m.find()) {
            if (result == null) result = m.group(0);
        }
        if (result != null) {
            return result;
        }
        else {
            return defaultValue;
        } 
    }
    
    public static String setBooleanINIValue (String fileContents, String section, String setting, boolean value) {
        String stringValue;
        if (value == true) {
            stringValue = "true";
        }
        else {
            stringValue = "false";
        }
        return setINIValue(fileContents, section, setting, stringValue);
    }
    
    public static String setIntegerINIValue (String fileContents, String section, String setting, Integer value) {
        return setINIValue(fileContents, section, setting, value.toString());
    }
    
    public static String setDoubleINIValue (String fileContents, String section, String setting, Double value) {
        return setINIValue(fileContents, section, setting, value.toString());
    }   
    
    public static String setLongINIValue (String fileContents, String section, String setting, Long value) {
        return setINIValue(fileContents, section, setting, value.toString());
    }
    
    public static String setINIValue(String fileContents, String section, String setting, String value) {
        /**
         *  The way we save a file is as follows:
         *  - Feed a string into this method using the first parameter
         *  - This string is loaded to a variable named fileContents
         *  - Use a regex to retrieve the section names to an ArrayList
         *  - Use another regex to split the file into its sections
         *  - Query the first ArrayList for the specified section
         *  - If found, retrieve the requested section
         *  - If not found, create a new entry in the lists
         *  - Append the value to the requested section
         *  - Merge all sections and return a string with the new file contents
         * 
         */
        
        String newContents = "";

        // Retrieve the requested section
        String selectedSection = splitINIfile(fileContents, section);
        
        // Retrieve all INI section names
        String r = "((?i)\\[.*\\])";
        Pattern p = Pattern.compile(r);
        Matcher m = p.matcher(fileContents);
        // Add the results to an ArrayList so we can read it later
        ArrayList <String> iniSectionNames = new ArrayList<>();
        while (m.find()) {
            iniSectionNames.add(m.group().substring(1,m.group().length() -1));
        }
        
        // Retrieve all sections, including data
        ArrayList <String> allSections = new ArrayList<>();
        for (int i = 0; i < iniSectionNames.size(); i++) {
            if (iniSectionNames.get(i).contains("-")) {
                allSections.add(splitINIfile(fileContents, iniSectionNames.get(i).replace("-", "\\-")));
            }
            else {
                allSections.add(splitINIfile(fileContents, iniSectionNames.get(i)));
            }
        }
        
        // Query the first ArrayList for the specified section
        for (int i = 0; i < iniSectionNames.size(); i++) {           
            if (selectedSection == null) {
                // If not found, create a new section entry in the lists
                iniSectionNames.add(section);
                selectedSection = "\n[" + section + "]" + "\n" + setting + "=" + value + "\n";
                allSections.add(selectedSection);
                break;
            }
            else if ( ( iniSectionNames.get(i).equals(section) ) ) {
                // If found, retrieve the requested section
                selectedSection = selectedSection.trim() + "\n" + setting + "=" + value + "\n";
                allSections.set(i, selectedSection);
            }
        }
        
        // Merge all sections
        for (int i = 0; i < allSections.size(); i++) {
            newContents = newContents + allSections.get(i);
        }
        // Make it look nicer
        if (newContents.contains("\n" + "\n" + "[")) {
            // Do nothing
        }
        else if (newContents.contains("\n" + "[")) {
            newContents = newContents.replace("\n" + "[", "\n" + "\n" + "[");
        }
        
        // Return changes
        return newContents;
    }
    
    private static String splitINIfile(String fileContents, String section) {
        String selectedSection = null;
        // Extract the specified section from fileContents
        String r1 = "(?i)(\\[";
        String r2;
        /**
         * The regex here is different and returns three groups
         * 0 returns the whole file
         * 1 returns the selected section name
         * 2 returns the selected section's contents without the name
         */
        if (fileContents.contains("\r\n")) {
            r2 = "\\]\\r\\n)((?<=\\]\\r\\n)[^\\[]*)";
        }
        else {
            r2 = "\\]\\n)((?<=\\]\\n)[^\\[]*)";
        }
        Pattern p1 = Pattern.compile(r1 + section + r2, Pattern.MULTILINE);
        Matcher m1 = p1.matcher(fileContents);
        // Add the result to a string
        while (m1.find()) {
            selectedSection = m1.group();
        }
        // Return the string we created
        return selectedSection;
    }
    
}
