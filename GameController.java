import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.io.*;
import javax.swing.*;


/**
 * The class <b>GameController</b> is the controller of the game. It has a method
 * <b>selectColor</b> which is called by the view when the player selects the next
 * color. It then computesthe next step of the game, and  updates model and view.
 *
 * @author Guy-Vincent Jourdan, University of Ottawa
 */


public class GameController implements ActionListener {

    /**
     * Reference to the view of the board
     */
    private GameView gameView;
    /**
     * Reference to the model of the game
     */
    private GameModel gameModel;

    private GenericLinkedStack<GameModel> stackOne;
    private GenericLinkedStack<GameModel> stackTwo;

    private int currentSelectedColor;
    private int numberOfSteps;

 
    /**
     * Constructor used for initializing the controller. It creates the game's view 
     * and the game's model instances
     * 
     * @param size
     *            the size of the board on which the game will be played
     */
    public GameController(int size) {
        if(!oldGame()){
            gameModel = new GameModel(size);
        }
        gameModel = new GameModel(size);
        gameView = new GameView(gameModel, this);
        stackOne = new GenericLinkedStack<GameModel>();
        stackTwo = new GenericLinkedStack<GameModel>();
        flood();
        gameView.update();
    }

    /**
     * resets the game
     */
    public void reset(){
        gameModel.reset();
        flood();
        gameView.update();
    }

    /**
     * Callback used when the user clicks a button (reset or quit)
     *
     * @param e
     *            the ActionEvent
     */

    /*private void restoreGameModel(GameModel a){
        
        for(int i = 0; i < a.getSize(); i++) {
            for(int j = 0; j < a.getSize(); j++) {
                if(a.isCaptured(i,j)) {
                    gameModel.get(i,j).setCaptured(false);
                } 
            }
        }
    }*/

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof DotButton) {
            if(gameModel.getNumberOfSteps() < 0) {
                gameModel.capture(((DotButton)(e.getSource())).getRow(),((DotButton)(e.getSource())).getColumn());
            }
            stackOne.push(gameModel.clone());
            selectColor(((DotButton)(e.getSource())).getColor());
        } else if (e.getSource() instanceof JButton) {
            JButton clicked = (JButton)(e.getSource());
            if (clicked.getText().equals("Quit")) {
                saveGame();
                 System.exit(0);
             } else if (clicked.getText().equals("Reset")){
                reset();
             } else if(clicked.getText().equals("Undo")) {
                stackTwo.push(gameModel.clone());
                GameModel gameModelTwo = stackOne.pop();
                gameModel = gameModelTwo;
                this.gameModel = gameModelTwo;
                gameView.gameModel = gameModelTwo;
                gameView.update();
             } else if(clicked.getText().equals("Redo")) {
                GameModel gameModelTwo = stackTwo.pop();
                gameModel = gameModelTwo;
                this.gameModel = gameModelTwo;
                gameView.gameModel = gameModelTwo;
                gameView.update();
             }
        } 
    }

    /**
     * <b>selectColor</b> is the method called when the user selects a new color.
     * If that color is not the currently selected one, then it applies the logic
     * of the game to capture possible locations. It then checks if the game
     * is finished, and if so, congratulates the player, showing the number of
     * moves, and gives to options: start a new game, or exit
     * @param color
     *            the newly selected color
     */
    public void selectColor(int color){
        if(color != gameModel.getCurrentSelectedColor()) {
            gameModel.setCurrentSelectedColor(color);
            flood();
            gameModel.step();
            gameView.update();

            if(gameModel.isFinished()) {
                      Object[] options = {"Play Again",
                                "Quit"};
                        int n = JOptionPane.showOptionDialog(gameView,
                                "Congratulations, you won in " + gameModel.getNumberOfSteps() 
                                    +" steps!\n Would you like to play again?",
                                "Won",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[0]);
                        if(n == 0){
                            reset();
                        } else{
                            System.exit(0);
                        }   
                }            
            }        
    }

   /**
     * <b>flood</b> is the method that computes which new dots should be ``captured'' 
     * when a new color has been selected. The Model is updated accordingly
     */
     private void flood() {

        Stack<DotInfo> stack = new GenericArrayStack<DotInfo>(gameModel.getSize()*gameModel.getSize());
        for(int i =0; i < gameModel.getSize(); i++) {
           for(int j =0; j < gameModel.getSize(); j++) {
                if(gameModel.isCaptured(i,j)) {
                    stack.push(gameModel.get(i,j));
                }
           }
        }

        DotInfo dotInfo;
        while(!stack.isEmpty()){
            dotInfo = stack.pop();
            if((dotInfo.getX() > 0) && shouldBeCaptured (dotInfo.getX()-1, dotInfo.getY())) {
                gameModel.capture(dotInfo.getX()-1, dotInfo.getY());
                stack.push(gameModel.get(dotInfo.getX()-1, dotInfo.getY()));
            }  
            if((dotInfo.getX() < gameModel.getSize()-1) && shouldBeCaptured (dotInfo.getX()+1, dotInfo.getY())) {
                gameModel.capture(dotInfo.getX()+1, dotInfo.getY());
                stack.push(gameModel.get(dotInfo.getX()+1, dotInfo.getY()));
            }
            if((dotInfo.getY() > 0) && shouldBeCaptured (dotInfo.getX(), dotInfo.getY()-1)) {
                gameModel.capture(dotInfo.getX(), dotInfo.getY()-1);
                stack.push(gameModel.get(dotInfo.getX(), dotInfo.getY()-1));
            }  
            if((dotInfo.getY() < gameModel.getSize()-1) && shouldBeCaptured (dotInfo.getX(), dotInfo.getY()+1)) {
                gameModel.capture(dotInfo.getX(), dotInfo.getY()+1);
                stack.push(gameModel.get(dotInfo.getX(), dotInfo.getY()+1));
            }
        }
    }


    /**
     * <b>shouldBeCaptured</b> is a helper method that decides if the dot
     * located at position (i,j), which is next to a captured dot, should
     * itself be captured
     * @param i
     *            row of the dot
     * @param j
     *            column of the dot
     */
    
   private boolean shouldBeCaptured(int i, int j) {
        if(!gameModel.isCaptured(i, j) &&
           (gameModel.getColor(i,j) == gameModel.getCurrentSelectedColor())) {
            return true;
        } else {
            return false;
        }
    }

    private void saveGame(){
        try{
            FileOutputStream savedGame = new FileOutputStream("savedGame.ser");
            ObjectOutputStream m = new ObjectOutputStream(savedGame);
            m.writeObject(gameModel);
            m.close(); 
        }
        catch(IOException a){
            System.out.println(a); 
        }
    }

    private boolean oldGame(){
       File old = new File("savedGame.ser");
       try{
           FileInputStream file = new FileInputStream(old);
           ObjectInputStream n = new ObjectInputStream(file);
           gameModel = (GameModel)n.readObject();
           n.close();
           return true;
       }catch(FileNotFoundException e){ 
           System.out.println("File not found " + e);
       }catch(IOException e){
           System.out.println("Error reading file " + e);
       }catch(ClassNotFoundException e){
           System.out.println("Invalid class " + e);
       }
       return false;
   }

}
