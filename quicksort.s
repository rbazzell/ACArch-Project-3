-- a program to quicksort an integer array
--
--
Begin Assembly
-- array pointer is stored in R30
ADDI R30, R0, 4000
--stack pointer starts @ 5000 in R29
ADDI R29, R0, 5000
--stack index pointer starts @ 5000 in R28
ADDI R28, R0, 5000
-- R27 = the first memory address of the array (p_0)
ADDI R27, R30, 4
-- R26 = the last address of the array (r_0)
LW R26, 0(R30)
SLL R26, R26, 2
ADD R26, R26, R30
--initial stack push (p_0, r_0) to 5000-5008
SW R27, 0(R28)
SW R26, 4(R28)
ADDI R28, R28, 8
LABEL QUICKSORT
--pop off the stack
ADDI R28, R28, -8
--R1 = p from stack
LW R1, 0(R28)
--R2 = r from stack
LW R2, 4(R28)
--quicksort algorithm
--if p < r, do the following, otherwise, jump over
SUB R3, R1, R2
BGEZ R3, ENDIF
-- R3 = q = partition(p, r)
-- x = R4 = array[r]
LW R4, 0(R2)
-- R5 = i = p - 1
ADDI R5, R1, -4
-- initalize R6 = j = p for the for loop
ADD R6, R1, R0
LABEL FORSTART
-- if not j < r, jump to forend
BEQ R6, R2, FOREND
--R6 = &array[j]
--R8 = array[j]
LW R8, 0(R6)
-- if array[j] <= x, move stuff around, otherwise go to next iteration
SUB R7, R8, R4
BGTZ R7, ENDFORIF
-- increment i
ADDI R5, R5, 4
--R9 = array[i]
LW R9, 0(R5)
--swap array[i] and array[j] in memory
SW R8, 0(R5)
SW R9, 0(R6)
LABEL ENDFORIF
ADDI R6, R6, 4
J FORSTART
LABEL FOREND
--increment i
ADDI R5, R5, 4
-- R8 = array[i]
LW R8, 0(R5)
--swap array[i] and array[r] in memory
SW R4, 0(R5)
SW R8, 0(R2)
-- q = i + 1
ADD R3, R5, R0
--push quicksort(p, q-1) on stack
ADDI R3, R3, -4
SW R1, 0(R28)
SW R3, 4(R28)
--push quicksort(q+1, r) on stack
ADDI R3, R3, 8
SW R3, 8(R28)
SW R2, 12(R28)
--adjust stack index pointer
ADDI R28, R28, 16
LABEL ENDIF
BNE R28, R29, QUICKSORT
LABEL ENDSORT
HALT
End Assembly
--begin main data
Begin Data 4000 52
50
29
6
0
18
31
11
15
15
10
15
15
9
19
17
1
23
25
12
19
34
15
4
31
5
14
21
20
1
10
30
26
3
22
33
13
27
39
14
22
3
20
23
21
34
31
30
24
31
19
38
End Data
--begin stack memory
Begin Data 5000 100
End Data