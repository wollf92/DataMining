/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mars;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    private HashMap<String, List<Double>> instanceValues;
    
    
    public Model(){
    }
    
    public void setFileToReadDataFrom(String text) throws FileNotFoundException, IOException {
        String line;
        BufferedReader br;
        br = new BufferedReader(new FileReader("src/mars/" + text + ".csv"));
        instanceValues = new HashMap<String, List<Double>>();
        while((line = br.readLine()) != null)
        {
            String[] perValue = line.split(",");
            String date = perValue[1] + "-" + perValue[5];
            instanceValues.put(date, new ArrayList<Double>());
            instanceValues.get(date).add((double)perValue[0].length());
            for(int i = 2; i <= 15; i++)
            {
                try{
                    instanceValues.get(date).add(Double.parseDouble(perValue[i]));
                } catch (NumberFormatException e) {}
            }
        }    
        printData();
        System.out.println(getDataFromDate("2011-04-15 5"));
    }
    
    public List<Double> getDataFromDate(String date)
    {
        return instanceValues.get(date);
    }
    
    public void printData()
    {
        Iterator<Entry<String, List<Double>>> it = instanceValues.entrySet().iterator();
        String line;
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            line = (String)pair.getKey();
            for(double d : (List<Double>)pair.getValue())
                line = line + " " + d;
            System.out.println(line);
        }
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
        double testGCV = 0;
        MARSTerm removed = new MARSTerm(0);
        while(testGCV < GCV){
            removed = RemoveTerm();
            testGCV = ComputeGCV(Formula);
        }
        if(removed.Coëff != 0)
            Formula.add(removed);
    }
    
    private MARSTerm RemoveTerm (){
        ArrayList<MARSTerm> testFormula = (ArrayList<MARSTerm>)Formula.clone();
        MARSTerm removed = new MARSTerm(0);
        double RSSdiff = RSS;
        for(int i = 1; i < Formula.size(); i++){
            MARSTerm remTest = testFormula.get(i);
            testFormula.remove(i);
            double testRSS = ComputeRSS(testFormula);
            if(Math.abs(RSS - testRSS) < RSSdiff){
                RSSdiff = Math.abs(RSS - testRSS);
                removed = remTest;
            }
            testFormula.add(i,remTest);
        }
        if(Formula.remove(removed))
            return removed;
        return new MARSTerm(0);
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
