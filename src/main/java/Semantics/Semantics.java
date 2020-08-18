package Semantics;

import AST.AST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Semantics {
    //Таблица символов ключ - имя переменной, значение - список областей видимости
    private static HashMap<String, List<Scope>> symbolTable = new HashMap<>();
    //Список семантических ошибок
    private static List<String> errors = new ArrayList<>();

    //Анализ дерева, передаётся в качестве аргумента само дерево, облать видимости и уровень
    // глубины в области видимости
    public static void analysis_tree(List<AST> tree, String scope, int level) {
        //Обход по узлам дерева
        for(AST node : tree) {
            switch (node.getType()) {
                case("Block Procedure"):
                case("Block While"):
                case("Block If Else"):
                case("Block If"):
                case("Block Else"):
                    //Если погружаемся глубже, то увеличиваем уровень
                    level++;
                    analysis_tree(node.getChildren(), scope + "->" + level, level);
                    break;
                case("Variable declaration"):
                case("Variable declaration and assign"):
                    //Если видим объявление переменных, то отправляем их для добавления в таблицу
                    add_in_symbol_table(node.getChildren(), scope);
                    //Если объявление и присваивание, то ещё и отправляем на проверку, чтобы сошлись типы
                    if(node.getType().equals("Variable declaration and assign"))
                        check_declaration_and_assignment(node.getChildren().get(1), scope);
                    break;
                case("Equality expr"):
                    //Если присваивание, отправляем на проверку типов
                    check_equality_expr(node, scope);
                    break;
                case("Appeal"):
                    //Отправляем на проверку Appeal
                    check_appeal(node, scope);
                    break;
                case ("If declaration"):
                case ("While declaration"):
                    check_logical_expr(node.getChildren().get(1), scope);
                    break;
            }
        }
    }

    public static void check_logical_expr(AST node, String scope) {
        //Проверка логического выражения, например a(операнд 1) < b(операнд 2)
        AST operand1 = node.getChildren().get(0);
        AST operand2 = node.getChildren().get(2);

        //Если допусти такая ситуация a < b And b < c, то получается логическое выражение из двух логических выражений
        //Тогда отправляем операнды (которые являются на самом логическим выражением, в эту же функцию
        if(operand1.getType().equals("Logical expr") || operand2.getType().equals("Logical expr")) {
            if (operand1.getType().equals("Logical expr"))
                check_logical_expr(operand1, scope);
            if (operand2.getType().equals("Logical expr"))
                check_logical_expr(operand2, scope);
        }
        else {
            //Сравниваем два операнда
            String result = checking_types_two_nodes(operand1, operand2, scope);
            if (result.equals("Error"))
                add_error_type_difference_two_nodes(operand1, operand2);
        }
    }

    public static void check_appeal(AST node, String scope) {
        AST arg;
        String name = node.getChildren().get(0).getName(), typeArg;
        int numberLine = node.getLine();
        int numberArg = node.getChildren().get(1).getChildren().size() - 2;
        switch (name) {
            case ("Console.Write"):
            case ("Console.WriteLine"):
                //У Console.Write может быть только 1 аргумент, иначе ошибка
                if(numberArg != 1) {
                    add_error_argument_appeal(name, 1, numberLine);
                    return;
                }
                //Проверка типа аргумента
                arg = node.getChildren().get(1).getChildren().get(1);
                typeArg = get_type_argument(arg, scope);
                if(!typeArg.equals("String") && !typeArg.equals("Integer"))
                    add_error_type_argument_appeal(name, "String' or 'Integer", numberLine);
                break;
            case ("Console.ReadLine"):
                //Должен быть без аргументов, иначе ошибка
                if(numberArg != 0) {
                    add_error_argument_appeal(name, 0, numberLine);
                    return;
                }
                break;
            default:
                //Если это array.Length() тогда не должно быть аргументов
                 if(name.contains(".Length")) {
                    if(numberArg != 0)
                        add_error_argument_appeal(name, 0, numberLine);
                    return;
                 }
                 //В остальных случаях только один аргумент целого типа
                if(numberArg != 1) {
                    add_error_argument_appeal(name, 1, numberLine);
                    return;
                }
                arg = node.getChildren().get(1).getChildren().get(1);
                typeArg = get_type_argument(arg, scope);
                if(!typeArg.equals("Decimal") && !typeArg.equals("Integer"))
                    add_error_type_argument_appeal(name, "Integer", numberLine);
                break;
        }
    }

    public static String get_type_argument(AST arg, String scope) {
        String typeArg = arg.getType();
        //Если аргумент переменная или массив, получаем тип с помощью функции
        if(typeArg.equals("Id") || typeArg.equals("Appeal")) {
            typeArg = get_data_type_variable(arg, scope);
        }
        return typeArg;
    }

    public static void check_equality_expr(AST node, String scope) {
        //Переменная, к которой приравниваем выражение
        AST variable = node.getChildren().get(0);
        //Её тип
        String typeVar = get_data_type_variable(variable, scope);
        //То, что приравниваем
        AST arithmeticExpr = node.getChildren().get(2);
        //Её тип
        String typeExpr = check_arithmetic_expression(arithmeticExpr, scope);
        //Сравниваем типы
        String resultCheck = checking_two_types(typeVar, typeExpr);
        //Если ошибка, то ошибка
        if(resultCheck.equals("Error"))
            add_error_declared_and_assign(arithmeticExpr.getLine());
     }

    public static void check_declaration_and_assignment(AST node, String scope) {
        //такой же смысл как и у check_equality_expr
        String dataType = get_type_declaration(node.getChildren());
        AST arithmeticExpr = node.getChildren().get(2);
        String typeExpr = check_arithmetic_expression(arithmeticExpr, scope);

        String resultCheck = checking_two_types(dataType, typeExpr);
        if(resultCheck.equals("Error"))
            add_error_declared_and_assign(arithmeticExpr.getLine());
    }

    public static String check_arithmetic_expression(AST expression, String scope) {
        //Проверка типа арифмитического выражения
        //Тип выражения, пока никакой
        String typeExpr = "";
        //Если вдруг мы приравниваем не ариф выражение, а переменную или число
        if(!expression.getType().equals("Arithmetic expr")) {
            typeExpr = expression.getType();
            //Если переменная, получаеем её тип
            if(typeExpr.equals("Id") || typeExpr.equals("Appeal"))
                return get_data_type_variable(expression, scope);
            else
                return typeExpr;
        } else {
            //Если ариф выражение состоит из большого примера, то делаем проверки
            // по два члена выражения и сравниваем их типы
            AST node1, node2;
            for(int i = 0; i + 2 < expression.getChildren().size(); i += 2) {
                node1 = expression.getChildren().get(i);
                node2 = expression.getChildren().get(i + 2);
                typeExpr = checking_types_two_nodes(node1, node2, scope);
                if(typeExpr.equals("Error"))
                    add_error_type_difference_two_nodes(node1, node2);
            }
            return typeExpr;
        }
    }

    public static String checking_types_two_nodes(AST node1, AST node2, String scope) {
        //Проверка типов двух узлов
        //Получаем их типы и отправляем в другую функцию, которая как раз и занимается сравнением типов
        String type1 = node1.getType();
        String type2 = node2.getType();

        if(type1.equals("Id") || type1.equals("Appeal"))
            type1 = get_data_type_variable(node1, scope);

        if(type2.equals("Id") || type2.equals("Appeal"))
            type2 = get_data_type_variable(node2, scope);

        return checking_two_types(type1, type2);
    }

    public static String checking_two_types(String type1, String type2) {
        //Если во время проверки где то выпало Error (разные типы), то и сейчас возвращаем Error
        if(type1.equals("Error") || type2.equals("Error"))
            return "Error";

        switch (type1) {
            case ("Decimal"):
            case ("Integer"):
            case ("Double"):
            case ("Float"):
                if(type2.equals("String"))
                    return "Error";
                else
                    return "Integer";
            case ("String"):
                if(type2.equals("String") || type2.equals("Read"))
                    return "String";
                else
                    return "Error";
            default:
                return "Error";
        }
    }

    public static String get_data_type_variable(AST node, String scope) {
        //Получение типа данных переменной
        //Если это массив, то нужно получить его имя и отправить в эту функ-ю
        if(node.getType().equals("Appeal")) {
            //Также отправляем на проверку элемента
            check_appeal(node, scope);
            return get_data_type_variable(node.getChildren().get(0), scope);
        }

        String nameVar = node.getName();
        //Если имя это функция
        if(nameVar.equals("Console.ReadLine"))
            return "Read";
        //Если это например array.Length(), то она Integer
        if(nameVar.contains(".Length"))
            return "Integer";
        //Если таблица содержит ключ
        if (symbolTable.containsKey(nameVar)) {
            //Получаем все возможные области объявлений
            List<Scope> listScopes = symbolTable.get(nameVar);
            //Обходим, пока не найдём нужную
            for (Scope s : listScopes) {
                //Если переменная входит в область видимости
                if (s.getName().equals(scope) || scope.contains(s.getName())) {
                    return s.getType();
                }
            }
        }
        add_error_undeclared_var(node);
        return "Error";
    }

    public static void add_in_symbol_table(List<AST> declarationVar, String scopeVis) {
        //Узел с перечислением перменных, которые объявляются
        List<AST> declaredVar = declarationVar.get(0).getChildren();
        //Тип объявления
        String type = get_type_declaration(declarationVar.get(1).getChildren());
        //Имя переменной
        String name;
        for (AST var : declaredVar) {
            if(var.getType().equals("Appeal")) {
                check_appeal(var, scopeVis);
                name = var.getChildren().get(0).getName();
            } else
                name = var.getName();

            if(!name.equals("Dim")) {
                //Если в таблице уже есть объявление это й переменной, то добавляем в список новую область
                if (symbolTable.containsKey(name)) {
                    List <Scope> scopes = symbolTable.get(name);
                    scopes.add(0, new Scope(type, scopeVis));
                    symbolTable.put(name, scopes);
                }
                else {
                    List <Scope> scopes = new ArrayList<>();
                    scopes.add(new Scope(type, scopeVis));
                    symbolTable.put(name, scopes);
                }
            }
        }
    }

    //Рекурсивная проходка к нужному узлу, чтобы извлечь имя типа
    public static String get_type_declaration(List<AST> nodes) {
        if(!nodes.get(0).getChildren().isEmpty())
            return get_type_declaration(nodes.get(0).getChildren());
        else
            return nodes.get(1).getName();
    }

    public static void print_symbolTable() {
        for (Map.Entry<String, List<Scope>> entry : symbolTable.entrySet()) {
            System.out.print("\n" + entry.getKey() + ":\n");
            for (Scope s : entry.getValue()) {
                System.out.println(s.getName() + " " +  s.getType());
            }
        }
    }

    private static void add_error_type_argument_appeal(String name, String expectType, int lineNumber) {
        String text = "'" + name + "' argument can only be of type '" + expectType + "'";
        String error = "SEMANTICS: Line: " + lineNumber + " \"" + text + "\"";

        if(!errors.isEmpty()) {
            if(errors.contains(error))
                return;
        }
        errors.add(error);
    }

    private static void add_error_argument_appeal(String name, int expectArg, int lineNumber) {
        String text = "'" + name + "' must have " + expectArg + " argument";
        String error = "SEMANTICS: Line: " + lineNumber + " \"" + text + "\"";

        if(!errors.isEmpty()) {
            if(errors.contains(error))
                return;
        }
        errors.add(error);
    }

    private static void add_error_declared_and_assign(int lineNumber) {
        String text = "The type declaration does not match the type of the assigned value";
        String error = "SEMANTICS: Line: " + lineNumber + " \"" + text + "\"";

        if(!errors.isEmpty()) {
            if(errors.contains(error))
                return;
        }
        errors.add(error);
    }

    private static void add_error_undeclared_var(AST variable) {
        int lineNumber = variable.getLine();
        String text = "Variable " + "'" + variable.getName() + "' is undeclared";
        String error = "SEMANTICS: Line: " + lineNumber + " \"" + text + "\"";

        if(!errors.isEmpty()) {
            if(errors.contains(error))
                return;
        }
        errors.add(error);
    }

    private static void add_error_type_difference_two_nodes(AST node1, AST node2) {
        int lineNumber = node1.getLine();
        String text = "'" + node1.getName() + "' and '" + node2.getName() + "' are of different types";
        String error = "SEMANTICS: Line: " + lineNumber + " \"" + text + "\"";

        if(!errors.isEmpty()) {
            if(errors.contains(error))
                return;
        }
        errors.add(error);
    }

    public static HashMap<String, List<Scope>> getSymbolTable() { return symbolTable; }

    public static List<String> getErrors() { return errors; }

    public static void setErrors(List<String> errors) { Semantics.errors = errors; }

    public static void setSymbolTable(HashMap<String, List<Scope>> symbolTable) {
        Semantics.symbolTable = symbolTable;
    }

    public static void print_errors() {
        if(!errors.isEmpty()) {
            System.out.println("Errors:");
            for(String e : errors) {
                System.out.println(e);
            }
        }
    }

}
