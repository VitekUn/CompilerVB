import AST.AST;
import Lexer.Lexer;
import Parser.Parser;
import Semantics.Semantics;
import Semantics.Scope;
import Token.Token;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestSemantics {

    @org.junit.jupiter.api.Test
    void logical_expr_test_error1 () {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("If a < \"Str\" Then", 1);
        Semantics.analysis_tree(tree, "0", 0);

        actual = Semantics.getErrors();
        expected.add("SEMANTICS: Line: 1 \"Variable 'a' is undeclared\"");
        expected.add("SEMANTICS: Line: 1 \"'a' and '\"Str\"' are of different types\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void appeal_test_error1 () {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Console.Write()", 1);
        Semantics.analysis_tree(tree, "0", 0);

        actual = Semantics.getErrors();
        expected.add("SEMANTICS: Line: 1 \"'Console.Write' must have 1 argument\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void appeal_test_error2 () {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Console.Write(End)", 1);
        Semantics.analysis_tree(tree, "0", 0);

        actual = Semantics.getErrors();
        expected.add("SEMANTICS: Line: 1 \"'Console.Write' argument can only be of type 'String' or 'Integer'\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void appeal_test_error3 () {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("array(\"1\")", 1);
        Semantics.analysis_tree(tree, "0", 0);

        actual = Semantics.getErrors();
        expected.add("SEMANTICS: Line: 1 \"'array' argument can only be of type 'Integer'\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void equality_expr_with_appeal_test_error1 () {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Dim a As Integer " +
                                                       "Dim b(10) As String " +
                                                       "a = b(2)", 1);
        Semantics.analysis_tree(tree, "0", 0);
print_nodes(tree);
        actual = Semantics.getErrors();
        expected.add("SEMANTICS: Line: 1 \"The type declaration does not match the type of the assigned value\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void equality_expr_test_error1 () {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("a = 1 + 2", 1);
        Semantics.analysis_tree(tree, "0", 0);

        actual = Semantics.getErrors();
        expected.add("SEMANTICS: Line: 1 \"Variable 'a' is undeclared\"");
        expected.add("SEMANTICS: Line: 1 \"The type declaration does not match the type of the assigned value\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void equality_expr_test_error2 () {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Dim a As Integer " +
                                                       "a = 1 + b", 1);
        Semantics.analysis_tree(tree, "0", 0);

        actual = Semantics.getErrors();
        expected.add("SEMANTICS: Line: 1 \"Variable 'b' is undeclared\"");
        expected.add("SEMANTICS: Line: 1 \"'1' and 'b' are of different types\"");
        expected.add("SEMANTICS: Line: 1 \"The type declaration does not match the type of the assigned value\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void equality_expr_test_error3 () {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Dim a As Integer " +
                                                       "a = \"1 + 2\"", 1);
        Semantics.analysis_tree(tree, "0", 0);

        actual = Semantics.getErrors();
        expected.add("SEMANTICS: Line: 1 \"The type declaration does not match the type of the assigned value\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void equality_expr_test_error4 () {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("If 0 < 1 Then " +
                "                                         Dim a As Integer End If " +
                                                      "If 2 > 1 Then " +
                                                          "a = 2 End If", 1);
        Semantics.analysis_tree(tree, "0", 0);

        actual = Semantics.getErrors();
        expected.add("SEMANTICS: Line: 1 \"Variable 'a' is undeclared\"");
        expected.add("SEMANTICS: Line: 1 \"The type declaration does not match the type of the assigned value\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void equality_expr_test_error5 () {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Dim a As String " +
                                                       "If 0 < 1 Then " +
                                                           "Dim a As Integer = 10 End If " +
                                                       "If 2 > 1 Then " +
                                                            "a = 2 End If", 1);
        Semantics.analysis_tree(tree, "0", 0);

        actual = Semantics.getErrors();
        expected.add("SEMANTICS: Line: 1 \"The type declaration does not match the type of the assigned value\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void declaration_and_assign_expr_test_error1 () {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Dim a As Integer = \"100\" + 2", 1);
        Semantics.analysis_tree(tree, "0", 0);

        actual = Semantics.getErrors();
        expected.add("SEMANTICS: Line: 1 \"'\"100\"' and '2' are of different types\"");
        expected.add("SEMANTICS: Line: 1 \"The type declaration does not match the type of the assigned value\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void declaration_and_assign_test_error1 () {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Dim a As Integer = \"100\"", 1);
        Semantics.analysis_tree(tree, "0", 0);

        actual = Semantics.getErrors();
        expected.add("SEMANTICS: Line: 1 \"The type declaration does not match the type of the assigned value\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void declaration_and_assign_test_error2 () {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Dim str As String = 125", 1);
        Semantics.analysis_tree(tree, "0", 0);

        actual = Semantics.getErrors();
        expected.add("SEMANTICS: Line: 1 \"The type declaration does not match the type of the assigned value\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void declaration_and_assign_test_error3 () {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Dim str As String = str2", 1);
        Semantics.analysis_tree(tree, "0", 0);

        actual = Semantics.getErrors();
        expected.add("SEMANTICS: Line: 1 \"Variable 'str2' is undeclared\"");
        expected.add("SEMANTICS: Line: 1 \"The type declaration does not match the type of the assigned value\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void get_type_declared_variable_test1 () {
        AST variable = new AST ("a", "Id", 1, null);
        List<AST> tree = create_AST_tree("Dim a As Integer", 1);
        Semantics.analysis_tree(tree, "0", 0);

        String actual = Semantics.get_data_type_variable(variable, "0");
        String expected = "Integer";

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void get_type_declared_variable_test2 () {
        AST variable = new AST ("a", "Id", 2, null);
        List<AST> tree = create_AST_tree("Sub Main() " +
                                                            "Dim a As Integer " +
                                                        "End Sub", 2);
        Semantics.analysis_tree(tree, "0", 0);

        String actual = Semantics.get_data_type_variable(variable, "0->1");
        String expected = "Integer";
        Assert.assertEquals(expected, actual);
    }

    private List<AST> create_AST_tree (String processedString, int line) {
        List<AST> nodes = new ArrayList<>();
        List<Token> tokens = new ArrayList<>();

        Lexer.line_handler(processedString, line, tokens);
        Parser.token_processing(tokens, nodes);
        Parser.tree_building(nodes);
        Semantics.setSymbolTable(new HashMap<String, List<Scope>>());

        return nodes;
    }

    private void print_nodes(List<AST> nodes) {
        for (AST s : nodes) { Parser.print_node(s, 4, 100); }
        Parser.print_errors();
        Semantics.print_errors();
    }
}
