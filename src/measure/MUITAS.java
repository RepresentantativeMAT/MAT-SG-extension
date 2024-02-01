/*
 * 
Vanessa: update on 26-11-2023

update in: 
temporal match to adapt with intervals
conceptual model to new one
threshold numeric -- exception manual setted as 1.0

 --------------->> Remember to analyze input dataset and update this manual values
 */
package measure;

import br.ufsc.model.AttributeValue;
import br.ufsc.model.Centroid;
import br.ufsc.model.MultipleAspectTrajectory;
import br.ufsc.model.Point;
import br.ufsc.model.STI;
import br.ufsc.model.SemanticAspect;
import br.ufsc.util.Util;
import java.text.ParseException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author vanes
 */
public class MUITAS {

    private Map<Object, Float> weights;
    private Map<Object, Float> thresholds;

    private double parityT1T2 = 0;
    private double parityT2T1 = 0;
    
    private final float THRESHOLD_AUX = (float) 1.0;

    public MUITAS() {
        this.weights = new HashMap<Object, Float>();
        this.thresholds = new HashMap<Object, Float>();
    }

    public void clear() {
        this.weights.clear();
        this.thresholds.clear();
    }

    public void setThreshold(Object att, float threshold) {
        this.thresholds.put(att, threshold);
    }

    public double getThreshold2(Object att) {
        if (thresholds.isEmpty()) {
            System.err.println("threshold list is Empty");
        }

        try {
            return this.thresholds.get(att);
        } catch (Exception e) {
            if (att instanceof SemanticAspect) {
                System.err.println("Error in getThreshold for attribute: '" + ((SemanticAspect) att).getName() + "' (thresholds: " + this.thresholds + ")");
            } else {
                System.err.println("ERROR in getThreshold for spatial data: " + e.getMessage());
            }
            throw new NullPointerException();
        }
    }

    public double getThreshold(Object att) {
//        System.out.println("Thresholds: "+thresholds);
        if (thresholds.isEmpty()) {
            System.err.println("Threshold list is empty");
            throw new IllegalStateException("Threshold list is empty");
        }

        try {
            if (thresholds.containsKey(att)) {
                return thresholds.get(att);
            } else {
                System.err.println("Threshold not found for attribute: '" + att + "'");
                throw new IllegalArgumentException("Threshold not found for attribute: '" + att + "'");
            }

        } catch (Exception e) {
            System.err.println("Invalid attribute type");
            throw new IllegalArgumentException("Invalid attribute type");

        }
    }

    public void getAllThreshold() {

        System.out.println("Thresholds: ");
        for (Map.Entry<Object, Float> eachTau : thresholds.entrySet()) {
            System.out.println("key: " + eachTau.getKey() + " | Value: " + eachTau.getValue());

        }
    }

    public void setWeight(Object attribute, float weight) {
        this.weights.put(attribute, weight);
    }

    public double getWeight(Object attribute) {
        try {
            if (attribute instanceof STI) {
                return this.weights.get("TIME");
            } else {
                return this.weights.get(attribute);
            }
        } catch (Exception e) {
            System.err.println("Error in getWeight for feature: '" + attribute + "' (weights: " + this.weights + ")");
            throw new NullPointerException();
        }
    }

    public float getAllWeight() {
        float sumWeight = 0.0f;

        for (Map.Entry<Object, Float> eachWeight : weights.entrySet()) {
            sumWeight += eachWeight.getValue();

        }
        return sumWeight;
    }

    public double getParityT1T2() {
        return parityT1T2;
    }

    public double getParityT2T1() {
        return parityT2T1;
    }

    public double similarityOf(MultipleAspectTrajectory t1, MultipleAspectTrajectory t2) throws ParseException {
        parityT1T2 = 0;
        parityT2T1 = 0;
        double[][] scores = new double[t1.getPointList().size()][t2.getPointList().size()];

        for (int i = 0; i < t1.getPointList().size(); i++) {
            double maxScoreRow = 0;

            for (int j = 0; j < t2.getPointList().size(); j++) {
                scores[i][j] = this.score((Centroid) t1.getPointList().get(i), t2.getPointList().get(j));
                maxScoreRow = scores[i][j] > maxScoreRow ? scores[i][j] : maxScoreRow;
            }

            parityT1T2 += maxScoreRow;

        }
        for (int j = 0; j < t2.getPointList().size(); j++) {
            double maxCol = 0;

            for (int i = 0; i < t1.getPointList().size(); i++) {

                maxCol = scores[i][j] > maxCol ? scores[i][j] : maxCol;
            }

            parityT2T1 += maxCol;
        }
        return (parityT1T2 + parityT2T1) / (t1.getPointList().size() + t2.getPointList().size());

    }

    private final double score(Centroid p1, Point p2) throws ParseException {
        double score = 0;

        ///// Problem identified: only points that derived rp, but the other point in the same cell not is valid?
//        if (p1.getPointListSource().contains(p2)) {
//             score += (getWeight("SPATIAL"));
////            System.out.println("Spatial Match: "+score);
//        }
        //Spatial match:
        if (Util.euclideanDistance(p1, p2) <= getThreshold("SPATIAL")) {
            score += (getWeight("SPATIAL"));
        }

        matchTemporal:
        {
          double match = 0;
            // Ordernate temporal ranking 
        List<STI> listSTI = p1.getListSTI().stream().sorted().collect(Collectors.toList());
            for(int i = 0;
                    i<listSTI.size();
                    i++){
                if(listSTI.get(i).getInterval().isInInterval(p2.getTime().getStartTime())){
                    match = 1;
                    break;
                } else if(i < listSTI.size() - 1 && 
                        p2.getTime().getStartTime().before(listSTI.get(i+1).getInterval().getStartTime())){
                    break;
                }
            }
            score += match * getWeight("TIME");

        }

        for (AttributeValue atvP1 : p1.getListAttrValues()) {
            AttributeValue tempAttP2 = atvP1.getAttibute() != null ? p2.getAttributeValue(atvP1.getAttibute()) : null;

            double tempSemanticMatch = computeMatch(atvP1, tempAttP2);
            score += tempSemanticMatch;

        }
        return score;
    }

    public double computeMatch(AttributeValue rep, AttributeValue atv) {
        double match = 0;

        if (atv == null || rep == null) {
            return 0;
        }

        if (rep.getValue() instanceof Map) { // Categorical values

            // case of semantic - categorical
            // Vanessa: 24/04 -- update to consider only if contains the in list of rank (representative values) match considered
            HashMap<String, Double> valuesRT = (HashMap) rep.getValue();
            if (valuesRT.containsKey(((String) atv.getValue()).toUpperCase())) {
                match = 1;
            }
        } else { //Numerical values

            if (rep.getNumericalValueSD() != 0) {
                match = Math.abs(Double.parseDouble((String) rep.getValue()) - Double.parseDouble((String) atv.getValue())) <= (rep.getNumericalValueSD() * 2.5) ? 1.0 : 0;
            } else //default value
            {
                try {
//                    System.out.println("Values of ATT:"+rep.getAttibute()+" rep: "+rep.getValue()+" --- atv: "+atv.getValue());
                    match = Math.abs(Double.parseDouble((String) rep.getValue()) - Double.parseDouble((String) atv.getValue())) <= getThreshold(rep.getAttibute()) ? 1.0 : 0;
                } catch (NullPointerException e) {
                    match = Math.abs(Double.parseDouble((String) rep.getValue()) - Double.parseDouble((String) atv.getValue())) <= THRESHOLD_AUX ? 1.0 : 0;
                } catch (NumberFormatException e2){
                    match = atv.getValue().equals(rep.getValue())?1.0:0.0;
                } catch (IllegalArgumentException e3){
                    match = Math.abs(Double.parseDouble((String) rep.getValue()) - Double.parseDouble((String) atv.getValue())) <= THRESHOLD_AUX ? 1.0 : 0;
                }
            }
//                
        }

        return match * getWeight(rep.getAttibute());
    }
    
   
}
