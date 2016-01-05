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
    
    private final String XAXISNAME = "";
    private final int ARRAYLENGTH = 0;
    private final double[][] XAXIS = {{}};
    private final double[] YAXIS = {};
    private ArrayList<MARSTerm> Formula = new ArrayList<>();
    private double RSS;
    
    
    public Model(){
    }
    
    private double ComputeRSS(){
        double sum = 0;
        for(int i = 0; i < ARRAYLENGTH; i++){
            double residu = ComputeValue(i) - YAXIS[i];
            sum += residu * residu;
        }
        sum = sum/(double)ARRAYLENGTH;
        return sum;
    }
    
    public ArrayList<MARSTerm> ForwardPass(){
        GetIntercept();
        RSS = ComputeRSS();
        
        return Formula;
    }
    
    private void GetIntercept(){
        double ic = 0;
        for(int i = 0; i < ARRAYLENGTH; i++){
            ic += YAXIS[i];
        }
        ic = ic/(double)ARRAYLENGTH;
        Formula.add(new MARSTerm(ic));
    }
    
    private double ComputeValue(int index){
       double result = 0;
       for(MARSTerm cur : Formula){
           if(cur.Knot.isEmpty())
               result += cur.Coëff;
           else{
               double prod = 1;
               for(int i = 0; i < cur.Knot.size(); i++){
                   if(cur.NegHinge.get(i))
                       prod *= Math.max(0,cur.Knot.get(i) - XAXIS[cur.VarRow.get(i)][index]);
                   else
                       prod *= Math.max(0,XAXIS[cur.VarRow.get(i)][index] - cur.Knot.get(i));
               }
               result += cur.Coëff*prod;
           }
       }
       return result;
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
