/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SimMetrics;

import Matrix.FrequencyMatrix;
import Matrix.IDFMatrix;
import Parsing.Parser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.SortedMap;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * This Lexical Ranking algorithm forms a sentence similarity graph. This similarity graph,
 * when scaled properly becomes a Markov chain that can be used to form random walks through the document
 * cluster. This can then estimate how important a sentence in the cluster is by how often it is linked to 
 * be another sentence (i.e. how similiar it is to other sentences) The more similar a sentence is to the 
 * other sentences of the cluster, the more important it is. This similarity random walk is then combined 
 * with another Markov chain that forms the random walk to determine how often a sentence comes up given 
 * a specific query. By determining the combined probability that, at any point, one would be on a certain
 * sentence, you can determine the sentence's importance according to the query and to the document itself.
 * 
 * 
 * @author Matthew Lai
 */
public class LexRankSim implements SimMetric {
    
    IDFMatrix idf;
    DenseMatrix64F SimMatrix;
    ArrayList<List<String>> matrixIndex;
    Double queryBias;
    HashMap<List<String>, DenseMatrix64F> qMatrixMap;
    
    /**
     * Constructor. Forms the Similarity Matrix that will be used to calculate
     * sentence scores. 
     * 
     * @param document  The document whose sentences will be used as the nodes of the similarity graph
     * @param idf The Inverse Document Frequence metric that is used by this LexRank
     * @param simThresh The similarity threshold for Nodes of the similarity graph to be considered neighbors
     * @param qBias The query bias, which is used by the algorithm to determine its random walk probabilities
     */
    public LexRankSim(List<List<String>> document, IDFMatrix idf, Double simThresh, Double qBias)
    {
        qMatrixMap = new HashMap<>();
        this.idf = idf;
        queryBias = qBias;
        SimMatrix = new DenseMatrix64F(document.size(), document.size());
        matrixIndex = new ArrayList<>(document.size());
        
        for(List<String> sentence : document)
        {
            int newIndex = matrixIndex.size();
            matrixIndex.add(sentence);
            for(int i = 0; i < matrixIndex.size(); i++)
            {
                double value = sim(sentence, matrixIndex.get(i));
                if(value >= simThresh)
                {
                    SimMatrix.set(newIndex, i, value);
                    SimMatrix.set(i, newIndex, value);
                }
                else
                {
                    SimMatrix.set(newIndex, i, 0);
                    SimMatrix.set(i, newIndex, 0);
                }
            }
        }
        for(int i = 0; i < SimMatrix.numRows; i++)
        {
            double normalizerValue = 0;
            for(int j = 0; j < SimMatrix.numCols; j++)
            {
                normalizerValue += SimMatrix.get(j, i);
            }
            for(int j = 0; j < SimMatrix.numRows && normalizerValue > 0; j++)
            {
                SimMatrix.set(i, j, SimMatrix.get(j, i) / normalizerValue);
            }
        }
    }
    
    /**
     * Returns the IDF used by this LexRank
     * 
     * @return the IDF used by this LexRank
     */
    public IDFMatrix getIDFMatrix()
    {
        return idf;
    }
    
    /**
     * Returns the relevance of the given document or sentence against the query.
     * The similarity metric is based on term frequency between the query and the 
     * sentence and weighted against the document IDF.
     * 
     * @param document  The document whose relevance will be checked against the query
     * @param query The query against whom the document will be checked.
     * @return the relevance score between the document or sentence and the query
     */
    private double relevance(List<String> document, List<String> query)
    {
        FrequencyMatrix tfDocument = new FrequencyMatrix(document);
        FrequencyMatrix tfQuery = new FrequencyMatrix(query);
        double score = 0;
        for(String queryWord : query)
        {
            score += Math.log(tfDocument.getFreq(queryWord) + 1)
                    * Math.log(tfQuery.getFreq(queryWord) + 1)
                    * idf.getIDF(queryWord);
        }
        return score;
    }
    
    /**
     * Retrieves the similarity between the two parameters. This similarity metric
     * is a weighted cosine similarity metric weighted with the document IDF.
     * 
     * @param document the document to be compared
     * @param query the query to be compared
     * @return Weighted Cosine Similarity between the document and the query
     */
    private double sim(List<String> document, List<String> query)
    {
        return new CosineSim(idf).documentRank(document, query);
    }
    
    /**
     * Finds the rank of the input sentence given the query using the LexRank method
     * 
     * @param document  The document that is to be ranked against the current cluster
     * @param query The query with which the document will be ranked along with the cluster similarity
     * @return The rank score of the document. Returns null if the sentence does not exist in the cluster
     */
    @Override
    public Double documentRank(List<String> document, List<String> query) 
    {
        if(matrixIndex.indexOf(document) == -1) return null;
        DenseMatrix64F calculatedMatrix = qMatrixMap.get(query);
        if(calculatedMatrix == null) calculatedMatrix = calculateQMatrix(query);
        return calculatedMatrix.get(matrixIndex.indexOf(document));
    }
    
    /**
     * Calculates the stochastic transition matrix that forms the Markov Chain
     * that results from the Random Walk Biased LexRank algorithm. By making the query
     * a parameter rather than part of the constructor we can query this cluster
     * multiple times efficiently without recreating the cluster every time.
     * 
     * @param query the query for which we are calculating the matrix for.
     * @return the 1D matrix that contains the stationary matrix for the Markov chain
     */
    private DenseMatrix64F calculateQMatrix(List<String> query)
    {
        DenseMatrix64F calculatedMatrix = new DenseMatrix64F(matrixIndex.size(), 1);
        DenseMatrix64F rMatrix = new DenseMatrix64F(SimMatrix.numRows, SimMatrix.numCols);
        DenseMatrix64F sMatrix = SimMatrix.copy();
        DenseMatrix64F qMatrix = new DenseMatrix64F(SimMatrix.numRows, SimMatrix.numCols);
        
        double normalizationValue = 0;
        for(int i = 0; i < matrixIndex.size(); i++)
        {
            Double score = relevance(matrixIndex.get(i), query);
            rMatrix.set(0, i, score);
            normalizationValue += score;
        }
        for(int i = 0; i < rMatrix.numCols; i++)
        {
            double simValue = 0.0;
            if(normalizationValue != 0) simValue = rMatrix.get(0, i) / normalizationValue;
            for (int j = 0; j < rMatrix.numRows; j++) rMatrix.set(j, i, simValue);
        }
        CommonOps.scale(queryBias, rMatrix);
        CommonOps.scale(1-queryBias, sMatrix);
        CommonOps.add(rMatrix, sMatrix, qMatrix);
        CommonOps.transpose(qMatrix);
//        qMatrix.print();
//        DenseMatrix64F result = new DenseMatrix64F(sMatrix.numCols, sMatrix.numRows);
//        for (int i = 0; i < 10; i++)
//        {
//            CommonOps.mult(qMatrix, qMatrix, result);
//            qMatrix = result.copy();
//            qMatrix.print();
//        }
//        EigenOps.computeEigenVector(qMatrix, 1).vector.print();
//        EigenDecomposition eD = DecompositionFactory.eig(qMatrix.numCols, true, true);
//        eD.decompose(qMatrix);
//        for (int i = 0; i < eD.getNumberOfEigenvalues(); i++) {
//            System.out.println(eD.getEigenvalue(i).getReal());
//            eD.getEigenVector(i).print();
//            System.out.println("\n");
//        }
        calculatedMatrix.set(0, 0, 1);
        DenseMatrix64F result = new DenseMatrix64F(qMatrix.numCols, 1);
        for(int i = 0; i < 50; i++)
        {
            CommonOps.mult(qMatrix, calculatedMatrix, result);
            calculatedMatrix = result.copy();
        }
        
        return calculatedMatrix;
    }

    @Override
    public SortedMap<List<String>, Double> orderedDocuments(List<List<String>> documentList, List<String> query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static void main(String[] args) throws IOException {
        
//        DenseMatrix64F test = new DenseMatrix64F(2,2);
//        test.set(0, 0, .6);
//        test.set(1, 0, .4);
//        test.set(0, 1, .2);
//        test.set(1, 1, .8);
//        test.print();
//        
//        DenseMatrix64F result = new DenseMatrix64F(2,1);
//        result.set(0, 0, .5);
//        result.set(1, 0, .5);
//        result.print();
//        
//        
//        DenseMatrix64F c = new DenseMatrix64F(2,1);
//        
//        for(int i = 0; i < 10; i++)
//        {
//            CommonOps.mult(test, result, c);
//            result = c.copy();
//            result.print();
//        }
        
        Parser p = new Parser(new File("CalPolyNews.txt"));
        List<List<String>> document = p.getStemmedDocument();
//        for(List<String> sentence : document ) System.out.println(p.getSentence(sentence));
        LexRankSim test = new LexRankSim(document, new IDFMatrix(document), 0.0, .7);
        
//        System.out.println(test.calculateQMatrix(Parser.vectorAndStem("economic and rubin")));

        java.util.PriorityQueue<Document_Score> scoredDocuments = new PriorityQueue<>(document.size(), new Comparator<Document_Score>() {

            @Override
            public int compare(Document_Score o1, Document_Score o2) {
                if(o1.getScore() == o2.getScore()) return 1;
                return -Double.compare(o1.getScore(), o2.getScore());
            }
        });
        for(List<String> sentence : document)
            scoredDocuments.add(test.new Document_Score(sentence, test.documentRank(sentence, Parser.vectorAndStem("economic and rubin"))));
        
//        System.out.println(p.getSentence(document.get(3)));
        while(!scoredDocuments.isEmpty())
        {
            Document_Score ds = scoredDocuments.poll();
            System.out.println(ds.getScore() + "\t:\t" + p.getSentence(ds.getDocument()));
        }
    }
    
        /**
     * Document_Score is a private class to pass and store the pair of a document
     * and its MMR score. Allows for comparison.
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
    
}
