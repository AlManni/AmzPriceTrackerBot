public class StateOfConversation {
    private int state;

    public StateOfConversation(){
        state = 0;
    }

    public int getState(){
        return state;
    }
    public void setState(int newState){
        //System.out.println("state: "+state+"->"+newState);
        state = newState;
    }
}
