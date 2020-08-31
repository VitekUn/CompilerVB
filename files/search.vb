Sub Main()
    Dim line As String = "String"
    Dim substring As String = "Str"
    Dim i, j, count As Integer = 0

    Console.Write("Строка :")
    Console.WriteLine(line)
    Console.Write("Подстрока :")
    Console.WriteLine(substring)

    While i < line.Length()
        If line(i) = substring(j) Then
            j = j + 1
            count = count + 1
        Else
            j = 0
            count = 0
        End If

        If count = substring.Length() Then
            Console.WriteLine("Является подстрокой!")
            i = line.Length()
        End If
        i = i + 1
    End While
    
End Sub