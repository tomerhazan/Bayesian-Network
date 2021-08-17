import java.util.HashMap;

public class Algorithm1 {

    Var[] networkVars;                       // An array of objects of type: "Var" that holds all the variable data in the system
    Query queryToCalc;                       // A variable that holds the Query
    Var varToCalc;                           // A variable of object-type "Var" that holds the data of the variable we need to find in the query
    HashMap<String,String> queryGivenVars;   // A HashMap that keeps all the query evidence variables with their values.
    Var[] freeVars;                          // An array that holds all the Vars that are free.
    int numberOfAdd = 0;                     // keeps the number of Addition in the algorithm.
    int numberOfMult = 0;                    // keeps the number of Multiply in the algorithm.
    boolean bool;                            // A global variable that helps to know if we need to normalize or not.


    // THE MAIN FUNCTION:
    // 1. SET UP THE GLOBAL VARIABLES
    // 2. CHECK IF WE CAN GET THE ANSWER IMMEDIATELY. (IF YES -> GET THE ANSWER AND RETURN IT).
    // 3. IF NOT -> USE THE MAIN FUNCTION "CALC".

    public double calcAlgo(Var[] Variables, Query query,int numberOfAdd, int numberOfMult,boolean bo){
        bool = bo;
        numberOfAdd = numberOfAdd;
        numberOfMult = numberOfMult;
        networkVars = Variables;
        queryToCalc = query;
        queryGivenVars = (HashMap<String, String>) query.givenVars.clone();
        setVarToCalc();
        double ans = checkIfQueryAnsIsInTheCPT();
        if (ans != -1){
            return ans;
        }else{
            setFreeVars();
            return calc();
        }
    }


    // CALC FUNCTION:
    // STEP 1: BUILD HASHMAP THAT KEEPS THE EVIDENCE VARS AND VAR-TO-FIND WITH THEIR VALUES.

    // STEP 2: BUILD 2D MATRIX THAT KEEPS ALL THE COMBINATIONS FOR THE "FREE VARS" BY THEIR VALUES.
    // EXAMPLE: FREE VARS = A,B
    //          VALUES = {A -> TRUE,FALSE} {B -> TRUE,FALSE}
    //          MATRIX(A,B):
    //          {{TRUE,TRUE},{TRUE,FALSE}
    //           {FALSE,TRUE}, {FALSE,FALSE}}

    // STEP 3: BUILD EQUATION ANS GET ANSWER - EVERY TIME TAKES THE HASHMAP WITH A DIFFERENT CELL IN THE MATRIX ANS SENT IT TO CALC THE EQUATION (FOR EXAMPLE -> (A -> TRUE, B -> FALSE, E-> TRUE, J = TRUE, M = FALSE)).
    // NOTE - TO GET THE EQUATION ANSWER("calcEquation()"): I BUILD NEW QUERY WITH ALL THE EQUATION DATA (ALL VARS) -> SEND IT TO THE MAIN ALGO (LIKE REGULAR QUERY) -> AND IT RETURN THE ANSWER IMMEDIATELY CUZ THE QUERY EVIDENCE VARS CONTAINS ALL THE VARS EXCEPT THE VAR WE NEED TO FIND.
    // STEP 4: SUM ALL THE EQUATIONS.
    // STEP 5: IF GLOBAL VARIABLE "BOOL" IS TRUE WE NEED TO NORMALIZE.
    // STEP 6: RETURN THE ANSWER AFTER NORMALIZE.

    private double calc(){
        HashMap<String,String> equations = (HashMap<String, String>) queryGivenVars.clone();
        equations.put(queryToCalc.varNameToCalc,queryToCalc.varValue);
        String[][] buildEquationFreeVarsOptions;
        if(freeVars.length == 1){
            buildEquationFreeVarsOptions = buildFor1Var();
        }else if(freeVars.length == 2){
            buildEquationFreeVarsOptions = buildFor2Vars();
        }else{
            buildEquationFreeVarsOptions = buildFor2Vars();
            for(int i=2; i<freeVars.length;i++){
                buildEquationFreeVarsOptions = addVarToMat(buildEquationFreeVarsOptions,freeVars[i]);
            }
        }
        double ans = 0;
        HashMap<String,String> equation = new HashMap<String,String>();
        for (int i=0;i<buildEquationFreeVarsOptions.length;i++){
            for(int j=0; j<buildEquationFreeVarsOptions[i].length;j++){
                String[] cell = buildEquationFreeVarsOptions[i][j].split(",");
                for (int l=0; l<cell.length;l++){
                    equation = equations;
                    equation.put(freeVars[l].Name,cell[l]);
                }
                if (ans == 0){
                    ans = calcEquation(equation);
                }else{
                    ans += calcEquation(equation);
                    numberOfAdd++;
                }
            }
        }
        if (bool){
            double Nirmol = ans;
            for(int i=0;i<varToCalc.Values.length;i++){
                if (!varToCalc.Values[i].equals(queryToCalc.varValue)){
                    Algorithm1 algorithm1 = new Algorithm1();
                    Query q = new Query();
                    q.varNameToCalc = varToCalc.Name;
                    q.varValue = varToCalc.Values[i];
                    q.givenVars = (HashMap<String, String>) queryGivenVars.clone();
                    numberOfAdd++;
                    Nirmol += algorithm1.calcAlgo(networkVars,q,0,0,false);
                    numberOfAdd += algorithm1.numberOfAdd;
                    numberOfMult += algorithm1.numberOfMult;
                }
            }
            ans = ans/Nirmol;
        }
        return ans;
    }

    private double calcEquation(HashMap<String,String> equation){
        Algorithm1 algorithm1 = new Algorithm1();
        double ans = 1;
        for (int i=0; i<networkVars.length;i++){
            if (networkVars[i].ParentsNames.length == 0) {
                if (ans == 1){
                    ans = networkVars[i].Cpt.VarMap.get(equation.get(networkVars[i].Name));
                }else{
                    numberOfMult++;
                    ans = ans * networkVars[i].Cpt.VarMap.get(equation.get(networkVars[i].Name));
                }
            }else{
                Query query = new Query();
                query.varNameToCalc = networkVars[i].Name;
                query.varValue = equation.get(networkVars[i].Name);
                for(int j=0;j<networkVars[i].ParentsNames.length;j++){
                    query.givenVars.put(networkVars[i].ParentsNames[j],equation.get(networkVars[i].ParentsNames[j]));
                }
                for (int j = 0; j < networkVars.length; j++) {
                    if(!networkVars[j].Name.equals(query.varNameToCalc) && !query.givenVars.containsKey(networkVars[j].Name)){
                        for (int k = 0; k < networkVars[j].ParentsNames.length; k++) {
                            if(networkVars[j].ParentsNames[k].equals(query.varNameToCalc)){
                                query.givenVars.put(networkVars[j].Name,equation.get(networkVars[j].Name));
                            }
                        }
                    }
                }
                if (ans == 1){
                    ans = algorithm1.calcAlgo(networkVars,query,numberOfAdd,numberOfMult,bool);
                }else{
                    numberOfMult++;
                    ans = ans * algorithm1.calcAlgo(networkVars,query,numberOfAdd,numberOfMult,bool);
                }
            }
        }
        return ans;
    }

    // SIDE FUNCTIONS

    private String[][] addVarToMat(String[][] mat,Var var){
        String[][] newMat = new String[var.Values.length][mat.length*mat[0].length];
        for (int i=0; i<newMat.length;i++){
            for (int j=0;j<newMat[i].length;j++){
                for (int k=0;k< mat.length;k++){
                    for (int n = 0;n<mat[k].length;n++){
                        newMat[i][j] = mat[k][n] + "," + var.Values[i];
                        j++;
                    }
                }
            }
        }
        return newMat;
    }



    private String[][] buildFor2Vars(){
        String[][] mat;
        mat = buildFor1Var();
        String[][] newMat = new String[freeVars[1].Values.length][mat[0].length];
        for (int i=0; i<newMat.length;i++){
            for (int j=0;j<newMat[i].length;j++){
                newMat[i][j] = mat[0][j] + "," + freeVars[1].Values[i];
            }
        }
        return newMat;
    }

    private String[][] buildFor1Var(){
        String[][] mat = new String[1][freeVars[0].Values.length];
        for(int i=0;i<freeVars[0].Values.length;i++){
            mat[0][i] = freeVars[0].Values[i];
        }
        return mat;
    }

    private void setFreeVars(){
        freeVars = new Var[networkVars.length - queryGivenVars.size()-1];
        int freeVarsIndex = 0;
        for(int i = 0;i<networkVars.length;i++){
            if (!networkVars[i].Name.equals(queryToCalc.varNameToCalc)){
                if(!queryGivenVars.containsKey(networkVars[i].Name)){
                    freeVars[freeVarsIndex] = networkVars[i];
                    freeVarsIndex++;
                }
            }
        }
    }

    private void setVarToCalc(){
        for (int i=0; i<networkVars.length;i++){
            if (queryToCalc.varNameToCalc.equals(networkVars[i].Name)){
                varToCalc = networkVars[i];
            }
        }
    }

    private Double checkIfQueryAnsIsInTheCPT(){
        double returnValue = -1;
        if(queryGivenVars.size() == 0){
            if (varToCalc.ParentsNames.length == 0){
                returnValue = findPWithNoGivenVarsAndNoParents();
            }
        }else if (queryGivenVars.size() >= varToCalc.ParentsNames.length && childrenInEvidence()){
            returnValue = findPWithGivenVarsAndTheyParents();
        }
        return returnValue;
    }

    private boolean childrenInEvidence(){

        for (int i = 0; i < networkVars.length; i++) {
            if(!networkVars[i].Name.equals(varToCalc.Name)){
                for (int j = 0; j < networkVars[i].ParentsNames.length; j++) {
                    if(networkVars[i].ParentsNames[j].equals(varToCalc.Name)){
                        if(!queryGivenVars.containsKey(networkVars[i].Name)){
                            return false;
                        }
                    }
                }
            }
        }


        return true;
    }

    private Double findPWithGivenVarsAndTheyParents(){
        double answer = -1;
        int counter = 0;                                           // count the number of times parents given value is in the CPT of the value, if counter == parents.length = we get the Probability.
        for (String[] CPTKey: varToCalc.Cpt.parentsMap.keySet()){
            for (int i=0; i< CPTKey.length;i = i+2){
                if(queryGivenVars.containsKey(CPTKey[i])){
                    if(CPTKey[i+1].equals(queryGivenVars.get(CPTKey[i]))){
                        counter++;
                        if (counter == varToCalc.ParentsNames.length){
                            String[] ans = varToCalc.Cpt.parentsMap.get(CPTKey);
                            for(int j=0; j<ans.length;j=j+2){
                                if (ans[j].equals(queryToCalc.varValue)){
                                    answer = Double.parseDouble(ans[j+1]);
                                    return answer;
                                }
                            }
                        }
                    }
                    else{
                        counter = 0;
                        break;
                    }
                }
            }
        }
        return answer;
    }

    private Double findPWithNoGivenVarsAndNoParents(){
        for(String key: varToCalc.Cpt.VarMap.keySet()){
            if(queryToCalc.varValue.equals(key)){
                return varToCalc.Cpt.VarMap.get(key);
            }
        }
        return (double)-1;
    }
}