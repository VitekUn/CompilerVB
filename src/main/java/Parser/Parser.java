package Parser;

import AST.AST;
import Token.Token;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static List<AST> nodesAST = new ArrayList<>();
    private static List<String> errors = new ArrayList<>();

    public static void token_processing (List<Token> listTokens, List<AST> nodes) { //Создание узлов дерева из токенов
        for (Token t : listTokens) {
            AST node = new AST(t.getToken(), t.getType(), t.getLine(), new ArrayList<AST>());
            nodes.add(node);
        }
    }

    public static void tree_building(List<AST> nodes) {
        AST node;
        int prevSize = 0;

        while (prevSize != nodes.size()) {
            prevSize = nodes.size();

            for (int i = 0; i < nodes.size(); i++) {
                node = nodes.get(i);
                switch (node.getType()) {
                    case ("Keyword"):
                        keyword_processing(nodes, i);
                        break;
                    case ("Brace"):
                        if(node.getName().equals("("))
                            brace_processing(nodes, i);
                        break;
                    case ("Id"):
                        id_processing(nodes, i);
                        break;
                    case ("Procedure declaration"):
                    case ("While declaration"):
                    case ("If declaration"):
                        DesignTemplate.temp_construction_block(nodes, i);
                        break;
                }
            }
        }
    }

    public static void id_processing(List<AST> nodes, int nodeIndex) {
        if(nodeIndex + 1 < nodes.size()) {
            AST nextNode = nodes.get(nodeIndex + 1);

            if(nextNode.getType().equals("Brace") && nextNode.getName().equals("(")) {
                brace_processing(nodes, nodeIndex + 1);
                nextNode = nodes.get(nodeIndex + 1);
            }

            switch (nextNode.getType()) {
                case ("Block brace"):
                    merging_nodes(nodes, nodeIndex, nodeIndex + 1, "Appeal", nodes.get(nodeIndex).getLine());
                    if(nodeIndex + 1 < nodes.size()) {
                        nextNode = nodes.get(nodeIndex + 1);
                        if (!nextNode.getType().equals("ArithmeticOp"))
                            id_processing(nodes, nodeIndex);
                    }
                    break;
                case ("ComparisonOp"):
                    DesignTemplate.temp_logical_expr(nodes, nodeIndex);
                    break;
                case ("Equality"):
                case ("AssignOp"):
                    DesignTemplate.temp_equality_expr(nodes, nodeIndex);
                    break;
                case ("ArithmeticOp"):
                    DesignTemplate.temp_arithmetic_expr(nodes, nodeIndex);
                    break;
            }
        }
    }

    public static void brace_processing(List<AST> nodes, int nodeIndex) {
        int i = nodeIndex + 1, indexLeft = nodeIndex, indexRight = 0;
        while(i < nodes.size()) {
            if(nodes.get(i).getName().equals("(")) {
                brace_processing(nodes, i);
            }
            if(nodes.get(i).getType().equals("Id")) {
                id_processing(nodes, i);
            }
            if(nodes.get(i).getName().equals(")")) {
                indexRight = i;
                merging_nodes(nodes, indexLeft, indexRight, "Block brace", nodes.get(indexLeft).getLine());
                break;
            }
            i++;
        }
    }

    public static void keyword_processing(List<AST> nodes, int nodeIndex) {
        AST node = nodes.get(nodeIndex);
        switch (node.getName()) {
            case ("Sub"):
                DesignTemplate.temp_procedure_declaration(nodes, nodeIndex);
                break;
            case ("End"):
                DesignTemplate.temp_end_design(nodes, nodeIndex);
                break;
            case ("Dim"):
                DesignTemplate.temp_variable_declaration(nodes, nodeIndex);
                break;
            case ("If"):
            case ("While"):
                DesignTemplate.temp_while_or_if_declaration(nodes, nodeIndex);
                break;
        }
    }

    public static void adding_in_node(List<AST> nodes, int start, int end, String type, int nodeIndex) {
        List<AST> childrenNode;
        String nameNode, typeNode, name = nodes.get(nodeIndex).getName();
        int lineNode;

        if(nodes.get(nodeIndex).getChildren().isEmpty() || nodes.get(nodeIndex).getType().equals("Appeal")) {
            //Если дочерний список узла пуст, сначала добавляем этот узел в список своих дочерних
            name = name + " ";
            nameNode = nodes.get(nodeIndex).getName();
            typeNode = nodes.get(nodeIndex).getType();
            lineNode = nodes.get(nodeIndex).getLine();
            childrenNode = new ArrayList<>(nodes.get(nodeIndex).getChildren());
            nodes.get(nodeIndex).setChildren(new ArrayList<AST>());
            nodes.get(nodeIndex).add_child(new AST(nameNode, typeNode, lineNode, childrenNode));
        }

        for (int i = start; i <= end; i++) {
            nameNode = nodes.get(i).getName();
            typeNode = nodes.get(i).getType();
            lineNode = nodes.get(i).getLine();
            childrenNode = nodes.get(i).getChildren();
            nodes.get(nodeIndex).add_child(new AST(nameNode, typeNode, lineNode, childrenNode));
            nodes.remove(i);
            i--;
            end--;
            name = name + nameNode + " ";
        }

        if(name.length() > 40 || name.contains(type)) {
            name = type;
        }

        nodes.get(nodeIndex).setName(name);
        nodes.get(nodeIndex).setType(type);
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

        if(name.length() > 40 || name.contains(type)) {
            name = type;
        }

        AST node = new AST(name, type, line, children);
        nodes.add(start, node);
    }

    public static void print_tree() {
        for (AST s : nodesAST) {
            print_node(s, 4, 100);
        }
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

    public static void adding_error_construction(List<AST> nodes, int startIndexNode, int endIndexNode, String expected) {
        String text = "After '";
        int lineNumber = nodes.get(startIndexNode).getLine();
        for (int i = startIndexNode; i <= endIndexNode; i++){
           text = text + " " + nodes.get(i).getName();
        }

        String error = "PARSER: Line: " + lineNumber + " \"" + text + " ' expected " + expected + "\"";

        if(!errors.isEmpty()) {
            if(errors.contains(error))
                return;
        }
        errors.add(error);
    }

    public static void adding_error_block(List<AST> nodes, int indexNode) {
        int lineNumber = nodes.get(indexNode).getLine();
        String text = "'" + nodes.get(indexNode).getName() + "' is an unfinished construction";
        String error = "PARSER: Line: " + lineNumber + " \"" + text + "\"";

        if(!errors.isEmpty()) {
            if(errors.contains(error))
                return;
        }
        errors.add(error);
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
