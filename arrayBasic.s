.data

msg_0:
		.word 44
		.ascii  "ArrayIndexOutOfBoundsError: negative index\n\0"

msg_1:
		.word 45
		.ascii  "ArrayIndexOutOfBoundsError: index too large\n\0"

msg_2:
		.word 5
		.ascii  "%.*s\0"

.text

.global main
main:
		PUSH {lr}
		SUB sp, sp, #4
		LDR r0, =8
		BL malloc
		MOV r4, r0
		LDR r5, =0
		STR r5, [r4, #4]
		LDR r5, =1
		STR r5, [r4]
		STR r4, [sp]
		LDR r4, =2
		ADD r5, sp, #0
		LDR r6, =1
		LDR r5, [r5]
		MOV r0, r6
		MOV r1, r5
		BL p_check_array_bounds
		ADD r5, r5, #4
		ADD r5, r5, r6, LSL #2
		STR r4, [r5]
		ADD sp, sp, #4
		LDR r0, =0
		POP {pc}
		.ltorg
p_check_array_bounds:
		PUSH {lr}
		CMP r0, #0
		LDRLT r0, =msg_0
		BLLT p_throw_runtime_error
		LDR r1, [r1]
		CMP r0, r1
		LDRCS r0, =msg_1
		BLCS p_throw_runtime_error
		POP {pc}
p_print_string:
		PUSH {lr}
		LDR r1, [r0]
		ADD r2, r0, #4
		LDR r0, =msg_2
		ADD r0, r0, #4
		BL printf
		MOV r0, #0
		BL fflush
		POP {pc}
p_throw_runtime_error:
		BL p_print_string
		MOV r0, #-1
		BL exit
