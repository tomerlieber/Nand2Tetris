@17
D=A
@SP
A=M
M=D
@SP
M=M+1
@17
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
A=A-1
D=M-D
@IF0
D;JNE
@SP
A=M-1
M=-1
@ELSE0
0;JMP
(IF0)
@SP
A=M-1
M=0
(ELSE0)
@17
D=A
@SP
A=M
M=D
@SP
M=M+1
@16
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
A=A-1
D=M-D
@IF1
D;JNE
@SP
A=M-1
M=-1
@ELSE1
0;JMP
(IF1)
@SP
A=M-1
M=0
(ELSE1)
@16
D=A
@SP
A=M
M=D
@SP
M=M+1
@17
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
A=A-1
D=M-D
@IF2
D;JNE
@SP
A=M-1
M=-1
@ELSE2
0;JMP
(IF2)
@SP
A=M-1
M=0
(ELSE2)
@892
D=A
@SP
A=M
M=D
@SP
M=M+1
@891
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
A=A-1
D=M-D
@IF3
D;JGE
@SP
A=M-1
M=-1
@ELSE3
0;JMP
(IF3)
@SP
A=M-1
M=0
(ELSE3)
@891
D=A
@SP
A=M
M=D
@SP
M=M+1
@892
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
A=A-1
D=M-D
@IF4
D;JGE
@SP
A=M-1
M=-1
@ELSE4
0;JMP
(IF4)
@SP
A=M-1
M=0
(ELSE4)
@891
D=A
@SP
A=M
M=D
@SP
M=M+1
@891
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
A=A-1
D=M-D
@IF5
D;JGE
@SP
A=M-1
M=-1
@ELSE5
0;JMP
(IF5)
@SP
A=M-1
M=0
(ELSE5)
@32767
D=A
@SP
A=M
M=D
@SP
M=M+1
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
A=A-1
D=M-D
@IF6
D;JLE
@SP
A=M-1
M=-1
@ELSE6
0;JMP
(IF6)
@SP
A=M-1
M=0
(ELSE6)
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1
@32767
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
A=A-1
D=M-D
@IF7
D;JLE
@SP
A=M-1
M=-1
@ELSE7
0;JMP
(IF7)
@SP
A=M-1
M=0
(ELSE7)
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
A=A-1
D=M-D
@IF8
D;JLE
@SP
A=M-1
M=-1
@ELSE8
0;JMP
(IF8)
@SP
A=M-1
M=0
(ELSE8)
@57
D=A
@SP
A=M
M=D
@SP
M=M+1
@31
D=A
@SP
A=M
M=D
@SP
M=M+1
@53
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
A=A-1
M=M+D
@112
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
A=A-1
M=M-D
D=0
@SP
A=M-1
M=D-M
@SP
AM=M-1
D=M
A=A-1
M=M&D
@82
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
A=A-1
M=M|D
@SP
A=M-1
M=!M
