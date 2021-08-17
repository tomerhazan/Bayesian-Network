public class Var{

    String Name;
    String[] Values;
    String[] ParentsNames;
    CPT Cpt = new CPT();

    Var(String name){
        Name = name;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String[] getValues() {
        return Values;
    }

    public void setValues(String[] values) {
        Values = values;
    }

    public void setParentsToZero(){
        ParentsNames = new String[0];
    }

    public String[] getParentsNames() {
        return ParentsNames;
    }

    public void setParentsNames(String[] parentsNames) {
        ParentsNames = parentsNames;
    }

    public CPT getCpt() {
        return Cpt;
    }

    public void setCpt(CPT cpt) {
        Cpt = cpt;
    }
}
