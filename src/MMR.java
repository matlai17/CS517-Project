
import SimMetrics.CosineSim;
import SimMetrics.SimMetric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

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
    
    SimMetric sim1;
    SimMetric sim2;
    static final int GRAPH_SIM = SimMetric.GRAPH_SIM;
    static final int COSINE_SIM = SimMetric.COSINE_SIM;
    static final int JACCARD_SIM = SimMetric.JACCARD_SIM;
    static final int PSEUDO_CODE_SIM = SimMetric.PSEUDO_CODE_SIM;
    
    public MMR()
    {
        sim1 = new CosineSim();
        sim2 = new CosineSim(); 
    }
    
    public MMR(int sim1Type, int sim2Type)
    {
        sim1 = new CosineSim();
        sim2 = new CosineSim(); 
        
        switch(sim1Type) {
            case COSINE_SIM: sim1 = new CosineSim(); 
                break;
            case GRAPH_SIM: 
                break;
            case JACCARD_SIM: sim1 = new JaccardSim();
                break;
            case PSEUDO_CODE_SIM: 
                break;
        }
        
        switch(sim2Type) {
            case COSINE_SIM: sim2 = new CosineSim(); 
                break;
            case GRAPH_SIM: 
                break;
            case JACCARD_SIM: sim2 = new JaccardSim();
                break;
            case PSEUDO_CODE_SIM: 
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
            double score = lambda * (sim1.documentRank(document, query) - ((1 - lambda) * maxSim2(document, selectedDocuments)));
            if(score > maxScore) 
            {
                maxScore = score;
                bestDocument = document;
            }
        }
        return new Document_Score(bestDocument, maxScore);
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
            double score = sim2.documentRank(unselectedDocument, it.next().getDocument());
            if(score > maxScore) maxScore = score;
        }
        return maxScore;
    }
    
    public static void main(String[] args) {
        
// Current Test Case is not a very good one
        
        ArrayList<String> document1 = new ArrayList<>();
        document1.add("one");
        document1.add("two");
        document1.add("three");
        document1.add("four");
        document1.add("five");
        document1.add("six");
        document1.add("seven");
        document1.add("eight");
        document1.add("nine");
        document1.add("ten");
        ArrayList<String> document2 = new ArrayList<>();
        document2.add("three");
        document2.add("four");
        document2.add("five");
        document2.add("six");
        document2.add("seven");
        ArrayList<String> document3 = new ArrayList<>();
        document3.add("eight");
        document3.add("nine");
        document3.add("ten");
        document3.add("eleven");
        document3.add("twelve");
        ArrayList<String> document4 = new ArrayList<>();
        document4.add("thirteen");
        document4.add("fourteen");
        document4.add("fifteen");
        document4.add("sixteen");
        document4.add("seventeen");
        ArrayList<String> document5 = new ArrayList<>();
        document5.add("eight");
        document5.add("nine");
        document5.add("ten");
        document5.add("eleven");
        document5.add("twelve");
        document5.add("thirteen");
        document5.add("fourteen");
        document5.add("fifteen");
        document5.add("sixteen");
        document5.add("seventeen");
        ArrayList<List<String>> documents = new ArrayList<>();
        documents.add(document1);
        documents.add(document2);
        documents.add(document3);
        documents.add(document4);
        documents.add(document5);
        ArrayList<String> query = new ArrayList<>();
        query.add("thirteen");
        query.add("fourteen");
        query.add("fifteen");
        query.add("sixteen");
        query.add("seventeen");
        
        MMR mmr = new MMR();
        for(List<String> results : mmr.rankedList(documents, query, 0.7, 5))
        {
            for(String word : results) System.out.print(word + " ");
            System.out.println("");
        }
    }
}
