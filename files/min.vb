Sub Main()
    Dim arr(10) As Integer 
    Dim max As Integer = 0
    Dim i As Integer = 0

    While i < arr.Length()
        a(i) = CInt(Console.ReadLine()) 
        i += 1
    End While

    i = 0

    While i < arr.Length()
        If a(i) > max Then 
            max = a(i)
            i += 1
        End If
    End While

    Console.Write("Максимум равен: ")
    Console.WriteLine(max)
End Sub
