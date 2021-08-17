public class Read {

    Network network = new Network();


    int readVarIndex = 0;
    boolean CPTOn = false;
    boolean QueryOn = false;

    public void firstLane(String network){
        if (!network.equals("Network")){
            System.out.println("not Network");
        }
    }
    public void secondLane(String variables){
        String[] split = variables.split(": ");
        String[] variable = split[1].split(",");
        network.setVariablesSize(variable.length);
        network.addVariablesNames(variable);
    }

    public void readLane(String lane){
        if(readVarIndex < network.Variables.length){
            if(lane.equals("Var " + network.Variables[readVarIndex].Name)){

            }else if(lane.contains("Values:")){
                readValuesLane(lane);
            }else if(lane.contains("Parents:")){
                readParentsLane(lane);
            }else if(lane.contains("CPT:")){
                CPTOn = true;
            }else if (CPTOn && !lane.equals("")){
                readCPTLane(lane);
            }else if (CPTOn && lane.equals("")){
                CPTOn = false;
                if (network.Variables[readVarIndex].ParentsNames.length > 0){
                    calcCPTParents();
                }
                readVarIndex++;
            }
        }else{
            if (QueryOn && lane.equals("")){
                QueryOn = false;
            }else if(QueryOn){
                readQuerylane(lane);
            }else if(lane.contains("Queries")){
                QueryOn = true;
            }
        }
    }

    private void readQuerylane(String lane){
        Query query = new Query();
        if (lane.contains("|")){
            String[] split = lane.split("\\|");
            String[] leftSplit = split[0].split("=");
            String varNameToCalc = leftSplit[0].substring(2);
            query.setVarNameToCalc(varNameToCalc);
            query.setVarValue(leftSplit[1]);
            String[] rightSplit = split[1].split(",");
            query.setAlgoNum(Integer.parseInt(rightSplit[rightSplit.length-1]));
            for (int i=0;i<rightSplit.length-1;i++){
                if(i == rightSplit.length-2){
                    String[] splitGivenVars = rightSplit[i].split("=");
                    String temp = splitGivenVars[1].substring(0,splitGivenVars[1].length()-1);
                    query.givenVars.put(splitGivenVars[0],temp);
                }else{
                    String[] splitGivenVars = rightSplit[i].split("=");
                    query.givenVars.put(splitGivenVars[0],splitGivenVars[1]);
                }
            }

        }else{
            String[] split = lane.split(",");
            query.setAlgoNum(Integer.parseInt(split[split.length-1]));
            String leftSplit = split[0].substring(2, split[0].length()-1);
            String[] splitAgain = leftSplit.split("=");
            query.varNameToCalc = splitAgain[0];
            query.varValue = splitAgain[1];
        }
        network.addQuery(query);
    }

    private void readCPTLaneNoParents(String lane){
        String[] split = lane.split("=");
        for (int i=0;i<split.length;i++){
            if (!split[i].equals("")){
                String[] splitP = split[i].split(",");
                network.Variables[readVarIndex].Cpt.VarMap.put(splitP[0],Double.parseDouble(splitP[1]));
            }
        }
        if(network.Variables[readVarIndex].Cpt.VarMap.size() == network.Variables[readVarIndex].Values.length-1){
            for(int i=0; i< network.Variables[readVarIndex].Values.length;i++){
                boolean isIn = false;
                double P = 1;
                for (String key: network.Variables[readVarIndex].Cpt.VarMap.keySet()){
                    if (network.Variables[readVarIndex].Values[i].equals(key)){
                        isIn = true;
                    }else{
                        P = P - network.Variables[readVarIndex].Cpt.VarMap.get(key);
                    }
                }
                if (!isIn){
                    network.Variables[readVarIndex].Cpt.VarMap.put(network.Variables[readVarIndex].Values[i],P);
                }
            }
        }
    }

    private void readCPTLaneWithParents(String lane){
        String[] split = lane.split("=");
        String[] key = new String[network.Variables[readVarIndex].ParentsNames.length*2];
        String[] value = new String[(split.length-1)*2];
        int keyInd = 0, valueInd = 0;
        for (int i=0; i<split.length;i++){
            if(i != 0){
                String[] splitRightSide = split[i].split(",");
                for (int j=0;j<splitRightSide.length;j++){
                    value[valueInd++] = splitRightSide[j];
                }
            }else{
                String[] splitLeftSide = split[i].split(",");
                if (splitLeftSide.length == network.Variables[readVarIndex].ParentsNames.length){
                    for(int j=0; j<splitLeftSide.length; j++){
                        key[keyInd++] = network.Variables[readVarIndex].ParentsNames[j];
                        key[keyInd++] = splitLeftSide[j];
                    }
                }else{
                    System.out.println("no match parents names size to count of split[0] side");
                }
            }
        }
        network.Variables[readVarIndex].Cpt.parentsMap.put(key,value);
    }

    private void calcCPTParents(){
        for(int i=0; i< network.Variables[readVarIndex].Values.length;i++){
            for (String[] Key: network.Variables[readVarIndex].Cpt.parentsMap.keySet()){
                boolean isIn = false;
                double P = 1;
                String[] cptLane = network.Variables[readVarIndex].Cpt.parentsMap.get(Key);
                for (int j=0;j<cptLane.length;j = j+2){
                    if (network.Variables[readVarIndex].Values[i].equals(cptLane[j])){
                        isIn = true;
                    }else{
                        P = P - Double.parseDouble(cptLane[j+1]);
                    }
                }
                if (!isIn){
                    float p = (float) (P);
                    String[] newValue = new String[cptLane.length+2];
                    for (int j=0;j<cptLane.length; j++){
                        newValue[j] = cptLane[j];
                    }
                    newValue[newValue.length-2] = network.Variables[readVarIndex].Values[i];
                    newValue[newValue.length-1] = String.valueOf(p);
                    network.Variables[readVarIndex].Cpt.parentsMap.put(Key, newValue);
                }
            }
        }
    }

    private void readCPTLane(String lane){
        if(network.Variables[readVarIndex].ParentsNames.length == 0){
            readCPTLaneNoParents(lane);
        }else{
            readCPTLaneWithParents(lane);
        }
    }

    private void readParentsLane(String lane){
        String[] split = lane.split(": ");
        if (split[1].equals("none")){
            network.Variables[readVarIndex].setParentsToZero();
        }else{
            String[] splitParents = split[1].split(",");
            network.Variables[readVarIndex].setParentsNames(splitParents);
        }
    }

    private void readValuesLane(String lane){
        String[] split = lane.split(": ");
        String[] splitValues = split[1].split(",");
        network.Variables[readVarIndex].setValues(splitValues);
    }

}
