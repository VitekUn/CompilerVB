Sub Main()
	Dim a, b, result As Integer = 0
	a = Console.ReadLine()
	b = Console.ReadLine()

	While a <> 0 And b <> 0
		If a >= b Then 
			a = a Mod b 
		Else 
			b = b Mod a
        End If
	End While

	result = a + b

	Console.Write("Наибольший общий делитель: ")
	Console.WriteLine(result)

End Sub