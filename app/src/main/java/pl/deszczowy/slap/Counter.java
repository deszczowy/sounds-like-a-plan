package pl.deszczowy.slap;

class Counter {
    private int main;
    private int additional;

    Counter(){
        this.main = 0;
    }

    void click(){
        this.main++;
    }

    void click(boolean countAdditional){
        click();
        if (countAdditional) this.additional++;
    }

    public int getMain(){
        return this.main;
    }

    int getAdditional(){
        return this.additional;
    }

    int getRemaining() { return this.main - this.additional; }
}
