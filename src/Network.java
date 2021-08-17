public class Network {

    Var[] Variables;
    Query[] queries;

    public void addVariablesNames(String[] names){
        for(int i=0; i<names.length; i++){
            Var var = new Var(names[i]);
            Variables[i] = var;
        }
    }

    public void setQueriesSize(int queriesSize){
        queries = new Query[queriesSize];
    }

    public void setVariablesSize(int VariablesSize){
        Variables = new Var[VariablesSize];
    }

    public Var[] getVariables() {
        return Variables;
    }

    public void setVariables(Var[] variables) {
        Variables = variables;
    }

    public Query[] getQueries() {
        return queries;
    }

    public void setQueries(Query[] queries) {
        this.queries = queries;
    }

    public void addQuery(Query query){
        if (queries != null){
            Query[] tempQuery = new Query[queries.length+1];
            tempQuery[tempQuery.length-1] = query;
            for (int i=0; i< queries.length;i++){
                tempQuery[i] = queries[i];
            }
            queries = tempQuery;
        }else if (queries == null){
            queries = new Query[1];
            queries[0] = query;
        }

    }
}
