import java.util.Scanner;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.io.FileWriter;
import java.io.PrintWriter;

public class systemProject {
    String mantissa = "";
    static int floatingPointSize = -2;
    static boolean isNegative = false;
    static String exponent = "";
    static int ExpMinusBias = 1;
    static int byteOrdering = 0;

    public static void main(String[] args) {
        String numberAsHexadecimal = "";
        Scanner inputScanner = new Scanner(System.in);
        try {
            File file = new File("output.txt");
            FileWriter outputFile = new FileWriter("output.txt");
            Scanner scanner = new Scanner(file);
            PrintWriter output = new PrintWriter(outputFile);

            System.out.println("Enter byte ordering.\nEnter 1 for Little Endian, 2 for Big Endian: ");
            byteOrdering = inputScanner.nextInt();
            while (byteOrdering != 1 && byteOrdering != 2) {
                System.out.println("Invalid byte ordering. Enter 1 for Little Endian, 2 for Big Endian: ");
                byteOrdering = inputScanner.nextInt();
            }

            System.out.println("Enter floating point size (1-4): ");
            floatingPointSize = inputScanner.nextInt();
            while (floatingPointSize != 1 && floatingPointSize != 2 && floatingPointSize != 3
                    && floatingPointSize != 4) {
                System.out.println("Invalid floating point size. Enter a number between 1 and 4 inclusive");
                floatingPointSize = inputScanner.nextInt();
            }

            while (scanner.hasNextLine()) {
                isNegative = false;
                String hexaDecimalAsBinary = "";
                String mantissa = "";

                String data = scanner.nextLine();
                if (data.equals("0") || data.equals("0.0")) {
                    outputWriter("00 00", outputFile, output);
                    continue;
                }
                if (data.contains("-")) {
                    StringBuilder sb = new StringBuilder(data);
                    sb.deleteCharAt(0);
                    data = sb.toString();
                    isNegative = true;
                }
                // For floating point
                if (data.contains(".")) {
                    String[] mantissaArray = stringSplitter(data);
                    String beforeDecimal = mantissaArray[0];
                    String afterDecimal = mantissaArray[1];
                    mantissa = mantissaFormatter(beforeDecimal, afterDecimal);
                    StringBuilder sb = new StringBuilder(mantissa);
                    sb.delete(0, 2);
                    mantissa = sb.toString();
                    exponent = exponentCalculator(mantissa);

                    if (exponent.equals("Overflow")) {
                        outputWriter("Overflow", outputFile, output);
                    }

                    if (isNegative)
                        hexaDecimalAsBinary += "1";
                    else
                        hexaDecimalAsBinary += "0";

                    hexaDecimalAsBinary += exponent + mantissa;
                    numberAsHexadecimal = binaryToHexadecimal(hexaDecimalAsBinary);
                    outputWriter(numberAsHexadecimal, outputFile, output);
                }
                // For unsigned
                else if (data.contains("u")) {
                    String[] u = data.split("u");
                    if (Math.pow(2, 16) - 1 < Integer.parseInt(u[0])) {
                        outputWriter("Overflow", outputFile, output);
                    }
                    String unSignedAsBinary = convertToBinaryBeforeDecimal(Integer.parseInt(u[0]), false);
                    numberAsHexadecimal = binaryToHexadecimal(unSignedAsBinary);
                    outputWriter(numberAsHexadecimal, outputFile, output);

                }
                // For signed
                else {
                    String signedAsBinary = "";
                    if (!isNegative) {
                        signedAsBinary = convertToBinaryBeforeDecimal(Integer.parseInt(data), false);
                        outputWriter(binaryToHexadecimal(signedAsBinary) + "\n", outputFile, output);
                    } else {
                        signedAsBinary = convertToBinaryBeforeDecimal(Integer.parseInt(data), false);
                        boolean oneFound = false;
                        StringBuilder sbuilder = new StringBuilder();
                        for (int i = 15; i >= 0; i--) {
                            if (oneFound) {
                                if (Character.compare(signedAsBinary.charAt(i), '1') == 0) {
                                    sbuilder.append(signedAsBinary);
                                    sbuilder.setCharAt(i, '0');
                                    signedAsBinary = sbuilder.toString();
                                    sbuilder.delete(0, sbuilder.length());
                                } else {
                                    sbuilder.append(signedAsBinary);
                                    sbuilder.setCharAt(i, '1');
                                    signedAsBinary = sbuilder.toString();
                                    sbuilder.delete(0, sbuilder.length());
                                }
                            } else if (Character.compare(signedAsBinary.charAt(i), '1') == 0) {
                                if (!oneFound) {
                                    oneFound = true;
                                }
                            }
                        }
                    }
                    outputWriter(binaryToHexadecimal(signedAsBinary), outputFile, output);
                }
            }
            inputScanner.close();
            scanner.close();
            outputFile.close();
            output.close();
        } catch (

        Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void outputWriter(String hexaToWrite, FileWriter outputFile, PrintWriter output) {
        try {
            output.write(hexaToWrite + "\n");
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public static String[] stringSplitter(String stringToParse) {
        String[] parts = stringToParse.split("\\.");
        String part1 = parts[0];
        String part2 = parts[1];
        parts[0] = convertToBinaryBeforeDecimal(Integer.parseInt(part1), true);
        parts[1] = convertToBinaryAfterDecimal(part2);
        return parts;
    }

    public static String mantissaFormatter(String beforeDecimal, String afterDecimal) {
        int roundedLength = 0;
        int finalLength = 0;
        if (floatingPointSize == 1) {
            roundedLength = 6;
        } else if (floatingPointSize == 2) {
            roundedLength = 9;
        } else if (floatingPointSize == 3) {
            roundedLength = 14;
            finalLength = 15;
        } else if (floatingPointSize == 4) {
            roundedLength = 14;
            finalLength = 21;
        }
        String formattedMantissa = beforeDecimal + afterDecimal;
        StringBuilder sb = new StringBuilder(formattedMantissa);
        if (beforeDecimal.equals("0")) {
            for (int i = 0; i < afterDecimal.length() - 1; i++) {
                if (Character.compare(afterDecimal.charAt(i), '1') == 0) {
                    sb.delete(0, i);
                    sb.insert(1, ".");
                    ExpMinusBias = (i + 1) * (-1);
                    formattedMantissa = sb.toString();
                    break;
                }
            }
        } else {
            sb.insert(1, ".");
            formattedMantissa = sb.toString();
            ExpMinusBias = formattedMantissa.length() - afterDecimal.length() - 2;
        }

        int truncation = roundedLength - formattedMantissa.length();
        if (truncation > 0) {
            while (truncation > 0) {
                formattedMantissa += "0";
                truncation--;
            }
            if (finalLength == 0)
                return formattedMantissa;
            else {
                while (finalLength > formattedMantissa.length()) {
                    formattedMantissa += "0";
                }
                return formattedMantissa;
            }
        }
        String partToRound = formattedMantissa.substring(roundedLength);
        formattedMantissa = formattedMantissa.substring(0, formattedMantissa.length() - partToRound.length());

        if (!partToRound.equals("")) {
            if (Character.compare(partToRound.charAt(0), '0') == 0) {
                // ROUND DOWN
                if (finalLength > 0) {
                    while (finalLength > formattedMantissa.length()) {
                        formattedMantissa += "0";
                    }
                }
                return formattedMantissa;
            } else {
                // CHECK IF THE PART THAT WILL BE ROUNDED OFF HAS ANY OTHER 1's
                for (int i = 1; i <= partToRound.length(); i++) {
                    if (Character.compare(partToRound.charAt(i), '1') == 0) {
                        formattedMantissa = roundUp(formattedMantissa);
                        if (finalLength > 0) {
                            while (finalLength > formattedMantissa.length()) {
                                formattedMantissa += "0";
                            }
                        }
                        return formattedMantissa;
                    }
                }
                // ROUND HALF DOWN
                if (Character.compare(formattedMantissa.charAt(roundedLength), '0') == 0) {
                    if (finalLength > 0) {
                        while (finalLength > formattedMantissa.length()) {
                            formattedMantissa += "0";
                        }
                    }
                    return formattedMantissa;
                } else
                    roundUp(formattedMantissa);
                if (finalLength > 0) {
                    while (finalLength > formattedMantissa.length()) {
                        formattedMantissa += "0";
                    }
                }
                return (formattedMantissa);
            }
        } else
            return formattedMantissa;
    }

    public static String roundUp(String formattedMantissa) {
        StringBuilder strBuilder = new StringBuilder(formattedMantissa);
        for (int j = formattedMantissa.length() - 1; j > 2; j--) {
            if (Character.compare(strBuilder.charAt(j), '0') == 0) {
                strBuilder.setCharAt(j, '1');
                return strBuilder.toString();
            } else {
                strBuilder.setCharAt(j, '0');
            }
        }
        return strBuilder.toString();
    }

    public static String convertToBinaryBeforeDecimal(int number, boolean isFloating) {
        if (number == 0)
            return Integer.toString(number);
        int binary[] = new int[40];
        int i = 0;
        String binaryNumberBeforeDecimal = "";
        while (number > 0) {
            binary[i++] = number % 2;
            number = number / 2;
        }
        for (int j = i - 1; j >= 0; j--) {
            binaryNumberBeforeDecimal += binary[j];
        }
        if (!isFloating) {
            String temp = "";
            for (int k = 16 - binaryNumberBeforeDecimal.length(); k > 0; k--) {
                temp += "0";
            }
            temp += binaryNumberBeforeDecimal;
            return temp;
        }

        return binaryNumberBeforeDecimal;
    }

    public static String convertToBinaryAfterDecimal(String number) {
        if (number.equals("0"))
            return number;
        number = "0." + number;
        double formattedNumber = Double.parseDouble(number);
        String binaryNumberAfterDecimal = "";
        while (formattedNumber != 0) {
            while (formattedNumber * 2 < 1) {
                formattedNumber *= 2;
                binaryNumberAfterDecimal += "0";
            }
            binaryNumberAfterDecimal += "1";
            formattedNumber *= 2;
            formattedNumber -= 1;
        }
        return binaryNumberAfterDecimal;
    }

    public static String exponentCalculator(String formattedMantissa) {
        int exponentBits = 0;
        int bias = 0;
        String exponent = "";
        if (floatingPointSize == 1) {
            exponentBits = 3;
        } else if (floatingPointSize == 2) {
            exponentBits = 8;
        } else if (floatingPointSize == 3) {
            exponentBits = 10;
        } else if (floatingPointSize == 4) {
            exponentBits = 12;
        }

        bias = (int) Math.pow(2, exponentBits - 1) - 1;

        exponent = convertToBinaryBeforeDecimal(bias + ExpMinusBias, true);
        if (exponent.length() > exponentBits) {
            exponent = "Overflow";
            return exponent;
        }
        int truncation = exponentBits - exponent.length();
        while (truncation > 0) {
            exponent += "0";
            truncation--;
        }
        return exponent;
    }

    public static String binaryToHexadecimal(String hexaDecimalAsBinary) {
        Map<String, String> map = new HashMap<String, String>();
        String numAsHexa = "";
        map.put("0000", "0");
        map.put("0001", "1");
        map.put("0010", "2");
        map.put("0011", "3");
        map.put("0100", "4");
        map.put("0101", "5");
        map.put("0110", "6");
        map.put("0111", "7");
        map.put("1000", "8");
        map.put("1001", "9");
        map.put("1010", "A");
        map.put("1011", "B");
        map.put("1100", "C");
        map.put("1101", "D");
        map.put("1110", "E");
        map.put("1111", "F");

        StringBuilder sb = new StringBuilder();
        String fourBits = "";

        for (int i = hexaDecimalAsBinary.length() - 1; i >= 0; i--) {
            fourBits += hexaDecimalAsBinary.charAt(i);
            if (fourBits.length() == 4) {
                sb.append(fourBits);
                fourBits = sb.reverse().toString();
                numAsHexa += map.get(fourBits.toString());
                fourBits = "";
                sb.delete(0, sb.length());
            }
        }

        sb.delete(0, sb.length());
        sb.append(numAsHexa);
        sb.reverse();
        int j = 2;
        for (int i = sb.length() / 2; i > 1; i--) {
            sb.insert(j, " ");
            j = j * 2 + 1;
        }

        if (byteOrdering == 1) {
            sb.append(" " + sb.substring(0, 2));
            sb.delete(0, 3);
        }
        return sb.toString();
    }
}