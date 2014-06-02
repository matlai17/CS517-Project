package SimMetrics;


import java.util.List;
import java.util.SortedMap;

/**
 * A Similarity Metric that compares two vectors, normally a document and a query,
 * and returns their similarity value between 0 and 1.
 * 
 * @author Matthew Lai
 */
public interface SimMetric {
    
    /**
     * Returns the ranking of the sentence given the implemented similarity 
     * metric algorithm, the sentence, and the query. The ranking should 
     * represent the similarity between the query and the sentence.
     * 
     * @param document the sentence that will be ranked by the similarity metric
     * @param query the query with which the sentence will be ranked against.
     * @return The ranking of the sentence based on implemented algorithm
     * 
     */
    public Double documentRank(List<String> document, List<String> query);
    
    /**
     * Returns a map of sentences and their rankings, sorted by the ranking
     * of each sentence from greatest rank to lowest rank. The map implemented should
     * allow for ordering of its keys and values. 
     * 
     * @param documentList the list of sentences that will be ranked and sorted.
     * @param query the query with which the sentences will be ranked and sorted against.
     * 
     * @return a map of the sentences ordered from greatest to least similarity to the query. 
     */
//    public SortedMap<List<String>, Double> orderedDocuments(List<List<String>> documentList, List<String> query);
}
