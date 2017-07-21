package pl.deszczowy.slap;

class QueryCommand{
    private QueryTag tag;
    private String param;

    QueryCommand(QueryTag tag, String param){
        this.tag = tag;
        this.param = param;
    }
    QueryTag getTag(){
        return this.tag;
    }

    String getParam(){
        return this.param;
    }

    public void setTag(QueryTag tag){
        this.tag = tag;
    }

    public void setParam(String param){
        this.param = param;
    }
}
