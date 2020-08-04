import AST.AST;
import Lexer.Lexer;
import Parser.Parser;
import Token.Token;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class TestParser {
    @org.junit.jupiter.api.Test
    void logical_expression_test_build1 () {
        List<AST> actual = create_nodes_AST("i<2 AND i>0", 1);
        Parser.tree_building(actual);

        List<AST> expected = new ArrayList<>();
        expected.add(new AST("i<2 AND i>0", "Logical expr", 1, null));

        Assert.assertEquals(expected.get(0).getType(), actual.get(0).getType());
    }

    @org.junit.jupiter.api.Test
    void logical_expression_test_build2 () {
        List<AST> actual = create_nodes_AST("i<2 AND i>0 OR a<=i", 1);
        Parser.tree_building(actual);

        List<AST> expected = new ArrayList<>();
        expected.add(new AST("i<2 AND i>0 OR a<=i", "Logical expr", 1, null));

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

}
