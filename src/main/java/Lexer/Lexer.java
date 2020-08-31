package Lexer;

import Token.Token;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private static List<Token> tokens = new ArrayList<>();
    private static List<Token> unidentifiedTokens = new ArrayList<>();

    public static void file_handler(File file) {
        String readLine;
        int lineCounter = 0;

        try {
            FileReader fr = new FileReader(file); //создаем BufferedReader с сущ. FileReader для построчного считывания
            BufferedReader reader = new BufferedReader(fr); // считаем сначала первую строку
            readLine = reader.readLine();
            while (readLine != null) {
                lineCounter++;
                line_handler(readLine, lineCounter, tokens);
                readLine = reader.readLine(); // считываем остальные строки в цикле
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void line_handler(String processedString, int numbLine, List<Token> listTokens) {
        List<Token> bufTokens = new ArrayList<>();

        List<String> subStr = spliting_string(processedString); //Разбивание строки на токены

        token_initialization(bufTokens, subStr, numbLine);

        listTokens.addAll(bufTokens);
    }

    public static List<String> spliting_string(String str) {
        List<String> subStr = new ArrayList<>();

        str = adding_space(str); //Вставка пробелов см. функцию

        if(str.contains("'")) {  // Игнорирование комментариев
            String[] bufStr = str.split("'");
            if(bufStr.length > 1)
                if(!bufStr[0].contains("'"))
                    str = bufStr[0];
            if(str.contains("'"))
                str = "";
            /*Берёмя только левую часть от комментария
            * т.е. если "1 + 2 = 3 'Это комментарий"
            * то останется только "1 + 2 = 3"   */
        }
        if(str.contains("\"")) {
            /* Проверка на строчный литерал, если есть то нужно аккуратно его вырезать, а не делить по пробелам
            * Всё остальное нужно разделить на пробелы */
            subStr = spliting_by_str_literal(str);
        } else {
            subStr = Arrays.asList(str.split(" ")); //Разбивка строки по пробелам
        }
        return subStr;
    }

    public static List<String> spliting_by_str_literal(String str) {
        int i, j, start = 0;
        //Список всех токенов
        //Делим строку по литералам, чтобы не делить литералы по пробелам
        List<String> subStr = new ArrayList<>();
        //Проходимся по строке
        for(i = 0; i < str.length(); i++) {
            //Если нашли кавычки
            if(str.charAt(i) == '"') {
                //Обрубаем всё до начала кавычек и вставляем в список
                subStr.addAll(Arrays.asList(str.substring(start, i - 1).trim().split(" ")));
                //Запускаем второй цикл для поиска закрывающей кавычки
                for(j = i + 1; j < str.length(); j++) {
                    //Когда нашли или прошли всю строку и не нашли, вырезаем и вставляем в список
                    //и продолжаем поиск на случай если в строке ещё литералы есть
                    if(str.charAt(j) == '"' || j == str.length() - 1) {
                        subStr.add(str.substring(i, j + 1));
                        start = j + 1;
                        i = j + 1;
                        break;
                    }
                }
            }
        }
        //Условие если после литерала последнего что-то осталось, то давбляем в список
        if(start < str.length())
            subStr.addAll(Arrays.asList(str.substring(start).trim().split(" ")));

        return subStr;
    }

    public static void token_initialization(List<Token> listTokens, List<String> subStr, int numbLine) {
        String typeToken;
        for (String s : subStr) {
            if(s.trim().length() != 0) { //Отбрасывание строк состоящих из пробела
                typeToken = token_type_definition(s); //Определение типа токена
                Token token = new Token(s, typeToken, numbLine);
                if(typeToken.equals("Unknown")) //Если неизвестный токен -> отправляется в список ошибок
                    unidentifiedTokens.add(token);
                else
                    listTokens.add(token);
            }
        }
    }

    public static String token_type_definition(String token) {
        switch (token) {
            case ("("):
            case (")"):
            case ("["):
            case ("]"):
                return "Brace";

            case (","):
                return "Comma";

            case ("+"):
            case ("-"):
            case ("*"):
            case ("/"):
            case ("^"):
            case ("Mod"):
                return "ArithmeticOp";

            case ("="):
                return "Equality";

            case (">"):
            case ("<"):
            case ("<="):
            case (">="):
            case ("<>"):
                return "ComparisonOp";

            case ("+="):
            case ("-="):
            case ("*="):
            case ("/="):
                return "AssignOp";

            case ("And"):
            case ("Or"):
            case ("Xor"):
            case ("Not"):
            case ("AndAlso"):
            case ("OrElse"):
                return "LogicOp";

            case ("Integer"):
            case ("String"):
            case ("Double"):
                return "DataType";

            case ("Sub"):
            case ("While"):
            case ("If"):
            case ("Else"):
            case ("Then"):
            case ("As"):
            case ("Dim"):
            case ("End"):
                return "Keyword";

            default:
                if(Pattern.matches("0[bB][_01]*[01]", token))
                    return "Binary";
                else if(Pattern.matches("0[0-7]+", token))
                    return "Octal";
                else if(Pattern.matches("0[xX][\\d[a-f][A-F]]+", token))
                    return "Hexadecimal";
                else if(Pattern.matches("[1-9]\\d*", token))
                    return "Decimal";
                else if(Pattern.matches("[0-9]", token))
                    return "Decimal";
                else if(Pattern.matches("[1-9][\\d.]*", token))
                    return "Float";
                else if(Pattern.matches("0\\.\\d*", token))
                    return "Float";
                else if(Pattern.matches("[\\w&&[^\\d]]\\w*", token))
                    return "Id";
                else if(Pattern.matches("[\\w&&[^\\d]][[.\\w]\\w]*", token))
                    return "Id";
                else if(Pattern.matches("[\"].*[\"]", token))
                    return "String";
                return "Unknown";
        }
    }

    /* Функция adding_space вставляет пробелы перед и после знаков
    * Например "1+2=3" после работы функции будет выглядеть "1 + 2 = 3"*/
    public static String adding_space(String line) {
        String newStr = line;
        String[] ArrayPattern = new String[] {"\\(", "\\)", "\\[", "]", "\\{", "}", "\\*", "/", ";", ":",
                ",", "%", "-", "\\+", "=", "-\\s\\s=",  "\\+\\s\\s=", "-\\s\\s-", "\\+\\s\\s\\+", "=\\s\\s=", "<", ">", "<\\s\\s=",
                ">\\s\\s=", "!\\s=", " \\*\\s\\s=", "/\\s\\s=", "\t", "<\\s\\s>", "\\s\\s"};
        String[] ArrayReplaceable = new String[] {" ( ", " ) ", " [ ", " ] ", " { ", " } ", " * ", " / ",  " ; ", " : ",
                " , ", " % ", " - ", " + ", " = ", " -= ", " += ", " -- ", " ++ ", " == ", " < ", " > ", " <= ", " >= ",
                " != ", " *= ", " /= ", " ", "<>", " "};
        for(int i = 0; i < ArrayPattern.length; i++) {
            Pattern pattern = Pattern.compile(ArrayPattern[i]);
            Matcher matcher = pattern.matcher(newStr);
            newStr = matcher.replaceAll(ArrayReplaceable[i]);
        }
        return newStr;
    }

    public static void print_tokens() {
        for(Token token : tokens) {
            System.out.println(token.getLine() + "  :  " + token.getToken() + "  :  " + token.getType());
        }
    }

    public static void print_errors() {
        if(!unidentifiedTokens.isEmpty()) {
            System.out.println("\nErrors lexer :");
            for (Token token : unidentifiedTokens) {
                System.out.println(token.getLine() + "  :  " + token.getToken() + "  :  " + token.getType());
            }
        }
    }

    public static List<Token> getTokens() {
        return tokens;
    }

    public static List<Token> getUnidentifiedTokens() {
        return unidentifiedTokens;
    }
}
