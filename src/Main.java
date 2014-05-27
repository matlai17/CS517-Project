
import MMR.MMR;
import Matrix.IDFMatrix;
import Parsing.Parser;
import SimMetrics.LexRankSim;
import SimMetrics.SimMetric;
import java.io.File;
import java.io.IOException;
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
        
        File f = new File("Philo.txt");
        Parser p = new Parser(f);
        
        String query = "thought method";
        System.out.println("REACHED1");
//        mmr.startGeneratingScores(p.getStemmedDocument(), 7);
//        Thread.sleep(10000);
        System.out.println("REACHED2");
        
        MMR mmr = new MMR(SimMetric.COSINE_IDF_SIM, SimMetric.JACCARD_SIM, new Object[]{new IDFMatrix(p.getStemmedDocument())});
        
        long time = System.currentTimeMillis();
        List<List<String>> results = mmr.rankedList(p.getStemmedDocument(), Parser.vectorAndStem(query), .8, 10);
        System.out.println("Time Taken (ms): " + (System.currentTimeMillis() - time));
//        LexRankSim lRS = new LexRankSim(p.getStemmedDocument(), .2);
//        System.out.println(lRS.retSize());
        
        for(List<String> resultSent : results)
            System.out.println(p.getSentence(resultSent));
        
        
    }
    
}
