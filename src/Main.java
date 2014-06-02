
import MMR.MMR;
import Matrix.IDFMatrix;
import Parsing.Parser;
import SimMetrics.CosineSim;
import SimMetrics.JaccardSim;
import SimMetrics.LexRankSim;
import SimMetrics.SimMetric;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Matthew Lai
 */
public class Main {
    
    public static void main(String[] args) throws IOException, InterruptedException {
        
        Parser p = new Parser();
        File [] paths = new File[5];
        paths[0] = new File("test/Documents/d132d"); //  Robert Rubin
        paths[1] = new File("test/Documents/d133c"); //  Stephen Hawking
        paths[2] = new File("test/Documents/d134h"); //  Desmond Tutu
        paths[3] = new File("test/Documents/d135g"); //  Brian Jones
        paths[4] = new File("test/Documents/d136c"); //  Gene Autry 
        
        File files[] = paths[0].listFiles();
        for(File f : files) p.addDocument(f);
        
        MMR mmr = new MMR(new LexRankSim(p.getStemmedDocument(), new IDFMatrix(p.getStemmedDocument()), .2, .9), new CosineSim());
        
        String query = "who is rubin";
        List<List<String>> results = mmr.rankedList(p.getStemmedDocument(), Parser.vectorAndStem(query), 1, 10);
        for(List<String> sentence : results)
        {
            System.out.println(p.getSentence(sentence) + "\n");
        }
        
        System.out.println("\n\n");
        
        query = "what are rubin's accomplishments";
        results = mmr.rankedList(p.getStemmedDocument(), Parser.vectorAndStem(query), 1, 10);
        for(List<String> sentence : results)
        {
            System.out.println(p.getSentence(sentence) + "\n");
        }
        
    }
    
}
