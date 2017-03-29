package sensor.information.collector;

/**
 * Created by yin on 12/5/16.
 */
public class TokenFilter {
    private String input;

    public TokenFilter(String input) {
        this.input = input;
    }

    public String CoarseGrained() {
        String result;
        result = input.replaceAll("[^A-Z_]+", " ");
        result = result.replaceAll("\\s[A-Z]\\s|\\s[A-Z]|[A-Z]\\s", " ");
        return result;
    }

    public String RemoveTokens(String input,String [] needRemove){
        String result="";
        for(int i=0;i<needRemove.length;i++){
            result=input.replace(needRemove[i],"");
        }
        return result;
    }

    /*public String FineGrained() {
        String result;

    }*/
}
