package Semantics;

import AST.AST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Semantics {
    private static HashMap<String, List<Scope>> symbolTable = new HashMap<>();
    private static List<String> errors = new ArrayList<>();

    public static void analysis_tree(List<AST> tree, String scope, int level) {
        for(AST node : tree) {
            switch (node.getType()) {
                case("Block Procedure"):
                case("Block While"):
                case("Block If Else"):
                case("Block If"):
                case("Block Else"):
                    level++;
                    analysis_tree(node.getChildren(), scope + "->" + level, level);
                    break;
                case("Variable declaration"):
                case("Variable declaration and assign"):
                    add_in_symbol_table(node.getChildren(), scope);
                    if(node.getType().equals("Variable declaration and assign"))
                        check_declaration_and_assignment(node.getChildren().get(1), scope);
                    break;
                case("Equality expr"):
                    check_equality_expr(node, scope);
                    break;
                case("Appeal"):
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
        AST operand1 = node.getChildren().get(0);
        AST operand2 = node.getChildren().get(2);

        if(operand1.getType().equals("Logical expr") || operand2.getType().equals("Logical expr")) {
            if (operand1.getType().equals("Logical expr"))
                check_logical_expr(operand1, scope);
            if (operand2.getType().equals("Logical expr"))
                check_logical_expr(operand2, scope);
        }
        else {
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
                if(numberArg != 1) {
                    add_error_argument_appeal(name, 1, numberLine);
                    return;
                }
                arg = node.getChildren().get(1).getChildren().get(1);
                typeArg = get_type_argument(arg, scope);
                if(!typeArg.equals("String") && !typeArg.equals("Integer"))
                    add_error_type_argument_appeal(name, "String' or 'Integer", numberLine);
                break;
            case ("Console.ReadLine"):
                if(numberArg != 0) {
                    add_error_argument_appeal(name, 0, numberLine);
                    return;
                }
                break;
            default:
                 if(name.contains(".Length")) {
                    if(numberArg != 0)
                        add_error_argument_appeal(name, 0, numberLine);
                    return;
                 }
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
        if(typeArg.equals("Id") || typeArg.equals("Appeal")) {
            typeArg = get_data_type_variable(arg, scope);
        }
        return typeArg;
    }

    public static void check_equality_expr(AST node, String scope) {
        AST variable = node.getChildren().get(0);
        String typeVar = get_data_type_variable(variable, scope);
        AST arithmeticExpr = node.getChildren().get(2);
        String typeExpr = check_arithmetic_expression(arithmeticExpr, scope);
        String resultCheck = checking_two_types(typeVar, typeExpr);
        if(resultCheck.equals("Error"))
            add_error_declared_and_assign(arithmeticExpr.getLine());
     }

    public static void check_declaration_and_assignment(AST node, String scope) {
        String dataType = get_type_declaration(node.getChildren());
        AST arithmeticExpr = node.getChildren().get(2);
        String typeExpr = check_arithmetic_expression(arithmeticExpr, scope);

        String resultCheck = checking_two_types(dataType, typeExpr);
        if(resultCheck.equals("Error"))
            add_error_declared_and_assign(arithmeticExpr.getLine());
    }

    public static String check_arithmetic_expression(AST expression, String scope) {
        String typeExpr = "";
        if(!expression.getType().equals("Arithmetic expr")) {
            typeExpr = expression.getType();
            if(typeExpr.equals("Id") || typeExpr.equals("Appeal"))
                return get_data_type_variable(expression, scope);
            else
                return typeExpr;
        } else {
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
        String type1 = node1.getType();
        String type2 = node2.getType();

        if(type1.equals("Id") || type1.equals("Appeal"))
            type1 = get_data_type_variable(node1, scope);
        else if(type1.equals("Arithmetic expr"))
            type1 = check_arithmetic_expression(node1, scope);

        if(type2.equals("Id") || type2.equals("Appeal"))
            type2 = get_data_type_variable(node2, scope);
        else if(type2.equals("Arithmetic expr"))
            type2 = check_arithmetic_expression(node2, scope);

        return checking_two_types(type1, type2);
    }

    public static String checking_two_types(String type1, String type2) {
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
        if(node.getType().equals("Appeal")) {
            check_appeal(node, scope);
            return get_data_type_variable(node.getChildren().get(0), scope);
        }

        String nameVar = node.getName();
        if(nameVar.equals("Console.ReadLine"))
            return "Read";
        if(nameVar.contains(".Length"))
            return "Integer";
        if (symbolTable.containsKey(nameVar)) {
            List<Scope> listScopes = symbolTable.get(nameVar);
            for (Scope s : listScopes) {
                if (s.getName().equals(scope) || scope.contains(s.getName())) {
                    return s.getType();
                }
            }
        }
        add_error_undeclared_var(node);
        return "Error";
    }

    public static void add_in_symbol_table(List<AST> declarationVar, String scopeVis) {
        List<AST> declaredVar = declarationVar.get(0).getChildren();
        String type = get_type_declaration(declarationVar.get(1).getChildren());
        String name;
        for (AST var : declaredVar) {
            if(var.getType().equals("Appeal")) {
                check_appeal(var, scopeVis);
                name = var.getChildren().get(0).getName();
            } else
                name = var.getName();

            if(!name.equals("Dim")) {
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
