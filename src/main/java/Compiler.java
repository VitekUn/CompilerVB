import CodeGenerator.CodeGenerator;
import Lexer.Lexer;
import Parser.Parser;
import Semantics.Semantics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Compiler {

    public static void main(String[] args) throws IOException {
        File file;
        String options = "non";

        if(args.length == 0) {
//            Compiler obj = new Compiler();
//            file = obj.get_file("gcd.vb");
            return;
        } else if(args.length == 2){
            options = args[0];
            file = new File(args[1]);
        } else {
            file = new File(args[0]);
        }

        switch (options) {
            case ("non"):
                Lexer.file_handler(file);
                Parser.token_processing(Lexer.getTokens(), Parser.getNodesAST());
                Parser.tree_building(Parser.getNodesAST());
                Semantics.analysis_tree(Parser.getNodesAST(), "0", 0);
                CodeGenerator.initialization();
                CodeGenerator.tree_analysis(Parser.getNodesAST());
                print_errors();
                if(get_number_errors() == 0)
                    write_to_file(CodeGenerator.get_assembly_code());
                else
                    update_file();
                break;
            case ("--dump-tokens"):
                Lexer.file_handler(file);
                Lexer.print_tokens();
                print_errors();
                break;
            case ("--dump-ast"):
                Lexer.file_handler(file);
                Parser.token_processing(Lexer.getTokens(), Parser.getNodesAST());
                Parser.tree_building(Parser.getNodesAST());
                Parser.print_tree();
                print_errors();
                break;
            case ("--dump-asm"):
                Lexer.file_handler(file);
                Parser.token_processing(Lexer.getTokens(), Parser.getNodesAST());
                Parser.tree_building(Parser.getNodesAST());
                Semantics.analysis_tree(Parser.getNodesAST(), "0", 0);
                CodeGenerator.initialization();
                CodeGenerator.tree_analysis(Parser.getNodesAST());
                CodeGenerator.print_asm_code();
                print_errors();
                break;
        }
    }

    private static int get_number_errors() {
        int numberErrors = Lexer.getUnidentifiedTokens().size();
        numberErrors += Parser.getErrors().size();
        numberErrors += Semantics.getErrors().size();
        return numberErrors;
    }

    private static void print_errors() {
        Lexer.print_errors();
        Parser.print_errors();
        Semantics.print_errors();
    }

    private static void write_to_file(List<String> asmCode) throws IOException {
        FileWriter writer = new FileWriter("output.s");
        for(String lineCode : asmCode) {
            writer.write(lineCode + System.getProperty("line.separator"));
        }
        writer.close();
    }

    //Стандартный шаблон для запуска gcc. Чтоб не было ошибок при запуске скрипта, когда компилятор находит ошибки
    public static void update_file() throws IOException {
        List<String> asmCode = new ArrayList<>();
        asmCode.add(".globl main\nmain:");
        FileWriter writer = new FileWriter("output.s");
        for(String lineCode : asmCode) {
            writer.write(lineCode + System.getProperty("line.separator"));
        }
        writer.close();

    }

    private File get_file(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(Objects.requireNonNull(classLoader.getResource(fileName)).getFile());
    }
}
