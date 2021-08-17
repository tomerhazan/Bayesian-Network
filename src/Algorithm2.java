import java.util.*;

public class Algorithm2 {

    Query query;                                                                // A variable that holds the query data
    Var[] variables;                                                            // An object-type array that holds all the vars that are in the network

    Var varToCalc;                                                              // Var that we want to calculate

    HashMap<String, String> evidenceVars = new HashMap<String, String>();       // HashMap that keeps all the evidence Vars with their value that was given by the query
    Var[] evidenceVar;                                                          // An object-type array that holds all the vars that are in the evidence

    HashMap<String, String[][]> factors = new HashMap<String, String[][]>();    // HashMap that keeps all the factors in 2D Matrix - > before transform it to HashMap FactorsMap
    HashMap<String, HashMap> factorsMap = new HashMap<String, HashMap>();       // HashMap that keeps all the factors that we really need for the query

    Var[] hiddenVars;                                                           // An object-type array that holds all the vars that are not in the evidence

    int numberOfAdd = 0;                                                            // A variable that holds the number of addition operations we performed in the algorithm
    int numberOfMult = 0;                                                           // A variable that holds the number of multiplication operations we performed in the algorithm


    // THE MAIN FUNCTION:
    // 1. SET UP THE GLOBAL VARIABLES
    // 2. CHECK IF WE CAN GET THE ANSWER IMMEDIATELY. (IF YES -> GET THE ANSWER AND RETURN IT).
    // 3. IF NOT:
    //    1. BUILD ALL FACTORS("initialFactors()").
    //    2. DELETE FACTORS WITH 1 LINE ("deleteFactorsWith1Row()").
    //    3. DELETE THE FACTORS OF THE VARS THAT IS INDEPENDENT WITH THE VAR-TO-FIND(delete()).
    //    4. THE MAIN CALC FUNCTION ("joinFactor()").
    //    5. NORMALIZE ("normalize2()").

    public double calcAlgo(Var[] Variables, Query queryy) {

        double ans = 0;
        query = queryy;

        setVariables(Variables);

        evidenceVars = (HashMap<String, String>) query.getGivenVars().clone();

        setVars();
        if (varToCalc.ParentsNames.length == 0 && evidenceVars.size() == 0) {
            ans = getVarProbWithNoParentsAndNoEvidence();
        } else if (checkParentsWithEvidence() && checkIfNoSuns()) {
            ans = getVarProbWithParentsContainsInEvidence();
        } else {
            initialFactors();
            deleteFactorsWith1Row();
            delete();
            factorsToMap();
            joinFactor(true);
            ans = normalize();
        }
        return ans;
    }

    // THE MAIN CALC FUNCTION:
    // STEP 1: BUILD A SORTED LIST BY "ABC".
    // STEP 2: FOR EVERY VARIABLE IN THE SORTED LIST:
    //         1. GET ALL THE FACTORS THAT RELATED TO HIM. (PUT THEM IN "joinByName" HASHMAP)
    //         2. REMOVE THEM FROM THE MAIN FACTOR LIST
    //         3. WHILE THERE IS STILL 2 FACTORS IN "joinByName" HASHMAP -> JOIN THEM AND PUT THE ANSWER IN "joinByName".
    //         4. JOIN -> PICK 2 FACTORS BY SIZE OR ASCII AND JOIN THEM IN THE "join2Factors()" FUNCTION.
    //         5. IF "joinByName" CONTAINS 1 FACTOR -> ELIMINATE.
    //         6. PUT THE NEW FACTOR IN THE MAIN FACTORS LIST FOR THE NEW VARIABLE.

    private void joinFactor(boolean bool) {
        HashMap<String, String> sortedHid = new HashMap<String, String>();
        if (bool) {
            for (int i = 0; i < hiddenVars.length; i++) {
                sortedHid.put(hiddenVars[i].Name, "");
            }
        } else {
            sortedHid.put(varToCalc.Name, "");
        }
        TreeMap<String, String> sortedHidden = new TreeMap<String, String>(sortedHid);
        for (String key : sortedHidden.keySet()) {
            HashMap<String, HashMap> joinByName = new HashMap<String, HashMap>();
            if (bool) {
                for (String factorsKey : factorsMap.keySet()) {
                    //if (!factorsKey.equals(varToCalc.Name)){
                    if (factorsKey.equals(key)) {
                        joinByName.put(factorsKey, factorsMap.get(factorsKey));
                    } else {
                        HashMap<HashMap<String, String>, String> m = (HashMap<HashMap<String, String>, String>) factorsMap.get(factorsKey).clone();
                        for (Object mKey : m.keySet()) {
                            HashMap<String, String> m1 = (HashMap<String, String>) mKey;
                            for (String name : m1.keySet()) {
                                if (name.equals(key)) {
                                    joinByName.put(factorsKey, factorsMap.get(factorsKey));
                                }
                            }
                        }
                    }

                }
            } else {
                joinByName = (HashMap<String, HashMap>) factorsMap.clone();
            }

            // remove the factors that we are going to join from the main factors array - "factorsMap"
            for (String joinKey : joinByName.keySet()) {
                if (factorsMap.containsKey(joinKey)) {
                    factorsMap.remove(joinKey);
                }
            }
            // while loop - join all the tables until we have 1 last table;
            while (joinByName.size() > 1) {
                HashMap<HashMap<String, String>, String> factor1 = null;
                HashMap<HashMap<String, String>, String> factor2 = null;
                int factor1ASCII = 0;
                String factor1Name = "";
                int factor2ASCII = 0;
                String factor2Name = "";
                int index = 1;
                for (String keyy : joinByName.keySet()) {
                    if (index == 1) {
                        factor1 = joinByName.get(keyy);
                        for (int i = 0; i < keyy.length(); i++) {
                            factor1ASCII += (int) keyy.charAt(i);
                        }
                        factor1Name = keyy;
                        index++;
                    } else if (index == 2) {
                        factor2 = joinByName.get(keyy);
                        for (int i = 0; i < keyy.length(); i++) {
                            factor2ASCII += (int) keyy.charAt(i);
                        }
                        factor2Name = keyy;
                        if (factor1.size() > factor2.size()) {
                            HashMap<HashMap<String, String>, String> temp = factor2;
                            int tempp = factor2ASCII;
                            String nameTemp = factor2Name;
                            factor2 = factor1;
                            factor2ASCII = factor1ASCII;
                            factor2Name = factor1Name;
                            factor1ASCII = tempp;
                            factor1 = temp;
                            factor1Name = nameTemp;
                        }
                        index++;
                    } else {
                        HashMap<HashMap<String, String>, String> temp = joinByName.get(keyy);
                        int tempASCII = 0;
                        for (int i = 0; i < keyy.length(); i++) {
                            tempASCII += (int) keyy.charAt(i);
                        }
                        String tempName = keyy;
                        if (factor1.size() == factor2.size() && temp.size() == factor1.size()) {
                            if ((factor1ASCII + tempASCII) < (factor1ASCII + factor2ASCII) && (factor1ASCII + tempASCII) < (tempASCII + factor2ASCII)) {
                                factor2 = temp;
                                factor2ASCII = tempASCII;
                                factor2Name = tempName;
                            } else if ((factor2ASCII + tempASCII) < (factor2ASCII + factor1ASCII) && (factor2ASCII + tempASCII) < (tempASCII + factor1ASCII)) {
                                factor1 = temp;
                                factor1ASCII = tempASCII;
                                factor1Name = tempName;
                            }
                        } else if (temp.size() == factor2.size()) {
                            if ((tempASCII + factor1ASCII) < (factor1ASCII + factor2ASCII)) {
                                factor2 = temp;
                                factor2ASCII = tempASCII;
                                factor2Name = tempName;
                            }
                        } else if (temp.size() < factor1.size()) {
                            factor2 = factor1;
                            factor2ASCII = factor1ASCII;
                            factor2Name = factor1Name;
                            factor1 = temp;
                            factor1ASCII = tempASCII;
                            factor1Name = tempName;
                        } else if (temp.size() < factor2.size()) {
                            factor2 = temp;
                            factor2ASCII = tempASCII;
                            factor2Name = tempName;
                        }
                        index++;
                    }
                }
                Iterator<String> iterator = joinByName.keySet().iterator();
                while (iterator.hasNext()) {
                    String certification = iterator.next();
                    if (certification.equals(factor1Name) || certification.equals(factor2Name)) {
                        iterator.remove();
                    }
                }
                //HashMap<HashMap<String, String>, String> newFactor = join2Factors(factor1, factor2, key);
                HashMap<HashMap<String, String>, String> other = findFactor(factor1,factor2,key);
                other = combine2FactorsData(other,factor1,factor2,key);
                joinByName.put(factor1Name+" join " + factor2Name, other);
            }
            if (joinByName.size() == 1) {
                for (String k : joinByName.keySet()) {
                    HashMap<HashMap<String, String>, String> eliminateFactor = (HashMap<HashMap<String, String>, String>) joinByName.get(k).clone();
                    if (bool) {
                        HashMap<HashMap<String, String>, String> newEliminateFactor = new HashMap<HashMap<String, String>, String>();
                        for (Object eliminateKey : eliminateFactor.keySet()) {
                            HashMap<String, String> eliKeyValue = (HashMap<String, String>) eliminateKey;
                            HashMap<String, String> newEliKeyValue = new HashMap<String, String>();
                            for (String KEY : eliKeyValue.keySet()) {
                                if (!KEY.equals(key)) {
                                    newEliKeyValue.put(KEY, eliKeyValue.get(KEY));
                                }
                            }
                            newEliminateFactor.put(newEliKeyValue, "0");
                        }
                        for (Object eliminateKey : eliminateFactor.keySet()) {
                            HashMap<String, String> eliKeyValue = (HashMap<String, String>) eliminateKey;
                            for (Object newEliminateKey : newEliminateFactor.keySet()) {
                                HashMap<String, String> neweliKeyValue = (HashMap<String, String>) newEliminateKey;
                                boolean isTrue = true;
                                for (String ke : neweliKeyValue.keySet()) {
                                    if (eliKeyValue.containsKey(ke)) {
                                        if (!eliKeyValue.get(ke).equals(neweliKeyValue.get(ke))) {
                                            isTrue = false;
                                            break;
                                        }
                                    }
                                }
                                if (isTrue) {
                                    double d1 = Double.parseDouble(eliminateFactor.get(eliKeyValue));
                                    double d2 = Double.parseDouble(newEliminateFactor.get(newEliminateKey));
                                    double ans = d1 + d2;
                                    if (d2 != 0) {
                                        numberOfAdd++;
                                    }
                                    newEliminateFactor.put(neweliKeyValue, ans + "");
                                }
                            }
                        }
                        factorsMap.put(key, newEliminateFactor);
                    } else {
                        factorsMap.put(key, eliminateFactor);
                    }
                }
            }
        }
        if (bool) {
            joinFactor(false);
        }
    }

    private void delete() {
        ArrayList<Var> hiddenV = new ArrayList<>();
        hiddenV.add(varToCalc);
        for (int i = 0; i < variables.length; i++) {
            if (evidenceVars.containsKey(variables[i].Name)){
                for (int j = 0; j < variables[i].ParentsNames.length; j++) {
                    if(variables[i].ParentsNames[j].equals(varToCalc.Name)){
                        if (!(variables[i].ParentsNames.length == 1)){
                            if (!hiddenV.contains(variables[i].ParentsNames[j]) && factors.containsKey(variables[i].ParentsNames[j]) && !query.givenVars.containsKey(variables[i].Name)){
                                hiddenV.add(variables[i]);
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < variables.length; i++) {
            if (evidenceVars.containsKey(variables[i].Name)){
                for(int j=0;j< variables[i].ParentsNames.length;j++){
                    for (int k = 0; k < variables.length; k++) {
                        if (variables[i].ParentsNames[j].equals(variables[k].Name)) {
                            if (!hiddenV.contains(variables[k]) && factors.containsKey(variables[k].Name) && !query.givenVars.containsKey(variables[k].Name)) {
                                hiddenV.add(variables[k]);
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < hiddenV.size(); i++) {
            for (int j = 0; j < hiddenV.get(i).ParentsNames.length; j++) {
                for(int k=0;k<variables.length;k++){
                    if (hiddenV.get(i).ParentsNames[j].equals(variables[k].Name)){
                        if (!hiddenV.contains(variables[k])) {
                            hiddenV.add(variables[k]);
                        }
                    }
                }
            }
        }
        ArrayList<Var> newV = new ArrayList<>();
        for (int i = 0; i < hiddenV.size(); i++) {
            for (int j = 0; j < hiddenVars.length; j++) {
                if(hiddenV.get(i).Name.equals(hiddenVars[j].Name)){
                    newV.add(hiddenV.get(i));
                }
            }
        }
        Var[] h = new Var[newV.size()];
        h = newV.toArray(h);
        hiddenVars = h;

        HashMap<String, String[][]> newFactors = (HashMap<String, String[][]>) factors.clone();
        for(String key: newFactors.keySet()){
            boolean isTrue = false;
            for(int i=0;i< hiddenVars.length;i++){
                if(key.equals(hiddenVars[i].Name) || key.equals(varToCalc.Name)){
                    isTrue = true;
                }else{
                    String[][] mat = newFactors.get(key);
                    boolean is = true;
                    for (int j = 0; j < mat[0].length-1; j++) {
                        if (mat[0][j].equals(key)){
                            is = false;
                        }
                    }
                    if(is){
                        isTrue = true;
                    }
                }
            }
            if(!isTrue){
                factors.remove(key);
            }
        }
    }

    private double normalize() {
        double Nirmol = 0;
        double ans = 0;
        numberOfAdd--;
        for (String key : factorsMap.keySet()) {
            HashMap<HashMap<String, String>, String> factor = (HashMap<HashMap<String, String>, String>) factorsMap.get(key).clone();
            for (Object key2 : factor.keySet()) {
                Nirmol += Double.parseDouble(factor.get(key2));
                numberOfAdd++;
                HashMap<String, String> map = (HashMap<String, String>) key2;
                for (String lastKey : map.keySet()) {
                    if (lastKey.contains(query.varNameToCalc)) {
                        if (map.get(lastKey).equals(query.varValue)) {
                            ans = Double.parseDouble(factor.get(key2));
                        }
                    }
                }

            }

        }
        return ans / Nirmol;
    }


    private void factorsToMap() {
        for (String key : factors.keySet()) {
            HashMap<HashMap<String, String>, String> line = new HashMap<HashMap<String, String>, String>();
            String[][] factor = factors.get(key);
            for (int i = 1; i < factor.length; i++) {
                HashMap<String, String> varNameValue = new HashMap<String, String>();
                for (int j = 0; j < factor[i].length; j++) {
                    if (j == factor[i].length - 1) {
                        line.put(varNameValue, factor[i][j]);
                    } else {
                        varNameValue.put(factor[0][j], factor[i][j]);
                    }
                }
            }
            factorsMap.put(key, line);
        }
    }


    private HashMap combine2FactorsData(HashMap newFactor, HashMap factor1,HashMap factor2,String name){

        HashMap<HashMap<String, String>, String> temp = (HashMap<HashMap<String, String>, String>) newFactor.clone();
        for (Object keyy : factor1.keySet()) {
            HashMap<String, String> key = (HashMap<String, String>) keyy;
            for (Object tempkey : temp.keySet()) {
                HashMap<String, String> tempk = (HashMap<String, String>) tempkey;
                boolean bool = true;
                for (String kkeeyy : key.keySet()) {
                    if (tempk.containsKey(kkeeyy)) {
                        if (!tempk.get(kkeeyy).equals(key.get(kkeeyy))) {
                            bool = false;
                            break;
                        }
                    }
                }
                if (bool) {
                    temp.put(tempk, factor1.get(keyy) + "");
                }
            }
        }
        for (Object keyy : factor2.keySet()) {
            HashMap<String, String> key = (HashMap<String, String>) keyy;
            for (Object tempkey : temp.keySet()) {
                HashMap<String, String> tempk = (HashMap<String, String>) tempkey;
                boolean bool = true;
                for (String kkeeyy : key.keySet()) {
                    if (tempk.containsKey(kkeeyy)) {
                        if (!tempk.get(kkeeyy).equals(key.get(kkeeyy))) {
                            bool = false;
                            break;
                        }
                    }
                }
                if (bool) {
                    String s = (String) factor2.get(keyy);
                    double f2 = Double.parseDouble(s);
                    double t = Double.parseDouble(temp.get(tempk));
                    numberOfMult++;
                    double ans = f2 * t;
                    temp.put(tempk, ans + "");
                }
            }
        }
        return temp;
    }

    private HashMap join2Factors(HashMap factor1, HashMap factor2, String name) {
        HashMap<HashMap<String, String>, String> newFactor = (HashMap<HashMap<String, String>, String>) factor1.clone();
        HashMap<HashMap<String, String>, String> temp = new HashMap<HashMap<String, String>, String>();
        String newFactorName = name;
        int index = 0;
        for (Object key : factor2.keySet()) {
            HashMap<String, String> k = (HashMap<String, String>) key;
            for (String keyValue : k.keySet()) {
                HashMap<String, String> firstLine = null;
                for (Object KEY : newFactor.keySet()) {
                    firstLine = (HashMap<String, String>) KEY;
                    break;
                }
                if (!firstLine.containsKey(keyValue)) {
                    for (int i = 0; i < variables.length; i++) {
                        if (variables[i].Name.equals(keyValue)) {
                            if (index == 0) {
                                for (Object KEY : newFactor.keySet()) {
                                    for (int j = 0; j < variables[i].Values.length; j++) {
                                        HashMap<String, String> line = (HashMap<String, String>) KEY;
                                        HashMap<String, String> l = (HashMap<String, String>) line.clone();
                                        l.put(keyValue, variables[i].Values[j]);
                                        temp.put(l, "");
                                    }
                                }
                            } else {
                                HashMap<HashMap<String, String>, String> t = (HashMap<HashMap<String, String>, String>) temp.clone();
                                temp.clear();
                                for (Object KEY : t.keySet()) {
                                    for (int j = 0; j < variables[i].Values.length; j++) {
                                        HashMap<String, String> line = (HashMap<String, String>) KEY;
                                        HashMap<String, String> l = (HashMap<String, String>) line.clone();
                                        l.put(keyValue, variables[i].Values[j]);
                                        temp.put(l, "");
                                    }
                                }
                            }
                            index++;
                        }
                    }
                }
            }
            break;
        }
        for (Object keyy : factor1.keySet()) {
            HashMap<String, String> key = (HashMap<String, String>) keyy;
            for (Object tempkey : temp.keySet()) {
                HashMap<String, String> tempk = (HashMap<String, String>) tempkey;
                boolean bool = true;
                for (String kkeeyy : key.keySet()) {
                    if (tempk.containsKey(kkeeyy)) {
                        if (!tempk.get(kkeeyy).equals(key.get(kkeeyy))) {
                            bool = false;
                            break;
                        }
                    }
                }
                if (bool) {
                    temp.put(tempk, factor1.get(keyy) + "");
                }
            }
        }
        for (Object keyy : factor2.keySet()) {
            HashMap<String, String> key = (HashMap<String, String>) keyy;
            for (Object tempkey : temp.keySet()) {
                HashMap<String, String> tempk = (HashMap<String, String>) tempkey;
                boolean bool = true;
                for (String kkeeyy : key.keySet()) {
                    if (tempk.containsKey(kkeeyy)) {
                        if (!tempk.get(kkeeyy).equals(key.get(kkeeyy))) {
                            bool = false;
                            break;
                        }
                    }
                }
                if (bool) {
                    String s = (String) factor2.get(keyy);
                    double f2 = Double.parseDouble(s);
                    double t = Double.parseDouble(temp.get(tempk));
                    numberOfMult++;
                    double ans = f2 * t;
                    temp.put(tempk, ans + "");
                }
            }
        }
        return temp;
    }

    // SIDE FUNCTION.

    private HashMap<HashMap<String, String>, String> findFactor(HashMap factor1, HashMap factor2,String name){

        HashMap<HashMap<String, String>, String> newFactor = (HashMap<HashMap<String, String>, String>) factor1.clone();

        for (Object factor2Key: factor2.keySet()){
            HashMap<String,String> factor2Lines = (HashMap<String, String>) factor2Key;

            for(String factor2LinesKey: factor2Lines.keySet()){

                if(!checkIfVarIsInTheFactor(newFactor,factor2LinesKey)){

                    String[] varValuesToAdd = getVarValuesByName(factor2LinesKey);

                    newFactor = addVarValuesToFactor(newFactor,varValuesToAdd,factor2LinesKey);
                }
            }
            break;
        }
        return newFactor;
    }

    private HashMap<HashMap<String, String>, String> addVarValuesToFactor(HashMap factor,String[] varValuesToAdd, String varName){

        HashMap<HashMap<String, String>, String> newFactor = new HashMap<>();

        for(Object factorLinesKey: factor.keySet()){
            HashMap<String, String> factorLines = (HashMap<String, String>) factorLinesKey;
            for (int i = 0; i < varValuesToAdd.length; i++) {
                HashMap<String, String> lineToAdd = (HashMap<String, String>) factorLines.clone();
                lineToAdd.put(varName,varValuesToAdd[i]);
                newFactor.put(lineToAdd,"");
            }
        }
        return newFactor;
    }

    private String[] getVarValuesByName(String varName){
        String[] arr = new String[0];
        for(int i=0; i<variables.length;i++){
            if (variables[i].Name.equals(varName)){
                arr = variables[i].Values;
            }
        }
        return arr;
    }

    private Boolean checkIfVarIsInTheFactor(HashMap factor,String key){

        for(Object facto2Key: factor.keySet()){
            HashMap<String,String> factorLine = (HashMap<String, String>) facto2Key;
            for (String factroKey: factorLine.keySet()){
                if(factroKey.equals(key)){
                    return true;
                }
            }
            break;
        }

        return false;
    }


    private void deleteFactorsWith1Row() {
        Iterator<String> iterator = factors.keySet().iterator();
        while (iterator.hasNext()) {
            String certification = iterator.next();
            String[][] factor = factors.get(certification);
            if (factor.length == 2) {
                iterator.remove();
            }
        }
    }


    private double getVarProbWithParentsContainsInEvidence() {
        double ans = 0;
        int counter = 0;
        for (String[] CPTKey : varToCalc.Cpt.parentsMap.keySet()) {
            for (int i = 0; i < CPTKey.length; i = i + 2) {
                if (evidenceVars.containsKey(CPTKey[i])) {
                    if (CPTKey[i + 1].equals(evidenceVars.get(CPTKey[i]))) {
                        counter++;
                        if (counter == varToCalc.ParentsNames.length) {
                            String[] getValues = varToCalc.Cpt.parentsMap.get(CPTKey);
                            for (int j = 0; j < getValues.length; j = j + 2) {
                                if (getValues[j].equals(query.varValue)) {
                                    ans = Double.parseDouble(getValues[j + 1]);
                                    return ans;
                                }
                            }
                        }
                    } else {
                        counter = 0;
                        break;
                    }
                }
            }
        }
        return ans;
    }

    private double getVarProbWithNoParentsAndNoEvidence() {
        double ans = 0;
        for (String key : varToCalc.Cpt.VarMap.keySet()) {
            if (query.varValue.equals(key)) {
                ans = varToCalc.Cpt.VarMap.get(key);
                return ans;
            }
        }
        return ans;
    }

    private boolean checkIfNoSuns(){
        for (int i = 0; i < variables.length; i++) {
            for (int j = 0; j < variables[i].ParentsNames.length; j++) {
                if(variables[i].ParentsNames[j].equals(varToCalc.Name)){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkParentsWithEvidence() {
        if (varToCalc.ParentsNames.length == evidenceVars.size()) {
            for (int i = 0; i < varToCalc.ParentsNames.length; i++) {
                if (!evidenceVars.containsKey(varToCalc.ParentsNames[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void setVars() {
        hiddenVars = new Var[variables.length - evidenceVars.size() - 1];
        int hiddenVarsIndex = 0;
        for (int i = 0; i < variables.length; i++) {
            if (!evidenceVars.containsKey(variables[i].Name)) {
                if (!variables[i].Name.equals(query.varNameToCalc)) {
                    hiddenVars[hiddenVarsIndex++] = variables[i];
                } else {
                    varToCalc = variables[i];
                }
            }
        }
        evidenceVar = new Var[evidenceVars.size()];
        int evidenceVarsIndex = 0;
        for (String evidenceKey : evidenceVars.keySet()) {
            for (int i = 0; i < variables.length; i++) {
                if (evidenceKey.equals(variables[i].Name)) {
                    evidenceVar[evidenceVarsIndex++] = variables[i];
                }
            }
        }
    }

    private void setVariables(Var[] Variables) {
        variables = new Var[Variables.length];
        for (int i = 0; i < Variables.length; i++) {
            Var var = new Var(Variables[i].Name);
            var.Values = Variables[i].Values;
            var.ParentsNames = Variables[i].ParentsNames;
            if (var.ParentsNames.length == 0) {
                var.Cpt.VarMap = (HashMap<String, Double>) Variables[i].Cpt.VarMap.clone();
            } else {
                var.Cpt.parentsMap = (HashMap<String[], String[]>) Variables[i].Cpt.parentsMap.clone();
            }
            variables[i] = var;
        }
    }

    private void initialFactors() {
        for (int i = 0; i < evidenceVar.length; i++) {
            String[] newValuess = new String[1];
            for (int j = 0; j < evidenceVar[i].Values.length; j++) {
                if (evidenceVar[i].Values[j].equals(evidenceVars.get(evidenceVar[i].Name))) {
                    newValuess[0] = evidenceVar[i].Values[j];
                }
            }
            evidenceVar[i].Values = newValuess;
            if (evidenceVar[i].ParentsNames.length == 0) {
                Iterator<String> iterator = evidenceVar[i].Cpt.VarMap.keySet().iterator();
                while (iterator.hasNext()) {
                    String certification = iterator.next();
                    if (!certification.equals(evidenceVars.get(evidenceVar[i].Name))) {
                        iterator.remove();
                    }
                }
            } else {
                for (String[] key : evidenceVar[i].Cpt.parentsMap.keySet()) {
                    String[] values = evidenceVar[i].Cpt.parentsMap.get(key);
                    String[] newValues = new String[2];
                    for (int j = 0; j < values.length; j = j + 2) {
                        if (values[j].equals(evidenceVars.get(evidenceVar[i].Name))) {
                            newValues[0] = values[j];
                            newValues[1] = values[j + 1];
                        }
                    }
                    evidenceVar[i].Cpt.parentsMap.put(key, newValues);
                }
            }
        }
        for (int i = 0; i < hiddenVars.length; i++) {
            if (hiddenVars[i].ParentsNames.length != 0) {
                for (int j = 0; j < hiddenVars[i].ParentsNames.length; j++) {
                    if (evidenceVars.containsKey(hiddenVars[i].ParentsNames[j])) {
                        String value = evidenceVars.get(hiddenVars[i].ParentsNames[j]);
                        Iterator<String[]> iterator = hiddenVars[i].Cpt.parentsMap.keySet().iterator();
                        while (iterator.hasNext()) {
                            String[] key = iterator.next();
                            for (int k = 0; k < key.length; k = k + 2) {
                                if (key[k].equals(hiddenVars[i].ParentsNames[j])) {
                                    if (!value.equals(key[k + 1])) {
                                        iterator.remove();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < variables.length; i++) {
            if (variables[i].ParentsNames.length == 0) {
                String[][] factor = new String[variables[i].Cpt.VarMap.size() + 1][2];
                factor[0][0] = variables[i].Name;
                factor[0][1] = variables[i].Name;
                int factorIndex = 1;
                for (String key : variables[i].Cpt.VarMap.keySet()) {
                    factor[factorIndex][0] = key;
                    factor[factorIndex++][1] = String.valueOf(variables[i].Cpt.VarMap.get(key));
                }
                factors.put(variables[i].Name, factor);
            } else {
                if(!(evidenceVars.containsKey(variables[i].Name) && ifAllParentsIsInEvidence(variables[i].ParentsNames))){
                    int numberOfRows = variables[i].Values.length;
                    int numberOfColumns = variables[i].ParentsNames.length + 1;
                    for (int j = 0; j < variables[i].ParentsNames.length; j++) {
                        for (int k = 0; k < variables.length; k++) {
                            if (variables[i].ParentsNames[j].equals(variables[k].Name)) {
                                numberOfRows *= variables[k].Values.length;
                                if (variables[k].Values.length == 1) {
                                    numberOfColumns--;
                                }
                            }
                        }
                    }
                    numberOfRows++;
                    if (variables[i].Values.length > 1) {
                        numberOfColumns++;
                    }
                    String[][] factor = new String[numberOfRows][numberOfColumns];
                    factor[0][factor[0].length - 1] = variables[i].Name;
                    int ind = 0;
                    for (int j = 0; j < variables[i].ParentsNames.length; j++) {
                        if (!evidenceVars.containsKey(variables[i].ParentsNames[j])) {
                            factor[0][ind] = variables[i].ParentsNames[j];
                            ind++;
                        }
                    }
                    if (variables[i].Values.length > 1) {
                        factor[0][factor[0].length - 2] = variables[i].Name;
                    }
                    int line = 1;
                    for (int n = 0; n < variables[i].Values.length; n++) {
                        for (String[] key : variables[i].Cpt.parentsMap.keySet()) {
                            boolean bool = true;
                            for (int j = 0; j < key.length; j = j + 2) {
                                if(!evidenceVars.containsKey(key[j])){
                                    for (int k = 0; k < factor[0].length - 1; k++) {
                                        if (key[j].equals(factor[0][k])){
                                            if(line < factor.length){
                                                factor[line][k] = key[j + 1];
                                                break;
                                            }
                                        }
                                    }
                                }else{
                                    if(!evidenceVars.get(key[j]).equals(key[j+1])){
                                        bool = false;
                                        break;
                                    }
                                }
                            }
                            if (bool){
                                if (variables[i].Values.length > 1) {
                                    factor[line][factor[line].length - 2] = variables[i].Values[n];
                                }
                                String[] values = variables[i].Cpt.parentsMap.get(key);
                                for (int j = 0; j < values.length; j = j + 2) {
                                    if (values[j].equals(variables[i].Values[n])) {
                                        factor[line][factor[line].length - 1] = values[j + 1];
                                    }
                                }
                                line++;
                            }
                        }
                    }
                    factors.put(variables[i].Name, factor);
                }
            }
        }
    }

    private boolean ifAllParentsIsInEvidence(String[] parentesNames){
        for (int i = 0; i < parentesNames.length; i++) {
            if(!evidenceVars.containsKey(parentesNames[i])){
                return false;
            }
        }
        return true;
    }
}