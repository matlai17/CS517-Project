
import MMR.MMR;
import MMR.MMR.Document_Score;
import Matrix.IDFMatrix;
import Parsing.Parser;
import SimMetrics.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
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
        Parser.populateStopwords(new File("src/Parsing/Stopwords.txt"));
        if(args.length == 0)
        {
            Parser p[] = new Parser[5];
            File [] paths = new File[5];
            paths[0] = new File("test/Documents/d132d"); //  Robert Rubin
            paths[1] = new File("test/Documents/d133c"); //  Stephen Hawking
            paths[2] = new File("test/Documents/d134h"); //  Desmond Tutu
            paths[3] = new File("test/Documents/d135g"); //  Brian Jones
            paths[4] = new File("test/Documents/d136c"); //  Gene Autry 

            for(int i = 0; i < p.length; i++)
            {
                File files[] = paths[i].listFiles();
                p[i] = new Parser();
                for(File f : files) p[i].addDocument(f);
            }

            MMR []topics = new MMR[paths.length];
            for(int i = 0; i < topics.length; i++)
                topics[i] = new MMR(new LexRankSim(p[i].getStemmedDocument(), new IDFMatrix(p[i].getStemmedDocument()), .2, .9), new CosineSim());
        
            Scanner io = new Scanner(System.in);
            String query;
            int fileNum = 0;
            double lambda = 1;
            String[] topicName = { "Robert Rubin", "Stephen Hawking", "Desmond Tutu", "Brian Jones", "Gene Autry" };
            System.out.print("Enter a query: ");
            while(!((query = io.nextLine()).equalsIgnoreCase("exit") || query.equalsIgnoreCase("quit")))
            {
                if(query.equalsIgnoreCase("$0")) fileNum = 0;
                else if(query.equalsIgnoreCase("$1")) fileNum = 1;
                else if(query.equalsIgnoreCase("$2")) fileNum = 2;
                else if(query.equalsIgnoreCase("$3")) fileNum = 3;
                else if(query.equalsIgnoreCase("$4")) fileNum = 4;
                else if(query.equalsIgnoreCase("$h")) System.out.println("$0 - Robert Rubin\n$1 - Stephen Hawking\n$2 - Desmond Tutu\n$3 - Brian Jones\n$4 - Gene Autry\n");
                else if(query.equalsIgnoreCase("$l")) { System.out.print("New Lambda Value: "); lambda = io.nextDouble(); }
                
                else
                {
                    List<Document_Score> results = topics[fileNum].rankedAndList(p[fileNum].getStemmedDocument(), Parser.vectorAndStem(query), lambda, 10);
                    System.out.println("Topic: " + topicName[fileNum]);
                    System.out.println("Query: " + query + "\n");
                    for(Document_Score sentence : results) System.out.println(sentence.getScore() + "\t" + p[fileNum].getSentence(sentence.getDocument()) + "\n");
                }
                System.out.print("Enter a query: ");
            }
        }
        
        if(args.length > 0)
        {
            for (int i = 0; i < args.length; i++) {
                String fileName = args[i];
                
                Scanner io = new Scanner(System.in);
                System.out.println();
                Parser p2 = new Parser(new File("test/" + fileName));
                MMR fMMR = new MMR(new LexRankSim(p2.getStemmedDocument(), new IDFMatrix(p2.getStemmedDocument()), .2, .9), new CosineSim());
                String query = "";

                System.out.print("Enter a query: ");
                while(!((query = io.nextLine()).equalsIgnoreCase("exit") || query.equalsIgnoreCase("quit")))
                {
                    List<Document_Score> results = fMMR.rankedAndList(p2.getStemmedDocument(), Parser.vectorAndStem(query), .9, 10);
                    System.out.println("Topic: " + fileName);
                    System.out.println("Query: " + query + "\n");
                    for(Document_Score sentence : results) System.out.println(sentence.getScore() + "\t" + p2.getSentence(sentence.getDocument()) + "\n");
                    System.out.print("Enter a query: ");
                }
            }
        }
        
    }
    
}
