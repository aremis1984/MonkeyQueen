package MonkeyQueenGame;

import java.io.Serializable;

public class PiecesToMove implements Serializable{
    private Pieces piece;
    private XYLocation to;
    
    public PiecesToMove(Pieces piece, XYLocation to){
        this.piece=piece;
        this.to=to;                    
    }

    public Pieces getPiece() {
        return piece;
    }

    public XYLocation getCoords() {
        return to;
    }    
    
    public String toString(){
        
        String text=""+( (piece.getPlayer()==1)?"Blancas":"Negras ");
        text+=" pieceStack="+piece.getStack();
        text+=""+piece.getCoordinates().toString()+" to "+to.toString();
        
        return text;
    }
    
    public String toStatusString(){
        
        String text=""+( (piece.getPlayer()==1)?"Blancas":"Negras ");
        text+=""+piece.getCoordinates().toString()+" -"+to.toString();
        
        return text;
    }
}
