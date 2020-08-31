import AST.AST;
import CodeGenerator.CodeGenerator;
import Lexer.Lexer;
import Parser.Parser;
import Semantics.Semantics;
import Token.Token;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class TestCodeGenerator {

    @org.junit.jupiter.api.Test
    void declared_integer_var() {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Dim a As Integer", 1);
        CodeGenerator.tree_analysis(tree);

        actual = CodeGenerator.get_assembly_code();
        expected.add("a:");
        expected.add("\t\t.space 4");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void declared_integer_var_and_assign1() {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Dim a As Integer = 5", 1);
        CodeGenerator.tree_analysis(tree);

        actual = CodeGenerator.get_assembly_code();
        expected.add("a:");
        expected.add("\t\t.int 5");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void declared_var_and_assign2() {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Dim a As Integer = 1 " +
                                                       "Dim str1 As String = \"Str\" ", 1);
        CodeGenerator.tree_analysis(tree);

        actual = CodeGenerator.get_assembly_code();
        expected.add("a:");
        expected.add("\t\t.int 1");
        expected.add("str1:");
        expected.add("\t\t.string \"Str\"");
        expected.add("str1.Length:");
        expected.add("\t\t.int 3");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void declared_integer_var_and_assign3() {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Dim a, b(10), c As Integer = 5 + 2", 1);
        CodeGenerator.init_registers();
        CodeGenerator.tree_analysis(tree);

        actual = CodeGenerator.get_assembly_code();
        expected.add("b.Length:");
        expected.add("\t\t.int 10");
        expected.add("a:");
        expected.add("\t\t.space 4");
        expected.add("b:");
        expected.add("\t\t.space 40");
        expected.add("c:");
        expected.add("\t\t.space 4");
        expected.add("#$5+$2");
        expected.add("mov \t$5, %ebx");
        expected.add("add \t$2, %ebx");
        expected.add("movl \t%ebx, a");
        expected.add("movl \t%ebx, c");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void equality_expr1() {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("a = 1 + 2 - 3", 1);
        CodeGenerator.init_registers();
        CodeGenerator.tree_analysis(tree);

        actual = CodeGenerator.get_assembly_code();
        expected.add("\n#a = 1 + 2 - 3 ");
        expected.add("#$1+$2");
        expected.add("mov \t$1, %ebx");
        expected.add("add \t$2, %ebx");
        expected.add("#%ebx-$3");
        expected.add("mov \t%ebx, %ebx");
        expected.add("sub \t$3, %ebx");
        expected.add("movl \t%ebx, a");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void equality_expr2() {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("a(1) = 1 * 2", 1);
        CodeGenerator.init_registers();
        CodeGenerator.tree_analysis(tree);

        actual = CodeGenerator.get_assembly_code();
        expected.add("\n#a ( 1 )   = 1 * 2 ");
        expected.add("#$1*$2");
        expected.add("mov \t$1, %eax");
        expected.add("mov \t$2, %ecx");
        expected.add("mul \t%ecx");
        expected.add("mov \t%eax, %ebx");
        expected.add("mov \t$1, %edx");
        expected.add("movl \t%ebx, a(,%edx,4)");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void block_if1() {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("If a < b Then b = 0 End If", 1);
        CodeGenerator.init_registers();
        CodeGenerator.tree_analysis(tree);

        actual = CodeGenerator.get_assembly_code();
        expected.add("\n#If");
        expected.add("\n#a<b");
        expected.add("mov \ta, %ebx");
        expected.add("mov \tb, %ecx");
        expected.add("cmp \t%ecx, %ebx");
        expected.add("jnl \tjump1");
        expected.add("\n#b = 0");
        expected.add("mov \t$0, %ebx");
        expected.add("mov \t%ebx, b");
        expected.add("jump1:");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void block_if_else1() {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("If a(1) < b(2) Then Else End If", 1);
        CodeGenerator.init_registers();
        CodeGenerator.tree_analysis(tree);

        actual = CodeGenerator.get_assembly_code();

        expected.add("\n#If");
        expected.add("\n#a ( 1 )  <b ( 2 )  ");
        expected.add("mov \t$1, %edx");
        expected.add("mov \ta(,%edx,4), %ebx");
        expected.add("mov \t$2, %edx");
        expected.add("mov \tb(,%edx,4), %ecx");
        expected.add("cmp \t%ecx, %ebx");
        expected.add("jnl \tjump2");
        expected.add("jmp \tjump1");
        expected.add("jump2:");
        expected.add("\n#Else");
        expected.add("jump1:");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void output1() {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Dim a As Integer Console.WriteLine(a)", 1);
        Semantics.analysis_tree(tree, "0", 0);
        CodeGenerator.tree_analysis(tree);

        actual = CodeGenerator.get_assembly_code();

        expected.add("a:");
        expected.add("\t\t.space 4");
        expected.add("\n#Console.WriteLine a");
        expected.add("mov \t$decimal_format, %rdi");
        expected.add("mov \ta, %rsi");
        expected.add("call \tprintf");
        expected.add("mov \t$new_line, %rdi");
        expected.add("call \tprintf");

        Assert.assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void output2() {
        Semantics.setErrors(new ArrayList<String>());
        List<String> actual, expected = new ArrayList<>();
        List<AST> tree = create_AST_tree("Console.WriteLine(\"MESSAGE!\")", 1);
        Semantics.analysis_tree(tree, "0", 0);
        CodeGenerator.tree_analysis(tree);

        actual = CodeGenerator.get_assembly_code();

        expected.add("str1:");
        expected.add("\t\t.string \"MESSAGE!\"");
        expected.add("\n#Console.WriteLine \"MESSAGE!\"");
        expected.add("mov \t$str1, %rdi");
        expected.add("call \tprintf");
        expected.add("mov \t$new_line, %rdi");
        expected.add("call \tprintf");

        Assert.assertEquals(expected, actual);
    }

    private List<AST> create_AST_tree (String processedString, int line) {
        List<AST> nodes = new ArrayList<>();
        List<Token> tokens = new ArrayList<>();

        Lexer.line_handler(processedString, line, tokens);
        Parser.token_processing(tokens, nodes);
        Parser.tree_building(nodes);
        CodeGenerator.clear_data();

        return nodes;
    }

    private void print_nodes(List<AST> nodes) {
        for (AST s : nodes) { Parser.print_node(s, 4, 100); }
        Parser.print_errors();
        Semantics.print_errors();
    }
}
