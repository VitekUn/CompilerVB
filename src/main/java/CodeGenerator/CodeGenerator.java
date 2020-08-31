package CodeGenerator;

import AST.AST;
import Parser.Parser;
import Semantics.Semantics;

import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {
    //Блок для объявления инициализированных переменных
    private static List <String> Data = new ArrayList<>();
    //Блок для объявления неинициализированных переменных
    private static List <String> Bss = new ArrayList<>();
    //Блок с самим кодом ассемблера
    private static List <String> Text = new ArrayList<>();

    //Список возможных регистров для выполнения арифмет. операций
    private static List<RegisterASM> registers = new ArrayList<>();
    //Количество объявленных вспомогательных строк
    private static int numberDeclareStr = 0;
    //Количество объявленных переходов
    private static int numberJump = 0;
    //Подобные переменные используются для создания отметок или переменных, которые не повторяются
    // блягодаря увеличению количество созданных (перед созданием переменная увеличивается)


    public static void tree_analysis(List<AST> tree) {
        for(AST node : tree) {
            switch (node.getType()) {
                case("Block Procedure"):
                    tree_analysis(node.getChildren());
                    break;
                case("Block While"):
                    processing_block_while(node);
                    break;
                case("Block If Else"):
                    processing_block_if_else(node);
                    break;
                case("Block If"):
                    processing_block_if(node);
                    break;
                case("Procedure declaration"):
                    processing_procedure_declaration(node);
                    break;
                case("Variable declaration and assign"):
                    processing_var_decl_and_assign(node);
                    break;
                case("Variable declaration"):
                    processing_var_decl(node);
                    break;
                case("Equality expr"):
                    processing_equality_expr(node);
                    break;
                case("Appeal"):
                    processing_appeal(node);
                    break;
            }
        }
    }

    public static void processing_block_if_else(AST block) {
        //Блок иф елсе состоит из блоков иф и елсе
        AST blockIf = block.getChildren().get(0);
        AST blockElse = block.getChildren().get(1);
        //Условие блока иф
        AST logicalExpr = blockIf.getChildren().get(0).getChildren().get(1);
        //Отметка к концу конструкции
        String jumpEnd = get_name_jump();
        //Отметка к блоку елсе
        String jumpElseBlock = get_name_jump();

        //Сначла собираем условие по которому будем переходить к елсе, если иф не выполняется
        Text.add("\n#If");
        processing_logical_expr(logicalExpr, jumpElseBlock);
        //После этого добавляем код блока иф (содержимое)
        tree_analysis(blockIf.getChildren());
        //Если блок иф выполнился, значит нужно пропустить плок елсе, поэтому мы его перепрыггиваем
        Text.add("jmp \t" + jumpEnd);
        //Отметка к блоку елсе и содержимое блока
        Text.add(jumpElseBlock + ":");
        Text.add("\n#Else");
        tree_analysis(blockElse.getChildren());
        Text.add(jumpEnd + ":");
    }

    public static void processing_block_if(AST block) {
        AST declarationIf = block.getChildren().get(0);
        String nameJump = get_name_jump();

        Text.add("\n#If");
        //Обработка условия иф
        processing_logical_expr(declarationIf.getChildren().get(1), nameJump);
        //Тело блока иф
        tree_analysis(block.getChildren());
        //Метка куда переходить, если условие не выполняется
        Text.add(nameJump + ":");
    }

    public static void processing_block_while(AST block) {
        AST declarationWhile = block.getChildren().get(0), node;
        String startNameJump = get_name_jump();
        String conditionJump = get_name_jump();

        Text.add("\n#While");
        //Отметка начала цикла
        Text.add(startNameJump + ":");
        //Обработка условия
        processing_logical_expr(declarationWhile.getChildren().get(1), conditionJump);
        //Тело цикла
        tree_analysis(block.getChildren());
        //Прыжок в начало цикла
        Text.add("jmp \t" + startNameJump);
        //Сюда прыжок, если не выполняется условия цикла
        Text.add(conditionJump + ":");
    }

    public static void processing_var_decl(AST node) {
        //Объявление переменной без начального значения
        //Получаем список всех переменных
        List<AST> variables = node.getChildren().get(0).getChildren();
        //Определяем какой тип
        String dataType = Semantics.get_type_declaration(node.getChildren().get(1).getChildren());
        //Размер переменной, которую объявляем
        int size = 0;

        switch (dataType) {
            case ("Integer"):
                //4 байта
                size = 4;
                break;
            case ("String"):
                //Непонятно какая будет строка, поэтому даём с запасом 100 (это костыль)
                size = 100;
                break;
        }

        //Начинаем не с нулевого, так как нулевой это "Dim"
        for(int i = 1; i < variables.size(); i++) {
            //Если объявляем массив
            if(variables.get(i).getType().equals("Appeal")) {
                processing_decl_array(variables, size, i);
            //Если обычная переменная
            } else {
                Bss.add(variables.get(i).getName() + ":");
                Bss.add("\t\t.space " + size);
            }
        }
    }

    public static void processing_var_decl_and_assign(AST node) {
        //Аналогично processing_var_decl только объявляяем в другом блоке и значение есть
        List<AST> variables = node.getChildren().get(0).getChildren();
        String dataType = Semantics.get_type_declaration(node.getChildren().get(1).getChildren());
        AST value = node.getChildren().get(1).getChildren().get(2);
        String declaration = "";
        int size = 0;

        if(value.getType().equals("Arithmetic expr")) {
            String reg = get_free_reg();
            processing_var_decl(node);
            arithmetic_expr_prioritization(value.getChildren());
            processing_arithmetic_expr(value.getChildren().get(0).getChildren(), reg);
            for(int i = 1; i < variables.size(); i++) {
                if (variables.get(i).getType().equals("Id")) {
                    Text.add("movl \t%" + reg + ", " + variables.get(i).getName());
                }
            }
            free_reg(reg);
            return;
        }

        switch (dataType) {
            case ("Integer"):
                declaration = ".int " + value.getName();
                size = 4;
                break;
            case ("String"):
                declaration = ".string " + value.getName();
                size = 100;
                break;
        }

        for(int i = 1; i < variables.size(); i++) {
            if(variables.get(i).getType().equals("Appeal")) {
                processing_decl_array(variables, size, i);
            } else {
                Data.add(variables.get(i).getName() + ":");
                Data.add("\t\t" + declaration);
                if(dataType.equals("String")) {
                    Data.add(variables.get(i).getName() + ".Length:");
                    int sizeValue = value.getName().length() - 2;
                    Data.add("\t\t.int " + sizeValue);
                }
            }
        }
    }

    public static void processing_logical_expr(AST expr, String nameJump) {
        AST operand1 = expr.getChildren().get(0);
        AST operation = expr.getChildren().get(1);
        AST operand2 = expr.getChildren().get(2);
        //Берём два свободных регистра
        String reg1 = get_free_reg(), reg2 = get_free_reg(), dataType = "";

        //Если один из операндов элемент массива, то нам важно знать тип данных
        //Так как если это строка, то нам нужно будет обращаться к элементам побайтово
        if(operand1.getType().equals("Appeal")) {
            dataType = get_type_variable(operand1.getChildren().get(0).getName());
        } else if (operand2.getType().equals("Appeal")) {
            dataType = get_type_variable(operand2.getChildren().get(0).getName());
        }

        Text.add("\n#" + operand1.getName() + operation.getName() + operand2.getName());
        //Кладём значения операндов в регистры
        register_init_value(reg1, operand1, dataType);
        register_init_value(reg2, operand2, dataType);

        //Сравниваем значения регистрок
        Text.add("cmp \t%" + reg_type(reg2, dataType) + ", %" + reg_type(reg1, dataType));
        //В зависимости от условия выбираем условие на ассемблере
        switch (operation.getName()) {
            case ("<"):
                Text.add("jnl \t" + nameJump);
                break;
            case (">"):
                Text.add("jng \t" + nameJump);
                break;
            case (">="):
                Text.add("jl \t" + nameJump);
                break;
            case ("<="):
                Text.add("jg \t" + nameJump);
                break;
            case ("="):
                Text.add("jne \t" + nameJump);
                break;
            case ("<>"):
                Text.add("je \t" + nameJump);
                break;
        }
        //Освобождаем регистры
        free_reg(reg1);
        free_reg(reg2);
    }

    public static String reg_type(String reg, String dataType) {
        //Если тип данных строка, нужно обращаться не к целому регистру, а к его части
        //Например %eax - 4 б, если строка (1 байт), то %ah - 1 б
        if(dataType.equals("String"))
            return reg.charAt(1) + "h";
        else
            return reg;
    }

    public static void register_init_value(String reg, AST value, String dataType) {
        //Инициализируем ригстр значением, в зависимоти от того, что является значением
        switch (value.getType()) {
            case ("Arithmetic expr"):
                arithmetic_expr_prioritization(value.getChildren());
                processing_arithmetic_expr(value.getChildren().get(0).getChildren(), reg);
                break;
            default:
                Text.add("mov" + dimension(dataType) + " \t" + processing_operand(value) + ", %" + reg_type(reg, dataType));
        }
    }

    public static String dimension(String dataType) {
        //Если тип данных строка - то обращением побайтовое значит нужно использовать "movb"
        if(dataType.equals("String"))
            return "b";
        else
            return "";
    }

    public static void processing_appeal(AST node) {
        //Обработка вызова функции
        AST nameCall = node.getChildren().get(0);
        AST argument = node.getChildren().get(1).getChildren().get(1);
        String dataType;
        switch (nameCall.getName()) {
            case ("Console.Write"):
            case ("Console.WriteLine"):
                Text.add("\n#" + nameCall.getName() + " " + argument.getName());
                //В зависимости от типа аргументов
                switch (argument.getType()) {
                    case ("String"):
                        Text.add("mov \t$" + create_buf_str(argument.getName()) + ", %rdi");
                        break;
                    case ("Id"):
                        dataType = get_type_variable(argument.getName());
                        if (dataType.equals("Integer"))
                            Text.add("mov \t$decimal_format, %rdi");
                        else if (dataType.equals("String"))
                            Text.add("mov \t$string_format, %rdi");
                        Text.add("mov \t" + processing_operand(argument) + ", %rsi");
                        break;
                    case ("Appeal"):
                        dataType = get_type_variable(argument.getChildren().get(0).getName());
                        if (dataType.equals("String"))
                            Text.add("mov \t$symbol_format, %rdi");
                        else
                            Text.add("mov \t$decimal_format, %rdi");
                        Text.add("mov \t" + processing_operand(argument) + ", %rsi");
                        break;
                }
                Text.add("call \tprintf");
                //Если условие, то нужно перевести на новую строку ("\n")
                if (nameCall.getName().equals("Console.WriteLine")) {
                    Text.add("mov \t$new_line, %rdi");
                    Text.add("call \tprintf");
                }
                break;
        }
    }

    public static void processing_equality_expr(AST node) {
        //Обработка присваивания
        AST variable = node.getChildren().get(0);
        AST value = node.getChildren().get(2);
        //Пока пустой регистр, так как он не всегда нужен (в зависимости от того, что яв-ся значением)
        String reg = "";
        String dataType = get_type_variable(variable.getName());
        Text.add("\n#" + variable.getName() + " = " + value.getName());

        //Если присваиваем строке
        if(dataType.equals("String")) {
            reg = get_free_reg();
            //Если строчный литерал
            if(value.getType().equals("String")) {
                Text.add("mov \t" + create_buf_str(value.getName()) + ", %rdi");
                Text.add("mov \t%rdi, " + variable.getName());
                int size = value.getName().length() - 2;
                Text.add("mov \t$" + size + ", %" + reg);
            //Если строка - переменная
            } else {
                Text.add("mov \t" + value.getName() + ", %rdi");
                Text.add("mov \t%rdi, " + variable.getName());
                Text.add("mov \t" + value.getName() + ".Length, %" + reg);
            }
            //Также изменяем переменную, которая хранит размер строки
            Text.add("mov \t%" + reg + ", " + variable.getName() + ".Length");
        } else {
            switch (value.getType()) {
                case ("Appeal"):
                    String name = value.getChildren().get(0).getName();
                    if (name.equals("Console.ReadLine")) {
                        Text.add("\nxorl\t%eax, %eax \t\t#Console.ReadLine");
                        Text.add("movq\t$decimal_format, %rdi");
                        Text.add("leaq\t" + processing_operand(variable) + ", %rsi");
                        Text.add("call\tscanf");
                    } else {
                        reg = get_free_reg();
                        Text.add("mov \t" + processing_operand(value) + ", %" + reg);
                        Text.add("mov \t%" + reg + ", " + processing_operand(variable));
                    }
                    break;
                case ("Id"):
                case ("Decimal"):
                    reg = get_free_reg();
                    Text.add("mov \t" + processing_operand(value) + ", %" + reg);
                    Text.add("mov \t%" + reg + ", " + processing_operand(variable));
                    break;
                case ("Arithmetic expr"):
                    arithmetic_expr_prioritization(value.getChildren());
                    reg = get_free_reg();
                    processing_arithmetic_expr(value.getChildren().get(0).getChildren(), reg);
                    Text.add("movl \t%" + reg + ", " + processing_operand(variable));
                    break;
            }
        }
        free_reg(reg);
    }

    public static void processing_arithmetic_expr(List<AST> expr, String reg) {
        AST operand1 = expr.get(0);
        AST operation = expr.get(1);
        AST operand2 = expr.get(2);
        String reg2 = "";

        //Если ситуация, что операнд1 яв-ся арифм выражением, то нам нужно сначала вычислить его
        if(!operand1.getChildren().isEmpty() && !operand1.getType().equals("Appeal")) {
            processing_arithmetic_expr(operand1.getChildren(), reg);
            //Так как мы вычислили и положили результат в регистр, то операнд теперь регистр
            operand1 = new AST(reg, "reg");
        }
        //Аналогично
        if(!operand2.getChildren().isEmpty() && !operand2.getType().equals("Appeal")) {
            reg2 = get_free_reg();
            processing_arithmetic_expr(operand2.getChildren(), reg2);
            operand2 = new AST(reg2, "reg");
        }
        //вычисляем
        processing_operation(operand1, operation, operand2, reg);
        //Освобождаем регистр, если вдруг он был использован
        free_reg(reg2);
    }

    public static void processing_operation(AST oper1, AST operation, AST oper2, String reg) {
        //Иногда нужен доп регистр, для вычисления операции
        String reg2 = get_free_reg();
        //Определяем как нам обращаться к операндам
        Text.add("#" + processing_operand(oper1) + operation.getName() + processing_operand(oper2));
        switch (operation.getName()) {
            case ("+"):
                Text.add("mov \t" + processing_operand(oper1) + ", %" + reg);
                Text.add("add \t" + processing_operand(oper2) + ", %" + reg);
                break;
            case ("-"):
                Text.add("mov \t" + processing_operand(oper1) + ", %" + reg);
                Text.add("sub \t" + processing_operand(oper2) + ", %" + reg);
                break;
            case ("*"):
                Text.add("mov \t" + processing_operand(oper1) + ", %eax");
                Text.add("mov \t" + processing_operand(oper2) + ", %" + reg2);
                Text.add("mul \t%" + reg2);
                Text.add("mov \t%eax, %" + reg);
                break;
            case ("/"):
            case ("Mod"):
                Text.add("mov \t" + processing_operand(oper1) + ", %eax");
                Text.add("mov \t" + processing_operand(oper2) + ", %" + reg2);
                Text.add("xor \t%edx, %edx");
                Text.add("div \t%" + reg2);
                if(operation.getName().equals("/"))
                    Text.add("mov \t%eax, %" + reg);
                else
                    Text.add("mov \t%edx, %" + reg);
                break;
        }
        free_reg(reg2);
    }

    public static String processing_operand(AST operand) {
        switch (operand.getType()) {
            case ("reg"):
                return "%" + operand.getName();
            case ("Id"):
                String dataType = get_type_variable(operand.getName());
                if(dataType.equals("String"))
                    return "$" + operand.getName();
                return operand.getName();
            case ("Appeal"):
                String nameArray = operand.getChildren().get(0).getName();
                //Если это вызов функции типа array.Length(), то нам нужно обратиться к заранее объявленной переменной,
                //которая храним в себе размер строки или массива
                if(operand.getName().contains(".Length"))
                    return nameArray;
                //А если это обращения к массиву, то кладём индекс в регистр и используем конструкцию, для обращения
                //к масиивам или элементам тсрок
                AST index = operand.getChildren().get(1).getChildren().get(1);
                Text.add("mov \t" + processing_operand(index) + ", %edx");
                return nameArray + "(,%edx," + get_size_type_data(get_type_variable(nameArray)) + ")";
            default:
                return "$" + operand.getName();
        }
    }

    public static int get_size_type_data(String type) {
        //Определение количества байт в зависимости от типа данных
        if(type.equals("Integer")) {
            return 4;
        } else if (type.equals("String")) {
            return 1;
        }
        return 4;
    }

    public static void processing_decl_array(List<AST> variables, int size, int i) {
        //Если объявление массива например array, то нам нужно также объявить переменную типа инт array.Length
        String name = variables.get(i).getChildren().get(0).getName();
        int numberElem = Integer.parseInt(variables.get(i).getChildren().get(1).getChildren().get(1).getName());
        int sizeData = size * numberElem;
        Bss.add(name + ":");
        Bss.add("\t\t.space " + sizeData);
        Data.add(name + ".Length:");
        Data.add("\t\t.int " + numberElem);
    }

    public static void processing_procedure_declaration(AST node) {
        Text.add(".globl main");
        Text.add(".type main, @function");
        Text.add("main:");
        Text.add("pushq \t%rbp");
        Text.add("movq \t%rsp, %rbp");
    }

    public static String get_type_variable(String name) {
        //Получаем тип данных из таблицы символов
        if(!Semantics.getSymbolTable().containsKey(name))
            return "Error";
        return Semantics.getSymbolTable().get(name).get(0).getType();
    }

    public static String create_buf_str(String value) {
        //Объявляем строку буффер для вызова Console.Write, помещаем строковый литералв переменную и используем
        //имя переменной для вызова
        numberDeclareStr++;
        String name = "str" + numberDeclareStr;
        Data.add(name + ":");
        Data.add("\t\t.string " + value);
        return name;
    }

    public static String get_name_jump() {
        numberJump++;
        return "jump" + numberJump;
    }

    public static void arithmetic_expr_prioritization(List<AST> expr) {
        //Сборка арифм выражения с учётом приоритетов
        String operation;
        //Собираем сначала те узлы, где операции деления, умножения, или остаток от деления
        for(int i = 1; i < expr.size(); i += 2) {
            operation = expr.get(i).getName();
            if(operation.equals("*") || operation.equals("/") || operation.equals("Mod")) {
                Parser.merging_nodes(expr, i - 1, i + 1, "Expr", expr.get(0).getLine());
                i -= 2;
            }
        }
        int i = 1;
        //потом собираем все остальные
        while(expr.size()!= 1) {
            operation = expr.get(i).getName();
            Parser.merging_nodes(expr, i-1, i+1, "Expr", expr.get(0).getLine());
        }
    }

    public static void initialization(){
        //Инициализация
        //Сразу объявляем вспомогательные переменные для Console.Write
        Data.add(".data\n");
        Data.add("decimal_format:");
        Data.add("\t\t.string \"%d\"");
        Data.add("string_format:");
        Data.add("\t\t.string \"%s\"");
        Data.add("symbol_format:");
        Data.add("\t\t.string \"%c\"");
        Data.add("new_line:");
        Data.add("\t\t.string \"\\n\"");
        Bss.add(".bss\n");

        Text.add(".text\n");

        //Инициализиурем список регистров
        init_registers();
    }

    public static void init_registers() {
        //Здесь не все регистры, так как некоторые использяуются в других целях
        //Например eax используется для хранения результатов операции умножения или деления
        registers.add(new RegisterASM("ebx", false));
        registers.add(new RegisterASM("ecx", false));
        registers.add(new RegisterASM("ebp", false));
        registers.add(new RegisterASM("esi", false));
        registers.add(new RegisterASM("edi", false));
    }

    public  static String get_free_reg () {
        //Получаем имя свободного регистра
        for(RegisterASM reg : registers) {
            //Проверка статуса
            if(!reg.isInWork()) {
                reg.setInWork(true);
                return reg.getName();
            }
        }
        return "Non";
    }

    public  static void free_reg (String nameReg) {
        for(RegisterASM reg : registers) {
            if(reg.getName().equals(nameReg)) {
                //Изменение статуса (делаем свободным)
                reg.setInWork(false);
            }
        }
    }

    public static List<String> get_assembly_code() {
        //Получаение всего кода ассемблера
        List<String> assemblyCode = new ArrayList<>();
        assemblyCode.addAll(Data);
        assemblyCode.addAll(Bss);
        assemblyCode.addAll(Text);
        return assemblyCode;
    }

    public static void clear_data() {
        Data = new ArrayList<>();
        Bss = new ArrayList<>();
        Text = new ArrayList<>();
        numberJump = 0;
        numberDeclareStr = 0;
    }

    public static void print_asm_code() {
        List<String> assemblyCode = new ArrayList<>();
        assemblyCode.addAll(Data);
        assemblyCode.addAll(Bss);
        assemblyCode.addAll(Text);
        for(String s : assemblyCode)
            System.out.println(s);
    }
}
