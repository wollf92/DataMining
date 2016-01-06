/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mars;

import java.util.ArrayList;

/**
 * @author Hessel Bongers
 */
public class Model {
    
    private final String[] XAXISNAMES = {""};
    private final double[][] XAXIS = {{}};
    private final double[] YAXIS = {};
    private ArrayList<MARSTerm> Formula = new ArrayList<>();
    private double RSS;
    private double GCV;
    
    
    public Model(){
    }
    
    private double ComputeRSS(ArrayList<MARSTerm> form){
        double sum = 0;
        for(int i = 0; i < YAXIS.length; i++){
            double residu = ComputeValue(form, i) - YAXIS[i];
            sum += residu * residu;
        }
        sum = sum/(double)YAXIS.length;
        return sum;
    }
    
    private double ComputeGCV(ArrayList<MARSTerm> form){
        double amtOfTerms = form.size() + 2*(form.size()-1)/2;
        double lower = (1 - amtOfTerms/(double)YAXIS.length);
        lower = YAXIS.length*lower*lower;
        return RSS/lower;
    }
    
    public ArrayList<MARSTerm> BackwardPass(){
        GCV = ComputeGCV(Formula);
        RemoveTerms();
        return Formula;
    }
    
    private void RemoveTerms(){
        
    }
    
    private double RemoveTerm (){
        
        return ComputeGCV(Formula);
    }
    
    public ArrayList<MARSTerm> ForwardPass(int maxTerms, int maxTermDepth){
        GetIntercept();
        RSS = ComputeRSS(Formula);
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
        ArrayList<MARSTerm> best = new ArrayList<>();
        for(MARSTerm parent : Formula){
            for(int i = 0; i < XAXISNAMES.length && parent.Knot.size() < maxTermDepth; i++){
                for(double knot : XAXIS[i]){
                    double x = TryHingePair(parent, knot, i);
                    if(x < RSS){
                        RSS = x;
                        best = (ArrayList<MARSTerm>)Formula.clone();
                    }
                    Formula.remove(Formula.size()-1);
                    Formula.remove(Formula.size()-1);
                }
            }
        }
        Formula = (ArrayList<MARSTerm>)best.clone();
    }
    
    private double TryHingePair(MARSTerm parent, double knot, int xrow){
        MARSTerm new1 = new MARSTerm(ComputeCoëff(Formula.get(0).Coëff, knot, xrow, false));
        CopyList(parent.NegHinge, new1.NegHinge);
        new1.NegHinge.add(false);
        CopyList(parent.Knot, new1.Knot);
        new1.Knot.add(knot);
        CopyList(parent.VarRow, new1.VarRow);
        new1.VarRow.add(xrow);
        Formula.add(new1);
        
        MARSTerm new2 = new MARSTerm(ComputeCoëff(Formula.get(0).Coëff, knot, xrow, true));
        CopyList(parent.NegHinge, new2.NegHinge);
        new2.NegHinge.add(true);
        CopyList(parent.Knot, new2.Knot);
        new2.Knot.add(knot);
        CopyList(parent.VarRow, new2.VarRow);
        new2.VarRow.add(xrow);
        Formula.add(new2);
        
        return ComputeRSS(Formula);
    }
    
    private double ComputeCoëff(double y, double x, int xrow, boolean neg){
        double lower = 0;
        double upper = 0;
        for(int i = 0; i < XAXIS[xrow].length; i++){
            double a;
            double b;
            if(neg)
                a = x - XAXIS[xrow][i];
            else
                a = XAXIS[xrow][i] - x;
            for(int j = 0; j < YAXIS.length; j++){
                b = YAXIS[j] - y;
                upper += a*b;
            }
            lower += a*a;
        }
        upper = upper/(double)(XAXIS[xrow].length*YAXIS.length);
        lower = lower/(double)(XAXIS[xrow].length*XAXIS[xrow].length);
        return upper/lower;
    }
    
    private double ComputeValue(ArrayList<MARSTerm> form, int index){
       double result = 0;
       for(MARSTerm cur : form){
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
