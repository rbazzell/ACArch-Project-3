Begin Assembly
LW R1, 4000(R1)
ADDI R1, R1, 5
HALT
End Assembly
-- begin main data
Begin Data 4000 16
4
8
1
End Data
--stack
Begin Data 5000 100
End Data