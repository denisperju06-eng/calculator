' ==============================================================================
' Paradigma Scriptica - Lucrarea de laborator 2: VBA Excel
' Fisier: sarcina1.vba
' Contine toate cele 10 macrocomenzi conform cerintelor Sarcina 1
' ==============================================================================

' ------------------------------------------------------------------------------
' 1. In Excel creeaza o macrocomanda care scrie textul "Salut !" in celula A1.
' ------------------------------------------------------------------------------
Sub Sarcina1_1_ScrieSalut()
    Range("A1").Value = "Salut !"
    MsgBox "Textul 'Salut !' a fost inscris in celula A1.", vbInformation, "Sarcina 1.1"
End Sub

' ------------------------------------------------------------------------------
' 2. In Excel scrie o macrocomanda care sterge continutul tuturor celulelor
'    dintr-o foaie activa.
' ------------------------------------------------------------------------------
Sub Sarcina1_2_StergeContinutFoaieActiva()
    ' ClearContents sterge continutul (valori, formule) pastrand formatarea celulelor
    ActiveSheet.Cells.ClearContents
    MsgBox "Continutul tuturor celulelor din foaia activa a fost sters.", vbInformation, "Sarcina 1.2"
End Sub

' ------------------------------------------------------------------------------
' 3. In Excel creeaza o procedura care completeaza automat coloana A
'    cu numerele de la 1 la 50.
' ------------------------------------------------------------------------------
Sub Sarcina1_3_CompleteazaColoanaA_1La50()
    Dim i As Long
    For i = 1 To 50
        Cells(i, 1).Value = i
    Next i
    MsgBox "Coloana A a fost completata cu numerele de la 1 la 50.", vbInformation, "Sarcina 1.3"
End Sub

' ------------------------------------------------------------------------------
' 4. In Excel scrie un script care calculeaza suma valorilor din coloana B
'    si o afiseaza intr-o fereastra MsgBox.
' ------------------------------------------------------------------------------
Sub Sarcina1_4_CalculSumaColoanaB()
    Dim suma As Double
    suma = Application.WorksheetFunction.Sum(ActiveSheet.Columns("B"))
    MsgBox "Suma valorilor din coloana B este: " & suma, vbInformation, "Sarcina 1.4 - Suma Coloana B"
End Sub

' ------------------------------------------------------------------------------
' 5. In Excel creeaza o macrocomanda care formateaza automat antetul unui tabel
'    (font bold, fundal gri, text alb).
' ------------------------------------------------------------------------------
Sub Sarcina1_5_FormateazaAntetTabel()
    Dim rngAntet As Range
    
    ' Daca este selectata o zona formata din mai multe celule, o folosim pe aceea;
    ' altfel, luam primul rand al zonei curente (CurrentRegion) sau celulele A1:E1.
    If TypeName(Selection) = "Range" And Selection.CountLarge > 1 Then
        Set rngAntet = Selection
    ElseIf WorksheetFunction.CountA(Range("A1").CurrentRegion) > 0 Then
        Set rngAntet = Range("A1").CurrentRegion.Rows(1)
    Else
        Set rngAntet = Range("A1:E1")
    End If
    
    With rngAntet
        .Font.Bold = True
        .Font.Color = RGB(255, 255, 255)       ' Text alb
        .Interior.Color = RGB(128, 128, 128)   ' Fundal gri
        .HorizontalAlignment = xlCenter
        .VerticalAlignment = xlCenter
    End With
    
    MsgBox "Antetul (" & rngAntet.Address(False, False) & ") a fost formatat cu succes!", vbInformation, "Sarcina 1.5"
End Sub

' ------------------------------------------------------------------------------
' 6. In Excel scrie un script care determina celula cu valoarea maxima
'    dintr-un interval si o evidentiaza cu galben.
' ------------------------------------------------------------------------------
Sub Sarcina1_6_EvidentiazaMaximGalben()
    Dim rng As Range
    Dim cel As Range
    Dim celMax As Range
    Dim maxVal As Double
    Dim gasit As Boolean
    
    ' Se utilizeaza intervalul selectat sau UsedRange daca este selectata o singura celula
    If TypeName(Selection) = "Range" And Selection.CountLarge > 1 Then
        Set rng = Selection
    Else
        Set rng = ActiveSheet.UsedRange
    End If
    
    On Error Resume Next
    maxVal = Application.WorksheetFunction.Max(rng)
    If Err.Number <> 0 Or (maxVal = 0 And Application.WorksheetFunction.Count(rng) = 0) Then
        MsgBox "Nu s-au gasit valori numerice in intervalul analizat!", vbExclamation, "Sarcina 1.6"
        Exit Sub
    End If
    On Error GoTo 0
    
    gasit = False
    For Each cel In rng
        If IsNumeric(cel.Value) And Not IsEmpty(cel.Value) Then
            If cel.Value = maxVal Then
                Set celMax = cel
                cel.Interior.Color = vbYellow   ' Evidentiere cu galben
                gasit = True
                Exit For                        ' Evidentiaza prima aparitie a valorii maxime
            End If
        End If
    Next cel
    
    If gasit Then
        celMax.Select
        MsgBox "Valoarea maxima (" & maxVal & ") a fost gasita in celula " & celMax.Address(False, False) & " si evidentiata cu galben.", vbInformation, "Sarcina 1.6"
    End If
End Sub

' ------------------------------------------------------------------------------
' 7. In Excel creeaza o procedura care cere un nume de foaie si apoi o
'    creeaza automat in registrul curent.
' ------------------------------------------------------------------------------
Sub Sarcina1_7_CreeazaFoaieNoua()
    Dim numeFoaie As String
    Dim ws As Worksheet
    Dim wsNou As Worksheet
    
    numeFoaie = InputBox("Introduceti numele foii de calcul noi:", "Creare Foaie Noua")
    numeFoaie = Trim(numeFoaie)
    
    If numeFoaie = "" Then
        MsgBox "Operatiunea a fost anulata sau nu ati introdus un nume valid.", vbInformation, "Sarcina 1.7"
        Exit Sub
    End If
    
    ' Verificare daca exista deja o foaie cu acest nume
    For Each ws In ActiveWorkbook.Worksheets
        If LCase(ws.Name) = LCase(numeFoaie) Then
            MsgBox "O foaie cu numele '" & numeFoaie & "' exista deja in acest registru!", vbExclamation, "Sarcina 1.7"
            Exit Sub
        End If
    Next ws
    
    On Error GoTo GestioneazaEroare
    Set wsNou = ActiveWorkbook.Worksheets.Add(After:=ActiveWorkbook.Worksheets(ActiveWorkbook.Worksheets.Count))
    wsNou.Name = numeFoaie
    MsgBox "Foaia de calcul '" & numeFoaie & "' a fost creata cu succes!", vbInformation, "Sarcina 1.7"
    Exit Sub

GestioneazaEroare:
    MsgBox "Eroare la crearea foii: " & Err.Description, vbCritical, "Sarcina 1.7 - Eroare"
End Sub

' ------------------------------------------------------------------------------
' 8. In Excel scrie o macrocomanda care cere o valoare si o cauta in foaie,
'    apoi afiseaza adresa primei potriviri.
' ------------------------------------------------------------------------------
Sub Sarcina1_8_CautaValoareInFoaie()
    Dim valoareCautata As String
    Dim gasit As Range
    
    valoareCautata = InputBox("Introduceti valoarea pe care doriti sa o cautati in foaia activa:", "Cautare Valoare")
    If Trim(valoareCautata) = "" Then Exit Sub
    
    Set gasit = ActiveSheet.Cells.Find(What:=valoareCautata, LookIn:=xlValues, LookAt:=xlPart)
    
    If Not gasit Is Nothing Then
        gasit.Select
        MsgBox "Valoarea '" & valoareCautata & "' a fost gasita la adresa: " & gasit.Address(False, False), vbInformation, "Sarcina 1.8 - Rezultat Cautare"
    Else
        MsgBox "Valoarea '" & valoareCautata & "' nu a fost gasita in foaia activa.", vbExclamation, "Sarcina 1.8 - Negasit"
    End If
End Sub

' ------------------------------------------------------------------------------
' 9. In Excel scrie un script care parcurge toate foile dintr-un fisier
'    si afiseaza numele lor in fereastra Immediate.
' ------------------------------------------------------------------------------
Sub Sarcina1_9_AfiseazaNumeFoiImmediate()
    Dim ws As Worksheet
    Dim contor As Long
    
    Debug.Print "========================================="
    Debug.Print "Lista foilor din registrul curent (" & ActiveWorkbook.Name & "):"
    Debug.Print "========================================="
    contor = 0
    For Each ws In ActiveWorkbook.Worksheets
        contor = contor + 1
        Debug.Print contor & ". " & ws.Name
    Next ws
    Debug.Print "Total foi: " & contor
    Debug.Print "========================================="
    
    MsgBox "Numele celor " & contor & " foi au fost afisate in fereastra Immediate." & vbCrLf & _
           "(Deschideti editorul VBA cu Alt+F11 si apasati Ctrl+G pentru a vizualiza fereastra Immediate)", vbInformation, "Sarcina 1.9"
End Sub

' ------------------------------------------------------------------------------
' 10. In Excel creeaza o procedura care copiaza toate datele din coloana A
'     a foii 'Intrare' in coloana A a foii 'Rezultate'.
' ------------------------------------------------------------------------------
Sub Sarcina1_10_CopiazaIntrareLaRezultate()
    Dim wsIntrare As Worksheet
    Dim wsRezultate As Worksheet
    
    On Error Resume Next
    Set wsIntrare = ActiveWorkbook.Worksheets("Intrare")
    Set wsRezultate = ActiveWorkbook.Worksheets("Rezultate")
    On Error GoTo 0
    
    If wsIntrare Is Nothing Then
        MsgBox "Foaia 'Intrare' nu exista in registrul curent! Creati o foaie cu numele 'Intrare' si reincercati.", vbCritical, "Sarcina 1.10 - Eroare"
        Exit Sub
    End If
    
    ' Daca foaia 'Rezultate' nu exista, o cream automat
    If wsRezultate Is Nothing Then
        Set wsRezultate = ActiveWorkbook.Worksheets.Add(After:=ActiveWorkbook.Worksheets(ActiveWorkbook.Worksheets.Count))
        wsRezultate.Name = "Rezultate"
    End If
    
    ' Copiem continutul si formatul coloanei A
    wsIntrare.Columns("A").Copy Destination:=wsRezultate.Columns("A")
    Application.CutCopyMode = False
    
    MsgBox "Datele din coloana A a foii 'Intrare' au fost copiate cu succes in coloana A a foii 'Rezultate'!", vbInformation, "Sarcina 1.10"
End Sub
