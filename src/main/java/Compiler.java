import Lexer.Lexer;
import Parser.Parser;
import Semantics.Semantics;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Compiler {

    public static void main(String[] args) throws IOException {
        File file;
        String options = "non";

        if(args.length == 0) {
            Compiler obj = new Compiler();
            file = obj.get_file("gcd.vb");
        } else if(args.length == 2){
            options = args[0];
            file = new File(args[1]);
        } else {
            file = new File(args[0]);
        }

        Lexer.file_handler(file);
//        Lexer.print_tokens();
//        Lexer.print_errors();

        Parser.token_processing(Lexer.getTokens(), Parser.getNodesAST());
        Parser.tree_building(Parser.getNodesAST());
        Parser.print_tree();
        Parser.print_errors();

        Semantics.analysis_tree(Parser.getNodesAST(), "0", 0);
        Semantics.print_symbolTable();
    }

    private File get_file(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(Objects.requireNonNull(classLoader.getResource(fileName)).getFile());
    }
}
