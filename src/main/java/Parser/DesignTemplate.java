package Parser;

import AST.AST;

import java.util.Arrays;
import java.util.List;

public class DesignTemplate {
    private static String[] completed_construction = new String[] {"Equality expr", "Appeal", "Block If", "Block While",
                                            "Block If Else", "Variable declaration", "Variable declaration and assign"};

    public static void temp_construction_block(List<AST> nodes, int nodeIndex) {
        int line = nodes.get(nodeIndex).getLine();
        AST node, nodeBlock = nodes.get(nodeIndex);
        int prevSize;
        for (int i = nodeIndex + 1; i < nodes.size(); i++) {
            node = nodes.get(i);
            if(!Arrays.asList(completed_construction).contains(node.getType())) { //Если не собранна
                switch (node.getType()) {
                    case ("While declaration"):
                    case ("If declaration"):
                        prevSize = nodes.size();
                        temp_construction_block(nodes, i);
                        if(prevSize > nodes.size())
                            i = nodeIndex + 1;
                        break;
                    case ("Keyword"):
                        prevSize = nodes.size();
                        if(node.getName().equals("Else")) {
                            temp_construction_block(nodes, i);
                            node = nodes.get(i);
                            if(node.getType().equals("Block Else")) {
                                i--;
                            } else { Parser.adding_error_block(nodes, i); }
                        } else { Parser.adding_error_block(nodes, i); }
                        if(prevSize > nodes.size())
                            i = nodeIndex + 1;
                        break;
                    case ("Block Else"):
                        if(nodeBlock.getType().equals("If declaration")) {
                            prevSize = nodes.size();
                            Parser.merging_nodes(nodes, nodeIndex, i - 1, "Block If", line);
                            int shift = prevSize - nodes.size();
                            Parser.merging_nodes(nodes, nodeIndex, i - shift, "Block If Else", line);
                            return;
                        } else { Parser.adding_error_block(nodes, i); }
                        break;
                    case ("End design"):
                        if(nodeBlock.getName().equals("Else") && node.getName().equals("End If ")) {
                            Parser.merging_nodes(nodes, nodeIndex, i, "Block Else", line);
                            return;
                        }
                        if(nodeBlock.getType().equals("If declaration") && node.getName().equals("End If ")) {
                            Parser.merging_nodes(nodes, nodeIndex, i, "Block If", line);
                            return;
                        } else if(nodeBlock.getType().equals("While declaration") && node.getName().equals("End While ")) {
                            Parser.merging_nodes(nodes, nodeIndex, i, "Block While", line);
                            return;
                        } else if(nodeBlock.getType().equals("Procedure declaration") && node.getName().equals("End Sub ")) {
                            Parser.merging_nodes(nodes, nodeIndex, i, "Block Procedure", line);
                            return;
                        }
                        break;
                    default:
                        Parser.adding_error_block(nodes, i);
                }
            }
        }
    }

    public static void temp_variable_declaration(List<AST> nodes, int nodeIndex) {
        if(nodeIndex + 1 >= nodes.size())
            return;
        String typeNextNode = nodes.get(nodeIndex + 1).getType();
        String typeNextNode2;
        int line = nodes.get(nodeIndex).getLine();

        if(typeNextNode.equals("Id") || typeNextNode.equals("Appeal")) {
            for(int i = nodeIndex + 1; i < nodes.size(); i += 2) {
                Parser.id_processing(nodes, i);
                typeNextNode = nodes.get(i).getType();
                typeNextNode2 = nodes.get(i + 1).getType();

                if(typeNextNode.equals("Id") || typeNextNode.equals("Appeal")) {
                    if(typeNextNode2.equals("Comma")) {
                        Parser.adding_in_node(nodes, i, i + 1, "Enum variables", nodeIndex);
                        i -= 2;
                        nodes.get(nodeIndex).getChildren().remove(nodes.get(nodeIndex).getChildren().size() - 1);
                    } else if (typeNextNode2.equals("Keyword")) {
                        Parser.adding_in_node(nodes, i, i, "Enum variables", nodeIndex);
                        temp_type_assignment(nodes, i);
                        AST nextNode = nodes.get(i);
                        if(nextNode.getType().equals("Assign data type")) {
                            Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 1, "Variable declaration", line);
                        } else if (nextNode.getType().equals("Assign data type and value")){
                            Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 1, "Variable declaration and assign", line);
                        } else { Parser.adding_error_construction(nodes, nodeIndex, nodeIndex, "'As' and data type"); }
                        break;
                    }  else {
                        Parser.adding_error_construction(nodes, nodeIndex + 1, nodeIndex + 1, "','");
                        break;
                    }
                } else {
                    Parser.adding_error_construction(nodes, nodeIndex, nodeIndex, "variable name");
                    break;
                }
            }
        } else { Parser.adding_error_construction(nodes, nodeIndex, nodeIndex, "variable name"); }
    }

    public static void temp_type_assignment(List<AST> nodes, int nodeIndex) {
        if(nodeIndex + 1 < nodes.size()) {
            AST node = nodes.get(nodeIndex);
            AST nextNode = nodes.get(nodeIndex + 1);
            int line = nodes.get(nodeIndex).getLine();
            if(node.getName().equals("As")) {
                if(nextNode.getType().equals("DataType")) {
                    Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 1, "Assign data type", line);

                    if(nodeIndex + 2 < nodes.size()) {
                        node = nodes.get(nodeIndex + 1);
                        String typeOperand = nodes.get(nodeIndex + 2).getType();
                        if(node.getType().equals("Equality")) {
                            if(typeOperand.equals("Id") || typeOperand.equals("Decimal")
                                    || typeOperand.equals("Appeal") || typeOperand.equals("String")) {
                                Parser.id_processing(nodes, nodeIndex + 2);
                                temp_arithmetic_expr(nodes, nodeIndex + 2);
                                Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 2, "Assign data type and value", line);
                            } else {
                                Parser.adding_error_construction(nodes, nodeIndex + 1,
                                        nodeIndex + 1, "number or variable");
                            }
                        }
                    }

                } else { Parser.adding_error_construction(nodes, nodeIndex, nodeIndex, "data type"); }
            } else { Parser.adding_error_construction(nodes, nodeIndex - 1, nodeIndex - 1, "'As'"); }
        }
    }

    public static void temp_equality_expr(List<AST> nodes, int nodeIndex) {
        if(nodeIndex + 2 < nodes.size()) {
            String typeNextNode = nodes.get(nodeIndex + 2).getType();
            int line = nodes.get(nodeIndex).getLine();

            if(typeNextNode.equals("Id") || typeNextNode.equals("Decimal")
                || typeNextNode.equals("Appeal") || typeNextNode.equals("String")) {
                //Отправляем на случай если это массив, чтоб он собрался (Appeal)
                Parser.id_processing(nodes, nodeIndex + 2);
                //Отправляем на случай если это арифмитическое выражение, чтоб собралось
                temp_arithmetic_expr(nodes, nodeIndex + 2);
                Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 2, "Equality expr", line);
            } else { Parser.adding_error_construction(nodes, nodeIndex, nodeIndex + 1, "number or variable"); }
        }
    }

    public static void temp_arithmetic_expr(List<AST> nodes, int nodeIndex) {
        if(nodeIndex + 2 < nodes.size()) {
            String typeOp = nodes.get(nodeIndex + 1).getType();
            String typeOperand = nodes.get(nodeIndex + 2).getType();

            if(typeOp.equals("ArithmeticOp")) {
                for(int i = nodeIndex + 1; i < nodes.size(); i += 2) {
                    typeOp = nodes.get(i).getType();
                    typeOperand = nodes.get(i + 1).getType();
                    if(typeOp.equals("ArithmeticOp")) {
                        if(i + 2 < nodes.size())
                            if(typeOperand.equals("Id") && nodes.get(i + 2).getName().equals("(")) {
                                Parser.id_processing(nodes, i + 1);
                            }
                        if (typeOperand.equals("Id") || typeOperand.equals("Decimal") || typeOperand.equals("Appeal")) {
                            Parser.adding_in_node(nodes, i, i + 1, "Arithmetic expr", nodeIndex);
                            i -= 2;
                        } else {
                            Parser.adding_error_construction(nodes, nodeIndex, i, "number or variable");
                            break;
                        }
                    } else { break; }
                }
            }
        }
    }

    public static void temp_procedure_declaration(List<AST> nodes, int nodeIndex) {
        AST node = nodes.get(nodeIndex);
        if(nodeIndex + 3 < nodes.size()) {
            if(nodes.get(nodeIndex + 1).getType().equals("Id")) {
                if(nodes.get(nodeIndex + 2).getName().equals("(")) {
                    if (nodes.get(nodeIndex + 3).getName().equals(")")) {
                        Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 3, "Procedure declaration", node.getLine());
                    } else { Parser.adding_error_construction(nodes, nodeIndex, nodeIndex + 2, "')'"); }
                } else { Parser.adding_error_construction(nodes, nodeIndex, nodeIndex + 1, "'('"); }
            } else { Parser.adding_error_construction(nodes, nodeIndex, nodeIndex, "procedure name"); }
        }
    }

    public static void temp_end_design(List<AST> nodes, int nodeIndex) {
        AST node = nodes.get(nodeIndex);
        if(nodeIndex + 1 < nodes.size()) {
            String nameNextNode = nodes.get(nodeIndex + 1).getName();
            if(nameNextNode.equals("While") || nameNextNode.equals("If") || nameNextNode.equals("Sub")) {
                Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 1, "End design", node.getLine());
            } else { Parser.adding_error_construction(nodes, nodeIndex, nodeIndex, "'While', 'If', 'Sub'"); }
        }
    }

    public static void temp_while_or_if_declaration(List<AST> nodes, int nodeIndex) {
        AST node = nodes.get(nodeIndex);
        AST nextNode = nodes.get(nodeIndex + 1);
        if(nextNode.getType().equals("Id") || nextNode.getType().equals("Decimal") || nextNode.getType().equals("Appeal")) {
            Parser.id_processing(nodes, nodeIndex + 1);
            DesignTemplate.temp_logical_expr(nodes, nodeIndex + 1);
            nextNode = nodes.get(nodeIndex + 1);
        }
        if(nextNode.getType().equals("Logical expr") || nextNode.getType().equals("Equality expr")) {
            if(node.getName().equals("If")) {
                if (nodeIndex + 2 < nodes.size()) {
                    if(nodes.get(nodeIndex + 2).getName().equals("Then")) {
                        Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 2, "If declaration", nextNode.getLine());
                    } else { Parser.adding_error_construction(nodes, nodeIndex, nodeIndex + 1, "'Then'"); }
                } else { Parser.adding_error_construction(nodes, nodeIndex, nodeIndex + 1, "'Then'"); }
            } else if (node.getName().equals("While")) {
                Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 1, "While declaration", nextNode.getLine());
            }
        } else { Parser.adding_error_construction(nodes, nodeIndex, nodeIndex, "logical expression"); }
    }

    public static void temp_logical_expr(List<AST> nodes, int nodeIndex) {
        if(nodeIndex + 2 < nodes.size()) {
            if(nodeIndex + 3 < nodes.size()) {
                if(nodes.get(nodeIndex + 3).getType().equals("Brace"))
                    Parser.id_processing(nodes, nodeIndex + 2);
            }
            int line =  nodes.get(nodeIndex).getLine();
            String typeOperation = nodes.get(nodeIndex + 1).getType();
            String typeOperand2 = nodes.get(nodeIndex + 2).getType();

            if(typeOperation.equals("ComparisonOp") || typeOperation.equals("Equality")) {
                if(typeOperand2.equals("Id") || typeOperand2.equals("Decimal")
                        || typeOperand2.equals("Appeal") || typeOperand2.equals("String")) {
                    Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 2, "Logical expr", line);

                    if(nodeIndex + 2 < nodes.size()) {
                        if (nodes.get(nodeIndex + 1).getType().equals("LogicOp")) {
                            typeOperand2 = nodes.get(nodeIndex + 2).getType();
                            if (typeOperand2.equals("Id") || typeOperand2.equals("Decimal")) {
                                DesignTemplate.temp_logical_expr(nodes, nodeIndex + 2);
                                typeOperand2 = nodes.get(nodeIndex + 2).getType();
                            }

                            if (typeOperand2.equals("Logical expr")) {
                                Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 2, "Logical expr", line);
                            } else {
                                Parser.adding_error_construction(nodes, nodeIndex, nodeIndex + 1, "variable name or number");
                            }
                        }
                    }
                } else {
                    Parser.adding_error_construction(nodes, nodeIndex, nodeIndex + 1, "variable name or number");
                }
            }
        }
    }
}
