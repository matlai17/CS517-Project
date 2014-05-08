
import java.util.List;
import java.util.SortedMap;

/**
 *
 * @author Matthew Lai
 */
public interface SimMetric {
    
    /**
     * Returns the ranking of the sentence given the implemented similarity 
     * metric algorithm, the sentence, and the query. The ranking should 
     * represent the similarity between the query and the sentence.
     * 
     * @param sentence the sentence that will be ranked by the similarity metric
     * @param query the query with which the sentence will be ranked against.
     * @return The ranking of the sentence based on implemented algorithm
     * 
     */
    public double sentenceRank(List<String> sentence, List<String> query);
    
    /**
     * Returns a map of sentences and their rankings, sorted by the ranking
     * of each sentence from greatest rank to lowest rank. The map implemented should
     * allow for ordering of its keys and values.
     * 
     * @param sentenceList the list of sentences that will be ranked and sorted.
     * @param query the query with which the sentences will be ranked and sorted against.
     * 
     * @return a map of the sentences ordered from greatest to least similarity to the query. 
     */
    public SortedMap<List<String>, Double> orderedSentences(List<List<String>> sentenceList, List<String> query);
}
