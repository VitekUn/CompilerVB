.data

decimal_format:
		.string "%d"
string_format:
		.string "%s"
symbol_format:
		.string "%c"
new_line:
		.string "\n"
a:
		.int 0
b:
		.int 0
i:
		.int 0
str1:
		.string "Введите первое число : "
str2:
		.string "Введите второе число : "
str3:
		.string "Наибольший общий делитель : "
.bss

.text

.globl main
.type main, @function
main:
pushq 	%rbp
movq 	%rsp, %rbp

#Console.Write "Введите первое число : "
mov 	$str1, %rdi
call 	printf

#a = Console.ReadLine ( )  

xorl	%eax, %eax 		#Console.ReadLine
movq	$decimal_format, %rdi
leaq	a, %rsi
call	scanf

#Console.Write "Введите второе число : "
mov 	$str2, %rdi
call 	printf

#b = Console.ReadLine ( )  

xorl	%eax, %eax 		#Console.ReadLine
movq	$decimal_format, %rdi
leaq	b, %rsi
call	scanf

#i = a
mov 	a, %ebx
mov 	%ebx, i

#While
jump1:

#i>0
mov 	i, %ebx
mov 	$0, %ecx
cmp 	%ecx, %ebx
jng 	jump2

#If

#a Mod i =0
#aModi
mov 	a, %eax
mov 	i, %ebp
xor 	%edx, %edx
div 	%ebp
mov 	%edx, %ebx
mov 	$0, %ecx
cmp 	%ecx, %ebx
jne 	jump3

#If

#b Mod i =0
#bModi
mov 	b, %eax
mov 	i, %ebp
xor 	%edx, %edx
div 	%ebp
mov 	%edx, %ebx
mov 	$0, %ecx
cmp 	%ecx, %ebx
jne 	jump4

#Console.Write "Наибольший общий делитель : "
mov 	$str3, %rdi
call 	printf

#Console.WriteLine i
mov 	$decimal_format, %rdi
mov 	i, %rsi
call 	printf
mov 	$new_line, %rdi
call 	printf

#i = 0
mov 	$0, %ebx
mov 	%ebx, i
jump4:
jump3:

#i = i - 1 
#i-$1
mov 	i, %ebx
sub 	$1, %ebx
movl 	%ebx, i
jmp 	jump1
jump2:
