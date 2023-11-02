Begin Assembly
LW R1, 4000(R0)
LABEL top
LW R2, 4000(R1)
ADD R3, R3, R2
ADDI R1, R1, -4
BEQ R1, R0, end
J top
NOP
NOP
LABEL end
SW R3, 4044(R0)
HALT
End Assembly
-- begin main data
Begin Data 4000 48
40
1
2
3
4
5
6
7
8
9
10
0
End Data
--stack
Begin Data 5000 100
End Data