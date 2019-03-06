/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MonkeyQueen;

import MonkeyQueenGame.MonkeyQueenApp;
import com.sun.org.apache.xpath.internal.operations.Equals;
import javax.swing.JOptionPane;

public class MonkeyQueen {

    public static void main(String[] args) {
        String s[]= new String[]{"","","",""};
        String op = (JOptionPane. showInputDialog("Escriba el tamaño del tablero" ));
        s[0]=op;
        op = (JOptionPane. showInputDialog("Escriba el numero para el stack de reinas" ));
        s[1]=op;
        op = (JOptionPane. showInputDialog("coordenadas (solo x,y) de reina blanca" ));
        s[2]=op;
        op = (JOptionPane. showInputDialog("coordenadas (solo x,y) de reina negra" ));
        s[3]=op;
        
        if ( !s[0].equals("") && !s[1].equals("")){
            MonkeyQueenApp monkeyQueen = new MonkeyQueenApp(s);
        }
    }
}
