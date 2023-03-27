/** File containing all the logic required to parse an AST Tree to the Symbolic Execution Tree */
class ExecTreeNode(
    val children: List<ExecTreeNode>,
    val nextExpr: Expr,
    var S: List<Let>,
    val Pi: List<Expr>,
    val unsolvable: List<Pair<String, Int>>? = null
)

/** Parses given function to the Symbolic Execution Tree */
fun parseExpr(func: Func): ExecTreeNode? {
    val initialS = func.variables.map { s -> Let(Var(s.name), SymVal(s.name)) }
    return parseExprRecursive(initialS, emptyList(), listOf(func.block))
}

private fun parseExprRecursive(
    existingS: List<Let>,
    existingPi: List<Expr>,
    exprs: List<Expr>
): ExecTreeNode? {
    if (exprs.isEmpty()) {
        return null
    }
    val expr = exprs[0]
    val nextExprs = exprs.takeLast(exprs.size - 1)

    when (expr) {
        // has almost no meaning, so we kinda skip it
        is Block -> return parseExprRecursive(existingS, existingPi, expr.exprs.toList() + nextExprs)

        // Declare an uninitialized  variable
        is Var -> {
            val newS = existingS + Let(expr, SymVal(expr.name))
            val childNode = parseExprRecursive(newS, existingPi, nextExprs)
            return ExecTreeNode(listOfNotNull(childNode), expr, newS, existingPi)
        }
        // Assign a value to a variable
        is Let -> {
            val substRes = substitution(expr.value, existingS)
            // Remove previous value and assign a new one
            val newS =
                existingS.filter { e -> e.variable.name != expr.variable.name } + Let(expr.variable, substRes)
            val childNode = parseExprRecursive(newS, existingPi, nextExprs)
            return ExecTreeNode(listOfNotNull(childNode), expr, newS, existingPi)
        }

        is If -> {
            when (expr.cond) {
                is LogicalOp -> {
                    val exprCondLeft = substitution(expr.cond.left, existingS)
                    val exprCondRight = substitution(expr.cond.right, existingS)

                    val newExprsEq = when (expr.cond) {
                        is Eq -> {
                            val newExprsEq = mutableListOf(expr.thenExpr)
                            newExprsEq.addAll(nextExprs)
                            newExprsEq
                        }

                        is NEq -> {
                            val newExprsEq =
                                if (expr.elseExpr == null) mutableListOf() else mutableListOf(expr.elseExpr)
                            newExprsEq.addAll(nextExprs)
                            newExprsEq
                        }

                    }
                    val newExprsNEq = when (expr.cond) {
                        is Eq -> {
                            val newExprsNEq =
                                if (expr.elseExpr == null) mutableListOf() else mutableListOf(expr.elseExpr)
                            newExprsNEq.addAll(nextExprs)
                            newExprsNEq
                        }

                        is NEq -> {
                            val newExprsNEq = mutableListOf(expr.thenExpr)
                            newExprsNEq.addAll(nextExprs)
                            newExprsNEq
                        }

                    }
                    val childEqNode =
                        parseExprRecursive(
                            existingS,
                            existingPi + Eq(exprCondLeft, exprCondRight),
                            newExprsEq
                        )
                    val childNEqNode = parseExprRecursive(
                        existingS,
                        existingPi + NEq(exprCondLeft, exprCondRight),
                        newExprsNEq
                    )
                    val children = listOfNotNull(childEqNode, childNEqNode)
                    return ExecTreeNode(children, expr, existingS, existingPi)
                }

                else -> {
                    throw Exception("The AST is invalid!")
                }
            }
        }

        else -> {
            /* I treat LogicalOps without If as assertions.
               If an assertion appears, I prove it or reject.
               If it is rejected, then I no longer continue with this branch */
            if (expr is LogicalOp) {
                val unsolvable = solveSmt(existingS, existingPi, expr)
                if (unsolvable == null) {
                    val childNode = parseExprRecursive(
                        existingS,
                        existingPi,
                        nextExprs
                    )
                    return ExecTreeNode(listOfNotNull(childNode), expr, existingS, existingPi)
                }
                return ExecTreeNode(emptyList(), expr, existingS, existingPi, unsolvable)
            }

            throw Exception("The AST is invalid!")
        }
    }
}

/** Substitutes all the variables in the expression by they value from S */
private fun substitution(expr: Expr, s: List<Let>): Expr {
    when (expr) {
        is Const -> return expr
        is SymVal -> return expr
        is Var -> {
            return s.find { v -> v.variable.name == expr.name }?.value ?: throw Exception("Unknown variable!")
        }

        is ArithmeticOp -> {
            val leftSubst = substitution(expr.left, s)
            val rightSubst = substitution(expr.right, s)
            when (expr) {
                is Plus -> {
                    if (leftSubst is Const && rightSubst is Const) {
                        return Const(leftSubst.value + rightSubst.value)
                    }
                    return Plus(leftSubst, rightSubst)
                }

                is Minus -> {
                    if (leftSubst is Const && rightSubst is Const) {
                        return Const(leftSubst.value - rightSubst.value)
                    }
                    return Minus(leftSubst, rightSubst)
                }

                is Mul -> {
                    if (leftSubst is Const && rightSubst is Const) {
                        return Const(leftSubst.value * rightSubst.value)
                    }
                    return Mul(leftSubst, rightSubst)
                }
            }

        }

        else -> {
            throw Exception("The AST is invalid!")
        }
    }
}
