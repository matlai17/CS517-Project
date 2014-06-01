/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Parsing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.regex.Pattern;
import Parsing.Stemmer;

public class Parser {
    
    HashMap<List<String>, String> stemMap;
    List<List<String>> stemList;
    
    public Parser(File f) throws FileNotFoundException, IOException
    {
        String document = "";
        stemMap = new HashMap<>();
        stemList = new ArrayList<>();
        
//        Scanner io = new Scanner(f);
        BufferedReader io = new BufferedReader(new FileReader(f));
        String line;
        boolean markUpFile = false, captureText = false;
        while((line = io.readLine()) != null)
        {
            if(line.startsWith("<DOC>")) markUpFile = true;
            if(markUpFile && line.startsWith("<TEXT>"))
            {
                captureText = true;
                continue;
            }
            if(markUpFile && line.startsWith("</TEXT>")) captureText = false;
            if(!captureText) continue;
            document += line + " ";
//            String line = io.nextLine();
//            System.out.println(line);
//            line = line.replaceAll("[^\\n\\w. ]", "");
//            String [] sentTokens = line.split("(?=.*?\\w{3,})(\\. |\\.$)");
            if(line.equals("\n"))
            {
                document = document.trim();
                for(String sentence : document.split("(?<=\\w[\\w\\)\\]\"](?<!Mrs?|Dr|Rev|Mr|Ms|vs|abd|ABD|Abd|resp|St|wt)[\\.\\?\\!\\:\\@]\\s)"))
                {
                    if(sentence.length() < 1) continue;
                    if(sentence.charAt(0) == ' ') sentence = sentence.substring(1);
                    ArrayList<String> sentVect = new ArrayList<String>();
                    ArrayList<String> stemmedSentVect = new ArrayList<String>();
                    for(String word : sentence.replaceAll("[^\\w ]", "").split("\\s")) 
                    {
                        if(word.replaceAll("\\s\n", "").length() < 1) continue;
                        word = word.replaceAll("[^\\w]", "");
                        sentVect.add(word);
                        stemmedSentVect.add(Stemmer.stemWord(word));
                    }
                    if(stemmedSentVect.size() > 0)
                    {
                        stemList.add(stemmedSentVect);
                        stemMap.put(stemmedSentVect, sentence.trim());
                    }
                }
                document = "";
            }
        }
        document = document.trim();
        for(String sentence : document.split("(?<=\\w[\\w\\)\\]\"](?<!Mrs?|Dr|Rev|Mr|Ms|vs|abd|ABD|Abd|resp|St|wt)[\\.\\?\\!\\:\\@]\\s)"))
        {
            sentence = sentence.trim();
            if(sentence.length() < 1) continue;
            ArrayList<String> sentVect = new ArrayList<String>();
            ArrayList<String> stemmedSentVect = new ArrayList<String>();
            for(String word : sentence.replaceAll("[^\\w ]", "").split("\\s")) 
            {
                if(word.replaceAll("\\s\n", "").length() < 1) continue;
                word = word.replaceAll("[^\\w]", "");
                sentVect.add(word);
                stemmedSentVect.add(Stemmer.stemWord(word));
            }
            if(stemmedSentVect.size() > 0)
            {
                stemList.add(stemmedSentVect);
                stemMap.put(stemmedSentVect, sentence.trim());
            }
        }
    }
    
    public static List<String> vectorAndStem(String sentence)
    {
        
        ArrayList<String> sentVect = new ArrayList<String>();
        ArrayList<String> stemmedSentVect = new ArrayList<String>();
        for(String word : sentence.replaceAll("[^\\w ]", "").split("\\s")) 
        {
            word = word.replaceAll("[^\\w]", "");
            sentVect.add(word);
            stemmedSentVect.add(Stemmer.stemWord(word));
        }
        
        
        return stemmedSentVect;
    }
    
    public List<List<String>> getStemmedDocument()
    {
        return stemList;
    }
    
    public String getSentence(List<String> stemmedVector)
    {
        return stemMap.get(stemmedVector);
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        
//        String test = "Testing 1. Testing 2. Testing 3. Testing 4.";
//        for(String i : test.split("(\\.\\s)|(\\.$)"))
//        {
//            System.out.println("'" + i + "'");
//            if(i.charAt(0) == ' ') i = i.substring(1);
//            for(String j : i.split("\\s"))
//                System.out.println("\t'" + j + "'");
//        }
        
//        String test = "A Markov chain is a stochastic process with the Markov property. The term "
//                + "\"Markov chain\" refers to the sequence of random variables such a process moves "
//                + "through, with the Markov property defining serial dependence only between adjacent "
//                + "periods (as in a \"chain\"). It can thus be used for describing systems that follow "
//                + "a chain of linked events, where what happens next depends only on the current state "
//                + "of the system.";
//        String test = "This is a test sentence. Test sentence two.";
//        test = test.replaceAll("[^\\w ]", "");
//        for(String s : test.split(" ")) System.out.println(Stemmer.stemWord(s));
        
        Parser p = new Parser(new File("TestFile.txt"));
        List<List<String>> document = p.getStemmedDocument();
        
        for(List<String> sentence : document)
        {
            System.out.println("\n");
            for(String s : sentence) System.out.print(s + " ");
            System.out.println("\n" + p.getSentence(sentence));
            System.out.println("\n");
        }
        
        System.exit(0);
        
        // TODO code application logic here
        FileReader fr = new FileReader("grenade.txt");
        BufferedReader br = new BufferedReader(fr);
        String s;
        Stemmer stemmer = new Stemmer();
        //Output gets stored in output.txt  
        BufferedWriter out = new BufferedWriter(new FileWriter("output.txt"));
        String token[];
        while ((s = br.readLine()) != null) {
            //Delimiter considered is . and compared using pattern matching technique  
            Pattern pat1 = Pattern.compile("[\n.]");
            token = pat1.split(s);
            for (int i = 0; i < token.length; i++) {
                byte buf[] = token[i].getBytes();
                // Writing into a file with a new line character indicating beginning of a new line after every sentence  
                for (int j = 0; j < buf.length; j = j + 1) {
                    out.write(buf[j]);
                    if (j == buf.length - 1) {
                        //out.write(".");
                        out.newLine();
                    }
                }
            }
        }
        out.close();
        fr.close();
        List<List<String>> sentences = new ArrayList<List<String>>();
        List<String> sentence = new ArrayList<String>();

        try {
            File file = new File("output.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            String a;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                
                String[] tokens = line.replaceAll("^[,\\s]+","").split("[,\\s]+");
                for (String temp : tokens) {
                    a = stemmer.stemming(temp);
                    sentence.add(a);
                }
                sentences.add(sentence);
                
            }
            System.out.println(sentence);
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
