package MonkeyQueenGame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//import aima.core.util.datastructure.XYLocation;

public class MonkeyQueenState implements Cloneable,Serializable {
    public static final String O = "0";
    public static final String X = "1";
    public static final String EMPTY = "-";
    
    
    private String playerToMove;
    private double utility; // 1: win for X, 0: win for O, 0.5: draw
    
    private int winner;
    public String[] board;

    public Pieces vectorOfPieces[];
    private Pieces origen;
    private int tamTablero;
    private int stack;
    private double stateValue;
    private boolean eatAPiece;    //aqui contabilizaremos el valor de haberse comido una ficha
    private boolean pieceCreated=false;
    private Pieces newPieceCreated=null;;
    
    public MonkeyQueenState(int tamTablero, int stack,XYLocation whiteQueenCoord,XYLocation blackQueenCoord){
        vectorOfPieces = new Pieces[tamTablero*tamTablero];
        origen=null;
        winner=-1;
        playerToMove = X;
        utility = -1;
        eatAPiece=false;
        
        for (int i=0;i<vectorOfPieces.length;i++){
            vectorOfPieces[i]=null;
        }
        this.tamTablero=tamTablero;
        this.stack=stack;
        
        XYLocation posIni;
        XYLocation posIni2;
        if (whiteQueenCoord!=null && blackQueenCoord!=null){
            posIni=blackQueenCoord;
            posIni2=whiteQueenCoord;
        } else {
            //establecemos las posiciones iniciales
            posIni = new XYLocation(tamTablero/2-1, 0);
            posIni2 = new XYLocation(tamTablero/2, tamTablero-1);
        }
        //creamos las dos piezas dentro del tablero
        vectorOfPieces[0]=new Pieces(stack,0,posIni);
        vectorOfPieces[1]=new Pieces(stack,1,posIni2);


        //inicializamos el tablero
        board = new String[tamTablero*tamTablero];
        
        int posReina0=(posIni.getYCoOrdinate()*tamTablero)+posIni.getXCoOrdinate();
        int posReina1=posIni2.getYCoOrdinate()*tamTablero+posIni2.getXCoOrdinate();

        for (int i=0;i<tamTablero*tamTablero;i++){
            if (posReina0==i){
                board[i]=Integer.toString(vectorOfPieces[0].getStack());
            } else{
                if (posReina1==i){
                    board[i]=Integer.toString(vectorOfPieces[1].getStack());
                } else {
                    board[i]=EMPTY;   
                }
            }
        }
    }

    /** Constructor para cargar juegos y para generar los nuevos estados sucesores     */
    public MonkeyQueenState(int stack, int tamTablero, Pieces[] vectorOfPieces, String playerToMove, int winner, double stateValue){
        this.stack=stack;
        this.tamTablero=tamTablero;
        this.playerToMove=playerToMove;
        this.winner=winner;
        this.stateValue=stateValue;
        this.eatAPiece=false;
                
        this.vectorOfPieces=new Pieces[vectorOfPieces.length];
        for (int i=0; i<vectorOfPieces.length;i++){
            if (vectorOfPieces[i]==null){
                this.vectorOfPieces[i]=null;
                continue;
            }
            this.vectorOfPieces[i]=new Pieces(vectorOfPieces[i].getStack(), 
                                              vectorOfPieces[i].getPlayer(), 
                                              vectorOfPieces[i].getCoordinates());
        }
        
        this.board = new String[tamTablero*tamTablero];
        int temp=0;
        for (int i=0;i<tamTablero*tamTablero;i++){
            if (this.vectorOfPieces[i]==null) board[i]=EMPTY;
            else {
                //board[i]=Integer.toString(vectorOfPieces[i].getStack());
                temp=this.vectorOfPieces[i].getCoordinates().getYCoOrdinate()*this.tamTablero+
                        this.vectorOfPieces[i].getCoordinates().getXCoOrdinate();
                board[temp]=""+this.vectorOfPieces[i].getStack();
            }
        }
    }

    public int getTamTablero(){ return tamTablero; }
    public int getStack() { return stack; }
    
    public String getPlayerToMove() {
        return playerToMove;
    }

//    public boolean isEmpty(int col, int row) {
//        return board[getAbsPosition(col, row)] == EMPTY;
//    }

    public String getValueOfPieceAt(int col, int row) {
        Pieces piece = getPieceAt(new XYLocation(col, row));
        if (piece==null) return EMPTY;
        else return Integer.toString(piece.getStack());
    }

    //define el estado final
    public double getUtility() {
        return winner;
    }

    public boolean mark(XYLocation action) {
        if (winner!=-1) return false;
        return mark(action.getXCoOrdinate(), action.getYCoOrdinate());
    }

    /**
     *  Esta funcion define el movimiento desde un origen (ya establecido)
     * a las coordenadas indicadas. El player que relaiza el movimiento esta
     * definido por la pieza origen.
     * 
     * El movimiento implica:
     *  - analizar si es un movimiento valido segun las reglas.
     *  - analizar si es un movimiento para comer o no
     *  - modificar las coordenadas de la pieza a la nueva posicion
     *  - modificar board????
     * 
     * @param col columna de la pos destino
     * @param row fila de la pos destino
     */
    public boolean mark(int col, int row) {
        if (validMove(col,row)) {
            //en validMove comprobamos que desde origen se pueda mover a destino
            
            if (origen.getStack()>=2){
                //significa que es una reina
                if (!removePieceAt(col,row)){
                    //significa que la reina NO va a comer, compruebo que tenga stack >2
                    //ya que sino seria un movimiento ilegal
                    if (origen.getStack()==2){
                        return false;
                    }
                    //entonces genera un monkey y se decrementa el stack
                    generateNewPiece(origen.getPlayer(),origen.getCoordinates());
                    origen.setStack(origen.getStack()-1);
                }
            } else {
                //es un monkey
                removePieceAt(col,row);
            }
            //actualizo las coordenadas del origen
            origen.setCoordinates(new XYLocation(col, row));    
            this.removeOrigen();
            playerToMove=playerToMove==X ? O:X;
            return true;
        } else {
            return false;
        }
    }

        
     /**
      * para cada estado, tras ejecutar la accion marcada, llamar a la funcion setStateValue
      * que hace uso de la heuristica para obtener el valor de ese estado para el jugador
      * que esta jugando. En caso de que se haya alcanzado un estado final para el jugador
      * o el jugador rival establecer un valor maximo o -maximo (segun sea si gana o pierde)
      * 
      * 
      * Una vez hecho esto modificar el alfaBethaSearch (en las funciones min y max)
      * las condiciones de salida, para que tambien tenga en cuenta si se ha profundizado mucho
      * o si se ha agotado el tiempo de la jugada.
      * En este caso debe devolver el valor min o max que tenga hasta ese entonces
      * 
      * En las funciones de min/max establecer una condicion de parada, que devuelva, en ese caso,
      * la llamada a la funcion getUtility, la cual va a devolver el valor que tiene
      * asociado ese estado (generado en la funcion mark de ese estado) segun la heuristica
      * utilizada
      */
    public void mark(PiecesToMove pieceToMove, int heuristic){

        Pieces piece = getPieceAt(pieceToMove.getPiece().getCoordinates());     //ahora piece es un puntero a un objeto de este estado
        XYLocation coord = pieceToMove.getCoords();   //coordenadas de destino
        
        if (piece.getStack()>=2){
            //significa que es una reina
            if (!removePieceAt(coord.getXCoOrdinate(),coord.getYCoOrdinate())){
                //significa que la reina NO va a comer, compruebo que tenga stack >2
                //ya que sino seria un movimiento ilegal
                eatAPiece=false;
                if (piece.getStack()!=2){
                    //entonces genera un monkey y se decrementa el stack
                    generateNewPiece(piece.getPlayer(),piece.getCoordinates());
                    piece.setStack(piece.getStack()-1);
                } else {
                    return;
                }
                
            }
        } else {
            //es un monkey
            if (!removePieceAt(coord.getXCoOrdinate(),coord.getYCoOrdinate())){
                eatAPiece=false;
            }
        }
        piece.setCoordinates(coord);   //actualizo las coordenadas de la pieza a mover
        setStateValue(heuristic);               //determina el valor heuristico del estado alcanzado
//        if (winner==-1)
            playerToMove=(playerToMove==X)? O:X;
    }
    
    /**
     * Es una replica del mark anterior, solo que para cuando se utiliza sin heuristica
     */
    public void mark(PiecesToMove pieceToMove){

        Pieces piece = getPieceAt(pieceToMove.getPiece().getCoordinates());     //ahora piece es un puntero a un objeto de este estado
        XYLocation coord = pieceToMove.getCoords();   //coordenadas de destino
        
        if (piece.getStack()>=2){
            //significa que es una reina
            if (!removePieceAt(coord.getXCoOrdinate(),coord.getYCoOrdinate())){
                //significa que la reina NO va a comer, compruebo que tenga stack >2
                //ya que sino seria un movimiento ilegal
                eatAPiece=false;
                if (piece.getStack()!=2){
                    //entonces genera un monkey y se decrementa el stack
                    generateNewPiece(piece.getPlayer(),piece.getCoordinates());
                    piece.setStack(piece.getStack()-1);
                } else {
                    return;
                }
                
            }
        } else {
            //es un monkey
            if (!removePieceAt(coord.getXCoOrdinate(),coord.getYCoOrdinate())){
                eatAPiece=false;
            }
        }
        piece.setCoordinates(coord);   //actualizo las coordenadas de la pieza a mover
        setStateValue(1);               //determina el valor heuristico del estado alcanzado
        playerToMove=(playerToMove==X)? O:X;
    }
    
    /**
     * Crearemos una lista de objetos PiecesToMove, que contiene dentro
     * una pieza y la posicion a la que se peude mover. Con lo cual tendremos una 
     * lista con todas las piezas y sus posibles movimientols
     * @return  lista conm todos los posibles movimientos
     */
    public List<PiecesToMove> getAllPosiblesMoves(){
        Pieces actualOrigen=origen;         //para salvaguardar el origen
        List<PiecesToMove> result = new ArrayList<PiecesToMove>();
        
        for (Pieces piece: vectorOfPieces){
            origen=piece;
            
            //si no hay pieza continuamos
            if (origen==null ) continue;
            
            //si la pieza no es del player que debe jugar continuamos
            if (origen.getPlayer()!=Integer.parseInt(playerToMove)) continue;
            
            /* miramos a ver todos los movimientos validos para cada pieza
             * y si es valido creamos (y añadimos a la lista) un nuevo objeto
             * PiecesToMove con la coordenada a la cual se puede mover          */
            for (int i=0;i<vectorOfPieces.length;i++){
                if (validMove(i%tamTablero, i/tamTablero)){
                    //si es un movimiento valido desde origen se crea el objeto, y se añade a la lista
                    result.add(new PiecesToMove(origen,new XYLocation(i%tamTablero, i/tamTablero)));
                }
            }
        }
        
        origen=actualOrigen;
        //en result tenemos toda una lista de objetos, cada cual contiene 1 pieza
        //y una posicion a la que se peude mover
        return result;
    }

    @Override
    public MonkeyQueenState clone() { //necesario para el juego normal
        MonkeyQueenState copy = null;
        try {
            copy = (MonkeyQueenState) super.clone();
            copy.board = Arrays.copyOf(board, board.length);
            //copy.vectorOfPieces = Arrays.copyOf(vectorOfPieces, vectorOfPieces.length);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace(); // should never happen...
        }
        return copy;
    }
    
    public MonkeyQueenState clone2(){//necesario para el juego por busqueda
        MonkeyQueenState copy=new MonkeyQueenState(this.stack,this.tamTablero,this.vectorOfPieces,this.playerToMove,this.winner,this.stateValue);
        return copy;
    }

    @Override
    public boolean equals(Object anObj) {
        if (anObj != null && anObj.getClass() == getClass()) {
            MonkeyQueenState anotherState = (MonkeyQueenState) anObj;
            for (int i = 0; i < tamTablero*tamTablero; i++)
                if (board[i] != anotherState.board[i])
                    return false;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int row = 0; row < tamTablero; row++) {
            for (int col = 0; col < tamTablero; col++)
                buf.append(getValueOfPieceAt(col, row) + " ");
            buf.append("\n");
        }
        return buf.toString();
    }

        
    /** Esta funcion nos indica que inicializemos la pieza q1ue controlara el movieminto
     *   estableciendop un origen para ella.
     * 
     * @param origen indica la coordenada para la pieza origen
     */
    public void setOrigen(XYLocation coord) {
        int p = playerToMove==X ? 1:0;
        Pieces temp=getPieceAt(coord);
        if(temp!=null && temp.getPlayer()==p){
            this.origen=getPieceAt(coord);
        }
    }

    /* Metodos para Piece:origen*/
    public void removeOrigen() {
        this.origen=null;
    }

    public Pieces getOrigen(){ 
        return this.origen; 
    }
    
    public XYLocation getOrigenCoord() {
        if (origen==null) return null;
        else return this.origen.getCoordinates();
    }

    public boolean hasOrigenPiece() {
        return (this.origen!=null);
    }
    
    
    
    /* -----------------------------------------------------------------------  *
     *                          METODOS desarrollados                           *
     * -----------------------------------------------------------------------  */

    private int getAbsPosition(int col, int row) {
        return row * tamTablero + col;
    }

    /** Nos devuelve el player localizado en la posicion indicada por X e Y     *
     * @param coordX: coordenada X                                              *
     * @param coordY: coordenada Y                                              */
    public int getPlayerAt(int coordX, int coordY) {
        XYLocation c= new XYLocation(coordX, coordY);
        Pieces p = getPieceAt(c);
        
        if (p!=null) return p.getPlayer();
        else return -1;
    }

    
    /** Accedemos a la pieza que esta localizada en las coordenadas pasadas por parametro
     * @param origen define las coordenadas para las que queremos obtener la pieza
     * @return devuelve la peiza en las coordenadas, o null
     */
    private Pieces getPieceAt(XYLocation origen) {
        for (int i=0;i<vectorOfPieces.length;i++){
           if(vectorOfPieces[i] != null){
                if(vectorOfPieces[i].getCoordinates().getXCoOrdinate()==origen.getXCoOrdinate() &&
                   vectorOfPieces[i].getCoordinates().getYCoOrdinate()==origen.getYCoOrdinate()){
                    return vectorOfPieces[i];
                }
            }
        }
        return null;
    }

    /**Esta funcion nos evalua si desde las coordenadas de origen se puede mover
     * a las coordenadas de destino
     * 
     * @param origenX: coordenada X de origen
     * @param origenY: coordenada Y de origen
     * @param col: coordenada X de destino
     * @param row: coordenada Y de destino
     * @return devuelve true si es posible dicho movimineto, false si no
     */
    private boolean validMoveDirection (int origenX, int origenY, int col, int row){
        int diagonal = Math.abs(origenX-col) - Math.abs(origenY - row);
        boolean valid=true; //sirve para indicar si el movimiento sera calido o no
        if (origenX==col || origenY==row || diagonal==0) {
            //compruebo que tipo de movimiento es, y en funcion del tipo
            //de movimiento compruebo si hay fichas en medio
            
            int ini, fin, ini2, fin2;
            if (origenX==col){
                //vamos a irnos desplazando por toda la columna buscando fichas
                if (origenY>row){
                    ini=row;
                    fin=origenY;
                } else{
                    ini=origenY;
                    fin=row;
                }
                for (int i=ini+1;i<fin;i++){
                    if (getPieceAt(new XYLocation(origenX, i))!=null) valid=false;
                }
            } else if (origenY==row) {
                //vamos a irnos desplazando por toda la fila buscando fichas
                if (origenX>col){
                    ini=col;
                    fin=origenX;
                } else{
                    ini=origenX;
                    fin=col;
                }
                for (int i=ini+1;i<fin;i++){
                    if (getPieceAt(new XYLocation(i, origenY))!=null) valid=false;
                }
            } else if (diagonal==0){
                //vamos a irnos desplazando por toda la diagonal
                //primero comprobamos si es una diagonal normal o inversa
                if ( (origenX+origenY)==(col+row)){
                    //diagonal normal
                    
                    //ahora definimos origen y fin para desplazarnos siempre de abajo hacia arriba
                    if (getAbsPosition(origenX, origenY)>getAbsPosition(col, row)){
                        ini=origenX;
                        fin=col;
                        ini2=origenY;
                        fin2=row;
                    } else {
                        ini=col;
                        fin=origenX;
                        ini2=row;
                        fin2=origenY;
                    }
                
                    for (int i=ini+1;i<fin;i++){        //incrementando
                        for (int j=ini2;j>fin2;j--){    //lo recorremos decrementando
                            if ((i+j)==(origenX+origenY)){  //condicion de diagonalidad
                                if (getPieceAt(new XYLocation(i, j))!=null) valid=false;
                            }
                        }
                    }
                } else {
                    //diagonal invertida
                    //definimos origen y fin, origen, para movernos de arriba hacia abajo
                    if (getAbsPosition(origenX, origenY)<getAbsPosition(col, row)){
                        ini=origenX;
                        fin=col;
                        ini2=origenY;
                        //fin2=row;
                    } else {
                        ini=col;
                        fin=origenX;
                        ini2=row;
                        //fin2=origenY;
                    }

                    //si encuentra una ficha en la diagonal define el movimiento como invalido
                    for (int i=ini+1;i<fin;i++){        
                        ini2++;
                        if (getPieceAt(new XYLocation(i, ini2))!=null) valid=false;
                    }
                }
            }
            
            if (!valid) return false;      
            else return true;
        }
        return false;
    }
    /**
     * Funcion para comprobar si me puedo mover al destino. Evalua:
     *  - que sea un destino valido (mismo eje X || mismo eje Y || en la diagonal)
     *  - que no existan piezas en el camino entre origen y destino
     *  - si hay una pieza en el destino que sea de otro jugador
     *  - si intento mover un monkey compruebo si lo hace acorde a las reglas.
     * @param col posicion en el eje x
     * @param row posicion en el eje y
     * @return true si es un movimiento valido, falso si no
     */
    private boolean validMove(int col, int row) {
        //lo primero es comprobar que es un destino valido
        int origenX=origen.getCoordinates().getXCoOrdinate();
        int origenY=origen.getCoordinates().getYCoOrdinate();
        
        // validMoveDirection(int origenX, int OrigenY, int col, int row)
        boolean valid=validMoveDirection(origenX, origenY, col, row);
        if (valid) {

            //ya se ha comprobado que sea un movimiento valido acorde a su recorrido
            
            Pieces piece = getPieceAt(new XYLocation(col, row));
            //primero comprobamos si hay una pieza en el destino,
            //en cuyo caso comprobamos si es del otro player (si va a comer se puede mover libremente)
            if (piece!=null){
                if (piece.getPlayer()==origen.getPlayer()) return false;
                else return true;
            }

            //comprobamos si va a jugar la reina o un monkey
            if (origen.getStack()<2){
                //es un monkey, comprobamos si el movimiento respeta las reglas.
                int temp=(origen.getPlayer()+1)%2;  //las reinas estan en pos 0 o 1
                XYLocation coord=vectorOfPieces[temp].getCoordinates();  //posicion de la reina enemiga

                /* obtengo la distancia DESDE la posicion inicial a la reina,   *
                 * obtendo la distancia DESDE la posicion destino a la reina.   *
                 * la segunda debe ser inferior a la primera para ser valido    */
                double aux;
                aux=(Math.pow((coord.getXCoOrdinate()-origenX), 2) +
                    Math.pow((coord.getYCoOrdinate()-origenY), 2));
                
                double dist_1 = Math.sqrt(aux);
                aux=(Math.pow((coord.getXCoOrdinate()-col), 2) +
                    Math.pow((coord.getYCoOrdinate()-row), 2));
                
                double dist_2 = Math.sqrt(aux);
                
                if (dist_2>=dist_1) return false;
            } else {
                //es una reina. Debo evaluar a ver si va a comer o si va a moverse y tiene stack>2
                if (origen.getStack()==2){
                    //si tiene stack==2 su movimiento solo es valido si VA a comer
                    //aunque eso ya se comprobo arriba, en "piece!=null", asi que
                    //si llega aqui entonces debe ser false
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Eliminamos una ficha de la posicion indicada por col y row. Tambien
     * se comprueba si eliminamos a la reina, en cuyo caso gana el jugador
     * que la elimino.
     * @param col indica la posicion en el eje x
     * @param row indica la posicion en el eje y
     * @return true si la elimina, false si no
     */
    private boolean removePieceAt(int col, int row) {
        for (int i=0;i<vectorOfPieces.length;i++){
           if(vectorOfPieces[i] != null){
                if(vectorOfPieces[i].getCoordinates().getXCoOrdinate()==col &&
                   vectorOfPieces[i].getCoordinates().getYCoOrdinate()==row){
                    vectorOfPieces[i]=null;
                    eatAPiece=true;
                    if (i==0 || i==1) {
                        if (i==0) winner=1; //ganan las blancas
                        if (i==1) winner=0; //ganan las negras
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * genera un nuevo monkey para el jugador indicado y en las coordenadas pasadas
     * 
     * @param player player dueño del monkey
     * @param coordinates coordenadas origen de la ficha
     */
    private void generateNewPiece(int player, XYLocation coordinates) {
        pieceCreated=true;
        for (int i=0;i<vectorOfPieces.length;i++){
            if (vectorOfPieces[i]==null){
                vectorOfPieces[i]=new Pieces(1, player, coordinates);
                newPieceCreated=vectorOfPieces[i];
                break;
            }
        }
    }

    //pendiente de analizar si la reina se puede mover o no
    public int getWinner() {
        return winner;
    }

    public double getStateValue() {
        
        return stateValue;
    }
    
    /* Si el player ha ganado entonces le marco el estado como valor maximo, sino
     llamo a la heuristica para determinar que es*/
    private void setStateValue(int heuristic){
        if (winner==-1)
            if (heuristic==1){ 
                this.stateValue=heuristic1();
            } else {
                this.stateValue=heuristic2();
            }
            //this.stateValue=heuristic2();
        else 
            stateValue=30000;
        
    }
    
    /* ---------------------------- HEURISTICAS ------------------------------- */
    /** heuristica 1, nos evalua acorde a una formula definida                  *
     * @return el valor heuristico del estado                                   */
    private double heuristic1(){
        //X=1(BLANCAS)    O=0(NEGRAS)
        int temp=0;
        double value=0;
        int playerNumberOfPieces=0;
        int enemyNumberOfPieces=0;
        int player=Integer.parseInt(playerToMove);
        XYLocation playerQueenCoord = vectorOfPieces[player].getCoordinates();
        
        
        if (vectorOfPieces[(player+1)%2]==null){
            return Double.POSITIVE_INFINITY;
        }
        if (vectorOfPieces[(player+1)]==null){
//        if (vectorOfPieces[(player)]==null){
            return Double.NEGATIVE_INFINITY;
        }
        /* Recorremos todas las piezas y vamos tomando diversos datos en funcion
         * de que pieza sea la que estamos mirando                              */
        for (Pieces piece: vectorOfPieces){
            if (piece!=null){
                if (piece.getPlayer()!=player){ 
                    enemyNumberOfPieces++;
                    /* la pieza es del rival!!.
                      * Debemos mirar:
                      *  - si puede comer a nuestra reina
                      *  - casillas a las que se puede mover libremente (sin morir)   */

                    //miramos a ver si podria comer a nuestra reina, condicion malisima
                    if (canMoveTo(piece,playerQueenCoord)){
                        value=Double.NEGATIVE_INFINITY;
                        break;
                    }

                    /* ahora contamos a ver el numero de movimientos libre de 
                      * esta pieza y al ser enemigo lo restamos al value.
                      *  Comprobamos si es la reina enemiga, en cuyo caso vale doble */
                    if (piece.getStack()>=2){
                        value=value-(numberOfSafeMovesForThePiece(piece)*2);
                    } else {
                        value=value-numberOfSafeMovesForThePiece(piece);
                    }
                    
                } else {
                    playerNumberOfPieces++;
                    /* La pieza es propia!!.
                     * Debemos mirar:
                     *  - si la reina puede comer a un monkey sin perjuicio... que lo haga
                     *  - numero de movimientos libres.
                     *    * si es la reina *2, pero en caso de que el numero
                     *      sea inferior a 3 al estado le ponemos un -70 de penalizacion
                     *  - si un monkey amenaza a la reina enemiga:
                     *    * amenaza segura (el monkey esta protegido)     +50
                     *    * amenaza valdia (el monkey no esta protegido)  +10
                     *  - Monkeys que pueden comer monkeys rivales:
                     *    * amenaza segura (protegido)                    +10
                     *    * amenaza valdia (no protegido)                 +1
                     *   ----------------------------------------------------   */
                    temp = numberOfSafeMovesForThePiece(piece);
                    if (piece.getStack()>=2){
                        //es la reina par la que estamos calculando el movimiento
                        value=value+((temp)*2);
                        if (temp<5){
                            //si tiene menos de 5 movimientos disponibles es un mal estado
                            //value=value-70;
                            value=value-10;
                        }
                        
                        if (piece.getStack()<=3){
                            if (!eatAPiece) {
                                value=value - ( (5-piece.getStack()) *5);
                            } else {
                                value=value+5;
                            }
                        }
                        
                        //si la reina puede comer sin peligro SIGNIFICA  QUE SE LA VAN A COMER
                        if (canBeEaten(piece)){
//                            if (canEatEnemyQueen(piece)){
//                                value=Double.POSITIVE_INFINITY;
//                            } else {
                                value=Double.NEGATIVE_INFINITY;
//                            }
                            break;
                        }
                        
                        if (pieceCreated){
                            //value=value+150;
                            value=value+15;
                        }
                        
                    } else {
                        //ademas ya sabemos que la ficha es un monkey
                        value=value+temp;
                        
                        //ahora miramos las amenazas de nuestro monkey sobre la reina enemiga
                        if (canEatEnemyQueen(piece)){
                            //value=value+10;
                            value=value+5;
                            
                            //ahora comprobamos si el monkey esta protegido.
                            if (protectedPiece(piece)){
                                //value=value+300;
                                value=value+25;
                                if (invulnerablePiece(piece)){
                                    value=value+2000;
                                    //value=Double.POSITIVE_INFINITY;
                                    //break;
                                } else{
                                    //value=value-250;
                                    value=value-35;
                                }
                            } else {
                                value=value-20;
                            }
                            if (canBeEaten(piece)){
                                value=value-5;
                            }
                              
                        } else {
                            
                            if (pieceCreated && newPieceCreated==piece) {
                                //estamos en el nuevo monkey creado
                                value=value+8;
                            } else {
                                if (canBeEaten(piece)){
                                    //value=value-200;
                                    value=value-10;
                                } else {
                                    value=value+5;
                                    //value=value+200;
                                }
                            }
                        }
                    }
                }
            }
        }
        //para favorecer estados que aumenten el numero de piezas del tablero
        temp=playerNumberOfPieces-enemyNumberOfPieces;
        //value=value+(1*temp);

//        if (temp<=0){
//            if (pieceCreated){
//                value=value+7;
//            } else {
//                value=value-5;
//            }
//        }
//        if (eatAPiece){
//          value+=250;
//          eatAPiece=false;
//        }
        
        return value;
    }
    
    /*REVISAR HEURISTIC2, FUNCIONA CASI GENIAL!!*/
    private double heuristic2(){
        double value=0;
        int playerNumberOfPieces=0;
        int enemyNumberOfPieces=0;
        int player=Integer.parseInt(playerToMove);
        int moves=0;
        
        if (vectorOfPieces[(player+1)%2]==null){
            return Double.POSITIVE_INFINITY;
        }
//        if (vectorOfPieces[player]==null){
//            return Double.NEGATIVE_INFINITY;
//        }
        //if (vectorOfPieces[(player+1)%2]==null) return Double.POSITIVE_INFINITY;
        for (Pieces piece:vectorOfPieces){
            if (piece!=null){
                if (piece.getPlayer()==player){
                    if (canEatEnemyQueen(piece) && piece.getStack()==1){
                        value+=15;
//                        if (invulnerablePiece(piece)){
//                            return Double.POSITIVE_INFINITY;
//                        }
                    }
                    if (pieceCreated) value+=10;
                    playerNumberOfPieces++;
                    moves=+numberOfSafeMovesForThePiece(piece);
                } else {
                    enemyNumberOfPieces--;
                    moves=-numberOfSafeMovesForThePiece(piece);
                }
                moves=numberOfSafeMovesForThePiece(piece);
            }
        }
        
        //if (eatAPiece) { value=10; }
//        if (moves!=0) value+=(playerNumberOfPieces-enemyNumberOfPieces)*moves;
        if (moves!=0) value+=(playerNumberOfPieces+enemyNumberOfPieces)*moves;
        else value+=playerNumberOfPieces-enemyNumberOfPieces;
        if (value==0){
            if (pieceCreated) return 10;
            else return 0;
        }
        return value;
    }
    
    /**
     * Esta funcion nos evalua si una determinada pieza puede ser atacada por mas de 1 ficha enemiga
     * @param piece pieza a evaluar
     * @return true si la pieza solo peude ser atacada por 1 ficha, false sino
     */
    private boolean invulnerablePiece(Pieces piece){
        int count=0;
        for (Pieces ficha:vectorOfPieces){
            if (ficha!=null && ficha.getPlayer()!=piece.getPlayer()){
                //ficha es una pieza del rival
                if (canMoveTo(ficha, piece.getCoordinates())){
                    //aqui comprobamos si la ficha rival se puede mover (comer)
                    //a la posicion de nuestra pieza
                    count++;
                    if (count>=2){
                        //si existen 2 piezas que puedan comer a nuestra ficha entonces false
                        return false;
                    }
                }
            }
        }      
//        return false;
        return true;
    }
    
    /**
     * Tenemos una pieza (piece) y debemos comprobar si peude comer alguna ficha
     * del rival que NO este protegida.
     * 
     * @param piece pieza a evaluar si puede comer alguna de manera segura
     * @return 
     */
    private boolean canBeEaten(Pieces piece){
        Pieces actualOrigen=origen;
        origen=piece;
        for (Pieces ficha:vectorOfPieces){
            /* si es una ficha valida y del player enemigo entonces:
             *  debo comprobar si puedo comerla
             *  debo comprobar si esta protegida                                */
            if (ficha!=null && ficha.getPlayer()!=piece.getPlayer()){
                if (validMoveDirection(piece.getCoordinates().getXCoOrdinate(),
                                       piece.getCoordinates().getYCoOrdinate(), 
                                       ficha.getCoordinates().getXCoOrdinate(),
                                       ficha.getCoordinates().getYCoOrdinate())){
                    //significa que puedo comer la ficha destino
//                    if (!protectedPiece(ficha)){
//                        //significa que esta ficha NO esta protegida
//                        origen=actualOrigen;
//                        return false;
//                    }
                    origen=actualOrigen;
                    return true;
                }
                
            }
        }
        origen=actualOrigen;
        return false;
    }
    
    /* Nos devuelve true si la pieza pasada pro parametro tiene a alguna otra ficha
     * del mismo jugador apuntando a su casilla, es decir, si esta protegida  */
    private boolean protectedPiece(Pieces piece){
        
        Pieces actualOrigen=origen;           //para salvaguardar el origen
        origen=piece;
        /* tenemos que recorrer todo el vector buscando las piezas que son del 
         * mismo player, y luego ver si dicha ficha se puede mover a las 
         * coordenadas de piece                                                 */
        for (Pieces ficha:vectorOfPieces){
            if (ficha!=null && ficha.getPlayer()==piece.getPlayer()){
                
                /*evaluamos a ver si es un movimiento valido desde ficha a piece*/
                if (validMoveDirection(ficha.getCoordinates().getXCoOrdinate(),
                                       ficha.getCoordinates().getYCoOrdinate(), 
                                       piece.getCoordinates().getXCoOrdinate(),
                                       piece.getCoordinates().getYCoOrdinate())) {
                    origen=actualOrigen;
                    return true;
                }
            }
        }
        origen=actualOrigen;
        return false;
    }
    
    /*comprobamos si la pieza pasada se puede mover a la posicion indicada*/
    private boolean canMoveTo(Pieces piece, XYLocation coord){
        Pieces preOrigen=origen;
        origen=piece;
        
        //boolean valid=validMove(coord.getXCoOrdinate(), coord.getYCoOrdinate());
        boolean valid=validMoveDirection(origen.getCoordinates().getXCoOrdinate(), 
                                         origen.getCoordinates().getYCoOrdinate(),
                                         coord.getXCoOrdinate(),
                                         coord.getYCoOrdinate());
        
        origen=preOrigen;
        return valid;
    }
    
    /** indica si con la ficha piece podemos comernos a la reina enemiga*/
    private boolean canEatEnemyQueen(Pieces piece){
        Pieces preOrigen=origen;
        origen=piece;
        
        XYLocation eQueenCoord=vectorOfPieces[(origen.getPlayer()+1)%2].getCoordinates();
        //boolean valid=validMove(eQueenCoord.getXCoOrdinate(), eQueenCoord.getYCoOrdinate());
        boolean valid=validMoveDirection(origen.getCoordinates().getXCoOrdinate(), 
                                         origen.getCoordinates().getYCoOrdinate(),
                                         eQueenCoord.getXCoOrdinate(),
                                         eQueenCoord.getYCoOrdinate());
        
        origen=preOrigen;
        return valid;
    }
    
    /* contamos el numero de casillas a las que se puede mover la ficha pasada  *
     * por parametro sin morir por ataques de las fichas rivales.               *
     * Valido tanto para contar los movimientos de mis piezas como del rival    */
    private int numberOfSafeMovesForThePiece(Pieces piece){
        int moves=0;
        //int jugador=Integer.parseInt(playerToMove);//para comprobar si es una ficha rival o propia
        
        List<PiecesToMove> pieceMoves = new ArrayList<PiecesToMove>();
        Iterator<PiecesToMove> it;
        //PiecesToMove posibleMove;
        
        Pieces actualOrigen=origen;           //para salvaguardar el origen
        origen=piece;
        
        /*Ahora tenemos que obtener todos los movimientos validos para esta pieza*/
        for (int i=0;i<vectorOfPieces.length;i++){
            if (validMove(i%tamTablero, i/tamTablero)){
                //si es un movimiento valido desde origen se crea el objeto, y se añade
                pieceMoves.add(new PiecesToMove(origen,new XYLocation(i%tamTablero, i/tamTablero)));
            }
        }
        /* EN ESTE PUNTO TENEMOS UNA LISTA CON TODOS LOS MOVIMIENTOS VALIDOS */
        
        /* en principio el numero de movimientos a salvo para cada ficha es igual
         * al tamaño de la lista de movimientos posibles (luego se va restando) */
        moves=pieceMoves.size();
        
        /* Ahora se van recorriendo todas las fichas y viendo a ver si el player
         * de esta ficha (pieceIterator) es el opuesto al player de la pieza pasada
         * por parametor, en cuyo caso habria que comprobar si esta ficha (pieceIterator)
         * se podria mover a las coordenadas de piece, en cuyo caso la comeria y 
         * debemos marcar este movimiento como NO seguro, con lo cual restamos 1 */
        for (Pieces pieceIterator:vectorOfPieces){
            if (pieceIterator!=null){
                if (pieceIterator.getPlayer()!=piece.getPlayer()){
                    for (PiecesToMove posibleMove:pieceMoves){
                        /* pieceIterator es del jugador rival
                         * posibleMove.getTo() indica ls posicion de un posible movimiento de mi pieza
                         * 
                         * si la ficha del rival (pieceIterator) se puede mover a
                         * las coordenadas de mi posible movimiento tonces decremento
                         * los movimientos libres */
                        //if (canMoveTo(pieceIterator, piece.getCoordinates())){
                        if (validMoveDirection(posibleMove.getCoords().getXCoOrdinate(),
                                posibleMove.getCoords().getYCoOrdinate(),
                                pieceIterator.getCoordinates().getXCoOrdinate(),
                                pieceIterator.getCoordinates().getYCoOrdinate())){
                            moves--;
                        }
                    }
                }
            }
        }
        
        origen=actualOrigen;
        return moves;
    }
    
    private int casillasEnAtaque(){
        int count=0;
        for (int i=0;i<tamTablero*tamTablero;i++){
            if (validMove(i%tamTablero, i/tamTablero))
                count++;
        }
        return count;
    }
            
    private int countMoves(int index){
        int count=0;
        Pieces actualOrigen=origen;
        
        origen=vectorOfPieces[index];
        for (int i=0;i<vectorOfPieces.length;i++){
            if (validMove(i%tamTablero, i/tamTablero)){
                //si es un movimiento valido desde origen se crea el objeto, y se añade
                count++;
            }
        }
        origen=actualOrigen;
        return count;
    }
    
    private int countMonkeys(){
        int count=0;
        for (Pieces piece : vectorOfPieces){
            if (piece!=null){
                if (piece.getStack()<2) count++;
            }
        }
        return count;
    }
    
    
    public String getPieces(){
        //if (hasOrigenPiece()){
//            if (vectorOfPieces[0]==null){
//                return "la posicion 0 esta a null y jugo antes: "+(playerToMove==this.X?O:X)+
//                        "y ademas en la pos 1 del vector esta la reina del jugador: "+
//                        vectorOfPieces[1].getPlayer();
//            }
//            if (vectorOfPieces[1]==null){
//                return "la posicion 1 esta a null y jugo: "+(playerToMove==this.X?O:X);
//            }
//            return "ninguna a null";
        //} else return "";
        if (!hasOrigenPiece()) return Double.toString(heuristic1());
        else return "";
    }
}
