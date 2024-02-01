/*
 Vanessa Lago Machado

Vanessa: update on 26-11-2023

update in: 
temporal match to adapt with intervals
conceptual model to new one
threshold to numeric values in MUITAS update to value 1.0

 --------------->> Remember to analyze input dataset and update this manual values


 */
package br.ufsc.methods;

import br.ufsc.model.AttributeValue;
import br.ufsc.model.Centroid;
import br.ufsc.model.MultipleAspectTrajectory;
import br.ufsc.model.Point;
import br.ufsc.model.STI;
import br.ufsc.model.SemanticAspect;
import br.ufsc.model.SemanticType;
import br.ufsc.model.TemporalAspect;
import br.ufsc.util.CSVWriter;
import br.ufsc.util.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import measure.MUITAS;

/**
 *
 * @author vanes
 */
public class MATSG_Validation {

    // setting to execute method
    String SEPARATOR;
    String[] valuesNulls;
    // --------------------- AUX ----------------------
    private static List<int[]> trajectories;
    private static int rId;
    private static int cId;
    int ord;
    private static String auxTid;
        // format of input data
    private static SimpleDateFormat formatDate;
    // format of output data
    private static SimpleDateFormat formatDateOut;
    private static DecimalFormat df = new DecimalFormat("###.######");

    // -- Load
    // For loading information from the dataset
    private static List<Point> points; //Points to be analysed
    private static List<SemanticAspect> attributes; //List of all diferent attributes found in the dataset
    private static Map<String, BitSet> spatialCellGrid; //Spatial grid array
    private static Map<String, List<Double>> sematicNumericFusionVal;  //Sum of each type of numerical attribute
//    private static Map<String, Map<String, Integer>> sematicCategoricalSummarizationVal; //Sum of ocorrunces os each categorical attribute
    private static Map<Object, Map<String, Integer>> sematicCategoricalSummarizationVal;

//    private static List<Point> pointsInCell; //List of all diferent points found in the each cell

    // ------------- to Spatial division -- Dataset file information
    private static String filename; //Filename of the dataset
    private static String directory;//Directory of the dataset
    private static String extension; //Extension of the filename

    // To create the Spatial division    
    private static float spatialThreshold; //Maximum possible size for a cell
    private static float cellSizeSpace; //Size of each cell
    //private static int valueZ; //Determines how many times the average dispersion of points will the cell size measure
    private float auxMaxZ; //

    // To model trajectory data
    private static MultipleAspectTrajectory trajectory; //Contain all points of a MAT
    private static List<MultipleAspectTrajectory> listTrajectories; //List of all MATs in the dataset
    private static MultipleAspectTrajectory representativeTrajectory; //Summarized MAT
    private static MultipleAspectTrajectory betterRT; //Better Summarized MAT

    // To create the Temporal summarization
//    private List<Integer> listTimesInCell; //List of all time marks in a cell 
    /// ---- Summarization step
    // To create the Temporal summarization
    private ArrayList<Date> listTimesInCell; //List of all time marks in a cell -- update: option times in Date
    

    // To create the Spatial summarization
    private double avgX, avgY;

    // To provide and compute runtime
    private Date initialTemp;

    private boolean dailyInfo = false;

    // --------------- to determine categoricals pre-defined values
    List<String> lstCategoricalsPD;
    List<String> lstIgnoreCols = null;

    // --- Define initial index value to semantic attributes
    private static int INDEX_SEMANTIC = 3;
    private final int THRESHOLD_TIME = 100;

    //V9 - parameters to MAT-SG
    private float rc; //To define relevant cells 
    private float threshold_rv; //To define relevant values in rank values, which values in rank are representative
    private float threshold_rc; //To define relevant values in rank values, which values in rank are representative

    // For validation
    private String filenameFullDataset; //Filename of the dataset
    private static List<MultipleAspectTrajectory> listAllTrajectories; //List of all MATs in the dataset

    private String auxTimeInfo;
    private String auxTimeBetterRT;
    private int betterZ;
    private float finalRMMAT;

    //pattern to number
    DecimalFormat formatNumber = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));

    /**
     * Method to perform all methods in order to summarize input MATs into a
     * representative MAT.
     *
     * @param spatialT Spatial Treshhold
     * @param file name of file
     * @param ext extension of file
     * @throws IOException
     *
     */
    // patternDateIn, threshold_rc, threshold_rv);
    public void execute(String dir, String file, String ext, String[] lstCategoricalPD, String SEPARATOR, String[] valuesNULL, String[] lstIgnoreColumns, String patternDateIn, float threshold_rc, float threshold_rv) throws IOException, ParseException, CloneNotSupportedException {
        initialTemp = new Date();
        //initialization of attribute values (Global attributes according to local data)
        directory = dir;
        filename = file;
        extension = ext;
        this.SEPARATOR = SEPARATOR;
        this.valuesNulls = valuesNULL;
        //this.valueZ = numberSpatialDistance;

        //initialization of aux attributes
        rId = 0;
        auxTid = "-1";
        cId = -1;

        //initialization of aux lists
//        listTimesInCell = new ArrayList<Integer>();
        listTimesInCell = new ArrayList<Date>();
        spatialCellGrid = new HashMap<String, BitSet>();
        sematicNumericFusionVal = new HashMap<String, List<Double>>();
//        sematicCategoricalSummarizationVal = new HashMap<String, Map<String, Integer>>();
        sematicCategoricalSummarizationVal = new HashMap<Object, Map<String, Integer>>();
        points = new ArrayList<Point>();
        attributes = new ArrayList<SemanticAspect>();
        listTrajectories = new ArrayList<MultipleAspectTrajectory>();

        if (lstCategoricalPD != null) {
            lstCategoricalsPD = Arrays.asList(lstCategoricalPD);
        }
        if (lstIgnoreColumns != null) {
            this.lstIgnoreCols = Arrays.asList(lstIgnoreColumns);
        }

//        pointsInCell = new ArrayList<>();
        //initialization of object of MAT as representative MAT
        representativeTrajectory = new MultipleAspectTrajectory("representative");
        
        if (!patternDateIn.equals("?")) {
            this.formatDate = new SimpleDateFormat(patternDateIn);
//            formatDateOut = formatDate;
            // Pisa Format
            formatDateOut = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        } else {
            this.formatDate = null;
            this.representativeTrajectory.setDailyInfo(true);
            dailyInfo = true;
            // Pisa Format
//            formatDateOut = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            //Foursquare
            formatDateOut = new SimpleDateFormat("HH:mm");
        }

        //Parameter for defining representativeness values and compute relevant cell
        this.threshold_rv = threshold_rv;

        load(); // Load dataset follow data model representation

        //rc is defined as the minimun number of points ( calculated by the % of all points) that should have in each cell
        this.threshold_rc = threshold_rc;
        rc = threshold_rc > 0.0 ? (threshold_rc * points.size()) : 2; //If threshold_rc is greater than zero sets threshold according with number of points, else sets to 2

        //As auxiliar var to load all input trajectories & all points of trajectories (validation)
        listAllTrajectories = List.copyOf(listTrajectories);
        listTrajectories = new ArrayList<>();
        List<Point> auxClusterPoints = List.copyOf(points);
        points = new ArrayList<Point>();

        loadAllDataset();

        List<MultipleAspectTrajectory> auxDataset = List.copyOf(listTrajectories);
        listTrajectories = null;
        listTrajectories = List.copyOf(listAllTrajectories);
        listAllTrajectories = null;
        listAllTrajectories = List.copyOf(auxDataset);
        points = List.copyOf(auxClusterPoints);

        //Compute minimun SpatialThreshold 
        computeMinSpatialThreshold();

        // ****** automation - definition of better Z value - the spatial threshold
        // Summarize Trajectories into a single representative data
        summarizeTrajetories();

    }

    public void summarizeTrajetories() throws ParseException, CloneNotSupportedException {

        int tempMaxZ = (int) auxMaxZ;
        int tempBetterZ = -1;
        float tempBetterRM = 0;
        float tempZvalueRM = 0;
        float iCoverZ = -1.0f;
        float tempOnlyRM;

        String infoBetterRT = "";
        int count = 0;

        while (tempMaxZ > 1) {
            resetValuesRT();
            resetValuesToSummarization();
            

            cellSizeSpace = (float) ((spatialThreshold * tempMaxZ) * 0.7071); // Calcultes size of the cells

            allocateAllPointsInCellSpace(); // Distributes all points in the spatial grid

            findCentroid(); //Creates the representative trajectory

            if (!representativeTrajectory.getPointList().isEmpty()) {
                tempZvalueRM = (float) medianSimilarityMeasure();
//                tempOnlyRM = tempZvalueRM;

                iCoverZ = (float) representativeTrajectory.getCoverPoints() / points.size();
                tempZvalueRM = (tempZvalueRM * 0.5f) + (iCoverZ * 0.5f);
//                tempZvalueRM = tempZvalueRM;
//                tempZvalueRM = (tempZvalueRM + iCoverZ) / 2; //*** melhor resultado

                if ((tempZvalueRM * 1.1) >= finalRMMAT) { //***melhor resultado
                    tempBetterZ = tempMaxZ;
                    finalRMMAT = tempZvalueRM;
                    count = 0;
                    betterRT = null;
                    betterRT = (MultipleAspectTrajectory) representativeTrajectory.clone();
                    auxTimeBetterRT = auxTimeInfo;
                    infoBetterRT = createInfoBetterRT(tempBetterZ);
                    betterZ = tempBetterZ;

                } else {
                    count++;
                }

            }

            tempMaxZ *= 0.85;

            if (count > 1) {
                break;

            }
        } // fim do laço infinito - // Fim automação
        if (tempBetterZ > 1) {
//            infoBetterRT += "\n Cells: \n";
//            for (Map.Entry<String, BitSet> entry : spatialCellGrid.entrySet()) {
//                infoBetterRT += entry.getKey()+" | ";
//            }
            String outputFile = directory + "output\\" + filename + " rc " + (int) (threshold_rc * 100) + " rv " + (int) (threshold_rv * 100);
            writeRepresentativeTrajectory(outputFile, infoBetterRT);
            rankMUITAS(outputFile);

        }

    }

    private String createInfoBetterRT(int tempBetterZ) {
        return tempBetterZ + "," // thresholdCellSize
                + cellSizeSpace + "," // cellSize
                + threshold_rc + "," // tauRelevantCell
                + threshold_rv + "," // tauRepresentativeValue
                + spatialCellGrid.size() + "," // |cell|
                + rc + "," // minPointRC
                + betterRT.getPointList().size() + "," // |rt|
                + betterRT.getCoverPoints(); // |coverPoints|
    }

    /**
     * Reads the dataset file and creates the all the MATs
     *
     * @throws IOException
     */
    private void load() throws IOException, ParseException {

        java.io.Reader input = new FileReader(directory + filename + extension);
        BufferedReader reader = new BufferedReader(input);

        String datasetRow = reader.readLine();
        //To Get the header of dataset
        String[] datasetColumns = datasetRow.split(SEPARATOR);

        //To add all types of attributes in the dataset, specified in the first line
        int order = 0;
        for (String s : Arrays.copyOfRange(datasetColumns, INDEX_SEMANTIC, datasetColumns.length)) {
            s = s.toUpperCase().trim();
             if (lstIgnoreCols == null || !lstIgnoreCols.contains(s)) {
                if (lstCategoricalsPD != null && lstCategoricalsPD.contains(s.toUpperCase())) //If attribute was predefined as categorical
                {
                     attributes.add(new SemanticAspect(s, order++, br.ufsc.model.SemanticType.CATEGORICAL));
                } else {
                    attributes.add(new SemanticAspect(s, order++));

                }
            } else {
                order++; //to skip column when it need to be ignored
            }

        }

        datasetRow = reader.readLine();
        //EoF - To get the trajectory data of dataset of each line
        while (datasetRow != null) {
            // Using regex to ignoring ',' '"' in dataset .
            datasetColumns = Util.splitCSV(datasetRow);
            addAttributeValues(datasetColumns);
            datasetRow = reader.readLine();
        }

        reader.close();

    }

    /**
     *
     * @param attrValues
     * @throws ParseException
     */
    private void addAttributeValues(String[] attrValues) throws ParseException {

        ++rId; //Id given to each data point 

        //Defines the semantic dimension as all attributes in predefined index to the end of line
        String[] semantics = Arrays.copyOfRange(attrValues, INDEX_SEMANTIC, attrValues.length);

        //All trajectory point follow the pattern:
        //id trajectory, coordinates (lat long), time, all semantic dimensions...
        // Follow the pattern add each MAT point in relative MAT
//        addTrajectoryData(attrValues[0], attrValues[1].split(" "), formatDate.parse(attrValues[2]), semantics);
        //For Running Example
        
        if (formatDate != null) {
            addTrajectoryData(attrValues[0], attrValues[1].split(" "), formatDate.parse(attrValues[2]), semantics);
//            System.out.println("Data: "+formatDate.parse(attrValues[2]));
        } else {
            addTrajectoryData(attrValues[0], attrValues[1].split(" "), Util.convertMinutesToDate(Integer.parseInt(attrValues[2])), semantics);
        }
    }

    /**
     * Add each MAT point in relative MAT object -- mapping input data to the
     * model predefined following O.O.
     *
     * @param tId - Id of MAT
     * @param coordinates - coordinates of point
     * @param time - time date of point
     * @param semantics - semantics attributes of point
     */
    private void addTrajectoryData(String tId, String[] coordinates, Date time, String[] semantics) {

        if (!tId.equals(auxTid)) { //IF the MAT is not created
            auxTid = tId;
            listTrajectories.add(new MultipleAspectTrajectory(Integer.valueOf(tId))); //Adds (Create) the new trajectory
            trajectory = listTrajectories.get(listTrajectories.size() - 1);
        }

        // aux values
        ArrayList<AttributeValue
                > attrs = new ArrayList<>();
        ord = 0;
        SemanticAspect a;

        //Organizes the point semantic attributes
        for (String val : semantics) {
            a = findAttributeForOrder(ord++);
            val = val.trim();
            if (a != null){
            if (a.getType() != null
                        && a.getType().equals(SemanticType.CATEGORICAL)) { //if it is predefined as Categorical

                    val = "*" + Integer.parseInt(val); // Use character '*' to force the number value to be a categorical value
                }
                attrs.add(new AttributeValue(val.toUpperCase().trim(), a));
            }
        }
        a = null; //clean memory 

        //Adds the MAT point to current MAT
        trajectory.addPoint(new Point(rId,// para mexer no id do ponto
                Double.parseDouble(coordinates[0]),
                Double.parseDouble(coordinates[1]),
                time,
                attrs));

        //Adds current MAT point to list of points
        points.add(trajectory.getLastPoint());

    }

    /**
     * allocate all points of input dataset in spatial cell grid
     */
    public void allocateAllPointsInCellSpace() {
        for (Point p : points) {
            allocateInSpaceCell(p);
        }
    }

    /**
     * add each point in the relative grid cell
     *
     * @param coordinates Coordinates of each trajectory point
     *
     */
    private static void allocateInSpaceCell(Point p) {

        //Get x,y of the point in the spatial grid
        String key = getCellPosition(p.getX(), p.getY());

        //Get id of the spatial grid cell
        BitSet rIds = spatialCellGrid.get(key);

        //If the cell doesn't exist
        if (rIds == null) {
            //Creates the cell and adds to the spatial grid
            rIds = new BitSet();
            rIds.set(p.getrId());
            spatialCellGrid.put(key, rIds);
        } else {
            rIds.set(p.getrId());
            spatialCellGrid.replace(key, rIds);
        }
    }

    /**
     * Compute the cell position based on x and y divided by the cell size
     * predefined (cellSizeSpace)
     *
     * @param x
     * @param y
     * @return Cell Position
     */
    private static String getCellPosition(double x, double y) {

        return ((int) Math.floor(x / cellSizeSpace)) + "," + ((int) Math.floor(y / cellSizeSpace));

    }

    /**
     * Compute the representative point of each cell in the spatial grid,
     * summarizating all aspects
     */
    public void findCentroid() {

//        int sizePoints = points.size();

        //Create iterator object of all spatial grid cells
        Iterator<String> cell = spatialCellGrid.keySet().iterator();
        while (cell.hasNext()) {
            String cellAnalyzed = cell.next(); //Selects next cell

            //Gets amount of points in the current cell
            int qntPoints = spatialCellGrid.get(cellAnalyzed).cardinality();

            if (qntPoints >= rc) { // IF number is at least a threshold RC

                resetValuesToSummarization();
                Centroid representativePoint = new Centroid(); //Creates new representative point

                // Loop in all points of the cell
                for (int pointId = spatialCellGrid.get(cellAnalyzed).nextSetBit(0);
                        pointId >= 0;
                        pointId = spatialCellGrid.get(cellAnalyzed).nextSetBit(pointId + 1)) {
                    
                    
                    //Adds point to representative source point list
                    Point p = points.get(pointId - 1);
                    representativePoint.addPoint(p); //To mapping the origin of the RP
                    
                    // Spatial data
                    avgX += p.getX();
                    avgY += p.getY();

                    //Semantic Data
                    Double val;
                    String attrActual; //storage attribute order to be used to get the object

                    for (AttributeValue atv : p.getListAttrValues()) {
                        attrActual = "" + atv.getAttibute().getOrder();

                        // numeric values - median computation 
                        //in this scope just create bitset with sum and count of values foreach quantitative attribute
                        try {

                            val = Double.parseDouble((String) atv.getValue()); // val -1 refers to empty value
                            
                            attributes.get(attributes.indexOf(atv.getAttibute())).setType(br.ufsc.model.SemanticType.NUMERICAL);
                            
                            if (!sematicNumericFusionVal.containsKey(attrActual)) {
                                sematicNumericFusionVal.put(attrActual, new ArrayList<Double>());
                            }

                            // add into this key the attribute value 
                            sematicNumericFusionVal.get(attrActual).add(val);

                        } catch (java.lang.NumberFormatException e) { //categorical values
                            /*
                            in this scope create the sematicCategoricalSummarizationVal with all possible values of each categorical attribute
                             and add its ids for after this step can computation the frequency of each one,
                             and identify the value more frequency of each qualitative attribute 
                             */

                            //IF not contains this key - attribute name
                            attributes.get(attributes.indexOf(atv.getAttibute())).setType(br.ufsc.model.SemanticType.CATEGORICAL);

                            if (!sematicCategoricalSummarizationVal.containsKey(atv.getAttibute())) {
                                sematicCategoricalSummarizationVal.put(atv.getAttibute(), new HashMap<String, Integer>());
                            }

                            // IF this attribute not contains this value
                            if (!sematicCategoricalSummarizationVal.get(atv.getAttibute()).containsKey(atv.getValue())) {
                                sematicCategoricalSummarizationVal.get(atv.getAttibute()).put((String) atv.getValue(), 1); //add this value to attribute and initialize the count
                            } else {
                                sematicCategoricalSummarizationVal.get(atv.getAttibute()).replace((String) atv.getValue(), sematicCategoricalSummarizationVal.get(atv.getAttibute()).get(atv.getValue()) + 1);
                            }

                        }
                    } //end FOR of all semantic attributes

                    //Temporal data
                    listTimesInCell.add(p.getTime().getStartTime()); // update: add start time (in Date) of point in a list 
                    
                }// end FOR each point in cell

                // spatial summarization
                //// Calculates average spatial position
                representativePoint.setSpatialDimension((avgX /= qntPoints), (avgY /= qntPoints));

                //Loop for numeric attributes
                sematicNumericFusionVal.entrySet().forEach((entrada) -> {
                    Double median;
                    Collections.sort(entrada.getValue());
                    //Calculates the median value for all numeric attributes of the representative point
                    if (entrada.getValue().size() % 2 == 0) {
                        median = (entrada.getValue().get(entrada.getValue().size() / 2) + entrada.getValue().get(entrada.getValue().size() / 2 - 1)) / 2;
                    } else {
                        median = entrada.getValue().get(entrada.getValue().size() / 2);
                    }

                    representativePoint.addAttrValue("" + median,
                            findAttributeForOrder(Integer.parseInt(entrada.getKey())));
                });

                //begin -------- Loop for a categorical attributes
                //To see the size of categorical attributes 
                //System.out.println("Size of categorical attr: "+sematicCategoricalSummarizationVal.size());
//                for (Map.Entry<String, Map<String, Integer>> allCategorical : sematicCategoricalSummarizationVal.entrySet()) {
                

            //begin -------- Loop for a categorical attributes
            for (Map.Entry<Object, Map<String, Integer>> allCategorical : sematicCategoricalSummarizationVal.entrySet()) {
                
                
                    Map<String, Integer> internalCategoricalList
                            = allCategorical.getValue().entrySet()
                                    .stream()
                                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
                    //Add mode value (tendency) of attribute to representative point
//                    representativePoint.addAttrValue(normalizeRankingValues(internalCategoricalList, qntPoints, 's'),
//                            findAttributeForOrder(Integer.parseInt(allCategorical.getKey())));
                    
                    representativePoint.addAttrValue(normalizeRankingValues(internalCategoricalList,
                            representativePoint.getPointListSource().size(),
                            's'),
                            (SemanticAspect) allCategorical.getKey()
                    );
                } // end ------------ Loop for a categorical attributes

                
                
                
                summarizeTemporalAspect(listTimesInCell, representativePoint);
                
                //Temporal data
//                defineRankingTemporal(listTimesInCell), qntPoints);
//                representativePoint.addAttrValue(normalizeTimeRank(normalizeRankingValues(defineRankingTemporal(listTimesInCell), qntPoints)),
//                representativePoint.addAttrValue(normalizeRankingValues(defineRankingTemporal(listTimesInCell), qntPoints, 't'),
//                        new("TIME"));
//                representativePoint.addAttrValue(normalizeRankingValues(defineRankingTemporal(listTimesInCell), qntPoints, 't'),
//                        new("TIME"));

                



            // after add all data in object point, add this representative point into representative MAT 
                representativeTrajectory.addPoint(representativePoint);
                
                representativeTrajectory.incrementValue(representativePoint.getPointListSource().size());

                
            } //End IF contains more than (RC) point in cell
            
        } // end loop each cell
//        System.out.println("Traj: "+representativeTrajectory);
    }// End method findCentroid

    /**
     * find the Attribute object by the order
     *
     * @param order
     * @return Attribute
     */
    public SemanticAspect findAttributeForOrder(int order) {
        for (SemanticAspect attr : attributes) {
            if (attr.getOrder() == order) {
                return attr;
            }
        }
        return null;
    }

    
    /**
     * For each valid temporal interval in the cell, a representative point is
     * defined -- For each valid STI it is created a new representative point,
     * but the computation of -- data summarization (spatial and semantic) it is
     * computed further (on computeCentroid method) -- i.e.
     * summarizeTemporalAspect summarize the Temporal dimension in each cell --
     * then find and define Representative points for cell
     *
     * This method computes the significant temporal intervals (STI) and
     * identifies representative points based on those intervals. These
     * representative points will be used for further computations in the
     * trajectory summarization.
     *
     * @param timeInPoints -- List of temporal information of all points in the
     * cell
     */
    public void summarizeTemporalAspect(ArrayList<Date> timeInPoints, Centroid repPoint) {
        List<STI> significantTemporalIntervals = new ArrayList<>();
        Collections.sort(timeInPoints);

        List<Integer> differences = computeTimeDifferences(timeInPoints);
        float averageDifference = Util.calculateAverage(differences);

        int thresholdDifferences = THRESHOLD_TIME; // Default threshold for <= 2 occurrences
        if (differences.size() > 2) {
            float median = Util.calculateMedian(differences);
            float standardDeviation = Util.calculateStandardDeviation(differences, averageDifference);

            float lowerValue = median - standardDeviation;
            float upperValue = median + standardDeviation;

            List<Integer> validDifferences = Util.removeOutliers(differences, lowerValue, upperValue);
            thresholdDifferences = (int) Math.floor(Util.calculateAverage(validDifferences));
        }

        createTemporalIntervals(timeInPoints, significantTemporalIntervals, thresholdDifferences);

        asortTemporalIntervals(significantTemporalIntervals);
      
        for(STI s: significantTemporalIntervals){
            repPoint.addSTI(s);
        }

    }//end summarizeTemporalAspect method
    
    
    private void createTemporalIntervals(List<Date> timeInPoints, List<STI> significantTemporalIntervals, int threshold) {
        int count = 1;
        TemporalAspect currentInterval = null;
        float sumProportions = 0;
        for (int i = 0; i < timeInPoints.size(); i++) {
            
            if (i != timeInPoints.size() - 1
                    && (TimeUnit.MINUTES.convert(timeInPoints.get(i).getTime(), TimeUnit.MILLISECONDS) + threshold)
                    >= (TimeUnit.MINUTES.convert(timeInPoints.get(i + 1).getTime(), TimeUnit.MILLISECONDS))) {
                if (currentInterval == null) {
                    currentInterval = new TemporalAspect(timeInPoints.get(i));
                }
                count++;
            } else {
                if (currentInterval == null) {
                    currentInterval = new TemporalAspect(timeInPoints.get(i));
                } else {
                    currentInterval.setEndTime(timeInPoints.get(i));
                }
                if((float) count / timeInPoints.size() >= threshold_rv){
                    significantTemporalIntervals.add(new STI(currentInterval, (float) count / timeInPoints.size()));
                    sumProportions += (float) count / timeInPoints.size();
                }
                count = 1;
                currentInterval = null;
            }
        }
        
        // Normalization of proportion temporal aspect
        if(sumProportions < 1){
            for(STI eachSTI: significantTemporalIntervals){
                eachSTI.setProportion(eachSTI.getProportion()/sumProportions);
            }
        }
    }

    private void asortTemporalIntervals(List<STI> significantTemporalIntervals) {
        significantTemporalIntervals.sort(Comparator.comparing(STI::getProportion).reversed());
    }
    
    private List<Integer> computeTimeDifferences(ArrayList<Date> timeInPoints) {
        List<Integer> differences = new ArrayList<>();
        for (int i = 1; i < timeInPoints.size(); i++) {
            int auxDif = (int) TimeUnit.MINUTES.convert(timeInPoints.get(i).getTime() - timeInPoints.get(i - 1).getTime(), TimeUnit.MILLISECONDS);
            if (auxDif > 0) {
                differences.add(auxDif);
            }
        }
        return differences;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    /// REMOVER
    /**
     * Computes the summarization of temporal data
     *
     * @param times occurence time of all input points in the cell
     * @return Map -- All STI (Significant Temporal Interval) and the number of
     * points in relative interval
     */
    public Map<String, Integer> defineRankingTemporal(List<Integer> times) {
        //order times
        Collections.sort(times);

        List<Integer> differences = new ArrayList<>(); //List of local time intervals

        //determine threshold by avg
        int threshold = 100; //the predefined threshold used when the time value is less than or equal to 2 occurrences

        //Begin -- Calculates the intervals of time
        float sumDifferences = 0;
        for (int i = 1; i < times.size(); i++) {
            differences.add(times.get(i) - times.get(i - 1));
            sumDifferences += differences.get(differences.size() - 1);
        }

        //Average temporal differences 
        float avg = sumDifferences / differences.size();

        sumDifferences = 0;

        if (differences.size() > 1) { //IF has more than 2 occurrences
            //order temporal differences
            Collections.sort(differences);

            /*
            compute the valid interval to remove the outliers
             -- computation: valid interval median minus and plus (- / +) SD.
             */
            //1st - compute the median value of the difference values
            if (differences.size() > 2) {
                int med;
                if (differences.size() % 2 == 1) {
                    med = differences.get(Math.floorDiv(differences.size(), 2));
                } else {
                    med = (differences.get(differences.size() / 2 - 1) + differences.get((differences.size() / 2))) / 2;
                }

                //2nd - Compute the SD
                for (int i = 0; i < differences.size(); i++) {
                    sumDifferences += Math.pow((differences.get(i) - avg), 2);
                }
                float SD = sumDifferences / differences.size();
                SD = (float) Math.sqrt(SD);

                //3rd - compute the valid interval (the value of median of temporal differences  minus and plus (- / +) SD)
                float lessValue = med - SD;
                float upperValue = med + SD;

                // update threshold value to average value of temporal differences
                //threshold = Math.floorDiv((int) (Math.abs(upperValue) - Math.abs(lessValue)), 2);
                //for removing outliers:
                sumDifferences = 0;
                //remove values temporal differences less and upper the valid interval defined
                for (int i = 0; i < differences.size(); i++) {
                    if (differences.get(i) < lessValue || differences.get(i) > upperValue) {
                        differences.remove(i);
                    } else {
                        sumDifferences += differences.get(i);
                    }
                }
                // update threshold value to average value of temporal differences considering only valid values
                threshold = Math.floorDiv((int) sumDifferences, differences.size());

            }
        }
        //End computation of temporal threshold

        String aux = null;
        int cont = 1;
        Map<String, Integer> temporalRanking = new HashMap<>();
        for (int i = 0; i < times.size(); i++) {

            /*
            IF the occurrence is not the last, 
            and two consecutive occurrences are considered a significant temporal interval (STI), 
            considering the threshold value 
             */
            if (i != times.size() - 1 && (times.get(i) + threshold) >= times.get(i + 1)) {
                if (aux == null) {
                    aux = "" + times.get(i);
                }
                cont++;
                /*
                IF has only one occurrence, this is add in the rank list or 
                if the occurence not is more considered into a previous STI 
                 */
            } else {
                if (aux == null) {
                    aux = "" + times.get(i);
                } else {
                    aux += "-" + times.get(i);
                }

                temporalRanking.put(aux, cont); //add occurrence or STI into rank list
                //reset aux values
                cont = 1;
                aux = null;
            }
        }
        // Ordernate temporal ranking 
        temporalRanking
                = temporalRanking.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        // END -- Calculates the intervals of time
        return temporalRanking;
        
        
        
    }

    
    
    
    // OK
    /**
     * Compute the average of the minimum spatial distance of the input MATs
     * points to provide dynamic space segmentation for clustering these input
     * points. Given set of input MATs, with n points, we compute the Euclidean
     * distance d() for each point pi ∈ T with the nearest point pk ∈ T.
     *
     * the spatialThreshold is computed as the average of the minimum spatial
     * distance the maximun Z value (diagonal size of each cell in the grid) is
     * computed as the max d() of the more distance point
     */
    public void computeMinSpatialThreshold() {
        float maxDistanceToZero = 0;
        float auxValueZ;

        float minDistance = Float.MAX_VALUE;
        float sumDistance = 0;
        ArrayList<Float> validDistances = new ArrayList<>();

        for (Point p : points) {
            auxValueZ = Util.euclideanDistanceToZero(p);
            if (auxValueZ > maxDistanceToZero) {
                maxDistanceToZero = auxValueZ;
            }

            for (Point q : points) {
                if (!p.equals(q)) {
                    float localDistance = (float) Util.euclideanDistance(p, q);
                    if (localDistance < minDistance) {
                        minDistance = localDistance;
                    }
                }
            }

            validDistances.add(minDistance);
            sumDistance += minDistance;
            minDistance = Float.MAX_VALUE;
        }

        float avgMinDist = sumDistance / points.size();

        // Remove outliers of minimum spatial distance
        if (validDistances.size() > 1) {
            // Order valid distances
            Collections.sort(validDistances);

            // Compute the median value of the distances
            float medianMinDist = Util.calculateMedian(validDistances);
            // Compute the standard deviation
            float sdMinDist = Util.calculateStandardDeviation(validDistances, avgMinDist);

            // Compute the valid interval
            float lessValueMinDist = medianMinDist - 4 * sdMinDist;
            float upperValueMinDist = medianMinDist + 4 * sdMinDist;

            // Create a new list with valid distances
            ArrayList<Float> validDistancesWithoutOutliers = new ArrayList<>();
            for (float distance : validDistances) {
                if (distance >= lessValueMinDist && distance <= upperValueMinDist && distance != 0.0) {
                    validDistancesWithoutOutliers.add(distance);
                }
            }

            // Compute the spatial threshold -- average of valid values
            spatialThreshold = br.ufsc.util.Util.calculateAverage(validDistancesWithoutOutliers);

            auxMaxZ = maxDistanceToZero / spatialThreshold;
        }
    }

    
    // OK
    /**
     * Writes the generated representative trajectory in a new .csv file
     *
     * @param fileOutput -- output file name
     * @param ext -- Extension of the file (e.g. csv)
     */
    public void writeRepresentativeTrajectory(String fileOutput, String infoBetterRT) {
        try {
            SimpleDateFormat formatRunTime = new SimpleDateFormat("yy-MM-dd HH:mm:ss.S");
            CSVWriter mxWriter = new CSVWriter(fileOutput + extension);

            mxWriter.writeLine("Info input dataset:");
            mxWriter.writeLine("|input.T|, |input.T.points|");
            mxWriter.writeLine(listTrajectories.size() + "," + points.size());
            mxWriter.writeLine("##");
            mxWriter.writeLine("RT setting infos:");

            mxWriter.writeLine("thresholdCellSize, CellSize, "
                    + "tauRelevantCell, tauRepresentativenessValue, "
                    + "|cell|, minPointRC, "
                    + "|rt|, |coverPoints|, "
                    + "runtime start, runtime end");

            mxWriter.writeLine(
                    infoBetterRT + ","
                    + formatRunTime.format(initialTemp) + ","
                    + formatRunTime.format(new Date())
            );
            mxWriter.writeLine("##");
            mxWriter.writeLine("RT description:");

            //Descrição do cabeçalho da RT
            String head = "lat_lon,time,";
            for (SemanticAspect att : attributes) {
                head += att.getName() + ",";
            }
            head += "mapping" //                    + ", cell"
                    ;

            mxWriter.writeLine(head);
            mxWriter.flush();
            for (Point p : betterRT.getPointList()) {
                Centroid rp = ((Centroid) p);
                String eachPoint = rp.getX() + " " + rp.getY() + ","; //Spatial info
                
                // Temporal info
                if (!rp.getListSTI().isEmpty()) {
                    eachPoint += "{";
                    for (STI sti : rp.getListSTI()) {
                        eachPoint += sti + "; ";
                    }
                    // Remove last ";", se existir
                    if (eachPoint.endsWith("; ")) {
                        eachPoint = eachPoint.substring(0, eachPoint.length() - 2);
                    }
                    eachPoint += "}";
                } 
                eachPoint += ", ";
                
                // Semantic dimension
                for (SemanticAspect att : attributes) {
                    AttributeValue atv = rp.findAttributeValue(att.getName());
                    eachPoint += atv == null ? "null ," : (atv.getValue().toString().replace(",", ";").replace("=", ": ") + ",");
                }
                eachPoint += rp.getMappingInformation();

                mxWriter.writeLine(eachPoint);
                mxWriter.flush();
            }
            mxWriter.flush();
            mxWriter.close();
        } catch (IOException e) {
            System.err.println("Error on writing RT: " + e.toString());
        }
    }

    /**
     * For updating the number of occurrences of each rank value by the ratio
     * value. Normalizing the Rank Value Map in the semantic or temporal
     * dimension, where the quantity of occurrences of each attribute value is
     * changed by the ratio of this value in relation to the size of the cell.
     *
     *
     * @param mapRank -- currently the Map of ranking values with number of
     * occurrences for each value
     * @param sizeCell -- size of points in the cell
     * @param dimension -- t: temporal and s: semantic
     * @return normalized -- the Map update with ratio values of occurrences.
     */
    public Map<Object, Double> normalizeRankingValues(Map<String, Integer> mapRank, int sizeCell, char dimension) {
        if (mapRank == null || mapRank.isEmpty()) {
            throw new IllegalArgumentException("Invalid input: mapRank must not be null or empty.");
        }
        if (sizeCell <= 0) {
            throw new IllegalArgumentException("Invalid input: sizeCell must be a positive value.");
        }
        Map<Object, Double> newMap = new HashMap<>();
        double trendEachVal;
        for (Map.Entry<String, Integer> eachValue : mapRank.entrySet()) {
            trendEachVal = (double) eachValue.getValue() / sizeCell;
            if (trendEachVal >= threshold_rv) {
                newMap.put(eachValue.getKey(), trendEachVal);
            }
        }

        Map<Object, Double> newMapSorted = newMap.entrySet().stream()
                .sorted(Map.Entry.<Object, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

//        if (dimension == 't') { // temporal dimension
//            /*
//            In the temporal dimension, the minutes values are converted to valid time according to the predefined mask.
//             */
//            auxTimeInfo = "{";
//            Map<Object, Double> newTimeMap = new HashMap<>();
//            for (Map.Entry<Object, Double> eachInt : newMapSorted.entrySet()) {
//                if (auxTimeInfo != "{") {
//                    auxTimeInfo += ";";
//                } 
////                else {
////                    auxTimeInfo += "{";
////                }
//                String interval = (String) eachInt.getKey();
//                String auxInterval;
//                if (interval.contains("-")) {
//                    auxInterval = formatDateOut.format(Util.convertMinutesToDate(Integer.parseInt(interval.substring(0, interval.indexOf("-")))));
//                    auxInterval += " - " + formatDateOut.format(Util.convertMinutesToDate(Integer.parseInt(interval.substring(interval.indexOf("-") + 1))));
//                } else {
//                    auxInterval = formatDateOut.format(Util.convertMinutesToDate(Integer.parseInt(interval)));
//                }
//
//                newTimeMap.put(auxInterval, newMap.get(interval));
//                auxTimeInfo += auxInterval + "::" + newMap.get(interval);
//            }
//            auxTimeInfo += "}";
//            return newTimeMap;
//
//        } else { // Semantic dimension
            return newMapSorted;
//        }

    }

    //// ---------------- Resets ------------------
    /**
     * Reset data of values to compute the better RT
     */
    public void resetValuesRT() {
        spatialCellGrid.clear();
        listTimesInCell.clear();
        representativeTrajectory = null;
        representativeTrajectory = new MultipleAspectTrajectory("representative");
        if (dailyInfo == true) {
            representativeTrajectory.setDailyInfo(true);
        }

    }

    /**
     * Reset all values of all attributes in MAT
     */
    public void resetValuesToSummarization() {
        //Data reset

        //spatial data
        avgX = 0;
        avgY = 0;

        // semantic data (multiple aspects)
        sematicNumericFusionVal.clear();
//                sematicFusionCount.clear();
        sematicCategoricalSummarizationVal.clear();

        //temporal data
        listTimesInCell.clear();
        finalRMMAT = 0;
    }
    


    // *********** Validation
    private void loadAllDataset() throws IOException, ParseException {

        java.io.Reader input = new FileReader(directory + filenameFullDataset + extension);
        BufferedReader reader = new BufferedReader(input);

        String datasetRow = reader.readLine();

        datasetRow = reader.readLine();
        String[] datasetColumns;
        //EoF - To get the trajectory data of dataset of each line
        while (datasetRow != null) {
            datasetColumns = datasetRow.toUpperCase().split(SEPARATOR);
            addAttributeValues(datasetColumns);
            datasetRow = reader.readLine();
        }

        reader.close();

    }

    // ############## For automatization
    // ################# -- dinamicaly identify better cell size -- #################
    /**
     * Compute the Representativeness Measure by Median for Recall
     *
     * @return Representativeness Measure by Median for Recall
     * @throws ParseException
     */
    public double medianSimilarityMeasure() throws ParseException {

        if (representativeTrajectory.getPointList().isEmpty()) {
            System.err.println("RT is empty");
            return -1;
        }

//        SimilarityMeasure measure = new SimilarityMeasure();
        MUITAS measure = new MUITAS();
        //Computing and setting thresholds
        //3D with equal weight (0.33) 
        measure.setWeight("SPATIAL", 0.34f);
        measure.setWeight("TIME", 0.33f);

        float auxWeight = 0.33f / (attributes.size());

        
        for (SemanticAspect eachAtt : attributes) {
            measure.setWeight(eachAtt, auxWeight);
            if (eachAtt.getType().equals(SemanticType.NUMERICAL)) {
                measure.setThreshold(eachAtt, 10);
//                measure.setThreshold(eachAtt, 1);
            }
        }

        //add spatial threshold -- Test: spatialThreshold x 2 -- pensando em atingir a distância de até 2 células
//        measure.setThreshold("SPATIAL", (spatialThreshold * 2));
        measure.setThreshold("SPATIAL", (spatialThreshold * 4));

//        measure.setPatternTemporalData(formatDateOut);
        double repMeasure = 0;
        List<Double> listValues = new ArrayList<>();

        for (MultipleAspectTrajectory eachTraj : listTrajectories) {
            listValues.add(measure.similarityOf(representativeTrajectory, eachTraj));
        }
        //after computed measure with each T and RT, it is computed median value
        repMeasure = Util.calculateMedian(listValues);

        return repMeasure;

    }

    public void rankMUITAS(String fileOutput) throws ParseException {
        if (betterRT.getPointList().isEmpty()) {
            System.out.println("RT is empty");
            return;
        }

        MUITAS measure = new MUITAS();

        // Compute weights
        float totalWeight = 1.0f;
        float spatialWeight = 0.34f;
        float timeWeight = 0.33f;
        float attributeWeight = totalWeight / attributes.size();
        measure.setWeight("SPATIAL", spatialWeight);
        measure.setWeight("TIME", timeWeight);
        for (SemanticAspect eachAtt : attributes) {
            measure.setWeight(eachAtt, attributeWeight);
            if (eachAtt.getType().equals(SemanticType.NUMERICAL)) {
                measure.setThreshold(eachAtt, 10);
            }
        }

        // Set spatial threshold
        measure.setThreshold("SPATIAL", spatialThreshold * 4);

//        measure.setPatternTemporalData(formatDateOut);
        
        // Compute rank measures
        Map<MultipleAspectTrajectory, Double> trajectoryRanks = new HashMap<>();
        StringBuilder infoMeasure = new StringBuilder();
        int countPrecisionRetrieved = 0;
        int countRecallRetrieved = 0;

        for (MultipleAspectTrajectory eachTraj : listAllTrajectories) {
            double similarity = measure.similarityOf(betterRT, eachTraj);
            trajectoryRanks.put(eachTraj, similarity);
        }

        trajectoryRanks = trajectoryRanks.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        int countTClass = 0;
        for (Map.Entry<MultipleAspectTrajectory, Double> retrievedT : trajectoryRanks.entrySet()) {
            countPrecisionRetrieved++;
            MultipleAspectTrajectory retrievedTrajectory = retrievedT.getKey();
            infoMeasure.append(retrievedTrajectory.getId()).append(",")
                    .append(retrievedT.getValue()).append(",")
                    .append(countPrecisionRetrieved).append(",");

            if (listTrajectories.contains(retrievedTrajectory)) {
                countTClass++;
                infoMeasure.append("1");

                if (countPrecisionRetrieved <= listTrajectories.size()) {
                    countRecallRetrieved++;
                }
            } else {
                infoMeasure.append("0");
            }
            infoMeasure.append("\n");

            if (countTClass == listTrajectories.size()) {
                break;
            }
        }

        // Write validation results to CSV file
        try {
            CSVWriter mxWriter = new CSVWriter(fileOutput+ " - z" + betterZ + "[Validation]" + extension);
            mxWriter.writeLine("Method validation information:");
            mxWriter.writeLine("|Ground Truth|, |all input dataset|, |T.P.retrieved|, Precision, |T.R.retrieved|, Recall, F-Score");
            mxWriter.writeLine(listTrajectories.size() + "," + listAllTrajectories.size() + ","
                    + countPrecisionRetrieved + "," + formatNumber.format((double) listTrajectories.size() / countPrecisionRetrieved) + ","
                    + countRecallRetrieved + "," + formatNumber.format((float) countRecallRetrieved / listTrajectories.size()) + ", ??");
            mxWriter.writeLine("##");
            mxWriter.writeLine("Measure description:");
            mxWriter.writeLine("Trajectory ID, MUITAS, #rank, Ground Truth?");
            mxWriter.writeLine(infoMeasure.toString());
            mxWriter.flush();
            mxWriter.close();

            String fileCompleteValidation = directory + "output\\" + filename + "[Validation]" + extension;
            CSVWriter valWriter;
            if (!new File(fileCompleteValidation).exists()) {
                valWriter = new CSVWriter(fileCompleteValidation);
                valWriter.writeLine("Method validation information:");
                valWriter.writeLine("Setting rv, Setting rc, |Ground Truth|, |all input dataset|, |T.P.retrieved|, Precision, |T.R.retrieved|, Recall, F-Score, RMMAT");
            } else {
                valWriter = new CSVWriter(fileCompleteValidation, true);
            }
            
            float precision = (float) listTrajectories.size() / countPrecisionRetrieved;
            float recall = (float) countRecallRetrieved / listTrajectories.size();
            float fScore = 2 * (precision * recall) / (precision + recall);
            
            valWriter.writeLine(formatNumber.format(threshold_rv) + "," + formatNumber.format(threshold_rc) + ","
                    + listTrajectories.size() + "," + listAllTrajectories.size() + ","
                    + countPrecisionRetrieved + "," + formatNumber.format(precision) + ","
                    + countRecallRetrieved + "," + formatNumber.format(recall) 
                    + ", "+formatNumber.format(fScore)
                    + ", "+formatNumber.format(finalRMMAT));
            valWriter.flush();
            valWriter.close();

        } catch (IOException e) {
            throw new RuntimeException("Error occurred while ranking input trajectories against RT", e);
        }
    }

    public void setFilenameFullDataset(String filenameFullDataset) {
        this.filenameFullDataset = filenameFullDataset;
    }

}
