package Parser;

import AST.AST;

import java.util.List;

public class DesignTemplate {

    public static void temp_procedure_declaration(List<AST> nodes, int nodeIndex) {
        AST node = nodes.get(nodeIndex);
        if(nodeIndex + 3 < nodes.size()) {
            if(nodes.get(nodeIndex + 1).getType().equals("Id")) {
                if(nodes.get(nodeIndex + 2).getName().equals("(")) {
                    if (nodes.get(nodeIndex + 3).getName().equals(")")) {
                        Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 3, "Procedure declaration", node.getLine());
                    } else { Parser.adding_error(nodes, nodeIndex, nodeIndex + 2, "')'"); }
                } else { Parser.adding_error(nodes, nodeIndex, nodeIndex + 1, "'('"); }
            } else { Parser.adding_error(nodes, nodeIndex, nodeIndex, "procedure name"); }
        }
    }

    public static void temp_end_design(List<AST> nodes, int nodeIndex) {
        AST node = nodes.get(nodeIndex);
        if(nodeIndex + 1 < nodes.size()) {
            String nameNextNode = nodes.get(nodeIndex + 1).getName();
            if(nameNextNode.equals("While") || nameNextNode.equals("If") || nameNextNode.equals("Sub")) {
                Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 1, "End design", node.getLine());
            } else { Parser.adding_error(nodes, nodeIndex, nodeIndex, "'While', 'If', 'Sub'"); }
        }
    }

    /* temp_logical_expr обрабатывает случаи:
    Операция сравнения (ComparisonOp) i < 0
    Логическую i < 0 AND a > 0
        В случае логической нужно сначала определить операции сравнения по бокам от AND
     */
    public static int temp_logical_expr(List<AST> nodes, int nodeIndex) {
        if(nodeIndex + 2 < nodes.size()) {
            String typeOperation = nodes.get(nodeIndex + 1).getType();
            String typeOperand2 = nodes.get(nodeIndex + 2).getType();
            switch (typeOperation) {
                case ("ComparisonOp"):
                    if(typeOperand2.equals("Id") || typeOperand2.equals("Decimal")) {
                        Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 2, "Logical expr", nodes.get(nodeIndex).getLine());
                        return 1;
                    } else {
                        Parser.adding_error(nodes, nodeIndex, nodeIndex + 1, "variable name or number");
                        return -1;
                    }
                case ("LogicOp"):
                    if(typeOperand2.equals("Logical expr")) {
                        Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 2, "Logical expr", nodes.get(nodeIndex).getLine());
                        return 1;
                    } else if(typeOperand2.equals("Id") || typeOperand2.equals("Decimal")) {
                        int flagBuild = DesignTemplate.temp_logical_expr(nodes, nodeIndex + 2);
                        if(flagBuild == -1) {
                            Parser.adding_error(nodes, nodeIndex, nodeIndex + 1, "logical expression");
                            return -1;
                        }
                    } else {
                        Parser.adding_error(nodes, nodeIndex, nodeIndex + 1, "logical expression");
                        return -1;
                    }
                    break;
                case ("Keyword"):
                     if(nodes.get(nodeIndex + 1).getName().equals("Then")) {
                         return 0;
                     }
                    break;
            }
        }
        return 0;
    }
}
