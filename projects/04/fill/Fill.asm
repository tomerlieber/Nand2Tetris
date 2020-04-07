// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

(LOOP)
	@SCREEN 	// The base address of the screen
	D=A
	@pixels 	// The address of the pixel that will be colored
  	M=D         // Starts at 16384 and ends at 16384 + 8192 == 24576
  	@KBD    	// The address of the keyboard
 	D=M
  	@WHITE
  	D;JEQ     	// if(keyboard == 0) goto WHITE
  	@BLACK
    0;JMP 		// else goto BLACK

(BLACK)
    @color
    M=-1    	// Set color to black
  	@COLOR_THE_SCREEN
  	0;JMP     	// Jump to code that colors the screen

(WHITE)
  	@color
  	M=0       	// Set color to white

(COLOR_THE_SCREEN)
    @color
    D=M         // D equals to the color (0 or -1)
    @pixels
    A=M         // A equals to the pixel address 
    M=D         // Color the pixel with the appropriate color
    
    @pixels
    M=M+1		// Advance to the value of the next pixel address
    D=M 		// D equals to the address of the next pixel
        
    @24576 		// The address of the last pixel plus one.
    D=A-D
    @COLOR_THE_SCREEN
    D;JGT 		// if(D>0) goto COLOR_THE_SCREEN

@LOOP
0;JMP // Inifinity loop - listen to the keyboard

