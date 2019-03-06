package MonkeyQueenGame;

import java.io.Serializable;

public class Pieces implements Serializable{
    private int stack;
    private int player;
    private XYLocation coordinates;
    
    public Pieces(int stack, int player, XYLocation coordinates) {
        this.stack = stack;
        this.player = player;
        this.coordinates = coordinates;
    }

    public XYLocation getCoordinates() {
        return coordinates;
    }

    public int getPlayer() {
        return player;
    }

    public int getStack() {
        return stack;
    }

    public void setCoordinates(XYLocation coordinates) {
        this.coordinates = coordinates;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public void setStack(int stack) {
        this.stack = stack;
    }
    
}
