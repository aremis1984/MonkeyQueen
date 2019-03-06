package MonkeyQueenGame;

//import aima.core.search.framework.Metrics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;


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
public class AlphaBetaSearch<STATE, ACTION, PLAYER> implements
		AdversarialSearch<STATE, ACTION> {

    Game<STATE, ACTION, PLAYER> game;
    private int expandedNodes;

    /** Creates a new search object for a given game. */
    public static <STATE, ACTION, PLAYER> AlphaBetaSearch<STATE, ACTION, PLAYER> createFor(
                    Game<STATE, ACTION, PLAYER> game) {
        return new AlphaBetaSearch<STATE, ACTION, PLAYER>(game);
    }

    public AlphaBetaSearch(Game<STATE, ACTION, PLAYER> game) {
        this.game = game;

    }

    @Override
    /**
     * Esta funcion es la encargada de controlar los nodos raiz para las
     * distintas ramas del arbol, que se generan en funcion de cada 
     * accion que se transmite a  minValue()
     * 
     * @param state estado inicial del que se parte
     */
    public ACTION makeDecision(STATE state) {
        expandedNodes = 0;
        ACTION result = null;
        double resultValue = Double.NEGATIVE_INFINITY;
        PLAYER player = game.getPlayer(state);
        
        int deep=2;
        int i=0;
        for (ACTION action : game.getActions(state)) {
//            double value = minValue(game.getResult(state, action), player,
//                            Double.NEGATIVE_INFINITY, 
//                            Double.POSITIVE_INFINITY,
//                            deep);
//            if (value > resultValue) {
//                result = action;
//                resultValue = value;
//            }
            //System.out.println("Estamos en la iteracion: "+i+".  Pieza: ("+action.getCla);
            i++;
            String op = (JOptionPane. showInputDialog("Parada de control, "
                    + "ha acabado el ciclo de acciones. Valor de result:"));

        }
        return result;
    }

    /* A LAS FUNCIONES MAX Y MIN SE LAS LLAMA PARA CADA ACCION POSIBLE, 
     * POR TANTO DEBERIAMOS DEFINIR UN LIMITE
     */
    public double maxValue(STATE state, PLAYER player, double alpha, double beta, int deep) {
        expandedNodes++;
        if (game.isTerminal(state))
            return game.getUtility(state, player);
        
        double value = Double.NEGATIVE_INFINITY;
        for (ACTION action : game.getActions(state)) {
            if (deep==0){
                //PROFUNDIDAD MAXIMA
                
                //value=Math.max(value,game.getStateValue(state,action));
            } else { 
                deep--;
                value=Math.max(value, 
                        minValue(game.getResult(state,action), player, alpha, beta, deep));
            }
            
            if (value >= beta)
                return value;
            if (deep==0) return Math.max(alpha, value);
            
            alpha = Math.max(alpha, value);
        }
        return value;
    }

    public double minValue(STATE state, PLAYER player, double alpha, double beta, int deep) {
        expandedNodes++;

//        if (game.isTerminal(state))
//            return game.getUtility(state, player);
//        
//        if (stop.Condition){
//            return game.getUtility(state, player);
//        }
       
        if (game.isTerminal(state) || deep==0){
            return game.getUtility(state, player);
        }
        double value = Double.POSITIVE_INFINITY;
        for (ACTION action : game.getActions(state)) {
            if (deep==0){
                //PROFUNDIDAD MAXIMA
                //value=Math.min(value,game.getStateValue(state,action));
            } else {
                deep--;
                value=Math.max(value, 
                        maxValue(game.getResult(state,action), player, alpha, beta, deep));
            }
            
            value = Math.min(value, 
                    maxValue(game.getResult(state, action), player, alpha, beta, deep));
            if (value <= alpha)
                return value;
            
            if (deep==0) return Math.min(beta, value);
            
            beta = Math.min(beta, value);
        }
        return value;
    }

	@Override
	public Metrics getMetrics() {
            Metrics result = new Metrics();
            result.set("expandedNodes", expandedNodes);
            return result;
	}
}
