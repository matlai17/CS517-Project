
import MMR.MMR;
import MMR.MMR.Document_Score;
import Matrix.IDFMatrix;
import Parsing.Parser;
import SimMetrics.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * This is the Main class that was programmed specifically to receive specific test
 * data and process it through MMR to get a summary back. 
 * 
 * Apologies for the very messy code. I was trying to get the the code up and running to get the
 * results as quickly as possible and just slung right through it to get something usable.
 * 
 * It implements runnable so that it can execute as quickly as possible because it was 
 * quite slow to start. Because each document is processed individually (and even some
 * MMR and LexRank parameters are processed individually) this made this program a prime 
 * target for parallelization.
 * 
 * I would not use this Main method for any further work as it is very specific for the task
 * that we needed it for. You can base any new Main class off of the initializations present
 * in this class.
 * 
 * @author Matthew Lai
 */
public class Main implements Runnable{
    
    private class ThreadTracker
    {
        AtomicInteger topicIndex;
        AtomicInteger simThreshold;
        AtomicInteger queryBias;
        public ThreadTracker(int maxThreads) throws IOException
        {
            topicIndex = new AtomicInteger(0);
            simThreshold = new AtomicInteger(0);
            queryBias = new AtomicInteger(6);
            for(int i = 0; i < maxThreads; i++) threadCreator();
        }
        
        public void threadCreator() throws IOException
        {
            int localI;
            int localJ;
            int localK;
            if((localK = queryBias.incrementAndGet()) > 10)
            {
                simThreshold.incrementAndGet();
                queryBias.set(0);
                localK = 0;
            }
            if((localJ = simThreshold.get()) > 4)
            {
                topicIndex.incrementAndGet();
                simThreshold.set(0);
                localJ = 0;
            }
            if((localI = topicIndex.get()) < 5)
            {
                if(topicIndex.get() > 0) return;
                (new Thread(new Main(localI, ((double)localJ)/10.0, ((double)localK)/10.0, this))).start();
            }
        }
    }
    
    int topicNumber;
    double simThresholdNumber, queryBiasNumber;
    Thread thisThread;
    ThreadTracker parentTracker;
    
    static final String outputLocation = "C:\\Users\\Matthew Lai\\Documents\\Work\\Graduate Work\\2014 Spring\\CS517\\MMR Output\\"; 
    static final String topicNames[] = {"Robert Rubin", "Stephen Hawking", "Desmond Tutu", "Brian Jones", "Gene Autry"};
    
    final int resultNum = 10;
    Parser p[] = new Parser[5];
    static File [] paths = new File[5];
    List<List<String>>[] queries = new List[5];
    
    
    public Main(int tN, double sTN, double qBN, ThreadTracker tt) throws IOException
    {
        parentTracker = tt;
        thisThread = new Thread(this);
        queryBiasNumber = qBN;
        topicNumber = tN;
        simThresholdNumber = sTN;
        parserAndQueryInitializer();
    }
    
    public Main(int threads) throws IOException
    {
        new ThreadTracker(threads);
    }

    @Override
    public void run() {
        try {
            baseCaseResultPrintThread(topicNumber, simThresholdNumber, queryBiasNumber);
        } catch (IOException ex) {
            System.out.println("File Not Found.");
        }
    }
    
    private void parserAndQueryInitializer() throws FileNotFoundException, IOException
    {
        for(int i = 0; i < p.length; i++)
        {
            queries[i] = new ArrayList<>();
            File files[] = paths[i].listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) { return !file.isDirectory(); }
            });
            p[i] = new Parser();
            for(File f : files) p[i].addDocument(f);

            File tF = new File(paths[i].getCanonicalPath() + "\\Queries");
            files = tF.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) { return !file.isDirectory(); }
            });
            for(File f : files)
            {
                if(!paths[i].getName().regionMatches(0, f.getName(), 0, 4)) 
                {
                    System.out.println("Topic Directory Name and Query File Name Do Not Match");
                    continue;
                }
                Scanner io = new Scanner(f);
                while(io.hasNextLine()) 
                {
                    String line = io.nextLine();
                    if(!line.startsWith("q")) throw new Error("Query File Not properly formated");
                    int fSpace = line.indexOf(" ");
                    queries[i].add(Parser.vectorAndStem(line.substring(fSpace+1)));
                }
            }
        }
    }
    
    public static void main(String[] args) throws IOException, InterruptedException {
        
        paths[0] = new File("test/Documents/d132d"); //  Robert Rubin
        paths[1] = new File("test/Documents/d133c"); //  Stephen Hawking
        paths[2] = new File("test/Documents/d134h"); //  Desmond Tutu
        paths[3] = new File("test/Documents/d135g"); //  Brian Jones
        paths[4] = new File("test/Documents/d136c"); //  Gene Autry 
        
        Parser.populateStopwords(new File("src/Parsing/Stopwords.txt"));
        if(args.length == 0)
        {
//            baseCaseResultPrint();
//            for(int i = 0; i < 5; i++) 
//            {
//                for(double j = 0; j <= 0.4; j+=0.1)
//                {
//                    for( double k = 0.7; k <= 1; k+= .1)
//                        (new Thread(new Main(i, j, k))).start();
//                }
//            }
            new Main(6);
//            baseCaseLoopQuery();
            
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
                    List<Document_Score> results = fMMR.rankedAndList(p2.getStemmedDocument(), Parser.vectorAndStem(query), .9, .2, 10);
                    System.out.println("Topic: " + fileName);
                    System.out.println("Query: " + query + "\n");
                    for(Document_Score sentence : results) System.out.println(sentence.getScore() + "\t" + p2.getSentence(sentence.getDocument()) + "\n");
                    System.out.print("Enter a query: ");
                }
            }
        }
        
    }
    
    private void baseCaseResultPrintThread(int topicIndex, double simThresh, double queryBias) throws IOException
    {
        
//Similarity Threshold, Query Bias, MMR Bias
                
//    for(int topicIndex = 0; topicIndex < paths.length; topicIndex++) {      // Document Cycle Level Begin
        
//        for (double simThresh = 0; simThresh <= 0.4; simThresh+= 0.1) {                     // Similarity Threshold Level Begin
            
//            for (double queryBias = 0.7; queryBias <= 1; queryBias += 0.1) {                // Query Bias Level Begin
                
                    MMR mmr1 = new MMR(new LexRankSim(p[topicIndex].getStemmedDocument(), new IDFMatrix(p[topicIndex].getStemmedDocument()), simThresh, queryBias), new CosineSim());;
                    MMR mmr2 = new MMR(new CosineSim(), new CosineSim());
                    
                    for (double MMRBias = 0.3; MMRBias <= 1; MMRBias += 0.1) {                    // MMR Bias Level Begin
                        
                        List<List<String>> results;
                        PrintWriter outputFile1;
                        PrintWriter outputFile2;
                        String pathName = outputLocation + paths[topicIndex].getName()+"\\SYSTEM";
                        File newDir = new File(pathName);
                        newDir.mkdirs();
                        for(int queryNum = 0; queryNum < queries[topicIndex].size(); queryNum++) {    // Query Cycle Level Begin
                            
                            System.out.println("Now creating results for topic: " + topicNames[topicIndex] + " using query #" + (queryNum+1) + " using LexRank-Cosine Document Reranking with parameters:"); //"ST-"+ String.format("%.1f", simThresh) + "_qB-" + String.format("%.1f", queryBias) +"_MMRB-"+String.format("%.1f", MMRBias)
                            System.out.format("LexRank Similarity Threshold of %.1f\nLexRank Query Bias of %.1f\nMMR Bias of %.1f\n\n", simThresh, queryBias, MMRBias);
                            outputFile1 = new PrintWriter(new BufferedWriter(new FileWriter(new File(newDir.getCanonicalPath() + "\\" + paths[topicIndex].getName() + "_q" + (queryNum + 1) + ".LexRank-Cosine" + "ST-"+ String.format("%.1f", simThresh) + "_qB-" + String.format("%.1f", queryBias) +"_MMRB-"+String.format("%.1f", MMRBias) + ".system" ))));
                            results = mmr1.rankedList(p[topicIndex].getStemmedDocument(), queries[topicIndex].get(queryNum), MMRBias, simThresh, resultNum);
                            for(List<String> sentence : results) outputFile1.print(p[topicIndex].getSentence(sentence) + "\n");
                            outputFile1.close();
                            
                            if(topicIndex == 0 && simThresh == 0 && queryBias == .7)
                            {
                                System.out.println("Now creating results for topic: " + topicNames[topicIndex] + " using query #" + (queryNum+1) + " using Cosine-Cosine Document Reranking with parameters:");
                                System.out.format("MMR Bias of %.1f\n\n", MMRBias);
                                outputFile2 = new PrintWriter(new BufferedWriter(new FileWriter(new File(outputLocation + "\\" + paths[topicIndex].getName() + "_q" + (queryNum + 1) + ".Cosine-Cosine_"+String.format("%.1f",MMRBias)+"MMRBias"))));
                                results = mmr2.rankedList(p[topicIndex].getStemmedDocument(), queries[topicIndex].get(queryNum), MMRBias, simThresh, resultNum);
                                for(List<String> sentence : results) outputFile2.print(p[topicIndex].getSentence(sentence) + "\n");
                                outputFile2.close();
                            }
                        }                                                                   // Query Cycle Level End
                    }                                                                       // Document Cycle Level End
//                }                                                                           // MMR Bias Level End
//            }                                                                               // Query Bias Level End
//        }                                                                                   // Similarity Threshold Level End
            parentTracker.threadCreator();
    }
    
    /**
     * Creates MMR based on hard coded documents and loops to ask the user for a query
     * Designed for demonstration.
     * 
     * @throws IOException 
     */
    private static void baseCaseLoopQuery() throws IOException {
        Parser p[] = new Parser[5];
        File [] paths = new File[5];
        paths[0] = new File("test/Documents/d132d"); //  Robert Rubin
        paths[1] = new File("test/Documents/d133c"); //  Stephen Hawking
        paths[2] = new File("test/Documents/d134h"); //  Desmond Tutu
        paths[3] = new File("test/Documents/d135g"); //  Brian Jones
        paths[4] = new File("test/Documents/d136c"); //  Gene Autry 

        for(int i = 0; i < p.length; i++)
        {
            File files[] = paths[i].listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) { return !pathname.isDirectory(); }
            });
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
                List<Document_Score> results = topics[fileNum].rankedAndList(p[fileNum].getStemmedDocument(), Parser.vectorAndStem(query), lambda, .2, 10);
                System.out.println("Topic: " + topicName[fileNum]);
                System.out.println("Query: " + query + "\n");
                for(Document_Score sentence : results) System.out.println(sentence.getScore() + "\t" + p[fileNum].getSentence(sentence.getDocument()) + "\n");
            }
            System.out.print("Enter a query: ");
        }
    }
    
}
