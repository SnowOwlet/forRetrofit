package chain;

import okhttp3.Response;

public class ChainTest {
    public static void main(String[] args) {
        Chain chain1 = new Chain1();
        Chain chain2 = new Chain1();
        Chain chain3 = new Chain1();
        chain1.setNextChain(chain2);
        chain2.setNextChain(chain3);
        MyResponse myResponse = chain1.handleRequest("111");
        System.out.println(myResponse.data);

    }
    static class Chain1  extends Chain{

        @Override
        public MyResponse handleRequest(String args) {
            //这里处理
            args = args +"Chain+1";
            if (nextChain != null){
                return nextChain.handleRequest(args);
            }
            return new MyResponse(args);
        }
    }
    static class Chain2  extends Chain{

        @Override
        public MyResponse handleRequest(String args) {
            if (nextChain != null){
                return nextChain.handleRequest(args);
            }
            return new MyResponse(args);
        }
    }
    static class Chain3  extends Chain{

        @Override
        public MyResponse handleRequest(String args) {
            if (nextChain != null){
                return nextChain.handleRequest(args);
            }
            return new MyResponse(args);
        }
    }
}
