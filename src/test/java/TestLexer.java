import Lexer.Lexer;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class TestLexer {
    @org.junit.jupiter.api.Test
    void token_type_definition_test_id () {
        List<String> expected = new ArrayList<>();
        List<String> actual = new ArrayList<>();

        actual.add(Lexer.token_type_definition("Variable"));
        expected.add("Id");
        actual.add(Lexer.token_type_definition("_Variable"));
        expected.add("Id");
        actual.add(Lexer.token_type_definition("Variable_n"));
        expected.add("Id");
        actual.add(Lexer.token_type_definition("1Variable"));
        expected.add("Unknown");
        actual.add(Lexer.token_type_definition("variable1"));
        expected.add("Id");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void token_type_definition_test_number () {
        List<String> expected = new ArrayList<>();
        List<String> actual = new ArrayList<>();

        actual.add(Lexer.token_type_definition("0b0_1"));
        expected.add("Binary");
        actual.add(Lexer.token_type_definition("012345"));
        expected.add("Octal");
        actual.add(Lexer.token_type_definition("012349"));
        expected.add("Unknown");
        actual.add(Lexer.token_type_definition("1012349"));
        expected.add("Decimal");
        actual.add(Lexer.token_type_definition("0"));
        expected.add("Decimal");
        actual.add(Lexer.token_type_definition("-0"));
        expected.add("Unknown");
        actual.add(Lexer.token_type_definition("12-42"));
        expected.add("Unknown");
        actual.add(Lexer.token_type_definition("0x12349"));
        expected.add("Hexadecimal");
        actual.add(Lexer.token_type_definition("1012.349"));
        expected.add("Float");
        actual.add(Lexer.token_type_definition("0.0"));
        expected.add("Float");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void spliting_string_test1 () {
        List<String> expected = new ArrayList<>();
        List<String> actual = new ArrayList<>();

        actual = Lexer.spliting_string("a=b+c");
        expected.add("a");
        expected.add("=");
        expected.add("b");
        expected.add("+");
        expected.add("c");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void spliting_string_test_literal1 () {
        List<String> expected = new ArrayList<>();
        List<String> actual = new ArrayList<>();

        actual = Lexer.spliting_string("Console.Write(\"Сообщение\")");
        expected.add("Console.Write");
        expected.add("(");
        expected.add("\"Сообщение\"");
        expected.add(")");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void spliting_string_test_literal2 () {
        List<String> expected = new ArrayList<>();
        List<String> actual = new ArrayList<>();

        actual = Lexer.spliting_string("Dim line As String=\"abcdef\"");
        expected.add("Dim");
        expected.add("line");
        expected.add("As");
        expected.add("String");
        expected.add("=");
        expected.add("\"abcdef\"");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void spliting_string_test_comment1 () {
        List<String> expected = new ArrayList<>();
        List<String> actual = new ArrayList<>();

        actual = Lexer.spliting_string("Console.Write(\"Сообщение\") 'Вывод в консоль \"Сообщение\"");
        expected.add("Console.Write");
        expected.add("(");
        expected.add("\"Сообщение\"");
        expected.add(")");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void spliting_string_test_comment2 () {
        List<String> expected = new ArrayList<>();
        List<String> actual = new ArrayList<>();

        actual = Lexer.spliting_string(" ' Комментарий");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void definition_type_tokens_from_string () {
        List<String> expected = new ArrayList<>();
        List<String> actual = new ArrayList<>();

        List<String> tokens = Lexer.spliting_string("Dim line As String=\"abcdef\"");
        for(String t : tokens) {
            actual.add(Lexer.token_type_definition(t));
        }

        expected.add("Keyword");
        expected.add("Id");
        expected.add("Keyword");
        expected.add("DataType");
        expected.add("Equality");
        expected.add("String");

        Assert.assertEquals(expected, actual);
    }

}
