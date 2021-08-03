package chain;


public abstract class Chain {
        protected Chain nextChain;

    public void setNextChain(Chain nextChain) {
        this.nextChain = nextChain;
    }
    public abstract MyResponse handleRequest(String args);
}
