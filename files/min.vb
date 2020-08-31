Sub Main()
    Dim min, arr(4) As Integer
    Dim i As Integer = 0

    While i < arr.Length()
        Console.Write("Введите элемент ")
        Console.Write(i)
        Console.Write(": ")
        arr(i) = Console.ReadLine()
        i = i + 1
    End While

    i = 0
    min = arr(0)

    While i < arr.Length()
        If arr(i) < min Then
            min = arr(i)
        End If
        i = i + 1
    End While

    Console.Write("Минимум равен: ")
    Console.WriteLine(min)
End Sub
