package MonkeyQueenGame;

import java.util.List;

//import aima.core.search.adversarial.Game;
//import aima.core.util.datastructure.XYLocation;

/**
 * Provides an implementation of the Tic-tac-toe game which can be used for
 * experiments with the Minimax algorithm.
 * 
 * @author Ruediger Lunde
 * 
 */
//public class MonkeyQueenGame implements Game<TicTacToeState, XYLocation, String> {
public class MonkeyQueenGame implements Game<MonkeyQueenState, PiecesToMove, String> {
    private int tamTablero;
    private int stack;
    MonkeyQueenState initialState;
    
    public MonkeyQueenGame(int tamTablero, int stack, XYLocation whiteQueenCoord,XYLocation blackQueenCoord){
        this.tamTablero=tamTablero;
        this.stack=stack;
        initialState = new MonkeyQueenState(tamTablero,stack,whiteQueenCoord,blackQueenCoord);
    }

    @Override
    public MonkeyQueenState getInitialState() {
        return initialState;
    }

    @Override
    public String[] getPlayers() {
        return new String[] { MonkeyQueenState.X, MonkeyQueenState.O };
    }

    @Override
    public String getPlayer(MonkeyQueenState state) {
        return state.getPlayerToMove();
    }


    public List<PiecesToMove> getActions(MonkeyQueenState state){
        return state.getAllPosiblesMoves();
    }

    /**
     * Esta funcion es la que implementa el concepto de generar sucesor
     * @param state: stado a partir del cual se obtendra el sucesor
     * @param action: coordenada sobre la cual se quiere realizar la accion
     */
    public MonkeyQueenState getResult(MonkeyQueenState state, XYLocation action) {
        if (state.hasOrigenPiece()) {
            if (state.getOrigenCoord().getXCoOrdinate()==action.getXCoOrdinate() &&
                state.getOrigenCoord().getYCoOrdinate()==action.getYCoOrdinate()) {
                //LE DIO DO VECES, QUIERE CANCELAR LA ACCION
                state.removeOrigen();
                return state;
            } else {
                
                //lo que estaba por defecto
                MonkeyQueenState result = state.clone();

                //esta funcion implementa la ejecucion de la accion sobre un estado
                if (result.mark(action)) return result;
                else return state;
            }
        } else {
            state.setOrigen(action);
            return state;
        }
        
    }

    @Override
    public MonkeyQueenState getResult(MonkeyQueenState state, PiecesToMove action){
         MonkeyQueenState result = state.clone2();
         
         result.mark(action);
         return result;
    }
    public  MonkeyQueenState getResult(MonkeyQueenState state, PiecesToMove action, int heuristic){
         MonkeyQueenState result = state.clone2();
         
         result.mark(action, heuristic);
         return result;
    }
    
    @Override
    public boolean isTerminal(MonkeyQueenState state) {
        return state.getWinner()!=-1; 
    }

    @Override
    public double getUtility(MonkeyQueenState state, String player) {
        double result = state.getUtility();
        if (result != -1) {
            if (player == MonkeyQueenState.O)
                result = 1 - result;
        } else {
            throw new IllegalArgumentException("State is not terminal.");
        }
        return result;
    }

    
     public double getStateValue(MonkeyQueenState state) {
        return state.getStateValue();
    }
}
