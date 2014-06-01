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
 */

package SimMetrics;

import Matrix.FrequencyMatrix;
import Matrix.IDFMatrix;
import Parsing.Parser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.Eigenpair;
import org.ejml.data.RowD1Matrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.factory.EigenDecomposition;
import org.ejml.ops.CommonOps;
import org.ejml.ops.EigenOps;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author Matthew Lai
 */
public class LexRankSim implements SimMetric {
    
    IDFMatrix idf;
    DenseMatrix64F SimMatrix;
    ArrayList<List<String>> matrixIndex;
    Double queryBias;
    HashMap<List<String>, DenseMatrix64F> qMatrixMap;
    
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
    
    public IDFMatrix getIDFMatrix()
    {
        return idf;
    }
    
    private double relavence(List<String> document, List<String> query)
    {
        FrequencyMatrix tfDocument = new FrequencyMatrix(document);
        FrequencyMatrix tfQuery = new FrequencyMatrix(query);
        double score = 0;
        for(String queryWord : query)
            score += Math.log(tfDocument.getFreq(queryWord) + 1)
                    * Math.log(tfQuery.getFreq(queryWord) + 1)
                    * idf.getIDF(queryWord);
        return score;
    }
    
    private double sim(List<String> document, List<String> query)
    {
        return new CosineSim(idf).documentRank(document, query);
    }
    

    @Override
    public double documentRank(List<String> document, List<String> query) 
    {
        DenseMatrix64F calculatedMatrix = qMatrixMap.get(query);
        if(calculatedMatrix == null) calculatedMatrix = calculateQMatrix(query);
        return calculatedMatrix.get(matrixIndex.indexOf(document));
    }
    
    private DenseMatrix64F calculateQMatrix(List<String> query)
    {
        DenseMatrix64F calculatedMatrix = new DenseMatrix64F(matrixIndex.size(), 1);
        DenseMatrix64F rMatrix = new DenseMatrix64F(SimMatrix.numRows, SimMatrix.numCols);
        DenseMatrix64F sMatrix = SimMatrix.copy();
        DenseMatrix64F qMatrix = new DenseMatrix64F(SimMatrix.numRows, SimMatrix.numCols);
        
        double normalizationValue = 0;
        for(int i = 0; i < matrixIndex.size(); i++)
        {
            Double score = relavence(matrixIndex.get(i), query);
            rMatrix.set(0, i, score);
            normalizationValue += score;
        }
        for(int i = 0; i < rMatrix.numCols; i++)
        {
            double simValue = rMatrix.get(0, i) / normalizationValue;
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
        for(List<String> sentence : document ) System.out.println(p.getSentence(sentence));
        LexRankSim test = new LexRankSim(document, new IDFMatrix(document), 0.0, .7);
        
        System.out.println(test.documentRank(document.get(3), Parser.vectorAndStem("economy rubin")));
        
        for(List<String> sentence : document)
        System.out.println(test.documentRank(sentence, Parser.vectorAndStem("economy and rubin")));
        System.out.println(p.getSentence(document.get(3)));
    }
    
}
