import AST.AST;
import Lexer.Lexer;
import Parser.Parser;
import Token.Token;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class TestParser {
    //Пишем строку кода, потом собираем её в дерево и смотрим тип узла, который получился
    @org.junit.jupiter.api.Test
    void variable_declaration_test_build1 () {
        List<AST> actual = create_nodes_AST("Dim a As Integer", 1);
        Parser.tree_building(actual);

        List<AST> expected = new ArrayList<>();
        expected.add(new AST("Dim a As Integer", "Variable declaration", 1, null));
        Assert.assertEquals(expected.get(0).getType(), actual.get(0).getType());
    }

    @org.junit.jupiter.api.Test
    void variable_declaration_test_build2 () {
        List<AST> actual = create_nodes_AST("Dim a As Integer = 100", 1);
        Parser.tree_building(actual);

        List<AST> expected = new ArrayList<>();
        expected.add(new AST("Dim a As Integer = 100", "Variable declaration and assign", 1, null));
        Assert.assertEquals(expected.get(0).getType(), actual.get(0).getType());
    }

    @org.junit.jupiter.api.Test
    void variable_declaration_test_build3 () {
        List<AST> actual = create_nodes_AST("Dim a, b, c As Integer", 1);
        Parser.tree_building(actual);

        List<AST> expected = new ArrayList<>();
        expected.add(new AST("Dim a, b, c As Integer", "Variable declaration", 1, null));
        Assert.assertEquals(expected.get(0).getType(), actual.get(0).getType());
    }

    @org.junit.jupiter.api.Test
    void variable_declaration_test_build4 () {
        List<AST> actual = create_nodes_AST("Dim a As Integer = 100 + q * 2 Mod 3", 1);
        Parser.tree_building(actual);

        List<AST> expected = new ArrayList<>();
        expected.add(new AST("Dim a As Integer = 100 + q * 2 Mod 3", "Variable declaration and assign", 1, null));
        Assert.assertEquals(expected.get(0).getType(), actual.get(0).getType());
    }

    @org.junit.jupiter.api.Test
    void variable_declaration_test_error1 () {
        Parser.setErrors(new ArrayList<String>());

        List<AST> nodes = create_nodes_AST("Dim a, b c As Integer", 1);
        Parser.tree_building(nodes);
        List<String>actual = Parser.getErrors();

        List<String> expected = new ArrayList<>();
        expected.add("PARSER: Line: 1 \"After ' b ' expected ','\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void variable_declaration_test_error2 () {
        Parser.setErrors(new ArrayList<String>());

        List<AST> nodes = create_nodes_AST("Dim 1a As Integer", 1);
        Parser.tree_building(nodes);
        List<String>actual = Parser.getErrors();

        List<String> expected = new ArrayList<>();
        expected.add("PARSER: Line: 1 \"After ' Dim ' expected variable name\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void variable_declaration_test_error3 () {
        Parser.setErrors(new ArrayList<String>());

        List<AST> nodes = create_nodes_AST("Dim a, 1b, c As Integer", 1);
        Parser.tree_building(nodes);
        List<String>actual = Parser.getErrors();

        List<String> expected = new ArrayList<>();
        expected.add("PARSER: Line: 1 \"After ' Dim a ,  ' expected variable name\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void variable_declaration_test_error4 () {
        Parser.setErrors(new ArrayList<String>());

        List<AST> nodes = create_nodes_AST("Dim a, b, c As Integer = Dim", 1);
        Parser.tree_building(nodes);
        List<String>actual = Parser.getErrors();

        List<String> expected = new ArrayList<>();
        expected.add("PARSER: Line: 1 \"After ' = ' expected number or variable\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void equality_arithmetic_expr_test_build1 () {
        List<AST> actual = create_nodes_AST("a = 2 + 1 * 3 + 10 - 2", 1);
        Parser.tree_building(actual);

        List<AST> expected = new ArrayList<>();
        expected.add(new AST("a = 2 + 1 * 3 + 10 - 2", "Equality expr", 1, null));
        Assert.assertEquals(expected.get(0).getType(), actual.get(0).getType());
    }

    @org.junit.jupiter.api.Test
    void equality_arithmetic_expr_test_error1 () {
        Parser.setErrors(new ArrayList<String>());

        List<AST> nodes = create_nodes_AST("a = 1 + 2 * ( + 4 - b", 1);
        Parser.tree_building(nodes);
        List<String>actual = Parser.getErrors();

        List<String> expected = new ArrayList<>();
        expected.add("PARSER: Line: 1 \"After ' 1 + 2  * ' expected number or variable\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void equality_expr_test_build1 () {
        List<AST> actual = create_nodes_AST("a = 2", 1);
        Parser.tree_building(actual);

        List<AST> expected = new ArrayList<>();
        expected.add(new AST("a = 2", "Equality expr", 1, null));
        Assert.assertEquals(expected.get(0).getType(), actual.get(0).getType());
    }

    @org.junit.jupiter.api.Test
    void equality_expr_test_build2 () {
        List<AST> actual = create_nodes_AST("a = Console.ReadLine()", 1);
        Parser.tree_building(actual);

        List<AST> expected = new ArrayList<>();
        expected.add(new AST("a = 2", "Equality expr", 1, null));
        Assert.assertEquals(expected.get(0).getType(), actual.get(0).getType());
    }

    @org.junit.jupiter.api.Test
    void equality_expr_test_error1 () {
        Parser.setErrors(new ArrayList<String>());

        List<AST> nodes = create_nodes_AST("a = ()", 2);
        Parser.tree_building(nodes);
        List<String>actual = Parser.getErrors();

        List<String> expected = new ArrayList<>();
        expected.add("PARSER: Line: 2 \"After ' a = ' expected number or variable\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void while_declaration_test_build1 () {
        List<AST> actual = create_nodes_AST("While i<2 And i>0 Or i<5", 1);
        Parser.tree_building(actual);

        List<AST> expected = new ArrayList<>();
        expected.add(new AST("While i<2 And i>0 Or i<5", "While declaration", 1, null));
        Assert.assertEquals(expected.get(0).getType(), actual.get(0).getType());
    }

    @org.junit.jupiter.api.Test
    void if_declaration_test_build1 () {
        List<AST> actual = create_nodes_AST("If i<2 And i>0 Then", 1);
        Parser.tree_building(actual);

        List<AST> expected = new ArrayList<>();
        expected.add(new AST("If i<2 And i>0", "If declaration", 1, null));
        Assert.assertEquals(expected.get(0).getType(), actual.get(0).getType());
    }

    @org.junit.jupiter.api.Test
    void if_declaration_test_error1 () {
        Parser.setErrors(new ArrayList<String>());

        List<AST> nodes = create_nodes_AST("If i < 100", 1);
        Parser.tree_building(nodes);
        List<String>actual = Parser.getErrors();

        List<String> expected = new ArrayList<>();
        expected.add("PARSER: Line: 1 \"After ' If i < 100  ' expected 'Then'\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void if_declaration_test_error2 () {
        Parser.setErrors(new ArrayList<String>());

        List<AST> nodes = create_nodes_AST("If i Then", 1);
        Parser.tree_building(nodes);
        List<String>actual = Parser.getErrors();

        List<String> expected = new ArrayList<>();
        expected.add("PARSER: Line: 1 \"After ' If ' expected logical expression\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void logical_expression_test_build1 () {
        List<AST> actual = create_nodes_AST("i<2 And i>0", 1);
        Parser.tree_building(actual);

        List<AST> expected = new ArrayList<>();
        expected.add(new AST("i<2 And i>0", "Logical expr", 1, null));

        Assert.assertEquals(expected.get(0).getType(), actual.get(0).getType());
    }

    @org.junit.jupiter.api.Test
    void logical_expression_test_build2 () {
        List<AST> actual = create_nodes_AST("i<2 And i>0 OR a<=i", 1);
        Parser.tree_building(actual);

        List<AST> expected = new ArrayList<>();
        expected.add(new AST("i<2 And i>0 OR a<=i", "Logical expr", 1, null));

        Assert.assertEquals(expected.get(0).getType(), actual.get(0).getType());
    }

    @org.junit.jupiter.api.Test
    void logical_expression_test_error1 () {
        Parser.setErrors(new ArrayList<String>());

        List<AST> nodes = create_nodes_AST("i < Then", 1);
        Parser.tree_building(nodes);
        List<String>actual = Parser.getErrors();

        List<String> expected = new ArrayList<>();
        expected.add("PARSER: Line: 1 \"After ' i < ' expected variable name or number\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void procedure_declaration_test_build1 () {
        List<AST> actual = create_nodes_AST("Sub Main()", 1);
        Parser.tree_building(actual);

        List<AST> expected = new ArrayList<>();
        expected.add(new AST("Sub Main()", "Procedure declaration", 1, null));

        Assert.assertEquals(expected.get(0).getType(), actual.get(0).getType());
    }

    @org.junit.jupiter.api.Test
    void procedure_declaration_test_error1 () {
        Parser.setErrors(new ArrayList<String>());

        List<AST> nodes = create_nodes_AST("Sub 1()", 1);
        Parser.tree_building(nodes);
        List<String>actual = Parser.getErrors();

        List<String> expected = new ArrayList<>();
        expected.add("PARSER: Line: 1 \"After ' Sub ' expected procedure name\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void end_design_test_error1 () {
        Parser.setErrors(new ArrayList<String>());

        List<AST> nodes = create_nodes_AST("End while", 1);
        Parser.tree_building(nodes);
        List<String> actual = Parser.getErrors();

        List<String> expected = new ArrayList<>();
        expected.add("PARSER: Line: 1 \"After ' End ' expected 'While', 'If', 'Sub'\"");

        Assert.assertEquals(expected, actual);
    }

    private List<AST> create_nodes_AST (String processedString, int line) {
        List<AST> nodes = new ArrayList<>();
        List<Token> tokens = new ArrayList<>();

        Lexer.line_handler(processedString, line, tokens);
        Parser.token_processing(tokens, nodes);

        return nodes;
    }

    private void print_nodes(List<AST> nodes) {
        for (AST s : nodes) { Parser.print_node(s, 4, 100); }
        Parser.print_errors();
    }

}
