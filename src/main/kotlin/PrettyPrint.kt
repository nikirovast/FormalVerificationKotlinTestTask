/** File containing all the logic required to pretty print a tree */
fun printPretty(node: ExecTreeNode?): String {
    if (node == null) {
        return "Empty tree"
    }
    val sb = StringBuilder()
    printPrettyRecursive(node, "", "", sb)
    return sb.toString()
}

private fun printPrettyRecursive(node: ExecTreeNode, prefix: String, childrenPrefix: String, sb: StringBuilder) {
    val unsolvable = if (node.unsolvable == null) "" else ", Assert failed for: ${node.unsolvable}"
    sb.append("${prefix}S:${prettyS(node.S)}, Pi:${prettyPi(node.Pi)}, Command:'${prettyCommand(node.nextExpr)}'${unsolvable}\n")
    node.children.forEachIndexed { index, element ->
        if (index != node.children.lastIndex) {
            printPrettyRecursive(element, "$childrenPrefix├── ", "$childrenPrefix│   ", sb)
        } else {
            printPrettyRecursive(node.children.last(), "$childrenPrefix└── ", "$childrenPrefix    ", sb)
        }
    }
}

// Utilities to pretty print commands, values, S and Pi

private fun prettyCommand(expr: Expr?): String {
    if (expr == null) {
        return ""
    }
    when (expr) {
        is Const -> {
            return "Const(${expr.value})"
        }

        is Var -> {
            return "Var(${expr.name})"
        }

        is SymVal -> {
            return "SymVal(${expr.name})"
        }

        is Let -> {
            return "Let(${prettyCommand(expr.variable)}, ${prettyCommand(expr.value)})"
        }

        is Eq -> {
            return "Eq(${prettyCommand(expr.left)}, ${prettyCommand(expr.right)})"
        }

        is NEq -> {
            return "NEq(${prettyCommand(expr.left)}, ${prettyCommand(expr.right)})"
        }

        is Plus -> {
            return "Plus(${prettyCommand(expr.left)}, ${prettyCommand(expr.right)})"
        }

        is Minus -> {
            return "Minus(${prettyCommand(expr.left)}, ${prettyCommand(expr.right)})"
        }

        is Mul -> {
            return "Mul(${prettyCommand(expr.left)}, ${prettyCommand(expr.right)})"
        }

        is If -> {
            return "If(${prettyCommand(expr.cond)})"
        }

        else -> {
            throw Exception("Something is wrong!")
        }
    }
}

private fun prettyValue(expr: Expr): String {
    when (expr) {
        is Const -> {
            return "${expr.value}"
        }

        is Var -> {
            return expr.name
        }

        is SymVal -> {
            return "SymVal(${expr.name})"
        }

        is Let -> {
            return "${prettyValue(expr.variable)} = ${prettyValue(expr.value)}"
        }

        is Eq -> {
            return "${prettyValue(expr.left)} == ${prettyValue(expr.right)}"
        }

        is NEq -> {
            return "${prettyValue(expr.left)} != ${prettyValue(expr.right)}"
        }

        is Plus -> {
            return "${prettyValue(expr.left)} + ${prettyValue(expr.right)}"
        }

        is Minus -> {
            return "${prettyValue(expr.left)} - ${prettyValue(expr.right)}"
        }

        is Mul -> {
            return "${prettyValue(expr.left)} * ${prettyValue(expr.right)}"
        }

        else -> {
            throw Exception("Not a value!")
        }
    }
}

private fun prettyS(s: List<Let>): String {
    val sb = StringBuilder("{")
    s.forEachIndexed { index, element ->
        sb.append("${element.variable.name} <─ ${prettyValue(element.value)}")
        if (index != s.lastIndex) {
            sb.append(", ")
        }
    }
    sb.append("}")
    return sb.toString()
}

private fun prettyPi(pi: List<Expr>): String {
    val sb = StringBuilder("{")
    pi.forEachIndexed { index, element ->
        sb.append((prettyValue(element)))
        if (index != pi.lastIndex) {
            sb.append(", ")
        }
    }
    sb.append("}")
    return sb.toString()
}