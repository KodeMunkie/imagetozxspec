1 BRIGHT 1: PAPER 0: BORDER 0: INK 7
10 CLS
20 LET Y = 3
30 PAPER 2:PRINT AT Y,1;" "
40 PAPER 4:PRINT AT Y,2;" "
50 PAPER 6:PRINT AT Y,3;" "
60 PAPER 1:PRINT AT Y,4;" "
70 PAPER 0: INK 7: BRIGHT 1
80 PRINT AT Y,5;" Image to ZX Spectrum "
90 PAPER 2:PRINT AT Y,27;" "
100 PAPER 4:PRINT AT Y,28;" "
110 PAPER 6:PRINT AT Y,29;" "
120 PAPER 1:PRINT AT Y,30;" "
130 LET Y=Y+2
140 PAPER 0: INK 7: BRIGHT 1
150 PRINT AT Y,1;"Copyright Silent Software 2017"
160 LET Y=Y+2
170 PRINT AT Y,1;"  * 128K GigaScreen Loader *  "
180 LET Y=Y+2
185 INK 2
190 PRINT AT Y,1;"**WARNING**"
195 INK 7
200 LET Y=Y+1
205 PRINT AT Y,1;"DO NOT CONTINUE IF YOU ARE"
206 LET Y=Y+1
210 PRINT AT Y,1;"EPILECTIC, GIGASCREEN USES"
220 LET Y=Y+1
230 PRINT AT Y,1;"FLASHING IMAGES."
240 LET Y=Y+2
250 PRINT AT Y,1;"NOTE: You must have switched to"
260 LET Y=Y+1
270 PRINT AT Y,1;"USR 0 in BASIC before loading"
280 LET Y=Y+1
290 PRINT AT Y,1;"(type USR 0 to switch to 48K)."
300 LET Y=Y+2
310 PRINT AT Y,1;"Press a key to change slides."
320 LET Y=Y+1
330 PRINT AT Y,1;"Type 'OK' to start."
340 INPUT A$
350 IF A$<>"OK" THEN GO TO 340
360 LET A$=INKEY$
370 CLEAR 49151
380 POKE 23739,111 
390 OUT 32765,21 
400 LOAD ""CODE 49152 
410 OUT 32765,23 
420 LOAD ""CODE 49152 
430 PAUSE 1 
440 OUT 32765,16 
450 PAUSE 1 
460 OUT 32765,24
470 IF INKEY$ <> "" THEN GO TO 370
480 GO TO 430