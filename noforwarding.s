Begin Assembly
LW R1, 4000(R0)
NOP
NOP
NOP
ADDI R1, R1, 5
ADDI R2, R0, 1
NOP
NOP
ADD R3, R1, R2
NOP
NOP
NOP
SW R3, 4004(R0)
HALT
End Assembly
-- begin main data
Begin Data 4000 8
10
0
End Data
--stack
Begin Data 5000 100
End Data