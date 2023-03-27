sealed class Expr
class Block(vararg val exprs: Expr) : Expr()
class Const(val value: Int) : Expr()
class Var(val name: String) : Expr()
class Let(val variable: Var, val value: Expr) : Expr()
class If(val cond: Expr, val thenExpr: Expr, val elseExpr: Expr? = null) : Expr()
class SymVal(val name: String) : Expr()

// Additional Class to store a Function body with input params
class Func(val block: Block, vararg val variables: Var) : Expr()
sealed class ArithmeticOp(val left: Expr, val right: Expr) : Expr()
class Plus(left: Expr, right: Expr) : ArithmeticOp(left, right)
class Minus(left: Expr, right: Expr) : ArithmeticOp(left, right)
class Mul(left: Expr, right: Expr) : ArithmeticOp(left, right)

sealed class LogicalOp(val left: Expr, val right: Expr) : Expr()
class Eq(left: Expr, right: Expr) : LogicalOp(left, right)
class NEq(left: Expr, right: Expr) : LogicalOp(left, right)
