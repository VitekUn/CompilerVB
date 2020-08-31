package Parser;

import AST.AST;

import java.util.Arrays;
import java.util.List;

public class DesignTemplate {
    //Типы собранных конструкций (используется для проверки конструкций собранная она или не до конца)
    private static String[] completed_construction = new String[] {"Equality expr", "Appeal", "Block If", "Block While",
                                            "Block If Else", "Variable declaration", "Variable declaration and assign"};

    /*Сборка блока If, While, Procedure
    * Проход с узла следующего после объявления If, While, Procedure
    * Задача найти конструкцию завершения (например для If  -  End If
    * Когда находит сворачивает всё в один узел (при словии, что внутри
    * находятся только собранные констракции*/
    public static void temp_construction_block(List<AST> nodes, int nodeIndex) {
        int line = nodes.get(nodeIndex).getLine();
        AST node, nodeBlock = nodes.get(nodeIndex);
        int prevSize;
        for (int i = nodeIndex + 1; i < nodes.size(); i++) {
            node = nodes.get(i);
            if(!Arrays.asList(completed_construction).contains(node.getType())) { //Если не собранна
                switch (node.getType()) {
                    case ("While declaration"): //Если объявление иф или вайл, то отправляем собираться сначала их
                    case ("If declaration"):
                        prevSize = nodes.size();
                        temp_construction_block(nodes, i);
                        //Запускаем цикл заново, так как количество узлов изменилось и мы можем что-то пропустить
                        if(prevSize > nodes.size())
                            i = nodeIndex + 1;
                        break;
                    case ("Keyword"): //Если это Else то собираем сначала его
                        prevSize = nodes.size();
                        if(node.getName().equals("Else")) {
                            temp_construction_block(nodes, i);
                            node = nodes.get(i);
                            if(node.getType().equals("Block Else")) { //Если собралось то уменьшаем i,
                                i--;                                 // чтобы заново пройтись по этому узлу
                            } else { Parser.adding_error_block(nodes, i); }
                        } else { Parser.adding_error_block(nodes, i); }
                        //Запускаем цикл заново, так как количество узлов изменилось и мы можем что-то пропустить
                        if(prevSize > nodes.size())
                            i = nodeIndex + 1;
                        break;
                    case ("Block Else"): //Для условия сворачивания If может также быть Block Else
                        if(nodeBlock.getType().equals("If declaration")) {
                            //Собираем Иф до блока Елсе
                            //Сохраняем сколько узлов сейчас в списке
                            prevSize = nodes.size();
                            Parser.merging_nodes(nodes, nodeIndex, i - 1, "Block If", line);
                            //Измеряем насколько узлов количество изменилось, чтобы учесть при сборке обоих блоков
                            int shift = prevSize - nodes.size();
                            //Объединяем два блока
                            Parser.merging_nodes(nodes, nodeIndex, i - shift, "Block If Else", line);
                            return;
                        } else { Parser.adding_error_block(nodes, i); }
                        break;
                    //Проверяем соответствие конструкций и если всё норм, сворачиваем их
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
        //Просматриваем по два узла после Dim
        String typeNextNode = nodes.get(nodeIndex + 1).getType();
        String typeNextNode2;
        int line = nodes.get(nodeIndex).getLine();

        if(typeNextNode.equals("Id") || typeNextNode.equals("Appeal")) {
            //Проход по два узла после Dim
            // На случай если объявляется сразу несколько переменных
            // Например Dim a, b, ... Просматриваем 'a' ',' потом 'b' ','
            for(int i = nodeIndex + 1; i < nodes.size(); i += 2) {
                Parser.id_processing(nodes, i); //На всякий случай отправляем на случай если это объявление массива (Appeal)
                typeNextNode = nodes.get(i).getType();
                typeNextNode2 = nodes.get(i + 1).getType();

                if(typeNextNode.equals("Id") || typeNextNode.equals("Appeal")) { //Если имя переменной или массив
                    if(typeNextNode2.equals("Comma")) { //Смотрим, чтобы следубщий узел был запятой
                        //Если всё норм, добавляем к узлу переменную и запятую и делается след. цикл
                        Parser.adding_in_node(nodes, i, i + 1, "Enum variables", nodeIndex);
                        //Если собрали, то уменьшаем i на 2 (столько же добавили)
                        i -= 2;
                        //Удаляем только что добавленные запятые (в дальнейшем они не нужны)
                        nodes.get(nodeIndex).getChildren().remove(nodes.get(nodeIndex).getChildren().size() - 1);
                    } else if (typeNextNode2.equals("Keyword")) {//Если не запятая то возможно это 'As'
                        //Добавляем только перменную без запятой (так как запятой нет)
                        Parser.adding_in_node(nodes, i, i, "Enum variables", nodeIndex);
                        //Отправляем в функцию чтобы собрать "As Integer" например
                        temp_type_assignment(nodes, i);
                        AST nextNode = nodes.get(i);
                        //Если "As Integer" например нормально собралось
                        if(nextNode.getType().equals("Assign data type")) {
                            Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 1, "Variable declaration", line);
                        //Если оказалось "As Ineger = 10" например
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

                    //Проверка на случай если "As Integer = 10" например
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
            String typeOp = nodes.get(nodeIndex + 1).getType(); //Тип операции
            String typeOperand = nodes.get(nodeIndex + 2).getType(); //Операнд

            if(typeOp.equals("ArithmeticOp")) { //Если арифмитическая операция
                //Проход по два узла (предполагаемый оператор (+ - * ...) и операнд
                //Если всё подходит, добавляем из в узел
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
        //Проверяем следующий узел после while или if и пытаемся собралось условие
        if(nextNode.getType().equals("Id") || nextNode.getType().equals("Decimal") || nextNode.getType().equals("Appeal")) {
            //Отправляем на случай если это массив, чтоб собралось
            Parser.id_processing(nodes, nodeIndex + 1);
            //Пытаемся собрать условие
            DesignTemplate.temp_logical_expr(nodes, nodeIndex + 1);
            nextNode = nodes.get(nodeIndex + 1);
        }
        if(nextNode.getType().equals("Logical expr") || nextNode.getType().equals("Equality expr")) {
            //Если это if то проверяем ещё, чтоб после условия был "Then"
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


    /* temp_logical_expr обрабатывает случаи:
    Операция сравнения (ComparisonOp) i < 0
    Логическую i < 0 AND a > 0
        В случае логической нужно сначала определить операции сравнения по бокам от AND
     */
    public static void temp_logical_expr(List<AST> nodes, int nodeIndex) {
        if(nodeIndex + 2 < nodes.size()) {
            if(nodeIndex + 3 < nodes.size()) {
                if(nodes.get(nodeIndex + 3).getType().equals("Brace"))
                    //Если второй операнд массив отправляем его собираться на всякий случай (f < f() ) например
                    Parser.id_processing(nodes, nodeIndex + 2);
            }
            int line =  nodes.get(nodeIndex).getLine();
            String typeOperation = nodes.get(nodeIndex + 1).getType();
            String typeOperand2 = nodes.get(nodeIndex + 2).getType();

            if(typeOperation.equals("ComparisonOp") || typeOperation.equals("Equality")) {
                if(typeOperand2.equals("Id") || typeOperand2.equals("Decimal")
                        || typeOperand2.equals("Appeal") || typeOperand2.equals("String")) {
                    //Если всё норм соединяем в условие
                    Parser.merging_nodes(nodes, nodeIndex, nodeIndex + 2, "Logical expr", line);

                    //Проверка на случай, если это большое условие (например a < b And a < c)
                    // Проверяем на And и если да, то собираем вторую часть (a < c)
                    //И если всё норм то объединяем в одно большое условие
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
