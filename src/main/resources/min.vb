Sub Main()
    Dim arr(10) As Integer 
    Dim max, i As Integer = 0

    While i < arr.Length()
        arr(i) = Console.ReadLine()
        i += 1
    End While

    i = 0

    While i < arr.Length()
        If arr(i) > max Then
            max = arr(i)
            i += 1
        End If
    End While

    Console.Write("Максимум равен: ")
    Console.WriteLine(max)
End Sub
