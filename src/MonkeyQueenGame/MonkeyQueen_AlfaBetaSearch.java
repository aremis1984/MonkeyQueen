/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MonkeyQueenGame;

import javax.swing.JOptionPane;

public class MonkeyQueen_AlfaBetaSearch {

/**
 * Artificial Intelligence A Modern Approach (3rd Ed.): Page 173.<br>
 * 
 * <pre>
 * <code>
 * function ALPHA-BETA-SEARCH(state) returns an action
 *   v = MAX-VALUE(state, -infinity, +infinity)
 *   return the action in ACTIONS(state) with value v
 *   
 * function MAX-VALUE(state, alpha, beta) returns a utility value
 *   if TERMINAL-TEST(state) then return UTILITY(state)
 *   v = -infinity
 *   for each a in ACTIONS(state) do
 *     v = MAX(v, MIN-VALUE(RESULT(s, a), alpha, beta))
 *     if v >= beta then return v
 *     alpha = MAX(alpha, v)
 *   return v
 *   
 * function MIN-VALUE(state, alpha, beta) returns a utility value
 *   if TERMINAL-TEST(state) then return UTILITY(state)
 *   v = infinity
 *   for each a in ACTIONS(state) do
 *     v = MIN(v, MAX-VALUE(RESULT(s,a), alpha, beta))
 *     if v <= alpha then return v
 *     beta = MIN(beta, v)
 *   return v
 * </code>
 * </pre>
 * 
 * Figure 5.7 The alpha-beta search algorithm. Notice that these routines are
 * the same as the MINIMAX functions in Figure 5.3, except for the two lines in
 * each of MIN-VALUE and MAX-VALUE that maintain alpha and beta (and the
 * bookkeeping to pass these parameters along).
 * 
 * @author Ruediger Lunde
 * 
 * @param <STATE>
 *            Type which is used for states in the game.
 * @param <ACTION>
 *            Type which is used for actions in the game.
 * @param <PLAYER>
 *            Type which is used for players in the game.
 */
    private MonkeyQueenGame game;
    private int expandedNodes;
    private int deep;
    private PiecesToMove result;
    private int heuristic;

    public MonkeyQueen_AlfaBetaSearch(MonkeyQueenGame game, int deep, int heuristic) {
        this.game = game;
        this.deep = deep;
        this.heuristic=heuristic;
    }
    
    /**
     * Esta funcion es la encargada de controlar los nodos raiz para las
     * distintas ramas del arbol, que se generan en funcion de cada 
     * accion que se transmite a  minValue()
     * 
     * @param state estado inicial del que se parte
     */
    public PiecesToMove makeDecision(MonkeyQueenState state) {
        expandedNodes = 0;
        result = null;
        double resultValue = Double.NEGATIVE_INFINITY;
        String player = game.getPlayer(state);
        
        for (PiecesToMove action : game.getActions(state)) {
            //MonkeyQueenState stateResult=game.getResult(state, action);
            MonkeyQueenState stateResult=game.getResult(state, action, heuristic);
            double value = minValue(stateResult, player,
                            Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,deep);
            
            if (value > resultValue) {
                result = action;
                resultValue = value;
            }
            //System.out.println("Nodos expandidos: "+expandedNodes);
        }
        
        return result;
    }

    /* A LAS FUNCIONES MAX Y MIN SE LAS LLAMA PARA CADA ACCION POSIBLE, limite de profundidad     */
    public double maxValue(MonkeyQueenState state, String player, double alpha, double beta, int deep) {
        
        if (game.isTerminal(state) || deep==0){
            if (game.isTerminal(state) && state.getWinner()!=Integer.parseInt(player))
                return state.getStateValue()*-1;
            return state.getStateValue();
        }
        expandedNodes++;
        deep--;
        
        double value = Double.NEGATIVE_INFINITY;
        for (PiecesToMove action : game.getActions(state)) {
            //MonkeyQueenState stateResult=game.getResult(state, action);
            MonkeyQueenState stateResult=game.getResult(state, action,heuristic);
            value=Math.max(value, minValue(stateResult, player, alpha, beta, deep));

            if (value >= beta)
                return value;
            
            alpha = Math.max(alpha, value);
        }
        return value;
    }

    public double minValue(MonkeyQueenState state, String player, double alpha, double beta, int deep) {
        
        if (game.isTerminal(state) || deep==0 ){
            if (game.isTerminal(state) && state.getWinner()!=Integer.parseInt(player))
                return state.getStateValue()*-1;
            return state.getStateValue();
        }
        expandedNodes++;
        deep--;
        
        double value = Double.POSITIVE_INFINITY;
        for (PiecesToMove action : game.getActions(state)) {
            //MonkeyQueenState stateResult=game.getResult(state, action);
            MonkeyQueenState stateResult=game.getResult(state, action,heuristic);
            value = Math.min(value, maxValue(stateResult, player, alpha, beta, deep));
            
            if (value <= alpha)
                return value;
           
            beta = Math.min(beta, value);
        }
        return value;
    }


    public Metrics getMetrics() {
        Metrics result = new Metrics();
        result.set("nodes", expandedNodes);
        
        return result;
    }
    
    public String getAction(){
        if (result!=null){
            String text=""+result.toStatusString();
            return text;
        } else {
            return "";
        }
    }
}
 
