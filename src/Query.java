import java.util.HashMap;

public class Query {

    String varNameToCalc;
    String varValue;
    HashMap<String,String> givenVars = new HashMap<String,String>();
    int algoNum;

    public String getVarNameToCalc() {
        return varNameToCalc;
    }

    public void setVarNameToCalc(String varNameToCalc) {
        this.varNameToCalc = varNameToCalc;
    }

    public String getVarValue() {
        return varValue;
    }

    public void setVarValue(String varValue) {
        this.varValue = varValue;
    }

    public HashMap<String, String> getGivenVars() {
        return givenVars;
    }

    public void setGivenVars(HashMap<String, String> givenVars) {
        this.givenVars = givenVars;
    }

    public int getAlgoNum() {
        return algoNum;
    }

    public void setAlgoNum(int algoNum) {
        this.algoNum = algoNum;
    }
}
