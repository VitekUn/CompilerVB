Sub Main()
	Dim a, b, i As Integer = 0
    Console.Write("Введите первое число: ")
	a = Console.ReadLine()
    Console.Write("Введите второе число: ")
	b = Console.ReadLine()
    i = a
    
    While i > 0
        If a Mod i = 0 Then
            If b Mod i = 0 Then
                Console.Write("Наибольший общий делитель: ")
                Console.WriteLine(i)
                i = 0
            End If
        End If
        i = i - 1
    End While
End Sub