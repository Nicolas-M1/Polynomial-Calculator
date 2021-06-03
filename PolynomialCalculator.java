// The advanced output will show the matrices at each step of the way

import java.math.BigDecimal;
import java.util.Scanner;
import java.math.RoundingMode;
import java.math.MathContext;

class Main {
  private static final String instructions = ""+
  "\n\nDISCLAIMERS:\nThis program takes a set of points and returns the polynomial which contains them all. \n"+
  "Repl.it has a character limit per line so if you would like to exceed that when entering a set of points, just type it in this format:\n"+"[[2,3],[4,5], ENTER\n[5,6],[7,8], ENTER\n[9,8]]\n"+
  "The detailed output will show the matrices at each step in the program.\n\n"
  +"";
   private static int printDecimalPlaces = 6; //number of decimal places it will round to when printing the equation
   private static final int PDP_ADVANCED = 6; //number of decimal places to round to in the advanced output equation
   private static final int MATRIX_DECIMAL_PLACES = 8;
   private static final int MATRIX_RESULT_DECIMAL_PLACES = 12;
   private static final RoundingMode TRIM_ROUNDING = RoundingMode.HALF_UP;
   
   private static final BigDecimal ROUND_UP_MIN = new BigDecimal(".5");
   private static final String SAMPLE_INPUT = "[[-4, 5], [2, 10], [20, 1/3], [-1, 0]]";
   
   private static final int MAX_DECIMAL_PLACES = 50;
   private static MathContext digitLimit = new MathContext(MAX_DECIMAL_PLACES, RoundingMode.HALF_UP); 
   // MathContext holds the instructions for rounding irrational quotients
   
   
   private static final char CLOSE_PAREN = ']';
   private static final char OPEN_PAREN = '[';
   private static final char SEPARATOR = ',';
   private static final char FRACTON = '/';

   private static final BigDecimal DEFAULT_VALUE = new BigDecimal("1");
   
   
  public static void main(String[] args) {  
    System.out.println(instructions);    
    Scanner input = new Scanner(System.in);
    System.out.println("Please enter the points for which you would like to"
    +" calculate a polynomial in this format "+SAMPLE_INPUT+":");
    String entry = input.nextLine();
    while(entry.trim().lastIndexOf(SEPARATOR) == entry.length()-1){
      entry += input.nextLine();
    }//if
    System.out.println("Would you like to see a more detailed output? (type \"yes\", anything else for no)");
    boolean advanced = input.nextLine().equals("yes");
    if(advanced) {printDecimalPlaces = PDP_ADVANCED;}

    BigDecimal[][] points = processInput(entry);
    BigDecimal[][] result = constructMatrix(points);
    if(advanced){
      System.out.println("System of equations matrix:");
      printMatrix(result);
      System.out.println();
    }//if
    rref(result); 
    if(advanced){
      System.out.println("Matrix in reduced row echelon form:");
      printMatrix(result);
      System.out.println();
    }//if
    
    System.out.println("Polynomial equation:");
    System.out.println(getEquation(result));   
  }


  private static BigDecimal[][] processInput(String input){
    int currentIndex = input.indexOf(OPEN_PAREN)+1;
    BigDecimal[][] temp = new BigDecimal[2][numPoints(input)];
    int arrayIndex = 0;

    //when it's equal to minus one it means it reached the last point
    while(currentIndex != -1){
      BigDecimal currentX = getNextX(input, currentIndex);
      BigDecimal currentY = getNextY(input, currentIndex);
      temp[0][arrayIndex] = currentX;
      temp[1][arrayIndex] = currentY;
      currentIndex = input.indexOf(OPEN_PAREN, currentIndex+1);
      arrayIndex++;
    }//while
    return temp;
  }//processInput

  //fromIndex is expected to be on or before the OPEN_PAREN
  private static BigDecimal getNextX(String source, int fromIndex){
    int startIndex = source.indexOf(OPEN_PAREN, fromIndex);
    int endIndex = source.indexOf(SEPARATOR,fromIndex);
    String strNum = source.substring(startIndex+1,endIndex);
    return toDouble(strNum);
  }//getNextX

  //fromIndex is expected to be immediately after the previous OPEN_PAREN
  private static BigDecimal getNextY(String input, int fromIndex){
    int startIndex = input.indexOf(SEPARATOR,fromIndex);
    int endIndex = input.indexOf(CLOSE_PAREN,fromIndex);
    String strNum = input.substring(startIndex+1, endIndex);
    return toDouble(strNum);
  }//getNextY

  private static BigDecimal toDouble(String input){
    input = input.trim();
    int fractionIndex = input.indexOf(FRACTON);
    if(fractionIndex!=-1){
      BigDecimal numerator;
      BigDecimal denominator;
      BigDecimal returnVal;
      try{
        numerator = new BigDecimal(input.substring(0,fractionIndex).trim());
        denominator = new BigDecimal(input.substring(fractionIndex+1).trim());
        returnVal = numerator.divide(denominator, digitLimit);
      }//try
      catch(Exception e){
        System.out.println("Invalid fraction, replaced \""+input+"\" with "+DEFAULT_VALUE);
        returnVal = DEFAULT_VALUE;
      }//catch
      return returnVal;
    }//if
    else{
      BigDecimal result;
      try{
        result =new BigDecimal(input.trim());
      }//try
      catch(Exception e){
        System.out.println("Invalid input, replaced \""+input+"\" with "+DEFAULT_VALUE);
        result = DEFAULT_VALUE;
      }//catch
      return result;
    }//else
  }//toDouble

  private static int numPoints(String input){
    int currentIndex = input.indexOf(OPEN_PAREN)+1;
    int count = 0;
    while(currentIndex != 0){
      currentIndex = input.indexOf(OPEN_PAREN,currentIndex);
      currentIndex++;
      count++;
    }//while
    return count-1;
  }//numPoints





   
   private static String getEquation(BigDecimal[][] result){
      String equation = "y = ";
      int current;
      for(current = 0; current < result.length; current++){
         int degree = result.length-1-current;
         BigDecimal rawValue = result[current][result[current].length-1];
         BigDecimal value;
         boolean hasE = rawValue.toString().indexOf("E-") !=-1;
         if(hasE){
           value = trim(rawValue, printDecimalPlaces+(-1)*getE(rawValue));
           if(value.compareTo(new BigDecimal(0)) == 0){
             value = new BigDecimal(0);
             hasE = false;
           }//if
         }//if
         else{
          value = trim(rawValue, printDecimalPlaces);
         }//else
         String stringDouble = value.toString();
         boolean isPositive = rawValue.compareTo(new BigDecimal(0))>0;
         if(isPositive || current == 0){
            if(current > 0){
               equation += " + ";
            }//if
         }//if
         else{
            equation += " - ";
            value = (value.multiply(new BigDecimal(-1)));
         }//else

         if(hasE){
           equation += "("+value.toString()+")";
         }//if
         else{
           equation += value.toString();
         }//else
         
         if(degree > 1) equation+= "x^"+degree;
         else if (degree ==1) equation += "x";
      }//for
      return equation;
   }//getEquation

   private static int getE(BigDecimal input){
     String inputStr = input.toString();
     String strNum = inputStr.substring(inputStr.indexOf('E')+1).trim();
     return Integer.parseInt(strNum);
   }//getE
   
   //this method rounds the decimal to [decimalPlaces] places
   private static BigDecimal trim(BigDecimal raw, int decimalPlaces){
      BigDecimal temp = raw.setScale(decimalPlaces, TRIM_ROUNDING);
      return temp;
   }//trim
   

   //will round BigDecimals up if their next decimal place is
   //greater than or equal to ROUND_UP_MIN
   private static BigDecimal round(BigDecimal input){
      if((input.remainder(new BigDecimal(1))).abs().compareTo(ROUND_UP_MIN) > 0){
         if(input.compareTo(new BigDecimal(0)) < 0){
            input.add(new BigDecimal(-1));
         }//if
         else{
            input.add(new BigDecimal(1));
         }//else
      }//if
      return input;
   }//round
   

  //input: a 2 by n point matrix
  //output: the polynomial matrix
  private static BigDecimal[][] constructMatrix(BigDecimal[][] points){
    if(points.length != 2){
      return null;
    }//if
    int size = points[0].length; //equal to the number of coefficients we need to calculate
    BigDecimal[][] matrix = new BigDecimal[size][size+1]; //prepares appended matrix
    for(int pointIndex = 0; pointIndex < points[0].length; pointIndex++){
      BigDecimal xVal = points[0][pointIndex]; //gets x val from points
      BigDecimal yVal = points[1][pointIndex]; //gets y val from points
      matrix[pointIndex][size] = yVal; //sets last value of each row to be y
      for(int xDegree = 0; xDegree < size; xDegree++){ 
        //xDegree is equal to the degree of the x value corresponding to the current coefficient
        BigDecimal currentResult = power(xVal, xDegree); 
        //currentResult is the corresponding x value to the current coefficient
        // i.e. if it's currently on ax^3, currentResult = x^3
        matrix[pointIndex][ size-1-xDegree] = currentResult;
      }//for
    }//for
    return matrix;
  }//constructMatrix

  private static BigDecimal power(BigDecimal base, int exponent){
    BigDecimal current = new BigDecimal(1);
    for(int i = 0; i < exponent; i++){
      current = current.multiply(base);
    }//for
    return current;
  }//power


  private static void rref(BigDecimal[][] input){
    for(int pos = 0; pos < input.length; pos++){
      BigDecimal previousVal = input[pos][pos];
      if(previousVal.compareTo(new BigDecimal(0)) == 0 && pos != input.length-1){
         swapRows(input, pos, input.length-1);
         previousVal = input[pos][pos];
         // the only cell that can already have zeros is the one that equals the constant
      }//if
      BigDecimal multiplier = (new BigDecimal(1)).divide(previousVal,digitLimit);
      multiplyRow(input,pos,multiplier);
      for(int row = 0; row < input.length; row++){ 
      //makes all other values in that column equal zero
        if(row!=pos){
          BigDecimal current = input[row][pos];
          addMultipleOfRow(input, pos, row, current.multiply(new BigDecimal(-1)));
        }//if
      }//for
    }//for
  }//rref
  
  private static void swapRows(BigDecimal[][] matrix, int first, int second){
   int size = matrix[first].length;
   BigDecimal[] temp = new BigDecimal[size];
   for(int col = 0; col < size; col++){
      temp[col] = matrix[first][col];
   }//for
   for(int col = 0; col < size; col++){
      matrix[first][col] = matrix[second][col];
   }//for
   for(int col = 0; col < size; col++){
      matrix[second][col] = temp[col];
   }//for
  }//swapRows

  private static void multiplyRow(BigDecimal[][] input, int row, BigDecimal multiplier){
    for(int column = 0; column < input[row].length; column++){
      input[row][column] = multiplier.multiply(input[row][column]);
    }//for
  }//multiplyRow

  private static void addMultipleOfRow(BigDecimal[][] input, int sourceRow, int targetRow, BigDecimal multiplier){
    for(int index = 0; index < input[targetRow].length; index++ ){
      input[targetRow][index] = input[targetRow][index].add(multiplier.multiply(input[sourceRow][index]));
    }//for
  }//addMultipleOfRow


  private static void printMatrix(BigDecimal[][] input){
    for(int row = 0; row<input.length; row++){
      for(int col = 0; col < input[row].length; col++){
        String num;
        if(col != input[row].length-1) num = trim(input[row][col], MATRIX_DECIMAL_PLACES).toString();
        else num = trim(input[row][col], MATRIX_RESULT_DECIMAL_PLACES).toString();
        System.out.print(num + "\t\t");
      }//for
      System.out.println();
    }//for
  }//printMatrix
}//class