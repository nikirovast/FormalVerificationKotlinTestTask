import org.ksmt.KContext
import org.ksmt.solver.KSolverStatus

import org.ksmt.solver.z3.KZ3SMTLibParser
import org.ksmt.solver.z3.KZ3Solver
import kotlin.time.Duration.Companion.seconds

/** File containing all the logic required to connect an SMT Solver
 *
 * I used this lib to do this https://github.com/UnitTestBot/ksmt
 * with the API to parse SMT-LIB2 format */
fun solveSmt(s: List<Let>, p: List<Expr>, assert: LogicalOp): List<Pair<String, Int>>? {
    with(KContext()) {
        // To prove the assertions for all the possible values, we have to solve it for the reverse Operation
        val formula = createConstraints(s, p) + "(assert ${parseArithmeticExpr(reverseLogicalOp(assert))})"
        val assertions = KZ3SMTLibParser(this).parse(formula)

        KZ3Solver(this).use { solver ->
            assertions.forEach { solver.assert(it) }
            when (solver.check(timeout = 1.seconds)) {
                // Reverse Op is solvable => initial assertion fails for those values
                KSolverStatus.SAT -> {
                    return solver.model().declarations.map {
                        Pair(it.name, solver.model().interpretation(it).toString().toInt())
                    }
                }
                // Reverse Op is unsolvable => initial assertion holds
                KSolverStatus.UNSAT -> {
                    return null
                }

                else -> {
                    throw Exception("Couldn't prove within timeout")
                }
            }
        }
    }
}

// Utilities to parse my representation of the constraint to the SMT-LIB2 Format
private fun createConstraints(s: List<Let>, p: List<Expr>): String {
    val sb = StringBuilder()
    s.forEach {
        sb.appendLine("(declare-fun ${it.variable.name} () Int)")
        if (it.value !is SymVal) {
            sb.appendLine("(assert (= ${it.variable.name} ${parseArithmeticExpr(it.value)}))")
        }
    }
    p.forEach {
        sb.appendLine("(assert ${parseArithmeticExpr(it)})")
    }
    return sb.toString()
}

private fun parseArithmeticExpr(expr: Expr): String {
    return when (expr) {
        is Const -> "${expr.value}"
        is Var -> expr.name
        is SymVal -> expr.name
        is Plus -> "(+ ${parseArithmeticExpr(expr.left)} ${parseArithmeticExpr(expr.right)})"
        is Minus -> "(- ${parseArithmeticExpr(expr.left)} ${parseArithmeticExpr(expr.right)})"
        is Mul -> "(* ${parseArithmeticExpr(expr.left)} ${parseArithmeticExpr(expr.right)})"
        is Eq -> "(= ${parseArithmeticExpr(expr.left)} ${parseArithmeticExpr(expr.right)})"
        is NEq -> "(not (= ${parseArithmeticExpr(expr.left)} ${parseArithmeticExpr(expr.right)}))"
        else -> {
            throw Exception("invalid")
        }
    }
}

private fun reverseLogicalOp(logicalOp: LogicalOp): LogicalOp {
    return when (logicalOp) {
        is Eq -> NEq(logicalOp.left, logicalOp.right)
        is NEq -> Eq(logicalOp.left, logicalOp.right)
    }
}