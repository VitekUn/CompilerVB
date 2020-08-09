package Semantics;

import AST.AST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Semantics {
    private static HashMap<String, List<Scope>> symbolTable = new HashMap<>();

    public static void analysis_tree(List<AST> tree, String scope, int level) {
        //Обход по узлам дерева
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
                    break;
            }
        }
    }

    public static void add_in_symbol_table(List<AST> declarationVar, String scopeVis) {
        //Узел с перечислением перменных, которые объявляются
        List<AST> declaredVar = declarationVar.get(0).getChildren();
        //Тип объявления
        String type = getTypeDeclaration(declarationVar.get(1).getChildren());
        //Имя переменной
        String name;
        for (AST var : declaredVar) {
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
    public static String getTypeDeclaration(List<AST> nodes) {
        if(!nodes.get(0).getChildren().isEmpty())
            return getTypeDeclaration(nodes.get(0).getChildren());
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

    public static HashMap<String, List<Scope>> getSymbolTable() {
        return symbolTable;
    }
}
