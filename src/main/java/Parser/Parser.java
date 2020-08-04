package Parser;

import AST.AST;
import Token.Token;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static List<AST> nodesAST = new ArrayList<>();
    private static List<String> errors = new ArrayList<>();

    public static void tree_building(List<AST> nodes) {
        AST node;
        for(int i = 0; i < nodes.size(); i++) {
            node = nodes.get(i);
            switch (node.getType()) {
                case("Keyword"):
                    keyword_processing(nodes, i);
                    break;
                case("Brace"):
                    brace_processing(nodes, i);
                    break;
                case("Id"):
                    id_processing(nodes, i);
                    break;
                case("Logical expr"):
                    DesignTemplate.temp_logical_expr(nodes, i);
                    break;
            }
        }
    }

    public static void id_processing(List<AST> nodes, int nodeIndex) {
        if(nodeIndex + 1 < nodes.size()) {
            AST nextNode = nodes.get(nodeIndex + 1);
            switch (nextNode.getType()) {
                case ("Block brace"):
                    merging_nodes(nodes, nodeIndex, nodeIndex + 1, "Appeal", nodes.get(nodeIndex).getLine());
                    break;
                case ("ComparisonOp"):
                    DesignTemplate.temp_logical_expr(nodes, nodeIndex);
                    break;
            }

        }
    }

    public static void brace_processing(List<AST> nodes, int nodeIndex) {
        int i = nodeIndex, indexLeft = nodeIndex, indexRight = 0;
        while(i < nodes.size()) {
            if(nodes.get(i).getName().equals("(")) {
                indexLeft = i;
            }
            if(nodes.get(i).getName().equals(")")) {
                indexRight = i;
                break;
            }
            i++;
        }
//        for(i = indexLeft + 1; i < indexRight; i++) {
            //check
  //      }
        merging_nodes(nodes, indexLeft, indexRight, "Block brace", nodes.get(indexLeft).getLine());
    }

    public static void keyword_processing(List<AST> nodes, int nodeIndex) {
        /* Проверка по шаблонам из if
        Если шаблон подходит, всё сливается в один узел, иначе генерируется сообщение об ошибке
         */
        AST node = nodes.get(nodeIndex);
        switch (node.getName()) {
            case ("Sub"):
                DesignTemplate.temp_procedure_declaration(nodes, nodeIndex);
                break;
            case ("End"):
                DesignTemplate.temp_end_design(nodes, nodeIndex);
                break;
        }
    }

    public static void merging_nodes(List<AST> nodes, int start, int end, String type, int line) {
        List<AST> children = new ArrayList<>();
        List<AST> childrenNode;
        String nameNode, typeNode, name = "";
        int lineNode;

        for(int i = start; i <= end; i++) {
            nameNode = nodes.get(i).getName();
            typeNode = nodes.get(i).getType();
            lineNode = nodes.get(i).getLine();
            childrenNode = nodes.get(i).getChildren();
            children.add(new AST(nameNode, typeNode, lineNode, childrenNode));
            nodes.remove(i);
            i--;
            end--;
            name = name + nameNode + " ";
        }

        AST node = new AST(name, type, line, children);
        nodes.add(start, node);

        tree_building(nodes);
    }

    public static void token_processing (List<Token> listTokens, List<AST> nodes) { //Создание узлов дерева из токенов
        for (Token t : listTokens) {
            AST node = new AST(t.getToken(), t.getType(), t.getLine(), new ArrayList<AST>());
            nodes.add(node);
        }
    }

    public static void print_tree() {
        for (AST s : nodesAST) { print_node(s, 4, 100); }
    }

    public static void print_node(AST node, int indent, int depth) {
        System.out.println("< '" + node.getName() + "' : '" + node.getType() + "' " + node.getLine() + ">");
        if(depth > 0) {
            if (!node.getChildren().isEmpty()) {
                for (AST s : node.getChildren()) {
                    for (int i = 0; i <= indent; i++) {
                        System.out.print(" ");
                        if(0 == i % 5)
                            System.out.print("|");
                    }
                    print_node(s, indent + 5, depth - 1);
                }
            }
        }
    }

    public static void adding_error(List<AST> nodes, int startIndexNode, int endIndexNode, String expected) {
        String text = "After '";
        int lineNumber = nodes.get(startIndexNode).getLine();
        for (int i = startIndexNode; i <= endIndexNode; i++){
         //   text = text + nodes.get(i).getName() + " ";
           text = text + " " + nodes.get(i).getName();
        }
        errors.add("PARSER: Line: " + lineNumber + " \"" + text + " ' expected " + expected + "\"");
    }

    public static void print_errors() {
        if(!errors.isEmpty()) {
            System.out.println("Errors:");
            for(String e : errors) {
                System.out.println(e);
            }
        }
    }

    public static List<AST> getNodesAST() {
        return nodesAST;
    }

    public static List<String> getErrors() {
        return errors;
    }

    public static void setErrors(List<String> errors) {
        Parser.errors = errors;
    }
}
