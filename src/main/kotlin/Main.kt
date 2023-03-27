fun main() {
    // Example from the paper
    val fooBarAst = Func(
        block = Block(
            Let(Var("x"), Const(1)),
            Let(Var("y"), Const(0)),
            If(
                NEq(Var("a"), Const(0)),
                Block(
                    Let(Var("y"), Plus(Const(3), Var("x"))),
                    If(
                        Eq(Var("b"), Const(0)),
                        Let(Var("x"), Mul(Const(2), Plus(Var("a"), Var("b")))),
                    )
                )
            ),
            NEq(Minus(Var("x"), Var("y")), Const(0))
        ), Var("a"), Var("b")
    )

    val res = parseExpr(fooBarAst)
    println(printPretty(res))
}