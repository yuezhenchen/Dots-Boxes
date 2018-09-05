package players.player8;

import players.*;
import board.*;
import util.*;
import java.lang.Math;
import java.util.*;
import javafx.scene.paint.Color;

public class Player8 implements Player {
  private DBG dbg;
  public Player8() {
    dbg = new DBG(DBG.PLAYERS,"Player8");
  }
  
  // labeling squares from 0 to 64
  private int getVal(Square square) {
    int val = square.getRow()*8+square.getCol();
    return val;}
  
  //get a specific square with given index
  private Square findSquare(int val, Board board) {
    Square square = board.getSquare(val/8, val%8);
    return square;
  }
  
  //a set that have lines for random process
  private Set<Line> randomSet(Board board) {
    //form a set with all avalible line + linkes that the sqare itself marked <2
    Set<Line> randomSet = new HashSet<Line>();
    Set<Square> temp = new HashSet<Square>();
    Set<Square> temp2 = new HashSet<Square>();
    temp = board.squaresWithMarkedSides(1);
    temp2 = board.squaresWithMarkedSides(0);
    temp.addAll(temp2);
    
    List<Square> listOfSquare = new ArrayList<Square>(temp);
    for (Square square : listOfSquare) {
      Iterator<Line> openLines = square.openLines().iterator();
      while(openLines.hasNext()){
        Line line = openLines.next();
        if (checkneighbor(line, board)) {
          randomSet.add(line);
        }
      }
    }
    return randomSet;
  }
  
  //form a set with all avaliable line of the neighbor square marked <2
  
  private boolean checkneighbor(Line line, Board board) {
    Set<Square> neighbor = line.getSquares(board);
    Iterator<Square> squareIterator = neighbor.iterator();
    while (squareIterator.hasNext()) {
      if (squareIterator.next().openLines().size() <= 2)
        return false;
    }
    return true;
  }
  
  // whether should be the random state
  private Boolean isRandom(Board board) {
    Set<Line> randomset = randomSet(board);
    if (randomset.isEmpty())
      return false;
    else
      return true;
  }
  
  //a helper function for the isBox function. Dealth with the squares with 
  //2 marked sides
  private boolean check2sidesbox(int value, Board board){
    Square square = findSquare(value, board);
    List<Line> list = new ArrayList<Line>(square.openLines());
    Line line1 = list.get(0);
    Line line2 = list.get(1);
    if (line1.getSquares(board).size()==1){//corner
      if (line2.getSquares(board).size()==1) {return true;}
    }
    if (line1.getSquares(board).size()==1) {
      Side side2 = line2.getSide();
      int neival = attached(side2, value);
      Square nei = findSquare(neival,board);
      if (nei.openLines().size()>2){
        return true;
      }
      return false;
    }
    if (line2.getSquares(board).size()==1) {
      Side side1 = line1.getSide();
      int neival = attached(side1, value);
      Square nei = findSquare(neival,board);
      if (nei.openLines().size()>2){
        return true;
      }
      return false;
    }
    return false;
  }
  
  // return a set of single boxes with the help of isBox function
  private Set<Integer> singlebox(Board board){ 
    Set<Square> square3sides = board.squaresWithMarkedSides(3);
    Set<Square> square2sides = board.squaresWithMarkedSides(2);
    List<Square> squares = new ArrayList<Square>();
    squares.addAll(square3sides);
    squares.addAll(square2sides);
    Set<Integer> single = new HashSet<Integer>();
    if (squares.size() > 0) {
      for (Square square: squares) {
        int val = getVal(square);
        if(isBox(val,board)) {
          single.add(val);
        }
        single.add(null);
      }
    }
    single.remove(65);
    single.remove(null);
    return single;
  }
  
  
  // identify whether a square is a box, which filling the line would not affect
  //other squares
  private boolean isBox(int value, Board board){//63
    Square square = findSquare(value, board);// (7,7)
    List<Line> list = new ArrayList<Line>(square.openLines());
    Line line1 = list.get(0); //east
    if (value == 65) {return false;}
    if (list.size() == 1){
      int nei = attached(line1.getSide(), value);
      if (nei == 65){return true;}
      else if(findSquare(nei,board).openLines().size()>2) {return true;}
      else return false;
    }
    else {//list.size ==2
      if (check2sidesbox(value,board)) {return true;}
      return false;}
  }
  
  //given the set of squares, find any open lines of the given square,
  //select the line if it has <2 marked side
  private Line chooseRandom(Board board) {
    List<Line> shuffledLines = new ArrayList<Line>(randomSet(board));
    Collections.shuffle(shuffledLines);
    return shuffledLines.get(0);
  }
  
  
  public Line makePlay(Board board, Line oppPlay, long timeRemaining) {
    Set<Line> emerg = board.openLines();
    Line emergency = emerg.iterator().next();
    try {
      if (board.gameOver())
        return null;
      if (isRandom(board)) {
        Set<Square> marked3SidesSquares = board.squaresWithMarkedSides(3);
        if (!marked3SidesSquares.isEmpty()) {
          Square square = marked3SidesSquares.iterator().next();
          return square.openLines().iterator().next();
        }
        Line line = chooseRandom(board);
        if (line != null) return line;
      }
      //the non random process
      
      Set<Square> checkset = board.squaresWithMarkedSides(3);
      List<Square> checked = new ArrayList<Square>(checkset);
      
      // when we are the one enter the non random state first
      if(checkset.size()==0) {
        Set<Integer> single = singlebox(board);
        Set<Integer> opendoubles = opendoubleset(board);
        // if there's single box, fill in it first
        if (single.size() > 0) {
          int val = single.iterator().next();          
          Square square = findSquare(val, board);
          Set<Line> sqaureol = square.openLines();
          Line line = sqaureol.iterator().next();
          return line;
        }
        //open double cross should be treated differently according to 
        // the odd or even amount of number in the board
        if(single.size() + opendoubles.size() >0){
          if((single.size()+opendoubles.size()/2)%2 == 1) {
            int sqval = opendoubles.iterator().next();
            Line line = opendoublesharedline(sqval, board);
            return line;
          }
          if((single.size()+opendoubles.size()/2)%2%2 == 0) {
            int sqval = opendoubles.iterator().next();
            Line noline = opendoublesharedline(sqval, board);
            Square sq = findSquare(sqval,board);
            Set<Line> lineset = sq.openLines();
            Line no = lineset.iterator().next();
            Line line3 = null;
            if (no.equals(noline)) {
              lineset.remove(no);
              line3 = lineset.iterator().next();
            }
            else {line3 = no;}
            return line3;
          }
        }
        else {
          Set<Square> randomsq = board.squaresWithMarkedSides(2);
          Square sq = randomsq.iterator().next();
          Line line = sq.openLines().iterator().next();
          return line;
        }
      }
      
      if (checkset.size() == 1) {
        int sqval = getVal(checkset.iterator().next());
        Set<Integer> single = singlebox(board);
        Set<Integer> opendoubles = opendoubleset(board);
        Set<Square> finalcheck = board.squaresWithMarkedSides(2);
        finalcheck.addAll(checkset);
        if(finalcheck.size() <=2 ) {
          return checked.get(0).openLines().iterator().next();}
//      see if the opponent fill in an open double for us, if it is a half open,
        //then we specify the odd or even amount of number on the board
        if(ishalfdouble(sqval,board)) {
          if ((single.size()+opendoubles.size()/2)%2 == 1){
            Square square = checkset.iterator().next();
            Line line = square.openLines().iterator().next();
            return line;
          }
          if((single.size()+opendoubles.size()/2)%2 == 0) {
            Line line = createcloseddouble(sqval, board);
            return line;
          }
          else {
            Line line = createcloseddouble(sqval, board);
            return line;
          }
        }
        
        if (single.size() > 0) {
          Square square = checked.get(0);
          Line line = square.openLines().iterator().next();
          return line;
        }
        
        if(opendoubles.size() >0){
          Square square = checked.get(0);
          Line line = square.openLines().iterator().next();
          return line;
        }

        Square s = checkset.iterator().next();   
        Line l = s.openLines().iterator().next();
        return l;
      }
//    if there is 2 3 marked sides squares
      if (checkset.size() == 2) {
        Set<Integer> single = singlebox(board);
        Set<Integer> opendoubles = opendoubleset(board);
        Set<Square> finalcheck = board.squaresWithMarkedSides(2);
        finalcheck.addAll(checkset);
//      // if there is only 4 squares left, just take them all
        if(finalcheck.size() <=4 ) {
          return checked.get(0).openLines().iterator().next();
        }
        
        // if there's still single box or open double left, just take the 3 marked
        //side squares
        if (single.size()!=0 && opendoubles.size()!=0) {
          Square square = checked.get(0);
          List<Line> lines = new ArrayList<Line>(square.openLines());
          Line line = lines.get(0);
          return line;
        }
        
        Square square1 = checked.get(0);
        Square square2 = checked.get(1);
        if (isBox(getVal(square1),board)) {//int value
          Line line = square1.openLines().iterator().next();
          return line;
        }
        
        if (isBox(getVal(square2), board)) {
          Line line = square2.openLines().iterator().next();
          return line;
        }
        // if it is loop, create two closed double for the opponent,
        // which force them to open the chain for us.
        if (isloop(square1, square2, board)) {
          Line line = halfloop(square1, square2, board);
          return line;
        }
//      if it is a closed double, just take them
        Set<Square> sq = board.squaresWithMarkedSides(3);
        if (isclosedDouble(getVal(sq.iterator().next()), board)) {
          return sq.iterator().next().openLines().iterator().next();
        }
        
        Line line = square1.openLines().iterator().next();
        return line;
      }
      
     //if checkset has more than two 3 marked sides square, it means opponent enter
      //the loop first
      if (checkset.size() > 2) {
        Set<Square> sq = board.squaresWithMarkedSides(3);
        if (isclosedDouble(getVal(sq.iterator().next()), board)) {
          return sq.iterator().next().openLines().iterator().next();
        }
      }
      return null;
    }
    catch (Exception e) {
      System.out.println(e);
      // return emergency;
      return null;
    }
  }
  
  
  private Line createcloseddouble(int value, Board board) {//marked3sides ==1
    Square sq = findSquare(value, board);
    Line line1 = sq.openLines().iterator().next();//only empty line in marked3 sides
    int neival = attached(line1.getSide(), value);
    Set<Line> neilines = findSquare(neival, board).openLines();
    Line nline1= neilines.iterator().next();
    neilines.remove(nline1);
    Line nline2 = neilines.iterator().next();
    if(nline1.getSquares(board).size() ==1) {return nline1;}
    if(findSquare(attached(nline1.getSide(),neival),board).openLines().size() >2 ) {
      return nline1;
    }
    return nline2;
  }
  
  
  private Set<Integer> closeddouble(Board board){
    List<Square> squares = new ArrayList<Square>(board.squaresWithMarkedSides(3));
    Set<Integer> closeddouble = new HashSet<Integer>();
    for (Square square: squares){
      int val = getVal(square);
      if(isclosedDouble(val,board)) {
        closeddouble.add(val);
      }
      closeddouble.add(65);
    }
    closeddouble.remove(65);
    return closeddouble;
  }
  
  private boolean isclosedDouble(int value, Board board) {
    Square sq = findSquare(value,board);
    Line line = sq.openLines().iterator().next();
    int nei = attached(line.getSide(), value);
    if (findSquare(nei,board).openLines().size() == 1) {return true;}
    return false;
  }
  
  private Set<Integer> opendoubleset(Board board) {
    List<Square> squares = new ArrayList<Square>(board.squaresWithMarkedSides(2));
    Set<Integer> opendouble = new HashSet<Integer>();
    for (Square square: squares){
      int val = getVal(square);
      if(isOpenDouble(val,board)) {
        opendouble.add(val);
      }
      opendouble.add(65);
    }
    opendouble.remove(65);
    return opendouble;
  }
  
  private Line opendoublesharedline(int value, Board board) {
    Square sq = findSquare(value, board);
    Set<Line> lines = sq.openLines();
    Line line1 = lines.iterator().next();
    lines.remove(line1);
    Line line2 = lines.iterator().next();
    int val1 = attached(line1.getSide(), value);
    int val2 = attached(line2.getSide(), value);
    if (val1 == 65) {return line2; }
    if (val2 ==65) { return line1;}
    if((checkneighbor(line1,board)) && (!checkneighbor(line2,board))) {
      return line2;
    }
    return line1;
  }
  
  private boolean isOpenDouble(int value, Board board){//only take 2markedside
    Square sq = findSquare(value, board);
    Set<Line> lines = sq.openLines();
    Line line1 = lines.iterator().next();
    lines.remove(line1);
    Line line2 = lines.iterator().next();
    int val1 = attached(line1.getSide(), value);
    int val2 = attached(line2.getSide(), value);//邻居
    
    if(val1 ==65 && val2 != 65) {
      Square nei = findSquare(val2, board);
      Set<Line> neilines = new HashSet<Line>(nei.openLines());
      Line neiline = neilines.iterator().next();
      Line line3 = null;
      if (neilines.size() == 2){
        if (neiline.equals(line2)) {
          neilines.remove(neiline);
          line3 = neilines.iterator().next();
        }
        else line3 = neiline;
        if(attached(line3.getSide(), val2) == 65){ return true;}
        int nval = attached(line3.getSide(), val2);
        Square nsq = findSquare(nval, board);
        if (nsq.openLines().size()>2) { return true;}
        return false;
      }
      return false;
    }
    if(val1 !=65 && val2 == 65) {
      Square nei = findSquare(val1, board);
      Set<Line> neilines = nei.openLines();
      Line neiline = neilines.iterator().next();
      
      if (neilines.size() == 2){
        Line line3 = null;
        if (neiline.equals(line1)) {
          neilines.remove(neiline);
          line3 = neilines.iterator().next();
        }
        else line3 = neiline;
        if(attached(line3.getSide(), val1) == 65){return true;}
        int nval = attached(line3.getSide(), val1);
        Square nsq = findSquare(nval, board);
        
        if (nsq.openLines().size()>2) { return true;}
        return false;
      }
      return false;
    }
    
    if (val1 == 65 && val2 == 65) {
      
    }
    if(val1 != 65 && val2 != 65) {
      if((checkneighbor(line2,board)) && (!checkneighbor(line1,board))) {
        Square nei = findSquare(val1,board);
        Set<Line> neilines = nei.openLines();
        Line neiline = neilines.iterator().next();
        
        if(neilines.size() ==2) {
          Line line3 = null;
          if (neiline.equals(line1)) {
            neilines.remove(neiline);
            line3 = neilines.iterator().next();
          }
          else line3 = neiline;
          if (checkneighbor(line3,board)) {return true; }
          return false;}
        return false;
      }
      
      if((checkneighbor(line1,board)) && (!checkneighbor(line2,board))) { 
        Square nei = findSquare(val2,board);
        Set<Line> neilines = nei.openLines();
        Line neiline = neilines.iterator().next();
        if(neilines.size() ==2) {
          Line line3 = null;
          if (neiline.equals(line1)) {
            neilines.remove(neiline);
            line3 = neilines.iterator().next();
          }
          else line3 = neiline;
          if (checkneighbor(line3,board)) {return true; }
          return false;}
        return false;
      }
    }
    return false;
  }
  
  private Set<Integer> halfdoubleset(Board board) {
    List<Square> squares = new ArrayList<Square>(board.squaresWithMarkedSides(3));
    Set<Integer> halfdoubleset = new HashSet<Integer>();
    for (Square square : squares) {
      if (ishalfdouble(getVal(square), board)) {
        halfdoubleset.add(getVal(square));
      }
      halfdoubleset.add(null);
    }
    halfdoubleset.remove(null);
    return halfdoubleset;
  }
  
  private boolean ishalfdouble(int value, Board board) {
    Square square = findSquare(value, board);
    Line line1 = square.openLines().iterator().next();
    int nei = attached(line1.getSide(),value);//65
    if (nei != 65 ){ 
      Set<Line> neilines = findSquare(nei,board).openLines();
      if(neilines.size() ==2) {
        Line line2 = null;
        Line neiline = neilines.iterator().next();
        if (neiline.equals(line1)) {
          neilines.remove(neiline);
          line2= neilines.iterator().next();
        }
        else line2 = neiline;
        
        if(line2.getSquares(board).size() == 1) {return true;}
        
        int sqval = attached(line2.getSide(), nei);
        if (findSquare(sqval,board).openLines().size() > 2) {return true;}
        return false;
      }
    }
    return false;
  }
  
  private boolean isloop(Square square1, Square square2, Board board) {
    Line line1 = square1.openLines().iterator().next();
    Line line2 = square2.openLines().iterator().next();
    int nei1 = attached(line1.getSide(), getVal(square1));
    int nei2 = attached(line2.getSide(), getVal(square2));
    if (findSquare(nei1, board).openLines().size() == 2 &&
        findSquare(nei2, board).openLines().size() == 2) {
      Set<Line> lines = findSquare(nei1, board).openLines();
      Line nline1 = lines.iterator().next();
      Line nline2 = null;
      if (nline1.equals(line1)) {
        lines.remove(nline1);
        nline2 = lines.iterator().next();
      }
      else {
        nline2 = nline1;
      }
      if(attached(nline2.getSide(), nei1) == nei2) {
        return true;
      }
      return false;
    }
    return false;
  }
  
  private Line halfloop(Square sq1, Square sq2, Board board) {
    Line line1 = sq1.openLines().iterator().next();
    Line line2 = sq2.openLines().iterator().next();
    int nei1 = attached(line1.getSide(), getVal(sq1));
    int nei2 = attached(line2.getSide(), getVal(sq2));
    
    Set<Line> lines = findSquare(nei1, board).openLines();
    Line nline1 = lines.iterator().next();
    Line nline2 = null;
    if (nline1.equals(line1)) {
      lines.remove(nline1);
      nline2 = lines.iterator().next();
    }
    else {nline2 = nline1;}
    return nline2;
  }
  
  //take in open line's side, and get the neighbor square
  private int attached(Side side, int value) { 
    int val = 65;
    if (value < 64 && value > 7 && side == Side.NORTH) {val = value - 8;}
    if (value >= 0 && value < 56 && side == Side.SOUTH) {val = value + 8;}
    if (value % 8 != 7 && side == Side.EAST) {val = value + 1;}
    if (value % 8 != 0 && side ==Side.WEST) { val = value - 1;}
    //    else val = 65;
    return val;
  }
  
  
  
  public String teamName() { return "Professional Procrastinator"; }
  public String teamMembers() { return "Yuezhen Chen & Kaining Shen"; }
  public Color getSquareColor() { return Util.PLAYER1_COLOR; }
  public Color getLineColor() { return Util.PLAYER1_LINE_COLOR; }
  public int getId() { return 1; }
  public String toString() { return teamName(); }
}
