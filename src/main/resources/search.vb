Sub Main()
    Dim line As String = "abcdef"
    Dim substring As String = "abc"
    Dim i, j, count As Integer = 0

    While i < line.Length()
        If line(j) = substring(i) Then
            j = j + 1
            count = count + 1
        Else
            j = 0
            count = 0
        End If

        If count = substring.Length() Then
            Console.WriteLine("Является подстрокой")
            i = line.Length()
        End If
        i += 1
    End While
    
End Sub