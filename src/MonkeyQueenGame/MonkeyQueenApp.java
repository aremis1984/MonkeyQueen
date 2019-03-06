package MonkeyQueenGame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Calendar;
import java.util.Date;

import javax.swing.*;


/**
 * Simple graphical Tic-tac-toe game application. It demonstrates the Minimax
 * algorithm for move selection as well as alpha-beta pruning.
 * 
 * @author Ruediger Lunde
 */
public class MonkeyQueenApp {

    private JPanel panel;
    JFrame frame;
    public MonkeyQueenApp(String args[]){
        int tamTablero=Integer.parseInt(args[0]);
        int stack=Integer.parseInt(args[1]);
        
        XYLocation whiteQueenCoord=null;
        XYLocation blackQueenCoord=null;
            
        
        if (!args[2].equals("") && !args[3].equals("")){
            //se especificaron coordenadas para blancas y negras
            
            int X=Integer.parseInt(args[2].split(",")[0]);
            int Y=Integer.parseInt(args[2].split(",")[1]);
            
            whiteQueenCoord=new XYLocation(X, Y);
            
            X=Integer.parseInt(args[3].split(",")[0]);
            Y=Integer.parseInt(args[3].split(",")[1]);
            
            blackQueenCoord=new XYLocation(X, Y);
        }
        
        panel = new MonkeyQueenPanel(tamTablero,stack,whiteQueenCoord,blackQueenCoord);
                
        frame=new JFrame();
        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int panelSize=tamTablero*53;
        frame.setSize(panelSize,panelSize);

        frame.setVisible(true);
    }
    
    public void close(){
        panel.setVisible(false);
        frame.setVisible(false);
    }

//  private void setState(MonkeyQueenState newState) {
//    throw new UnsupportedOperationException("Not yet implemented");
//  }


	/** Simple panel to control the game. */
    private class MonkeyQueenPanel extends JPanel implements ActionListener {
        private static final long serialVersionUID = 1L;
        
        JComboBox strategyCombo;
        JButton clearButton;
        JButton proposeButton;
        
        JButton saveButton;
        JButton loadButton;
        
        JButton[] squares;
        JLabel statusBar;

        MonkeyQueenGame game;
        MonkeyQueenState currState;
        Metrics searchMetrics;

        int tamTablero;
        int stack;
        XYLocation whiteQueenCoord;
        XYLocation blackQueenCoord;
        
        private String actionText;
        private String listOfActions;
        
        //TicTacToePanel() { /** Standard constructor. */
        MonkeyQueenPanel(int tamTablero, int stack,XYLocation whiteQueenCoord, XYLocation blackQueenCoord) {
            this.setLayout(new BorderLayout());
            
            this.tamTablero=tamTablero;
            this.stack=stack;
            this.actionText="";
            this.listOfActions="tamTablero="+tamTablero+"\n";
            this.listOfActions+="stack="+stack+"\n";
            this.whiteQueenCoord=whiteQueenCoord;
            this.blackQueenCoord=blackQueenCoord;
            
            //Creamos una barra que contendra varios botones
            JToolBar tbar = new JToolBar();
            tbar.setFloatable(false);
//            strategyCombo = new JComboBox(new String[] { "Minimax",
//                            "Alpha-Beta", "Iterative Deepening Alpha-Beta",
//                            "Iterative Deepening Alpha-Beta (log)" });
            //strategyCombo = new JComboBox(new String[] { "Alfa-Beta prof 2","Alfa-Beta prof 4","Alpha-Beta prof 6" });
            strategyCombo = new JComboBox(new String[] { "Alfa-Beta heuristic1","Alfa-Beta heuristic2" });
            strategyCombo.setSelectedIndex(0);
            tbar.add(strategyCombo);
            tbar.add(Box.createHorizontalGlue());
            
            //añadimo el boton de clear
            clearButton = new JButton("Clear");
            clearButton.addActionListener(this);
            tbar.add(clearButton);
            
            //añadimos el boton de proponer movimiento
            proposeButton = new JButton("Propose Move");
            proposeButton.addActionListener(this);
            tbar.add(proposeButton);

            //añadimos el boton de guardar
            saveButton=new JButton("Save");
            saveButton.addActionListener(this);
            tbar.add(saveButton);
            
            //añadimos el boton de load
            loadButton=new JButton("Load");
            loadButton.addActionListener(this);
            tbar.add(loadButton);
                        
            //añadimos la barra de menus al panel del juego
            this.add(tbar, BorderLayout.NORTH);


            //AQUI SE DEFINE EL TAMAÑO DEL TABLERO
            JPanel gameBoard = new JPanel();
            gameBoard.setLayout(new GridLayout(tamTablero, tamTablero));
            this.add(gameBoard, BorderLayout.CENTER);

            //DEFINIMOS EL NUMERO DE CELDAS DEL TABLERO
            squares = new JButton[tamTablero*tamTablero];

            Font f = new java.awt.Font(Font.SANS_SERIF, Font.PLAIN, 20);
            
            Color blanco=new Color(225, 211, 130);
            Color negro= new Color(153,100,21);
            
            Color rowColor=blanco;
            Color prevColumnColor=blanco;
            
            for (int i = 0; i < tamTablero*tamTablero; i++) {
                JButton square = new JButton("");
                square.setFont(f);
                
                /* bloque para generar el tablero de distinto color */
                square.setBackground(rowColor);

                //vamos alternando en funcion del valor de i
                if( (i+1)%tamTablero==0){
                    if(rowColor==prevColumnColor){
                        rowColor=(rowColor==blanco)?negro:blanco;
                    }
                    prevColumnColor=rowColor;
                } else {
                    rowColor=(rowColor==blanco)?negro:blanco;
                }
                /* ----- */
                square.setBorder(null);
                square.setFocusPainted(false);
                square.setBorderPainted(false);
                square.setHorizontalTextPosition(SwingConstants.CENTER);
                square.addActionListener(this);
                
                squares[i] = square;
                gameBoard.add(square);
            }
            
            statusBar = new JLabel(" ");
            statusBar.setBorder(BorderFactory.createEtchedBorder());
            this.add(statusBar, BorderLayout.SOUTH);

            game = new MonkeyQueenGame(tamTablero, stack,whiteQueenCoord,blackQueenCoord);
            actionPerformed(null);
        }

        /** Handles all button events and updates the view. */
        @Override
        public void actionPerformed(ActionEvent ae) {
            //int tamTablero=12;

            searchMetrics = null;
            
            if (ae!=null && (ae.getSource()==saveButton || ae.getSource()==loadButton) ){
                if (ae.getSource()==saveButton){
                    saveGame();
                }
                if (ae.getSource()==loadButton){
                    loadGame();
                }
            }
            if (ae == null || ae.getSource() == clearButton) {
                if (ae!=null){  
                    //SIGNIFICA QUE SE LE DIO AL BOTON CLEAR
                    this.actionText="";
                    this.listOfActions="";
                    System.out.println("\n\n");
                    currState= new MonkeyQueenGame(tamTablero, stack,whiteQueenCoord,blackQueenCoord).getInitialState();
                } else {
                    currState = game.getInitialState();
                }
            //else if (!game.isTerminal(currState)) {
            } else if (!game.isTerminal(currState)) {
                //AUN HAY JUEGO
                if (ae.getSource() == proposeButton)
                    proposeMove();
                else {
                    for (int i = 0; i < tamTablero*tamTablero; i++)
                        if (ae.getSource() == squares[i]){
                            /* DESDE AQUI SE REALIZO LA ACCION, le paso al metodo
                             el estado actual, la posicion donde se realizo la accion*/
                            if (currState.hasOrigenPiece()){
                                PiecesToMove pm = new PiecesToMove(currState.getOrigen(), new XYLocation(i%tamTablero, i/tamTablero)); 
                                this.listOfActions+=pm.toString()+"\n";
                            }
                            currState = game.getResult(currState,
                                    new XYLocation(i%tamTablero, i/tamTablero));
                            
                        }
                }
            }
            
            //se repinta el tablero
            for (int i = 0; i < tamTablero*tamTablero; i++) {
                String val = currState.getValueOfPieceAt(i%tamTablero, i/tamTablero);
                
                if (val == MonkeyQueenState.EMPTY){
                    val = "";
                    squares[i].setOpaque(true);
                    squares[i].setIcon(new ImageIcon(""));
                } else {
                    int p=currState.getPlayerAt(i%tamTablero, i/tamTablero);
                    squares[i].setOpaque(true);
                    //squares[i].setIcon(new ImageIcon(""));
                    XYLocation coord = currState.getOrigenCoord();
                    if (coord!=null){
                        int t=coord.getYCoOrdinate()*tamTablero+coord.getXCoOrdinate();
                        if (t==i){
                            squares[i].setOpaque(false);
                        }
                    }
                    //AQUI SE COMPRUEBA SI LA CASILLA ACTUAL IGUAL AL ORIGEN
                    if(p==-1){
                       // System.out.println("Como coño has llegado aqui?");
                    } else {
                        
                        squares[i].setIcon(null);
                        if (p==0){
                            squares[i].setForeground(Color.yellow);
                            squares[i].setIcon(new ImageIcon("negras.png"));
                        }else{
                            squares[i].setForeground(new Color(163, 1, 26));
                            squares[i].setIcon(new ImageIcon("blancas.png"));
                        }
                    }
                }
                
                squares[i].setText(val);                
            }
            updateStatus();
        }

        /** Uses adversarial search for selecting the next action. */
        private void proposeMove() {
            AdversarialSearch<MonkeyQueenState, PiecesToMove> search;
            PiecesToMove action;
            MonkeyQueen_AlfaBetaSearch gameSearch;
            switch (strategyCombo.getSelectedIndex()) {
                case 0:
                    gameSearch= new MonkeyQueen_AlfaBetaSearch(game, 4, 1);
                    break;
//                case 1:
//                    gameSearch= new MonkeyQueen_AlfaBetaSearch(game,4);
//                    break;
//                case 2:
//                    gameSearch= new MonkeyQueen_AlfaBetaSearch(game,6);
//                    break;
                case 1:
                    gameSearch=new MonkeyQueen_AlfaBetaSearch(game, 4, 2);
                    break;
                default:
                    gameSearch= new MonkeyQueen_AlfaBetaSearch(game, 4, 1);
                    break;
            }
            long time=System.currentTimeMillis();
            
            action=gameSearch.makeDecision(currState);
            searchMetrics = gameSearch.getMetrics();
            time=System.currentTimeMillis()-time;
            actionText = gameSearch.getAction()+" en "+(time/1000)+"s";
            
            
            if (action==null) {
                System.out.println("El jugador: "+( (currState.getPlayerToMove()==currState.X)?"Blancas":"Negras")+" se rinde.....");
            } else  {
              currState = game.getResult(currState, action);
              System.out.println("Tiempo: "+(time/1000)+"s   - "+actionText);
              this.listOfActions+=action.toString()+"\n";
            }
        }

        /** Updates the status bar. */
        private void updateStatus() {
            String statusText;
            if (game.isTerminal(currState))
                if (game.getUtility(currState, MonkeyQueenState.X) == 1)
                    statusText = "Blancas has won :-)";
                else if (game.getUtility(currState, MonkeyQueenState.O) == 1)
                    statusText = "Negras has won :-)";
                else
                    statusText = "No winner...";
            else{
                String t = game.getPlayer(currState)==currState.X ? "Blancas":"Negras";
                statusText = "Next: " + t;
            
            }
            if (searchMetrics != null)
                statusText += "    " + searchMetrics;
            if (actionText != null){
                statusText+=" | "+actionText;
                actionText="";
            }
            
            statusBar.setText(statusText);
            
        }

        
        /**
         * Esta funcion guarda el juego en un fichero predefinido, va guardando
         * los juegos sucecivamente segun se solicite
         */
        private void saveGame() {
            int i=1;
            String path="mqSave_"+Calendar.DAY_OF_YEAR;
            
            FileWriter fw=null;
            File file=null;
            try {
                file = new File(path+".txt");
                while (file.exists()){
                    file = new File(path+"_("+i+").txt");
                    i++;
                }
                
                fw = new FileWriter(file);
                fw.write(listOfActions);
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                try {
                    if (fw!=null){
                        fw.close();
                    }
                } catch (Exception e2){
                    e2.printStackTrace();
                }
            }
        }

        /**
         * Esta funcion solicita al usuario la ruta de un fichero
         * que debe contener la distribucion adecuada de un juego
         */
        private void loadGame() {
            String path=JOptionPane.showInputDialog("indique el fichero" );
            if (path=="") return;
            
            MonkeyQueenState newState=null;
            PiecesToMove action=null;
            
            String line;
            int newTamTablero=0;
            int newStack=0;
                
            File file = null;
            FileReader fr= null;
            BufferedReader br = null;
            try{
                file = new File(path);
                fr = new FileReader(file);
                br = new BufferedReader(fr);
                
                line=br.readLine();   
                
                while (line!=null){
                    if (line.contains("tamTablero")){
                        newTamTablero=Integer.parseInt(line.split("=")[1]);
                    } else if (line.contains("stack")){
                        newStack=Integer.parseInt(line.split("=")[1]);
                        newState=new MonkeyQueenState(newTamTablero, newStack,null,null);
                    }
                    else {
                        //ya esta creado el estado asi que puedo analizar para tener las acciones
                        //ahora hay que coger y de cada linea extraer el stack, el color, la pos origen y la pos destino.
                        //las dos primeras crear un objeto piece, y con la pos destino creo un pieceToMove
                        //estas lineas son:   Blancas|Negras stack=N (X,Y) to (X,Y)
                        if (line.contains("Blancas") || line.contains("Negras")){
                            int player= line.contains("Negras")? 1: 0;
                            
                            int stack=0;
                            String text="";
                            
                            char cArray[]=line.toCharArray();
                            boolean valid=false;
                            boolean validCoord1=false;
                            boolean validCoord2=false;
                            
                            int X=0;
                            int Y=0;
                            
                            Pieces piece=null;
                            for (char c:cArray){
                                if (c=='=' || valid){
                                    valid=true;
                                    //lo que viene ahora corresponde al numero
                                    if (c==' '){
                                        //significa que se acabo lo que son los numeros
                                        valid=false;
                                        stack=Integer.parseInt(text);
                                        text="";
                                    } else {
                                        if (c!='='){
                                            text+=c;
                                        }
                                    }
                                } else {
                                    //busco el primer parentesis
                                    if (piece==null){
                                        if (c=='(' || validCoord1){
                                            validCoord1=true;
                                            if (c==','){
                                                //acabo de procesar la X de la primera coordenada
                                                X=Integer.parseInt(text);
                                                text="";
                                            }

                                            if (c==')'){
                                                //acabo de procesar la Y de la primera coordenada
                                                Y=Integer.parseInt(text);
                                                piece = new Pieces(stack, player, new XYLocation(X, Y));
                                                validCoord1=false;
                                                text="";
                                            }
                                            if (c!='(' && c!=',' &&c!=')'){
                                                text+=c;
                                            }
                                        }

                                    } else {
                                        //ya se creo la pieza, asi que solo debo buscar la otra coordenada
                                        if (c=='(' || validCoord2){
                                            validCoord2=true;
                                        
                                            if (c==','){
                                                //acabo de procesar la X de la segunda coordenada
                                                X=Integer.parseInt(text);
                                                text="";
                                            }
                                        
                                            if (c==')'){
                                                //acabo de procesar la Y de la segunda coordenada
                                                Y=Integer.parseInt(text);
                                                action=new PiecesToMove(piece, new XYLocation(X, Y));
                                                validCoord2=false;
                                                text="";
                                            }
                                        
                                            if (c!='(' && c!=',' && c!=')') {
                                                //voy acumulando los datos de la segunda coordenada
                                                text+=c;
                                            }
                                        }
                                    }      
                                }
                            }
                            //AL ACABAR EL FOR YA TENGO UN OBJETO DE PiecesToMove
                            if (action!=null && newState!=null){
                                newState.mark(action);
                            }
                        }
                    } 
                    
                    line=br.readLine();
                }
                
            }catch (Exception e){
                e.printStackTrace();
            } finally{
                //nos aprovechamos para cerrar el fichero
                try{
                    if (fr!=null){
                        fr.close();
                    }
                } catch (Exception e2){
                    e2.printStackTrace();
                }   
            }  
                
            //newState
            //System.out.println(newState.toString());
            String args[]=new String[]{"","","",""};
            args[0]=Integer.toString(newTamTablero);
            args[1]=Integer.toString(newStack);
            
            MonkeyQueenApp gameApp=new MonkeyQueenApp(args);
            ((MonkeyQueenPanel)(gameApp.getPanel())).setState(newState);
            
        }   
        
        private void setState(MonkeyQueenState state){
            this.currState=state;
            updateStatus();
        }
    }
    
    public JPanel getPanel(){
        return this.panel;
    }
    
}
