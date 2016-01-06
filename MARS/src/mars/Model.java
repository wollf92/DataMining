/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mars;

import java.util.ArrayList;

/**
 *5
 * @author hessel
 */
public class Model {
    
    private final String[] XAXISNAMES = {""};
    private final double[][] XAXIS = {{}};
    private final double[] YAXIS = {};
    private ArrayList<MARSTerm> Formula = new ArrayList<>();
    private double RSS;
    
    
    public Model(){
    }
    
    private double ComputeRSS(){
        double sum = 0;
        for(int i = 0; i < YAXIS.length; i++){
            double residu = ComputeValue(i) - YAXIS[i];
            sum += residu * residu;
        }
        sum = sum/(double)YAXIS.length;
        return sum;
    }
    
    public ArrayList<MARSTerm> ForwardPass(int maxTerms, int maxTermDepth){
        GetIntercept();
        RSS = ComputeRSS();
        while(Formula.size() < maxTerms){
            FindNextPair(maxTermDepth);
        }
        return Formula;
    }
    
    private void GetIntercept(){
        double ic = 0;
        for(int i = 0; i < YAXIS.length; i++){
            ic += YAXIS[i];
        }
        ic = ic/(double)YAXIS.length;
        Formula.add(new MARSTerm(ic));
    }
    
    private void FindNextPair(int maxTermDepth){
        for(MARSTerm parent : Formula){
            for(int i = 0; i < XAXISNAMES.length; i++){
                for(double knot : XAXIS[i]){
                    
                }
            }
        }
    }
    
    private double ComputeValue(int index){
       double result = 0;
       for(MARSTerm cur : Formula){
           result += cur.ComputeTermValue(index, XAXIS);
       }
       return result;
    }
    
    public <T>void CopyList(ArrayList<T> a, ArrayList<T> b){
        for(T cur : a){
            b.add(cur);
        }
    }
    
    private double StringtoDouble(String s){
        boolean negative = false;
        int dot = negative ? s.length()-1 : s.length();
        ArrayList<Double> numbers = new ArrayList<>();
        double result = 0;
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i) == '-')
                negative = true;
            else if(s.charAt(i) == '.'){
                if(negative)
                    dot = i - 1;
                else
                    dot = i;
            }
            else
                numbers.add((double)(s.charAt(i) - 48));
        }
        for(int i = 0; i < numbers.size(); i++){
            result += numbers.get(i)*Math.pow(10, dot - i - 1);
        }
        return negative ? -result : result;
    }
}
