/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import SimMetrics.CosineSim;
import SimMetrics.SimMetric;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.HashSet;

/**
 *
 * @author Matthew Lai
 */
public class JaccardSim implements SimMetric{

    @Override
    public double documentRank(List<String> document, List<String> query) {
        
        if(document.isEmpty() && query.isEmpty()) return 1;
        if(document.isEmpty() && !query.isEmpty()) return 0;
        if(query.isEmpty() && !document.isEmpty()) return 0;
        
        HashSet<String> union = new HashSet<>();
        HashSet<String> intersection = new HashSet<>();
        
        for(String s : document)
        {
            union.add(s);
            if(query.contains(s)) intersection.add(s);
        }
        
        for(String s : query) union.add(s);
        
        return (double)intersection.size() / union.size();
    }

    @Override
    public SortedMap<List<String>, Double> orderedDocuments(List<List<String>> documentList, List<String> query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static void main(String[] args) {
        ArrayList<String> document = new ArrayList<>();
        ArrayList<String> query = new ArrayList<>();
        
        document.add("I");
        document.add("like");
        document.add("to");
        document.add("eat");
        document.add("pie");
        document.add("for");
        document.add("breakfast");
        document.add("lunch");
        document.add("and");
        document.add("dinner");
        
// This block of query statement should get near 1        
//        query.add("I");
//        query.add("like");
//        query.add("to");
//        query.add("eat");
//        query.add("pie");
//        query.add("for");
//        query.add("breakfast");
//        query.add("lunch");
//        query.add("and");
//        query.add("dinner");

// This block of query statement should get higher than 0
        query.add("can");
        query.add("I");
        query.add("get");
        query.add("pie");
        query.add("for");
        query.add("dinner");

// This block of query statement should get higher than the one above       
//        query.add("like");
//        query.add("pie");
//        query.add("breakfast");
//        query.add("lunch");
//        query.add("dinner");
        
// This block of query statement should get lower than the one above       
//        query.add("john");        
//        query.add("like");
//        query.add("pie");
//        query.add("breakfast");
//        query.add("lunch");
//        query.add("dinner");

// This block of query statement should get 0        
//        query.add("whats");
//        query.add("up");
//        query.add("doc");
        
        System.out.println(new JaccardSim().documentRank(document, query));
    }
    
    
    
}
