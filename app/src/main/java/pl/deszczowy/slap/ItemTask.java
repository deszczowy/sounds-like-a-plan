package pl.deszczowy.slap;

class ItemTask {
    private String name;
    private String serie;
    private int status;
    private boolean current;

    ItemTask(int status, String name, String serie, boolean current) {
        super();
        this.name = name;
        this.serie = serie;
        this.status = status;
        this.current = current;
    }

    String getName(){
        return this.name;
    }

    String getSerie(){
        if (this.serie.equals("")){
            return "";
        }else{
            return "- " + this.serie;
        }
    }

    int getStatus(){
        return this.status;
    }

    boolean getIsCurrent(){
        return this.current;
    }
}
