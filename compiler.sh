 #!/bin/bush

if [ $# -ne 2 ]
then
	if [ $# -ne 1 ]
	then
		# Неверное количество параметров
		echo "Error. Expected './compiler.sh <file.vb>' or './compiler.sh [options] <file.vb>'"
	else
		# Один параметр		
		if [ $1 == --help ]
		then
			echo "Start: './compiler.sh <file.vb>' or './compiler.sh [options] <file.vb>'"
			echo "Options:"
			echo "'--dump-tokens' — Вывести результат работы лексического анализатора"
			echo "'--dump-ast' — Вывести AST дерево"
			echo "'--dump-asm' — Вывести сгенерированный код ассемблера"
		else
			java -classpath target/classes Compiler $1
			gcc -no-pie ./output.s -o output
			./output
		fi
	fi
else
	# Два параметра
	java -classpath target/classes Compiler $1 $2
fi
