package MMR;


import Matrix.IDFMatrix;
import SimMetrics.CosineSim;
import SimMetrics.JaccardSim;
import SimMetrics.LexRankSim;
import SimMetrics.SimMetric;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Multi-Document ranking and summarization program the implements the algorithm set
 * forth in the published report "The Use of MMR, Diversity-Based Reranking for Reordering
 * Documents and Producing Summaries". Allows ranking of documents, not only by their 
 * relevance to a query, but by their marginality to other relevant documents.
 * 
 * To add a new Similarity Metric, assign it a unique integer identification
 * and name as a "static final int" variable. This new identification does not 
 * needed to be added the the SimMetric interface file. Then add the new metric to the 
 * second constructor in the Switch-Case using the new similarity metric's 
 * identification as the case statement. Then simply initialize the MMR class
 * using the new metric's identification.
 * 
 * @author Matthew Lai
 */
public class MMR {
    
    private class GenerateScores implements Runnable
    {
        MMR parent;
        GenerateScores thisParent;
        List<List<String>> documents;
        AtomicInteger loopIndex, index;
        boolean isMaster, kill;
        Thread [] threadList;
        public GenerateScores(MMR p, List<List<String>> docs, boolean m, GenerateScores tP, int numP)
        {
            threadList = new Thread[numP];
            index = new AtomicInteger(0);
            loopIndex = new AtomicInteger(0);
            parent = p;
            documents = docs;
            isMaster = m;
            kill = false;
            thisParent = tP;
        }
        
        public GenerateScores(MMR p, List<List<String>> docs, int numberOfProcessors)
        {
            threadList = new Thread[numberOfProcessors];
            index = new AtomicInteger(0);
            loopIndex = new AtomicInteger(0);
            parent = p;
            documents = docs;
            isMaster = true;
            kill = false;
            thisParent = null;
        }
        
        public void killSwitch() { kill = true; }
        
        public DoubleDoc getNewJob()
        {
            if(index.get() >= documents.size()) return null;
            int lI = loopIndex.getAndIncrement();
            if(lI >= documents.size()) 
            {
                loopIndex.set(0);
                index.incrementAndGet();
                lI = loopIndex.getAndIncrement();
            }
            int i = index.get();
            if(i >= documents.size()) return null;
            return new DoubleDoc(documents.get(i), documents.get(lI), -1);
        }
        
        public void calculateScore(List<String> doc1, List<String> doc2)
        {
            parent.getSim1(doc1, doc2);
            parent.getSim2(doc1, doc2);
        }
        
        @Override
        public void run() {
            
            if(isMaster)
            {
                for(int i = 0; i < threadList.length; i++)
                {
                    threadList[i] = new Thread(new GenerateScores(parent, documents, false, this, 0));
                    threadList[i].start();
                }
//                System.out.println("Threads Started.");
                for(int i = 0; i < threadList.length; i++)
                {
                    try {
//                        System.out.println("Waiting on Thread " + i);
                        threadList[i].join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MMR.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            else
            {
                DoubleDoc job;
                while((job = thisParent.getNewJob()) != null) calculateScore(job.doc1, job.doc2);
            }
            
        }
        
    }
    
    /**
     * Document_Score is a private class to pass and store the tuple of a document
     * and its MMR score.
     */
    private class Document_Score
    {
        private final List<String> document;
        private final double score;
        public Document_Score(List<String> document, double score)
        {
            this.document = document;
            this.score = score;
        }
        public List<String> getDocument() { return document; }
        public double getScore() { return score; }
    }
    
    
    /**
     * DoubleDoc is a private class that acts as a Document and Query pair for use
     * in a HashMap.
     */
    private class DoubleDoc
    {
        final List<String> doc1;
        final List<String> doc2;
        final int simType;
        public DoubleDoc(List<String> d1, List<String> d2, int sT)
        {
            doc1 = d1;
            doc2 = d2;
            simType = sT;
        }
        
        @Override
        public int hashCode()
        {
            return (37 * (37 * simType + doc1.hashCode()) + doc2.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DoubleDoc other = (DoubleDoc) obj;
//            if (!Objects.equals(this.doc1, other.doc1)) {
//                return false;
//            }
            return (Objects.equals(this.doc2, other.doc2) && Objects.equals(this.doc1, other.doc1))
                    || (Objects.equals(this.doc1, other.doc2) && Objects.equals(this.doc2, other.doc1))
                    && Objects.equals(this.simType, other.simType);
        }
        
        public List<String> getDoc1() { return doc1; }
        public List<String> getDoc2() { return doc2; }
        
        
    }
    
    
//<editor-fold defaultstate="collapsed" desc="Declarations">
    /**
     * ConcurrentHashMap for use in concurrently generating scores.
     */
    private ConcurrentHashMap<DoubleDoc, Double> scores;
    int sim1Type;
    int sim2Type;
    SimMetric sim1;
    SimMetric sim2;
    static final int LEXRANK_SIM = SimMetric.LEXRANK_SIM;
    static final int COSINE_SIM = SimMetric.COSINE_SIM;
    static final int JACCARD_SIM = SimMetric.JACCARD_SIM;
    static final int PSEUDO_CODE_SIM = SimMetric.PSEUDO_CODE_SIM;
    static final int COSINE_IDF_SIM = SimMetric.PSEUDO_CODE_SIM+1;
    GenerateScores master;
//</editor-fold>
    
    public MMR()
    {
        scores = new ConcurrentHashMap<>();
        sim1 = new JaccardSim();
        sim2 = new JaccardSim();
    }
    
    public MMR(int sim1Type, int sim2Type)
    {
        this.sim1Type = sim1Type;
        this.sim2Type = sim2Type;
        scores = new ConcurrentHashMap<>();
        sim1 = new CosineSim();
        sim2 = new CosineSim(); 
        
        switch(sim1Type) {
            case COSINE_SIM: sim1 = new CosineSim(); 
                break;
            case LEXRANK_SIM: throw new Error("Must use constructer with an object array. "
                    + "Index 0 must contain a List<List<String>> of the document and "
                    + "Index 1 must contain the Cosine Similarity Threshold with value between 0 and 1.");
            case JACCARD_SIM: sim1 = new JaccardSim();
                break;
            case PSEUDO_CODE_SIM: 
                break;
            case COSINE_IDF_SIM: throw new Error("Must use constructer with an object array. "
                    + "Index 0 must contain an IDFMatrix.");
        }
        
        switch(sim2Type) {
            case COSINE_SIM: sim2 = new CosineSim(); 
                break;
            case LEXRANK_SIM: throw new Error("Must use constructer with an object array. "
                    + "Index 0 must contain a List<List<String>> of the document and "
                    + "Index 1 must contain the Cosine Similarity Threshold with value between 0 and 1.");
            case JACCARD_SIM: sim2 = new JaccardSim();
                break;
            case PSEUDO_CODE_SIM: 
                break;
            case COSINE_IDF_SIM: throw new Error("Must use constructer with an object array. "
                    + "Index 0 must contain an IDFMatrix.");
                
        }
    }
    
    public MMR(int sim1Type, int sim2Type, Object[] initializers)
    {
        this.sim1Type = sim1Type;
        this.sim2Type = sim2Type;
        scores = new ConcurrentHashMap<>();
        sim1 = new CosineSim();
        sim2 = new CosineSim(); 
        
        switch(sim1Type) {
            case COSINE_SIM: sim1 = new CosineSim(); 
                break;
            case LEXRANK_SIM: 
                if(initializers.length != 2 || !((initializers[0] instanceof List) 
                        && (((List)initializers[0]).get(0) instanceof List) 
                        && ((((List)((List)initializers[0]).get(0)).get(0)) instanceof String)) 
                        || !(initializers[1] instanceof Double))
                    throw new Error("Must use constructer with an object array. "
                    + "Index 0 must contain a List<List<String>> of the document and "
                    + "Index 1 must contain the Cosine Similarity Threshold with value between 0 and 1.");
                sim1 = new LexRankSim((List<List<String>>) initializers[0], (Double)initializers[1]);
                break;
            case JACCARD_SIM: sim1 = new JaccardSim();
                break;
            case PSEUDO_CODE_SIM: 
                break;
            case COSINE_IDF_SIM: 
                if(initializers.length != 1 || !(initializers[0] instanceof IDFMatrix))
                        throw new Error("Must use constructer with an object array. "
                        + "Index 0 must contain a List<List<String>> of the document and "
                        + "Index 1 must contain the Cosine Similarity Threshold with value between 0 and 1.");
                    sim1 = new CosineSim((IDFMatrix)initializers[0]);
                break;
        }
        
        switch(sim2Type) {
            case COSINE_SIM: sim2 = new CosineSim(); 
                break;
            case LEXRANK_SIM: 
                if(initializers.length != 2 || !((initializers[0] instanceof List) 
                        && (((List)initializers[0]).get(0) instanceof List) 
                        && ((((List)((List)initializers[0]).get(0)).get(0)) instanceof String)) 
                        || !(initializers[1] instanceof Double))
                    throw new Error("Must use constructer with an object array. "
                    + "Index 0 must contain a List<List<String>> of the document and "
                    + "Index 1 must contain the Cosine Similarity Threshold with value between 0 and 1.");
                sim1 = new LexRankSim((List<List<String>>) initializers[0], (Double)initializers[1]);
                break;
            case JACCARD_SIM: sim2 = new JaccardSim();
                break;
            case PSEUDO_CODE_SIM: 
                break;
            case COSINE_IDF_SIM: if(initializers.length != 1 || !(initializers[0] instanceof IDFMatrix))
                        throw new Error("Must use constructer with an object array. "
                        + "Index 0 must contain a List<List<String>> of the document and "
                        + "Index 1 must contain the Cosine Similarity Threshold with value between 0 and 1.");
                sim1 = new CosineSim((IDFMatrix)initializers[0]);
                break;
        }
    }
    
    /**
     * Returns a ranked list given the a list of documents, a query, and a lambda value.
     * The list will be ranked according to the lambda value. A lambda value of 0 will
     * return a list ranked more on diversity while a lambda value of 1 will return a 
     * standard relevance ranked list. It is recommended the lambda value be between 
     * 0 and 1.
     * 
     * @param documentList Documents stored in a list as a list of Strings, representing its words
     * @param query A query represented as a list of Strings, representing query words
     * @param lambda The lambda value to determine MMR search ranking
     * @param maxResults The maximum size of the returned resulting ranked list.
     * @return The list of ranked sentences, ranked according to the lambda value
     */
    public List<List<String>> rankedList(List<List<String>> documentList, List<String> query, double lambda, int maxResults)
    {
        TreeSet<Document_Score> sList = new TreeSet<>(new Comparator<Document_Score>() {
            @Override
            public int compare(Document_Score o1, Document_Score o2) {
                if(o1.getScore() == o2.getScore()) return 1;
                return -Double.compare(o1.getScore(), o2.getScore());
            }
        });
        ArrayList<List<String>> r_sList = new ArrayList<>(documentList);
        while(!r_sList.isEmpty())
        {
//            System.out.println(r_sList.size());
            Document_Score retrievedDocument = retrieveDocument(r_sList, sList, query, lambda);
            sList.add(retrievedDocument);
            r_sList.remove(retrievedDocument.getDocument());
        }
        
        ArrayList<List<String>> rankedList = new ArrayList<>();
        for(Document_Score ds : sList) 
        {
            rankedList.add(ds.getDocument());
            if(rankedList.size() >= maxResults) break;
        }
        
        return rankedList;
    }
    
    /**
     * Private method that returns a singe document that has the maximum score based
     * on the its relevance to the query and its marginality compared to the 
     * sentences already retrieved.
     * 
     * @param unselectedDocuments A list of documents that will be considered for selection
     * @param selectedDocuments The list of documents already selected
     * @param query The query against which the list of documents will be compared
     * @param lambda The value which will determine relevance versus marginality
     * @return A Document and Score tuple of the highest ranking sentence
     */
    private Document_Score retrieveDocument(List<List<String>> unselectedDocuments, TreeSet<Document_Score> selectedDocuments, List<String> query, double lambda)
    {
        List<String> bestDocument = unselectedDocuments.get(0);
        double maxScore = 0;
        for(List<String> document : unselectedDocuments)
        {
            double score = lambda * (getSim1(document, query) - ((1 - lambda) * maxSim2(document, selectedDocuments)));
            if(score > maxScore) 
            {
                maxScore = score;
                bestDocument = document;
            }
        }
        return new Document_Score(bestDocument, maxScore);
    }
    
    /**
     * Method to retrieve the similarity score between documents if it exists in the HashMap. 
     * If it does not exist in the HashMap, it calculates the similarity between the two
     * parameters, adds it to the HashMap, and returns the value;
     * 
     * @param document One of the two vectors to retrieve the similarity score for
     * @param query The other of the two vectors to retrieve the similarity score for.
     * @return The similarity score
     */
    private double getSim1(List<String> document, List<String> query)
    {
        Double score = scores.get(new DoubleDoc(document, query, sim1Type));
        if(score != null) return score;
        Double newScore = sim1.documentRank(document, query);
        scores.putIfAbsent(new DoubleDoc(document, query, sim1Type), newScore);
        return newScore;
    }
    
    /**
     * Method to retrieve the similarity score between documents if it exists in the HashMap. 
     * If it does not exist in the HashMap, it calculates the similarity between the two
     * parameters, adds it to the HashMap, and returns the value;
     * 
     * @param document1 One of the two vectors to retrieve the similarity score for
     * @param document2 The other of the two vectors to retrieve the similarity score for.
     * @return The similarity score
     */
    private double getSim2(List<String> document1, List<String> document2)
    {
        Double score = scores.get(new DoubleDoc(document1, document2, sim2Type));
        if(score != null) return score;
        Double newScore = sim2.documentRank(document1, document2);
        scores.putIfAbsent(new DoubleDoc(document1, document2, sim2Type), newScore);
        return newScore;
    }
    
    
    /**
     * Private method that is used by retrieveDocument to iterate through the selected
     * documents and returns the score for the document with the greatest similarity
     * to the considered document
     * 
     * @param unselectedDocument The document against which all selected documents will be compared against
     * @param selectedDocuments The list of documents against which the considered document will be compared
     * @return The greatest similarity score between the selected documents and the considered document.
     */
    private double maxSim2(List<String> unselectedDocument, TreeSet<Document_Score> selectedDocuments)
    {
        double maxScore = 0;
        Iterator<Document_Score> it = selectedDocuments.iterator();
        while(it.hasNext())
        {
            double score = getSim2(unselectedDocument, it.next().getDocument());
            if(score > maxScore) maxScore = score;
        }
        return maxScore;
    }
    
    public void startGeneratingScores(List<List<String>> docs, int cpus)
    {
        master = new GenerateScores(this, docs, cpus);
        new Thread(master).start();
//        master.run();
    }
    
    public static void main(String[] args) {
        
//        DoubleDoc one = new MMR().new DoubleDoc(new ArrayList<String>(), new ArrayList<String>(), 1);
//        DoubleDoc two = new MMR().new DoubleDoc(new ArrayList<String>(), new ArrayList<String>(), 1);
////        System.out.println(one.equals(two));
//        HashSet<DoubleDoc> test = new HashSet<>();
//        System.out.println(test.add(one));
//        System.out.println(test.add(two));
//        System.out.println(test.size());
//        System.out.println("");
        
        List<List<String>> documents = new ArrayList<>();
        ArrayList<String> query = new ArrayList<>();
        
        String documentText[] = { "abcdxyz" , "abcdefg", "hijklmn", "opqrstu", "vwxyz"};
        String queryText = "abcdxyzhio";
        
        for (int i = 0; i < documentText.length; i++) {
            ArrayList<String> document = new ArrayList<>();
            for(int j = 0; j < documentText[i].length(); j++){
                document.add(documentText[i].charAt(j) + "");
            }
            documents.add(document);
        }
        
        for(int i = 0; i < queryText.length(); i++) query.add(queryText.charAt(i) + "");
        
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////        
//                                             Change This Lambda Value to Test Algorithm                                     //
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        double lambda = 1;
        
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////        
//                                             Change This Lambda Value to Test Algorithm                                     //
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////        
        
//        List<List<String>> ret = new MMR().rankedList(documents, query, lambda, 5);
//        
//        for(List<String> retSent : ret)
//        {
//            for(String word : retSent) 
//            {
//                System.out.print(word + "\t");
//            }
//            System.out.println("");
//        }
    }
}
