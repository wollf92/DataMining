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
    private final int[] XAXIS = {};
    private final int[] YAXIS = {};
    private String Formula = "";
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
    
    public String ForwardPass(){
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
        Formula = Double.toString(ic);
    }
    
    private double ComputeValue(int index){
        ArrayList<Double> coëfficients = new ArrayList<>();
        for(int i = 0; i < Formula.length(); i++){
            char a = Formula.charAt(i);
            if(a == '-' || (a >= 48 && a <= 57)){
                int j = i + 1;
                char b = Formula.charAt(j);
                for(; j < Formula.length() && !((b >= 65 && b <= 90) || (b >= 97 && b <= 122) || b == '-' || b == '+'); j++){
                    b = Formula.charAt(j);
                }
                coëfficients.add(StringtoDouble(Formula.substring(i, j)));
                i = j - 1;
            }
        }
        double result = coëfficients.get(0);
        for(int i = 1; i < coëfficients.size(); i++){
            result += coëfficients.get(i)*XAXIS[index];
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
